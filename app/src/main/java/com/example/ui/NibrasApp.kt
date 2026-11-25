package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayLesson
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayLesson
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.NibrasAiService
import com.example.core.attemptAutoSignIn
import com.example.core.authStateFlow
import com.example.core.signOutUser
import com.example.data.Course
import com.example.data.Lesson
import com.example.data.LibraryBook
import com.example.data.NibrasRepository
import com.example.i18n.bi
import com.example.ui.screens.AccountSettingsScreen
import com.example.ui.screens.ActiveExamScreen
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.AssessmentsHubScreen
import com.example.ui.screens.CourseDetailsScreen
import com.example.ui.screens.CoursesCatalogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DigitalLibraryScreen
import com.example.ui.screens.ExamResultScreen
import com.example.ui.screens.LearningTracksScreen
import com.example.ui.screens.LessonPlayerScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProgressAnalyticsScreen
import com.example.ui.screens.SmartBookReaderScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentCommunitiesScreen
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSurface
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTheme
import com.example.viewmodel.NibrasViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

private enum class StudentTab(
    val tag: String,
    val labelAr: String,
    val labelEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("nav_home", "الرئيسية", "Home", Icons.Rounded.Home, Icons.Outlined.Home),
    COURSES("nav_courses", "الدورات", "Courses", Icons.Rounded.PlayLesson, Icons.Outlined.PlayLesson),
    LIBRARY("nav_library", "المكتبة", "Library", Icons.Rounded.MenuBook, Icons.Outlined.MenuBook),
    EXAMS("nav_exams", "الاختبارات", "Exams", Icons.Rounded.Quiz, Icons.Outlined.Quiz),
    AI_TUTOR("nav_ai_tutor", "المساعد AI", "AI Tutor", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome)
}

private sealed interface NibrasDestination {
    data object MainTabs : NibrasDestination
    data class CourseDetails(val course: Course) : NibrasDestination
    data class LessonPlayer(val course: Course, val lesson: Lesson) : NibrasDestination
    data class BookReader(val book: LibraryBook) : NibrasDestination
    data object LearningTracks : NibrasDestination
    data object Communities : NibrasDestination
    data object Analytics : NibrasDestination
    data object Settings : NibrasDestination
    data object ActiveExam : NibrasDestination
    data object ExamResult : NibrasDestination
    data object AdminConsole : NibrasDestination
}

@Composable
fun NibrasApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val authFlow = remember { Firebase.auth.authStateFlow() }
    val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var preAuthLanguage by remember { mutableStateOf(AppLanguage.AR) }
    var isCheckingAuth by remember { mutableStateOf(Firebase.auth.currentUser == null) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            delay(450L)
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { isCheckingAuth = false },
                onUnauthenticated = { isCheckingAuth = false },
                scope = scope
            )
        } else {
            isCheckingAuth = false
        }
    }

    if (currentUser == null) {
        NibrasTheme(language = preAuthLanguage) {
            if (isCheckingAuth) {
                SplashScreen()
            } else {
                LoginScreen(
                    onToggleLanguage = {
                        preAuthLanguage = if (preAuthLanguage == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
                    },
                    onAuthSuccess = { isCheckingAuth = false }
                )
            }
        }
    } else {
        val signedInUser = currentUser!!
        key(signedInUser.uid) {
            AuthenticatedNibrasShell(
                user = signedInUser,
                credentialManager = credentialManager,
                initialLanguage = preAuthLanguage
            )
        }
    }
}

