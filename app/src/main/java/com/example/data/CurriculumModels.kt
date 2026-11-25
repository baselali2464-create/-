package com.example.data

data class Course(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val categoryAr: String,
    val categoryEn: String,
    val levelAr: String,
    val levelEn: String,
    val durationHours: Int,
    val lessonsCount: Int,
    val rating: Double,
    val studentsCount: Int,
    val instructorNameAr: String,
    val instructorNameEn: String,
    val instructorTitleAr: String,
    val instructorTitleEn: String,
    val instructorAvatar: String,
    val bannerUrl: String,
    val summaryAr: String,
    val summaryEn: String,
    val defaultProgress: Int,
    val units: List<CourseUnit>
)

data class CourseUnit(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val lessons: List<Lesson>
)

data class Lesson(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val durationMin: Int,
    val videoPosterUrl: String,
    val contentAr: String,
    val contentEn: String,
    val codeLanguage: String,
    val codeSnippet: String
)

data class LearningTrack(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val badgeAr: String,
    val badgeEn: String,
    val bannerUrl: String,
    val progressPercent: Int,
    val estimatedWeeks: Int,
    val milestones: List<TrackMilestone>
)

enum class MilestoneStatus { COMPLETED, ACTIVE, LOCKED, CAPSTONE }

data class TrackMilestone(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val durationAr: String,
    val durationEn: String,
    val status: MilestoneStatus,
    val linkedCourseId: String? = null
)

data class LibraryBook(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val authorAr: String,
    val authorEn: String,
    val categoryAr: String,
    val categoryEn: String,
    val coverUrl: String,
    val totalPages: Int,
    val defaultPage: Int,
    val rating: Double,
    val fileSizeMb: Double,
    val sha256Prefix: String,
    val aiSummaryAr: String,
    val aiSummaryEn: String,
    val pages: List<BookChapterPage>
)

data class BookChapterPage(
    val pageNumber: Int,
    val chapterTitleAr: String,
    val chapterTitleEn: String,
    val paragraphsAr: List<String>,
    val paragraphsEn: List<String>,
    val highlightableQuoteAr: String,
    val highlightableQuoteEn: String,
    val codeSnippet: String? = null,
    val showBinaryTreeDiagram: Boolean = false
)

data class TechnicalExam(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val categoryAr: String,
    val categoryEn: String,
    val difficultyAr: String,
    val difficultyEn: String,
    val durationMinutes: Int,
    val passingPercent: Int,
    val isAdaptiveAi: Boolean = false,
    val questions: List<ExamQuestion>
)

data class ExamQuestion(
    val id: String,
    val topicAr: String,
    val topicEn: String,
    val promptAr: String,
    val promptEn: String,
    val codeSnippet: String? = null,
    val optionsAr: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationAr: String,
    val explanationEn: String
)
