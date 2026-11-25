package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val major: String = "علوم الحاسب",
    val studyYear: String = "السنة الثالثة",
    val skills: List<String> = emptyList(),
    val preferredLanguage: String = "ar",
    val aiStyle: String = "academic",
    val role: String = "student",
    val accountStatus: String = "active",
    val onboardingDone: Boolean = false,
    val dailyTokensUsed: Long = 0L,
    val gpa: Double = 4.85,
    val completedLessonsCount: Long = 0L,
    val studyHoursWeek: Double = 14.5,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CourseProgressDoc(
    @DocumentId val id: String = "",
    val courseId: String = "",
    val completedLessonIds: List<String> = emptyList(),
    val lastLessonId: String = "",
    val progressPercent: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LessonNoteDoc(
    @DocumentId val id: String = "",
    val courseId: String = "",
    val lessonId: String = "",
    val timestampLabel: String = "00:00",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ReadingStateDoc(
    @DocumentId val id: String = "",
    val bookId: String = "",
    val currentPage: Long = 1L,
    val totalPages: Long = 100L,
    val progressPercent: Long = 0L,
    val bookmarked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookHighlightDoc(
    @DocumentId val id: String = "",
    val bookId: String = "",
    val page: Long = 1L,
    val quote: String = "",
    val note: String = "",
    val aiExplanation: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ExamAttemptDoc(
    @DocumentId val id: String = "",
    val examId: String = "",
    val examTitle: String = "",
    val scorePercent: Long = 0L,
    val correctCount: Long = 0L,
    val totalQuestions: Long = 1L,
    val durationSeconds: Long = 0L,
    val aiDiagnostics: String? = null,
    val status: String = "completed",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AiMessageDoc(
    @DocumentId val id: String = "",
    val sender: String = "user",
    val mode: String = "academic",
    val text: String = "",
    val tokensEstimate: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudyPlanDoc(
    @DocumentId val id: String = "",
    val trackId: String = "",
    val trackTitle: String = "",
    val weeklyHours: Long = 10L,
    val pace: String = "balanced",
    val scheduleMarkdown: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CommunityPostDoc(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val authorAvatar: String? = null,
    val category: String = "",
    val title: String = "",
    val body: String = "",
    val codeSnippet: String? = null,
    val visibility: String = "public",
    val upvotes: Long = 0L,
    val repliesCount: Long = 0L,
    val aiDigest: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminAiConfigDoc(
    @DocumentId val id: String = "",
    val dailyStudentTokenLimit: Long = 25000L,
    val activeModel: String = "gemini-3.8-flash",
    val codeModeEnabled: Boolean = true,
    val examHintsEnabled: Boolean = true,
    val safetyGuardrailsStrict: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LibraryAssetAuditDoc(
    @DocumentId val id: String = "",
    val bookId: String = "",
    val title: String = "",
    val category: String = "",
    val fileSizeMb: Double = 0.0,
    val scanStatus: String = "verified",
    val sha256Prefix: String = "",
    val reviewerNote: String? = null,
    val visibility: String = "catalog",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
