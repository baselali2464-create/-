package com.example.data

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NibrasRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    // Secondary DI constructor resolving custom named database ID from firebase_applet_config.xml
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "baselali2464@gmail.com"
    }

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    private fun sanitizeDocId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { "doc_${UUID.randomUUID().toString().replace("-", "").take(12)}" }.take(64)
    }

    // --- USER PROFILE ---

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> = flow {
        require(userId.isNotBlank()) { "Blank userId rejected" }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfileDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    logIfActiveUser(error, OperationType.GET, path, userId)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfileExists(
        uid: String,
        displayName: String,
        email: String,
        photoUrl: String?
    ): Result<UserProfileDoc> {
        val path = "users/$uid"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid) { "Signed-in user mismatch." }
            val ref = db.collection("users").document(uid)
            val snap = ref.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(
                    UserProfileDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (existing != null) return Result.success(existing)
            }
            val isBootAdmin = email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) &&
                (auth.currentUser?.isEmailVerified == true)
            val initialRole = if (isBootAdmin) "admin" else "student"
            val safeName = displayName.trim().ifBlank { "طالب نبراس" }.take(100)
            val payload = mapOf(
                "uid" to uid,
                "displayName" to safeName,
                "email" to email.take(200),
                "photoUrl" to photoUrl?.take(1000),
                "major" to "علوم الحاسب",
                "studyYear" to "السنة الثالثة",
                "skills" to listOf("Python", "SQL", "Algorithms"),
                "preferredLanguage" to "ar",
                "aiStyle" to "academic",
                "role" to initialRole,
                "accountStatus" to "active",
                "onboardingDone" to false,
                "dailyTokensUsed" to 1240L,
                "gpa" to 4.85,
                "completedLessonsCount" to 12L,
                "studyHoursWeek" to 14.5,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            ref.set(payload).await()
            val created = ref.get().await().toObject(
                UserProfileDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: UserProfileDoc(uid = uid, displayName = safeName, email = email, role = initialRole)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun completeOnboarding(
        major: String,
        studyYear: String,
        skills: List<String>,
        preferredLanguage: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            requireUserId()
            val boundedSkills = skills.map { it.trim().take(40) }.filter { it.isNotBlank() }.distinct().take(15)
            val lang = if (preferredLanguage == "en") "en" else "ar"
            db.collection("users").document(uid).update(
                mapOf(
                    "major" to major.trim().take(100),
                    "studyYear" to studyYear.trim().take(50),
                    "skills" to boundedSkills,
                    "preferredLanguage" to lang,
                    "onboardingDone" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateUserPreferences(
        preferredLanguage: String? = null,
        aiStyle: String? = null,
        displayName: String? = null
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (preferredLanguage != null && preferredLanguage in listOf("ar", "en")) {
                updates["preferredLanguage"] = preferredLanguage
            }
            if (aiStyle != null && aiStyle in listOf("simple", "academic", "code", "summary", "quiz")) {
                updates["aiStyle"] = aiStyle
            }
            if (!displayName.isNullOrBlank()) {
                updates["displayName"] = displayName.trim().take(100)
            }
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementDailyTokens(deltaTokens: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            requireUserId()
            val safeDelta = deltaTokens.coerceIn(1L, 50000L)
            db.collection("users").document(uid).update(
                mapOf(
                    "dailyTokensUsed" to FieldValue.increment(safeDelta),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- COURSE PROGRESS & LESSON NOTES ---

    fun observeCourseProgress(userId: String): Flow<List<CourseProgressDoc>> = flow {
        val path = "users/$userId/courseProgress"
        emitAll(
            db.collection("users").document(userId).collection("courseProgress")
                .snapshots()
                .map { it.toObjects(CourseProgressDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun markLessonComplete(course: Course, lessonId: String, existingCompleted: List<String>): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val safeCourseId = sanitizeDocId(course.id)
        val path = "users/$uid/courseProgress/$safeCourseId"
        return try {
            requireUserId()
            val allLessons = course.units.flatMap { it.lessons }
            val updatedIds = (existingCompleted + lessonId).distinct().take(200)
            val pct = if (allLessons.isEmpty()) 100 else ((updatedIds.size * 100) / allLessons.size).coerceIn(0, 100)
            val docRef = db.collection("users").document(uid).collection("courseProgress").document(safeCourseId)
            val exists = docRef.get().await().exists()
            if (exists) {
                docRef.update(
                    mapOf(
                        "completedLessonIds" to updatedIds,
                        "lastLessonId" to lessonId.take(64),
                        "progressPercent" to pct.toLong(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "courseId" to safeCourseId,
                        "completedLessonIds" to updatedIds,
                        "lastLessonId" to lessonId.take(64),
                        "progressPercent" to pct.toLong(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            if (lessonId !in existingCompleted) {
                db.collection("users").document(uid).update(
                    mapOf(
                        "completedLessonsCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeLessonNotes(userId: String): Flow<List<LessonNoteDoc>> = flow {
        val path = "users/$userId/lessonNotes"
        emitAll(
            db.collection("users").document(userId).collection("lessonNotes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(LessonNoteDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun addLessonNote(courseId: String, lessonId: String, timestampLabel: String, content: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val trimmed = content.trim()
        if (trimmed.isBlank()) return Result.failure(IllegalArgumentException("Note cannot be empty"))
        val noteId = sanitizeDocId("note_${System.currentTimeMillis()}")
        val path = "users/$uid/lessonNotes/$noteId"
        return try {
            requireUserId()
            db.collection("users").document(uid).collection("lessonNotes").document(noteId).set(
                mapOf(
                    "courseId" to sanitizeDocId(courseId),
                    "lessonId" to sanitizeDocId(lessonId),
                    "timestampLabel" to timestampLabel.take(32),
                    "content" to trimmed.take(2000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- DIGITAL LIBRARY READING STATE & HIGHLIGHTS ---

    fun observeReadingStates(userId: String): Flow<List<ReadingStateDoc>> = flow {
        val path = "users/$userId/readingState"
        emitAll(
            db.collection("users").document(userId).collection("readingState")
                .snapshots()
                .map { it.toObjects(ReadingStateDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveReadingState(bookId: String, currentPage: Int, totalPages: Int, bookmarked: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val safeId = sanitizeDocId(bookId)
        val path = "users/$uid/readingState/$safeId"
        return try {
            requireUserId()
            val safeTotal = totalPages.coerceIn(1, 5000)
            val safePage = currentPage.coerceIn(1, safeTotal)
            val pct = ((safePage * 100) / safeTotal).coerceIn(0, 100)
            val ref = db.collection("users").document(uid).collection("readingState").document(safeId)
            if (ref.get().await().exists()) {
                ref.update(
                    mapOf(
                        "currentPage" to safePage.toLong(),
                        "totalPages" to safeTotal.toLong(),
                        "progressPercent" to pct.toLong(),
                        "bookmarked" to bookmarked,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.set(
                    mapOf(
                        "bookId" to safeId,
                        "currentPage" to safePage.toLong(),
                        "totalPages" to safeTotal.toLong(),
                        "progressPercent" to pct.toLong(),
                        "bookmarked" to bookmarked,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeHighlights(userId: String): Flow<List<BookHighlightDoc>> = flow {
        val path = "users/$userId/highlights"
        emitAll(
            db.collection("users").document(userId).collection("highlights")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(BookHighlightDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveBookHighlight(
        bookId: String,
        page: Int,
        quote: String,
        note: String,
        aiExplanation: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val hlId = sanitizeDocId("hl_${System.currentTimeMillis()}")
        val path = "users/$uid/highlights/$hlId"
        return try {
            requireUserId()
            val payload = mapOf(
                "bookId" to sanitizeDocId(bookId),
                "page" to page.coerceIn(1, 5000).toLong(),
                "quote" to quote.trim().take(1500).ifBlank { "Highlight" },
                "note" to note.trim().take(2000),
                "aiExplanation" to aiExplanation?.trim()?.take(4000)?.ifBlank { null },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            db.collection("users").document(uid).collection("highlights").document(hlId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- EXAM ATTEMPTS ---

    fun observeExamAttempts(userId: String): Flow<List<ExamAttemptDoc>> = flow {
        val path = "users/$userId/examAttempts"
        emitAll(
            db.collection("users").document(userId).collection("examAttempts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(ExamAttemptDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveExamAttempt(
        examId: String,
        examTitle: String,
        scorePercent: Int,
        correctCount: Int,
        totalQuestions: Int,
        durationSeconds: Int
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val attemptId = sanitizeDocId("att_${System.currentTimeMillis()}")
        val path = "users/$uid/examAttempts/$attemptId"
        return try {
            requireUserId()
            db.collection("users").document(uid).collection("examAttempts").document(attemptId).set(
                mapOf(
                    "examId" to sanitizeDocId(examId),
                    "examTitle" to examTitle.trim().take(200).ifBlank { "Exam" },
                    "scorePercent" to scorePercent.coerceIn(0, 100).toLong(),
                    "correctCount" to correctCount.coerceIn(0, 200).toLong(),
                    "totalQuestions" to totalQuestions.coerceIn(1, 200).toLong(),
                    "durationSeconds" to durationSeconds.coerceIn(0, 36000).toLong(),
                    "status" to "completed",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(attemptId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun attachExamAiDiagnostics(attemptId: String, diagnostics: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/examAttempts/$attemptId"
        return try {
            requireUserId()
            db.collection("users").document(uid).collection("examAttempts").document(attemptId).update(
                mapOf(
                    "aiDiagnostics" to diagnostics.trim().take(5000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- AI MESSAGES & STUDY PLANS ---

    fun observeAiMessages(userId: String): Flow<List<AiMessageDoc>> = flow {
        val path = "users/$userId/aiMessages"
        emitAll(
            db.collection("users").document(userId).collection("aiMessages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { it.toObjects(AiMessageDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveAiMessage(sender: String, mode: String, text: String, tokensEstimate: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val msgId = sanitizeDocId("msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}")
        val path = "users/$uid/aiMessages/$msgId"
        return try {
            requireUserId()
            val safeSender = if (sender == "assistant") "assistant" else "user"
            val safeMode = if (mode in listOf("simple", "academic", "code", "summary", "quiz")) mode else "academic"
            db.collection("users").document(uid).collection("aiMessages").document(msgId).set(
                mapOf(
                    "sender" to safeSender,
                    "mode" to safeMode,
                    "text" to text.trim().take(12000).ifBlank { "..." },
                    "tokensEstimate" to tokensEstimate.coerceIn(0L, 50000L),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeStudyPlans(userId: String): Flow<List<StudyPlanDoc>> = flow {
        val path = "users/$userId/studyPlans"
        emitAll(
            db.collection("users").document(userId).collection("studyPlans")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(StudyPlanDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveStudyPlan(
        trackId: String,
        trackTitle: String,
        weeklyHours: Int,
        pace: String,
        scheduleMarkdown: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val planId = sanitizeDocId("plan_$trackId")
        val path = "users/$uid/studyPlans/$planId"
        return try {
            requireUserId()
            val safePace = if (pace in listOf("relaxed", "balanced", "intensive")) pace else "balanced"
            val ref = db.collection("users").document(uid).collection("studyPlans").document(planId)
            if (ref.get().await().exists()) {
                ref.update(
                    mapOf(
                        "weeklyHours" to weeklyHours.coerceIn(1, 80).toLong(),
                        "pace" to safePace,
                        "scheduleMarkdown" to scheduleMarkdown.trim().take(8000).ifBlank { "Study Plan" },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.set(
                    mapOf(
                        "trackId" to sanitizeDocId(trackId),
                        "trackTitle" to trackTitle.trim().take(200).ifBlank { "Track" },
                        "weeklyHours" to weeklyHours.coerceIn(1, 80).toLong(),
                        "pace" to safePace,
                        "scheduleMarkdown" to scheduleMarkdown.trim().take(8000).ifBlank { "Study Plan" },
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- SHARED COMMUNITY DISCUSSIONS (Query aligned: visibility == "public") ---

    fun observeCommunityPosts(userId: String): Flow<List<CommunityPostDoc>> = flow {
        val path = "communityPosts"
        emitAll(
            db.collection("communityPosts")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CommunityPostDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun createCommunityPost(
        authorName: String,
        authorRole: String,
        authorAvatar: String?,
        category: String,
        title: String,
        body: String,
        codeSnippet: String?
    ): Result<Unit> {
        val postId = sanitizeDocId("post_${System.currentTimeMillis()}")
        val path = "communityPosts/$postId"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanBody = body.trim()
            require(cleanTitle.length >= 3) { "Title must be at least 3 characters" }
            require(cleanBody.length >= 3) { "Body must be at least 3 characters" }
            val payload = mapOf(
                "authorId" to uid,
                "authorName" to authorName.trim().take(100).ifBlank { "طالب نبراس" },
                "authorRole" to authorRole.trim().take(60),
                "authorAvatar" to authorAvatar?.take(1000),
                "category" to category.trim().take(60).ifBlank { "عام" },
                "title" to cleanTitle.take(200),
                "body" to cleanBody.take(4000),
                "codeSnippet" to codeSnippet?.trim()?.take(3000)?.ifBlank { null },
                "visibility" to "public",
                "upvotes" to 0L,
                "repliesCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            db.collection("communityPosts").document(postId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun upvoteCommunityPost(postId: String, currentUpvotes: Long): Result<Unit> {
        val path = "communityPosts/$postId"
        return try {
            requireUserId()
            db.collection("communityPosts").document(postId).update(
                mapOf(
                    "upvotes" to (currentUpvotes + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun attachCommunityAiDigest(postId: String, digest: String): Result<Unit> {
        val path = "communityPosts/$postId"
        return try {
            requireUserId()
            db.collection("communityPosts").document(postId).update(
                mapOf(
                    "aiDigest" to digest.trim().take(3000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN GOVERNANCE: AI CONFIG, USERS RBAC, LIBRARY SECURITY AUDIT ---

    fun observeAdminAiConfig(userId: String): Flow<AdminAiConfigDoc> = flow {
        val path = "adminConfig/aiSettings"
        emitAll(
            db.collection("adminConfig").document("aiSettings")
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(AdminAiConfigDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            ?: AdminAiConfigDoc()
                    } else AdminAiConfigDoc()
                }
                .catch { error ->
                    logIfActiveUser(error, OperationType.GET, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveAdminAiConfig(
        dailyStudentTokenLimit: Long,
        activeModel: String,
        codeModeEnabled: Boolean,
        examHintsEnabled: Boolean,
        safetyGuardrailsStrict: Boolean
    ): Result<Unit> {
        val path = "adminConfig/aiSettings"
        return try {
            requireUserId()
            val ref = db.collection("adminConfig").document("aiSettings")
            val safeLimit = dailyStudentTokenLimit.coerceIn(1000L, 500000L)
            val safeModel = activeModel.trim().take(64).ifBlank { "gemini-3.8-flash" }
            if (ref.get().await().exists()) {
                ref.update(
                    mapOf(
                        "dailyStudentTokenLimit" to safeLimit,
                        "activeModel" to safeModel,
                        "codeModeEnabled" to codeModeEnabled,
                        "examHintsEnabled" to examHintsEnabled,
                        "safetyGuardrailsStrict" to safetyGuardrailsStrict,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.set(
                    mapOf(
                        "dailyStudentTokenLimit" to safeLimit,
                        "activeModel" to safeModel,
                        "codeModeEnabled" to codeModeEnabled,
                        "examHintsEnabled" to examHintsEnabled,
                        "safetyGuardrailsStrict" to safetyGuardrailsStrict,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeAllUsersForAdmin(adminUid: String): Flow<List<UserProfileDoc>> = flow {
        val path = "users"
        emitAll(
            db.collection("users")
                .snapshots()
                .map { it.toObjects(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, adminUid)
                    throw error
                }
        )
    }

    suspend fun adminUpdateUserRoleAndStatus(targetUid: String, role: String, status: String): Result<Unit> {
        val path = "users/$targetUid"
        return try {
            requireUserId()
            val safeRole = if (role in listOf("student", "instructor", "admin")) role else "student"
            val safeStatus = if (status in listOf("active", "suspended")) status else "active"
            db.collection("users").document(targetUid).update(
                mapOf(
                    "role" to safeRole,
                    "accountStatus" to safeStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeLibraryAssetAudits(userId: String): Flow<List<LibraryAssetAuditDoc>> = flow {
        val path = "libraryAssets"
        emitAll(
            db.collection("libraryAssets")
                .whereEqualTo("visibility", "catalog")
                .snapshots()
                .map { it.toObjects(LibraryAssetAuditDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    logIfActiveUser(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun adminUpdateLibraryAssetStatus(
        book: LibraryBook,
        newStatus: String,
        reviewerNote: String?
    ): Result<Unit> {
        val assetId = sanitizeDocId(book.id)
        val path = "libraryAssets/$assetId"
        return try {
            requireUserId()
            val safeStatus = if (newStatus in listOf("verified", "scanning", "flagged", "quarantined")) newStatus else "verified"
            val ref = db.collection("libraryAssets").document(assetId)
            if (ref.get().await().exists()) {
                val updates = mutableMapOf<String, Any>(
                    "scanStatus" to safeStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!reviewerNote.isNullOrBlank()) {
                    updates["reviewerNote"] = reviewerNote.trim().take(500)
                }
                ref.update(updates).await()
            } else {
                val payload = mapOf(
                    "bookId" to assetId,
                    "title" to book.titleEn.take(200),
                    "category" to book.categoryEn.take(80),
                    "fileSizeMb" to book.fileSizeMb,
                    "scanStatus" to safeStatus,
                    "sha256Prefix" to book.sha256Prefix,
                    "reviewerNote" to reviewerNote?.trim()?.take(500)?.ifBlank { null },
                    "visibility" to "catalog",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                ref.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    private fun logIfActiveUser(error: Throwable, op: OperationType, path: String, expectedUid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