@Composable
private fun AuthenticatedNibrasShell(
    user: FirebaseUser,
    credentialManager: CredentialManager,
    initialLanguage: AppLanguage
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val scope = rememberCoroutineScope()

    val viewModel: NibrasViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                NibrasViewModel(
                    repository = NibrasRepository(appContext),
                    aiService = NibrasAiService(),
                    currentUserId = user.uid,
                    currentUserEmail = user.email.orEmpty(),
                    currentUserName = user.displayName ?: "طالب نبراس",
                    currentUserPhoto = user.photoUrl?.toString()
                )
            }
        }
    )

    LaunchedEffect(initialLanguage) {
        if (initialLanguage == AppLanguage.EN) {
            viewModel.setLanguage(AppLanguage.EN)
        }
    }

    val language by viewModel.language.collectAsStateWithLifecycle()
    val profileReady by viewModel.profileReady.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val courseProgressMap by viewModel.courseProgressMap.collectAsStateWithLifecycle()
    val lessonNotes by viewModel.lessonNotes.collectAsStateWithLifecycle()
    val readingStatesMap by viewModel.readingStatesMap.collectAsStateWithLifecycle()
    val bookHighlights by viewModel.bookHighlights.collectAsStateWithLifecycle()
    val examAttempts by viewModel.examAttempts.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val studyPlans by viewModel.studyPlans.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val adminAiConfig by viewModel.adminAiConfig.collectAsStateWithLifecycle()
    val libraryAssetAudits by viewModel.libraryAssetAudits.collectAsStateWithLifecycle()
    val adminUsers by viewModel.adminUsers.collectAsStateWithLifecycle()

    val tutorAiState by viewModel.tutorAiState.collectAsStateWithLifecycle()
    val selectedAiMode by viewModel.selectedAiMode.collectAsStateWithLifecycle()
    val lessonSummaryState by viewModel.lessonSummaryState.collectAsStateWithLifecycle()
    val bookCopilotState by viewModel.bookCopilotState.collectAsStateWithLifecycle()
    val studyPlanAiState by viewModel.studyPlanAiState.collectAsStateWithLifecycle()
    val examDiagnosticsState by viewModel.examDiagnosticsState.collectAsStateWithLifecycle()
    val localPostDigests by viewModel.localPostDigests.collectAsStateWithLifecycle()
    val digestLoadingPostId by viewModel.digestLoadingPostId.collectAsStateWithLifecycle()
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val lastExamResult by viewModel.lastExamResult.collectAsStateWithLifecycle()
    val pomodoroJoined by viewModel.pomodoroJoined.collectAsStateWithLifecycle()

    val isAdminEligible = viewModel.isAdminEligible(userProfile)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        val message = statusBanner
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearBanner()
        }
    }

    var selectedTab by remember { mutableStateOf(StudentTab.HOME) }
    val navStack = remember { mutableStateListOf<NibrasDestination>(NibrasDestination.MainTabs) }
    val currentDestination = navStack.lastOrNull() ?: NibrasDestination.MainTabs

    fun navigateTo(dest: NibrasDestination) {
        if (navStack.lastOrNull() != dest) {
            navStack.add(dest)
        }
    }

    fun popBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
        }
    }

    fun navigateToTab(tab: StudentTab) {
        selectedTab = tab
        navStack.clear()
        navStack.add(NibrasDestination.MainTabs)
    }

    // Auto-route to ExamResult if the countdown timer auto-submits an active exam
    LaunchedEffect(activeExam, lastExamResult, currentDestination) {
        if (currentDestination is NibrasDestination.ActiveExam && activeExam == null && lastExamResult != null) {
            navStack[navStack.lastIndex] = NibrasDestination.ExamResult
        }
    }

    NibrasTheme(language = language) {
        if (!profileReady) {
            SplashScreen()
            return@NibrasTheme
        }

        if (userProfile != null && !userProfile!!.onboardingDone) {
            OnboardingScreen(
                userName = userProfile?.displayName?.ifBlank { user.displayName ?: "طالب نبراس" }
                    ?: (user.displayName ?: "طالب نبراس"),
                photoUrl = userProfile?.photoUrl ?: user.photoUrl?.toString(),
                onComplete = { major, studyYear, skills ->
                    viewModel.completeOnboarding(major, studyYear, skills)
                }
            )
            return@NibrasTheme
        }

        // Handle system back button cleanly for all secondary destinations
        BackHandler(enabled = currentDestination != NibrasDestination.MainTabs) {
            when (currentDestination) {
                is NibrasDestination.ExamResult -> navigateToTab(StudentTab.EXAMS)
                else -> popBack()
            }
        }

        val isMainTabs = currentDestination is NibrasDestination.MainTabs

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = isMainTabs && maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = NibrasSurface,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (isMainTabs && !useNavigationRail) {
                        NavigationBar(
                            containerColor = NibrasSurfaceLowest,
                            tonalElevation = 4.dp,
                            windowInsets = NavigationBarDefaults.windowInsets
                        ) {
                            StudentTab.entries.forEach { tab ->
                                val selected = selectedTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { selectedTab = tab },
                                    modifier = Modifier.testTag(tab.tag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = bi(tab.labelAr, tab.labelEn)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = bi(tab.labelAr, tab.labelEn),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NibrasPrimary,
                                        selectedTextColor = NibrasPrimary,
                                        indicatorColor = NibrasPrimaryFixed
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = NibrasSurfaceLowest,
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                        ) {
                            StudentTab.entries.forEach { tab ->
                                val selected = selectedTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { selectedTab = tab },
                                    modifier = Modifier.testTag(tab.tag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = bi(tab.labelAr, tab.labelEn)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = bi(tab.labelAr, tab.labelEn),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = NibrasPrimary,
                                        selectedTextColor = NibrasPrimary,
                                        indicatorColor = NibrasPrimaryFixed
                                    )
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (val dest = currentDestination) {
                            is NibrasDestination.MainTabs -> {
                                when (selectedTab) {
                                    StudentTab.HOME -> {
                                        DashboardScreen(
                                            profile = userProfile,
                                            courseProgressMap = courseProgressMap,
                                            isAdminEligible = isAdminEligible,
                                            onToggleLanguage = { viewModel.toggleLanguage() },
                                            onOpenCourse = { navigateTo(NibrasDestination.CourseDetails(it)) },
                                            onOpenBook = {
                                                viewModel.resetBookCopilot()
                                                navigateTo(NibrasDestination.BookReader(it))
                                            },
                                            onOpenAiTutor = { selectedTab = StudentTab.AI_TUTOR },
                                            onOpenTracks = { navigateTo(NibrasDestination.LearningTracks) },
                                            onOpenCommunities = { navigateTo(NibrasDestination.Communities) },
                                            onOpenAnalytics = { navigateTo(NibrasDestination.Analytics) },
                                            onOpenSettings = { navigateTo(NibrasDestination.Settings) },
                                            onOpenExams = { selectedTab = StudentTab.EXAMS },
                                            onOpenCoursesCatalog = { selectedTab = StudentTab.COURSES },
                                            onOpenLibrary = { selectedTab = StudentTab.LIBRARY },
                                            onOpenAdminConsole = {
                                                viewModel.startObservingAdminUsers()
                                                navigateTo(NibrasDestination.AdminConsole)
                                            }
                                        )
                                    }

                                    StudentTab.COURSES -> {
                                        CoursesCatalogScreen(
                                            courseProgressMap = courseProgressMap,
                                            onOpenCourse = { navigateTo(NibrasDestination.CourseDetails(it)) },
                                            onOpenAiTutor = { selectedTab = StudentTab.AI_TUTOR }
                                        )
                                    }

                                    StudentTab.LIBRARY -> {
                                        DigitalLibraryScreen(
                                            readingStatesMap = readingStatesMap,
                                            onOpenBook = {
                                                viewModel.resetBookCopilot()
                                                navigateTo(NibrasDestination.BookReader(it))
                                            },
                                            onOpenAiTutor = { selectedTab = StudentTab.AI_TUTOR }
                                        )
                                    }

                                    StudentTab.EXAMS -> {
                                        AssessmentsHubScreen(
                                            examAttempts = examAttempts,
                                            onStartExam = { exam ->
                                                viewModel.startExam(exam)
                                                navigateTo(NibrasDestination.ActiveExam)
                                            }
                                        )
                                    }

                                    StudentTab.AI_TUTOR -> {
                                        AiTutorScreen(
                                            messages = aiMessages,
                                            aiState = tutorAiState,
                                            selectedMode = selectedAiMode,
                                            dailyTokensUsed = userProfile?.dailyTokensUsed ?: 0L,
                                            dailyTokenLimit = adminAiConfig.dailyStudentTokenLimit,
                                            codeModeEnabled = adminAiConfig.codeModeEnabled,
                                            onSelectMode = { viewModel.setAiMode(it) },
                                            onSendMessage = { viewModel.sendTutorMessage(it) }
                                        )
                                    }
                                }
                            }

                            is NibrasDestination.CourseDetails -> {
                                CourseDetailsScreen(
                                    course = dest.course,
                                    progressDoc = courseProgressMap[dest.course.id],
                                    onBack = { popBack() },
                                    onOpenLesson = { course, lesson ->
                                        viewModel.resetLessonSummary()
                                        navigateTo(NibrasDestination.LessonPlayer(course, lesson))
                                    }
                                )
                            }

                            is NibrasDestination.LessonPlayer -> {
                                val completedIds = courseProgressMap[dest.course.id]?.completedLessonIds ?: emptyList()
                                LessonPlayerScreen(
                                    course = dest.course,
                                    lesson = dest.lesson,
                                    isCompleted = dest.lesson.id in completedIds,
                                    notes = lessonNotes.filter {
                                        it.courseId == dest.course.id && it.lessonId == dest.lesson.id
                                    },
                                    aiSummaryState = lessonSummaryState,
                                    onBack = { popBack() },
                                    onMarkComplete = { viewModel.markLessonComplete(dest.course, dest.lesson.id) },
                                    onSaveNote = { ts, content ->
                                        viewModel.saveLessonNote(dest.course.id, dest.lesson.id, ts, content)
                                    },
                                    onGenerateAiSummary = {
                                        viewModel.generateLessonAiSummary(dest.course, dest.lesson)
                                    },
                                    onSelectLesson = { nextLesson ->
                                        viewModel.resetLessonSummary()
                                        navStack[navStack.lastIndex] =
                                            NibrasDestination.LessonPlayer(dest.course, nextLesson)
                                    }
                                )
                            }

                            is NibrasDestination.BookReader -> {
                                SmartBookReaderScreen(
                                    book = dest.book,
                                    readingState = readingStatesMap[dest.book.id],
                                    highlights = bookHighlights.filter { it.bookId == dest.book.id },
                                    copilotState = bookCopilotState,
                                    onBack = { popBack() },
                                    onUpdatePage = { page, bookmarked ->
                                        viewModel.updateBookReadingProgress(dest.book, page, bookmarked)
                                    },
                                    onRunCopilot = { passage, action ->
                                        viewModel.runBookCopilot(dest.book, passage, action)
                                    },
                                    onSaveHighlight = { page, quote, note, aiExpl ->
                                        viewModel.saveHighlight(dest.book.id, page, quote, note, aiExpl)
                                    }
                                )
                            }

                            is NibrasDestination.LearningTracks -> {
                                LearningTracksScreen(
                                    studyPlans = studyPlans,
                                    studyPlanAiState = studyPlanAiState,
                                    onBack = { popBack() },
                                    onOpenCourse = { navigateTo(NibrasDestination.CourseDetails(it)) },
                                    onGenerateStudyPlan = { track, hours, pace ->
                                        viewModel.generateStudyPlan(track, hours, pace)
                                    }
                                )
                            }

                            is NibrasDestination.Communities -> {
                                StudentCommunitiesScreen(
                                    posts = communityPosts,
                                    localDigests = localPostDigests,
                                    digestLoadingPostId = digestLoadingPostId,
                                    pomodoroJoined = pomodoroJoined,
                                    onBack = { popBack() },
                                    onTogglePomodoro = { viewModel.togglePomodoroRoom() },
                                    onUpvotePost = { viewModel.upvotePost(it) },
                                    onRequestAiDigest = { viewModel.generatePostAiDigest(it) },
                                    onCreatePost = { cat, title, body, code ->
                                        viewModel.createCommunityPost(cat, title, body, code)
                                    }
                                )
                            }

                            is NibrasDestination.Analytics -> {
                                ProgressAnalyticsScreen(
                                    profile = userProfile,
                                    examAttempts = examAttempts,
                                    onBack = { popBack() }
                                )
                            }

                            is NibrasDestination.Settings -> {
                                AccountSettingsScreen(
                                    profile = userProfile,
                                    dailyTokenLimit = adminAiConfig.dailyStudentTokenLimit,
                                    isAdminEligible = isAdminEligible,
                                    onBack = { popBack() },
                                    onSetLanguage = { viewModel.setLanguage(it) },
                                    onSelectAiStyle = { viewModel.setAiMode(it) },
                                    onUpdateDisplayName = { viewModel.updateDisplayName(it) },
                                    onOpenAdminConsole = {
                                        viewModel.startObservingAdminUsers()
                                        navigateTo(NibrasDestination.AdminConsole)
                                    },
                                    onSignOut = {
                                        signOutUser(credentialManager, scope)
                                    }
                                )
                            }

                            is NibrasDestination.ActiveExam -> {
                                val session = activeExam
                                if (session != null) {
                                    ActiveExamScreen(
                                        session = session,
                                        examHintsEnabled = adminAiConfig.examHintsEnabled,
                                        onSelectQuestion = { viewModel.selectExamQuestion(it) },
                                        onAnswerQuestion = { qIdx, optIdx ->
                                            viewModel.answerExamQuestion(qIdx, optIdx)
                                        },
                                        onToggleFlag = { viewModel.toggleFlagQuestion(it) },
                                        onRequest5050Hint = { viewModel.requestExam5050Hint() },
                                        onSubmitExam = {
                                            viewModel.submitActiveExam()
                                            navStack[navStack.lastIndex] = NibrasDestination.ExamResult
                                        },
                                        onExitExam = { popBack() }
                                    )
                                } else {
                                    LaunchedEffect(Unit) { popBack() }
                                }
                            }

                            is NibrasDestination.ExamResult -> {
                                val result = lastExamResult
                                if (result != null) {
                                    ExamResultScreen(
                                        result = result,
                                        diagnosticsState = examDiagnosticsState,
                                        onDone = { navigateToTab(StudentTab.EXAMS) },
                                        onRetake = { exam ->
                                            viewModel.startExam(exam)
                                            navStack[navStack.lastIndex] = NibrasDestination.ActiveExam
                                        }
                                    )
                                } else {
                                    LaunchedEffect(Unit) { navigateToTab(StudentTab.EXAMS) }
                                }
                            }

                            is NibrasDestination.AdminConsole -> {
                                AdminConsoleScreen(
                                    adminUsers = adminUsers,
                                    aiConfig = adminAiConfig,
                                    libraryAudits = libraryAssetAudits,
                                    onExitAdmin = { popBack() },
                                    onUpdateUserRole = { uid, role, status ->
                                        viewModel.adminUpdateUserRole(uid, role, status)
                                    },
                                    onSaveAiConfig = { limit, model, codeMode, hints, strict ->
                                        viewModel.adminSaveAiConfig(limit, model, codeMode, hints, strict)
                                    },
                                    onUpdateAssetStatus = { book, status, note ->
                                        viewModel.adminUpdateLibraryAssetStatus(book, status, note)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
