package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ExamHintResult
import com.example.core.NibrasAiService
import com.example.data.AdminAiConfigDoc
import com.example.data.AiMessageDoc
import com.example.data.BookHighlightDoc
import com.example.data.CommunityPostDoc
import com.example.data.Course
import com.example.data.CourseProgressDoc
import com.example.data.ExamAttemptDoc
import com.example.data.LearningTrack
import com.example.data.Lesson
import com.example.data.LessonNoteDoc
import com.example.data.LibraryAssetAuditDoc
import com.example.data.LibraryBook
import com.example.data.NibrasCatalog
import com.example.data.NibrasRepository
import com.example.data.ReadingStateDoc
import com.example.data.StudyPlanDoc
import com.example.data.TechnicalExam
import com.example.data.UserProfileDoc
import com.example.ui.theme.AppLanguage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val VM_TAG = "NibrasViewModel"
private const val STOP_TIMEOUT_MS = 5_000L

sealed interface AiStreamUiState {
    data object Idle : AiStreamUiState
    data object Loading : AiStreamUiState
    data class Streaming(val text: String) : AiStreamUiState
    data class Success(val text: String) : AiStreamUiState
    data class Error(val message: String) : AiStreamUiState
}

data class ActiveExamSession(
    val exam: TechnicalExam,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val flaggedIndices: Set<Int> = emptySet(),
    val remainingSeconds: Int = exam.durationMinutes * 60,
    val hintsByQuestion: Map<Int, ExamHintResult> = emptyMap(),
    val isHintLoading: Boolean = false,
    val hintError: String? = null
)

data class CompletedExamResult(
    val attemptId: String,
    val exam: TechnicalExam,
    val selectedAnswers: Map<Int, Int>,
    val correctCount: Int,
    val totalQuestions: Int,
    val scorePercent: Int,
    val durationSeconds: Int
)

class NibrasViewModel(
    private val repository: NibrasRepository,
    private val aiService: NibrasAiService,
    val currentUserId: String,
    private val currentUserEmail: String,
    private val currentUserName: String,
    private val currentUserPhoto: String?
) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.AR)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _profileReady = MutableStateFlow(false)
    val profileReady: StateFlow<Boolean> = _profileReady.asStateFlow()

    // --- FIRESTORE STREAMS WITH TWO-TIER CANCELLATION UNWRAPPING ---

    private fun <T> Flow<T>.safeStateIn(defaultValue: T): StateFlow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(VM_TAG, "Firestore stream error: ${root.message}", root)
            _statusBanner.value = root.localizedMessage ?: "Cloud sync issue occurred."
            emit(defaultValue)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = defaultValue
        )

    val userProfile: StateFlow<UserProfileDoc?> = repository.observeUserProfile(currentUserId)
        .map { profile ->
            if (profile != null) {
                _language.value = if (profile.preferredLanguage == "en") AppLanguage.EN else AppLanguage.AR
            }
            profile
        }
        .safeStateIn(null)

    val courseProgressMap: StateFlow<Map<String, CourseProgressDoc>> =
        repository.observeCourseProgress(currentUserId)
            .map { list -> list.associateBy { it.courseId } }
            .safeStateIn(emptyMap())

    val lessonNotes: StateFlow<List<LessonNoteDoc>> =
        repository.observeLessonNotes(currentUserId).safeStateIn(emptyList())

    val readingStatesMap: StateFlow<Map<String, ReadingStateDoc>> =
        repository.observeReadingStates(currentUserId)
            .map { list -> list.associateBy { it.bookId } }
            .safeStateIn(emptyMap())

    val bookHighlights: StateFlow<List<BookHighlightDoc>> =
        repository.observeHighlights(currentUserId).safeStateIn(emptyList())

    val examAttempts: StateFlow<List<ExamAttemptDoc>> =
        repository.observeExamAttempts(currentUserId).safeStateIn(emptyList())

    val aiMessages: StateFlow<List<AiMessageDoc>> =
        repository.observeAiMessages(currentUserId).safeStateIn(emptyList())

    val studyPlans: StateFlow<List<StudyPlanDoc>> =
        repository.observeStudyPlans(currentUserId).safeStateIn(emptyList())

    val communityPosts: StateFlow<List<CommunityPostDoc>> =
        repository.observeCommunityPosts(currentUserId)
            .map { livePosts ->
                if (livePosts.isEmpty()) NibrasCatalog.starterCommunityPosts
                else livePosts + NibrasCatalog.starterCommunityPosts
            }
            .safeStateIn(NibrasCatalog.starterCommunityPosts)

    val adminAiConfig: StateFlow<AdminAiConfigDoc> =
        repository.observeAdminAiConfig(currentUserId).safeStateIn(AdminAiConfigDoc())

    val libraryAssetAudits: StateFlow<Map<String, LibraryAssetAuditDoc>> =
        repository.observeLibraryAssetAudits(currentUserId)
            .map { list -> list.associateBy { it.bookId } }
            .safeStateIn(emptyMap())

    private val _adminUsers = MutableStateFlow<List<UserProfileDoc>>(emptyList())
    val adminUsers: StateFlow<List<UserProfileDoc>> = _adminUsers.asStateFlow()
    private var adminUsersJob: Job? = null

    // --- AI FEATURE STATES ---

    private val _tutorAiState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
    val tutorAiState: StateFlow<AiStreamUiState> = _tutorAiState.asStateFlow()

    private val _selectedAiMode = MutableStateFlow("academic")
    val selectedAiMode: StateFlow<String> = _selectedAiMode.asStateFlow()

    private val _lessonSummaryState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
    val lessonSummaryState: StateFlow<AiStreamUiState> = _lessonSummaryState.asStateFlow()

    private val _bookCopilotState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
    val bookCopilotState: StateFlow<AiStreamUiState> = _bookCopilotState.asStateFlow()

    private val _studyPlanAiState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
    val studyPlanAiState: StateFlow<AiStreamUiState> = _studyPlanAiState.asStateFlow()

    private val _examDiagnosticsState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
    val examDiagnosticsState: StateFlow<AiStreamUiState> = _examDiagnosticsState.asStateFlow()

    private val _localPostDigests = MutableStateFlow<Map<String, String>>(emptyMap())
    val localPostDigests: StateFlow<Map<String, String>> = _localPostDigests.asStateFlow()

    private val _digestLoadingPostId = MutableStateFlow<String?>(null)
    val digestLoadingPostId: StateFlow<String?> = _digestLoadingPostId.asStateFlow()

    // --- EXAM ENGINE STATE ---

    private val _activeExam = MutableStateFlow<ActiveExamSession?>(null)
    val activeExam: StateFlow<ActiveExamSession?> = _activeExam.asStateFlow()

    private val _lastExamResult = MutableStateFlow<CompletedExamResult?>(null)
    val lastExamResult: StateFlow<CompletedExamResult?> = _lastExamResult.asStateFlow()

    private var examTimerJob: Job? = null

    // --- POMODORO LIVE STUDY ROOM ---
    private val _pomodoroJoined = MutableStateFlow(false)
    val pomodoroJoined: StateFlow<Boolean> = _pomodoroJoined.asStateFlow()

    init {
        viewModelScope.launch {
            val result = repository.ensureUserProfileExists(
                uid = currentUserId,
                displayName = currentUserName,
                email = currentUserEmail,
                photoUrl = currentUserPhoto
            )
            result.onSuccess { profile ->
                _language.value = if (profile.preferredLanguage == "en") AppLanguage.EN else AppLanguage.AR
                _selectedAiMode.value = profile.aiStyle
                if (isAdminEligible(profile)) {
                    startObservingAdminUsers()
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to initialize profile."
            }
            _profileReady.value = true
        }
    }

    fun isAdminEligible(profile: UserProfileDoc? = userProfile.value): Boolean {
        return profile?.role == "admin" ||
            currentUserEmail.equals(NibrasRepository.BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)
    }

    fun startObservingAdminUsers() {
        if (adminUsersJob != null) return
        adminUsersJob = viewModelScope.launch {
            repository.observeAllUsersForAdmin(currentUserId)
                .catch { err ->
                    val fsErr = generateSequence(err) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && err is CancellationException) throw err
                    Log.w(VM_TAG, "Admin users stream unavailable: ${err.message}")
                }
                .collect { users ->
                    _adminUsers.value = users
                }
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
        _language.value = next
        viewModelScope.launch {
            repository.updateUserPreferences(preferredLanguage = next.code)
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        viewModelScope.launch {
            repository.updateUserPreferences(preferredLanguage = lang.code)
        }
    }

    fun setAiMode(mode: String) {
        _selectedAiMode.value = mode
        viewModelScope.launch {
            repository.updateUserPreferences(aiStyle = mode)
        }
    }

    fun completeOnboarding(major: String, studyYear: String, skills: List<String>) {
        viewModelScope.launch {
            repository.completeOnboarding(
                major = major,
                studyYear = studyYear,
                skills = skills,
                preferredLanguage = _language.value.code
            ).onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to save onboarding profile."
            }
        }
    }

    fun updateDisplayName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateUserPreferences(displayName = name).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم تحديث الاسم بنجاح" else "Profile updated"
            }
        }
    }

    // --- COURSES & LESSONS ---

    fun markLessonComplete(course: Course, lessonId: String) {
        val existing = courseProgressMap.value[course.id]?.completedLessonIds ?: emptyList()
        viewModelScope.launch {
            repository.markLessonComplete(course, lessonId, existing).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) {
                    "تم تسجيل إتمام الدرس بنجاح"
                } else {
                    "Lesson marked as completed"
                }
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Could not save progress"
            }
        }
    }

    fun saveLessonNote(courseId: String, lessonId: String, timestampLabel: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addLessonNote(courseId, lessonId, timestampLabel, content).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم حفظ الملاحظة سحابياً" else "Note saved to cloud"
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to save note"
            }
        }
    }

    fun generateLessonAiSummary(course: Course, lesson: Lesson) {
        viewModelScope.launch {
            _lessonSummaryState.value = AiStreamUiState.Loading
            val lang = _language.value
            val cTitle = if (lang == AppLanguage.AR) course.titleAr else course.titleEn
            val lTitle = if (lang == AppLanguage.AR) lesson.titleAr else lesson.titleEn
            val lContent = if (lang == AppLanguage.AR) lesson.contentAr else lesson.contentEn
            try {
                var finalResult = ""
                aiService.streamLessonSummary(cTitle, lTitle, lContent, lesson.codeSnippet, lang)
                    .collect { partial ->
                        finalResult = partial
                        _lessonSummaryState.value = AiStreamUiState.Streaming(partial)
                    }
                _lessonSummaryState.value = AiStreamUiState.Success(finalResult)
                repository.incrementDailyTokens((finalResult.length / 3L).coerceAtLeast(80L))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _lessonSummaryState.value = AiStreamUiState.Error(e.localizedMessage ?: "AI summary failed.")
            }
        }
    }

    fun resetLessonSummary() {
        _lessonSummaryState.value = AiStreamUiState.Idle
    }

    // --- LIBRARY & SMART BOOK READER ---

    fun updateBookReadingProgress(book: LibraryBook, page: Int, bookmarked: Boolean) {
        viewModelScope.launch {
            repository.saveReadingState(book.id, page, book.totalPages, bookmarked)
        }
    }

    fun saveHighlight(bookId: String, page: Int, quote: String, note: String, aiExplanation: String? = null) {
        viewModelScope.launch {
            repository.saveBookHighlight(bookId, page, quote, note, aiExplanation).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم حفظ التظليل والملاحظة" else "Highlight saved"
            }
        }
    }

    fun runBookCopilot(book: LibraryBook, passage: String, action: String) {
        viewModelScope.launch {
            _bookCopilotState.value = AiStreamUiState.Loading
            val lang = _language.value
            val title = if (lang == AppLanguage.AR) book.titleAr else book.titleEn
            try {
                var finalResult = ""
                aiService.streamBookAssistant(title, passage, action, lang).collect { partial ->
                    finalResult = partial
                    _bookCopilotState.value = AiStreamUiState.Streaming(partial)
                }
                _bookCopilotState.value = AiStreamUiState.Success(finalResult)
                repository.incrementDailyTokens((finalResult.length / 3L).coerceAtLeast(90L))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _bookCopilotState.value = AiStreamUiState.Error(e.localizedMessage ?: "AI Copilot failed.")
            }
        }
    }

    fun resetBookCopilot() {
        _bookCopilotState.value = AiStreamUiState.Idle
    }

    // --- AI TUTOR CHAT ---

    fun sendTutorMessage(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank()) return
        val mode = _selectedAiMode.value
        val lang = _language.value
        val history = aiMessages.value.map { it.sender to it.text }

        viewModelScope.launch {
            _tutorAiState.value = AiStreamUiState.Loading
            repository.saveAiMessage("user", mode, clean, (clean.length / 3L).coerceAtLeast(12L))
            try {
                var reply = ""
                aiService.streamTutorReply(clean, mode, history, lang).collect { partial ->
                    reply = partial
                    _tutorAiState.value = AiStreamUiState.Streaming(partial)
                }
                val estTokens = ((clean.length + reply.length) / 3L).coerceAtLeast(100L)
                repository.saveAiMessage("assistant", mode, reply, estTokens)
                repository.incrementDailyTokens(estTokens)
                _tutorAiState.value = AiStreamUiState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _tutorAiState.value = AiStreamUiState.Error(e.localizedMessage ?: "Gemini AI failed.")
            }
        }
    }

    // --- PERSONALIZED STUDY PLAN GENERATOR ---

    fun generateStudyPlan(track: LearningTrack, weeklyHours: Int, pace: String) {
        viewModelScope.launch {
            _studyPlanAiState.value = AiStreamUiState.Loading
            val lang = _language.value
            val trackTitle = if (lang == AppLanguage.AR) track.titleAr else track.titleEn
            val milestoneTitles = track.milestones.map { if (lang == AppLanguage.AR) it.titleAr else it.titleEn }
            try {
                var planText = ""
                aiService.streamStudyPlan(trackTitle, milestoneTitles, weeklyHours, pace, lang).collect { partial ->
                    planText = partial
                    _studyPlanAiState.value = AiStreamUiState.Streaming(partial)
                }
                _studyPlanAiState.value = AiStreamUiState.Success(planText)
                repository.saveStudyPlan(track.id, trackTitle, weeklyHours, pace, planText)
                repository.incrementDailyTokens((planText.length / 3L).coerceAtLeast(120L))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _studyPlanAiState.value = AiStreamUiState.Error(e.localizedMessage ?: "Study plan generation failed.")
            }
        }
    }

    // --- INTERACTIVE EXAM ENGINE ---

    fun startExam(exam: TechnicalExam) {
        examTimerJob?.cancel()
        _examDiagnosticsState.value = AiStreamUiState.Idle
        _activeExam.value = ActiveExamSession(exam = exam)
        examTimerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val current = _activeExam.value ?: break
                if (current.remainingSeconds <= 1) {
                    submitActiveExam()
                    break
                } else {
                    _activeExam.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                }
            }
        }
    }

    fun selectExamQuestion(index: Int) {
        _activeExam.update { session ->
            session?.copy(currentQuestionIndex = index.coerceIn(0, session.exam.questions.lastIndex))
        }
    }

    fun answerExamQuestion(questionIndex: Int, optionIndex: Int) {
        _activeExam.update { session ->
            session?.copy(selectedAnswers = session.selectedAnswers + (questionIndex to optionIndex))
        }
    }

    fun toggleFlagQuestion(questionIndex: Int) {
        _activeExam.update { session ->
            if (session == null) null
            else {
                val updated = if (questionIndex in session.flaggedIndices) {
                    session.flaggedIndices - questionIndex
                } else {
                    session.flaggedIndices + questionIndex
                }
                session.copy(flaggedIndices = updated)
            }
        }
    }

    fun requestExam5050Hint() {
        val session = _activeExam.value ?: return
        val qIdx = session.currentQuestionIndex
        if (session.hintsByQuestion.containsKey(qIdx) || session.isHintLoading) return
        val q = session.exam.questions[qIdx]
        val lang = _language.value
        viewModelScope.launch {
            _activeExam.update { it?.copy(isHintLoading = true, hintError = null) }
            val result = aiService.generateExamHint(
                questionText = if (lang == AppLanguage.AR) q.promptAr else q.promptEn,
                codeSnippet = q.codeSnippet,
                options = if (lang == AppLanguage.AR) q.optionsAr else q.optionsEn,
                correctIndex = q.correctIndex,
                language = lang
            )
            result.onSuccess { hint ->
                _activeExam.update { current ->
                    current?.copy(
                        hintsByQuestion = current.hintsByQuestion + (qIdx to hint),
                        isHintLoading = false
                    )
                }
                repository.incrementDailyTokens(140L)
            }.onFailure { err ->
                _activeExam.update {
                    it?.copy(isHintLoading = false, hintError = err.localizedMessage ?: "Hint unavailable")
                }
            }
        }
    }

    fun submitActiveExam(): CompletedExamResult? {
        val session = _activeExam.value ?: return null
        examTimerJob?.cancel()
        val questions = session.exam.questions
        val correctCount = questions.indices.count { idx ->
            session.selectedAnswers[idx] == questions[idx].correctIndex
        }
        val scorePercent = if (questions.isEmpty()) 0 else (correctCount * 100) / questions.size
        val elapsedSeconds = (session.exam.durationMinutes * 60 - session.remainingSeconds).coerceAtLeast(1)
        val lang = _language.value
        val examTitle = if (lang == AppLanguage.AR) session.exam.titleAr else session.exam.titleEn
        val missedTopics = questions.indices
            .filter { idx -> session.selectedAnswers[idx] != questions[idx].correctIndex }
            .map { idx -> if (lang == AppLanguage.AR) questions[idx].topicAr else questions[idx].topicEn }

        val placeholderResult = CompletedExamResult(
            attemptId = "",
            exam = session.exam,
            selectedAnswers = session.selectedAnswers,
            correctCount = correctCount,
            totalQuestions = questions.size,
            scorePercent = scorePercent,
            durationSeconds = elapsedSeconds
        )
        _lastExamResult.value = placeholderResult
        _activeExam.value = null

        viewModelScope.launch {
            val savedId = repository.saveExamAttempt(
                examId = session.exam.id,
                examTitle = examTitle,
                scorePercent = scorePercent,
                correctCount = correctCount,
                totalQuestions = questions.size,
                durationSeconds = elapsedSeconds
            ).getOrNull().orEmpty()

            if (savedId.isNotBlank()) {
                _lastExamResult.update { it?.copy(attemptId = savedId) }
            }

            _examDiagnosticsState.value = AiStreamUiState.Loading
            try {
                var diagText = ""
                aiService.streamExamDiagnostics(
                    examTitle = examTitle,
                    scorePercent = scorePercent,
                    correctCount = correctCount,
                    totalQuestions = questions.size,
                    missedTopics = missedTopics,
                    language = lang
                ).collect { partial ->
                    diagText = partial
                    _examDiagnosticsState.value = AiStreamUiState.Streaming(partial)
                }
                _examDiagnosticsState.value = AiStreamUiState.Success(diagText)
                if (savedId.isNotBlank()) {
                    repository.attachExamAiDiagnostics(savedId, diagText)
                }
                repository.incrementDailyTokens((diagText.length / 3L).coerceAtLeast(110L))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _examDiagnosticsState.value = AiStreamUiState.Error(e.localizedMessage ?: "AI diagnostics failed.")
            }
        }
        return placeholderResult
    }

    // --- STUDENT COMMUNITIES ---

    fun togglePomodoroRoom() {
        _pomodoroJoined.value = !_pomodoroJoined.value
    }

    fun createCommunityPost(category: String, title: String, body: String, codeSnippet: String?) {
        val profile = userProfile.value
        val name = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        val role = "${profile?.major ?: "علوم الحاسب"} • ${profile?.studyYear ?: "المستوى 3"}"
        viewModelScope.launch {
            repository.createCommunityPost(
                authorName = name,
                authorRole = role,
                authorAvatar = profile?.photoUrl ?: currentUserPhoto,
                category = category,
                title = title,
                body = body,
                codeSnippet = codeSnippet
            ).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم نشر المناقشة بنجاح" else "Discussion published"
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to publish discussion"
            }
        }
    }

    fun upvotePost(post: CommunityPostDoc) {
        if (post.id.startsWith("starter_")) return
        viewModelScope.launch {
            repository.upvoteCommunityPost(post.id, post.upvotes)
        }
    }

    fun generatePostAiDigest(post: CommunityPostDoc) {
        viewModelScope.launch {
            _digestLoadingPostId.value = post.id
            val result = aiService.generateCommunityDigest(
                postTitle = post.title,
                postBody = post.body,
                codeSnippet = post.codeSnippet,
                language = _language.value
            )
            result.onSuccess { digest ->
                _localPostDigests.update { it + (post.id to digest) }
                if (!post.id.startsWith("starter_")) {
                    repository.attachCommunityAiDigest(post.id, digest)
                }
                repository.incrementDailyTokens(95L)
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "AI digest failed"
            }
            _digestLoadingPostId.value = null
        }
    }

    // --- ADMIN CONTROL PANEL ACTIONS ---

    fun adminUpdateUserRole(targetUid: String, role: String, status: String) {
        viewModelScope.launch {
            repository.adminUpdateUserRoleAndStatus(targetUid, role, status).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم تحديث صلاحيات المستخدم" else "User role updated"
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Admin update failed"
            }
        }
    }

    fun adminSaveAiConfig(
        dailyLimit: Long,
        activeModel: String,
        codeModeEnabled: Boolean,
        examHintsEnabled: Boolean,
        safetyStrict: Boolean
    ) {
        viewModelScope.launch {
            repository.saveAdminAiConfig(
                dailyStudentTokenLimit = dailyLimit,
                activeModel = activeModel,
                codeModeEnabled = codeModeEnabled,
                examHintsEnabled = examHintsEnabled,
                safetyGuardrailsStrict = safetyStrict
            ).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم حفظ إعدادات الذكاء الاصطناعي" else "AI governance settings saved"
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to save AI settings"
            }
        }
    }

    fun adminUpdateLibraryAssetStatus(book: LibraryBook, status: String, note: String? = null) {
        viewModelScope.launch {
            repository.adminUpdateLibraryAssetStatus(book, status, note).onSuccess {
                _statusBanner.value = if (_language.value == AppLanguage.AR) "تم تحديث حالة فحص الكتاب" else "Library security status updated"
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to update asset status"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        examTimerJob?.cancel()
        adminUsersJob?.cancel()
    }
}
