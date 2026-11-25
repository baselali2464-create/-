package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Course
import com.example.data.ExamAttemptDoc
import com.example.data.LearningTrack
import com.example.data.MilestoneStatus
import com.example.data.NibrasCatalog
import com.example.data.StudyPlanDoc
import com.example.data.UserProfileDoc
import com.example.i18n.bi
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.components.NibrasSectionHeader
import com.example.ui.components.RadialProgressGauge
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasErrorContainer
import com.example.ui.theme.NibrasOnErrorContainer
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSecondaryFixed
import com.example.ui.theme.NibrasSuccess
import com.example.ui.theme.NibrasSuccessContainer
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryFixed
import com.example.ui.theme.NibrasWarning
import com.example.viewmodel.AiStreamUiState

/**
 * Screen 8: Interactive Learning Tracks + Live Gemini Personalized Study Plan Generator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningTracksScreen(
    studyPlans: List<StudyPlanDoc>,
    studyPlanAiState: AiStreamUiState,
    onBack: () -> Unit,
    onOpenCourse: (Course) -> Unit,
    onGenerateStudyPlan: (track: LearningTrack, weeklyHours: Int, pace: String) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var activeTrack by remember { mutableStateOf(NibrasCatalog.tracks.first()) }
    var showPlannerSheet by remember { mutableStateOf(false) }
    var weeklyHours by remember { mutableFloatStateOf(12f) }
    var selectedPace by remember { mutableStateOf("balanced") }

    val savedPlanForTrack = remember(studyPlans, activeTrack.id) {
        studyPlans.firstOrNull { it.trackId == activeTrack.id }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("tracks_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = bi("المسارات التعليمية التفاعلية", "Interactive Learning Tracks"),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            NibrasAiBadge(label = bi("خطة AI", "AI Plan"))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Track Switcher Pills
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NibrasCatalog.tracks.forEach { track ->
                        FilterChip(
                            selected = activeTrack.id == track.id,
                            onClick = { activeTrack = track },
                            label = { Text(if (isAr) track.badgeAr else track.badgeEn) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NibrasPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Active Track Hero Card
            item {
                NibrasGradientHeroCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) activeTrack.badgeAr else activeTrack.badgeEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isAr) activeTrack.titleAr else activeTrack.titleEn,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isAr) activeTrack.subtitleAr else activeTrack.subtitleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            RadialProgressGauge(
                                progressPercent = activeTrack.progressPercent,
                                size = 72.dp,
                                strokeWidth = 7.dp,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                progressColor = Color.White,
                                centerTextColor = Color.White
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { showPlannerSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NibrasPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_ai_study_planner_button")
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(bi("توليد جدول دراسي شخصي بـ Gemini", "Generate AI Weekly Study Plan"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Saved or Streaming AI Study Plan Card
            val displayPlanText = when (studyPlanAiState) {
                is AiStreamUiState.Streaming -> studyPlanAiState.text
                is AiStreamUiState.Success -> studyPlanAiState.text
                else -> savedPlanForTrack?.scheduleMarkdown
            }
            if (studyPlanAiState is AiStreamUiState.Loading || !displayPlanText.isNullOrBlank() || studyPlanAiState is AiStreamUiState.Error) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasTertiaryFixed.copy(alpha = 0.52f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NibrasAiBadge(label = bi("جدولك المخصّص السحابي", "Your Synced AI Schedule"))
                                if (savedPlanForTrack != null) {
                                    Text(
                                        text = bi("${savedPlanForTrack.weeklyHours} ساعة/أسبوع", "${savedPlanForTrack.weeklyHours} hrs/wk"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NibrasTertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            when (studyPlanAiState) {
                                AiStreamUiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = NibrasTertiary)
                                    Spacer(Modifier.width(10.dp))
                                    Text(bi("يقوم Gemini ببناء جدولك الدراسي...", "Gemini is building your personalized schedule..."))
                                }
                                is AiStreamUiState.Error -> Surface(color = NibrasErrorContainer, shape = RoundedCornerShape(10.dp)) {
                                    Text(studyPlanAiState.message, color = NibrasOnErrorContainer, modifier = Modifier.padding(10.dp))
                                }
                                else -> Text(displayPlanText.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Vertical Timeline Stepper
            item {
                NibrasSectionHeader(title = bi("خارطة الطريق والمراحل", "Track Milestones Roadmap"))
            }

            items(activeTrack.milestones, key = { it.id }) { milestone ->
                val (icon, tint, bg) = when (milestone.status) {
                    MilestoneStatus.COMPLETED -> Triple(Icons.Rounded.CheckCircle, NibrasSuccess, NibrasSuccessContainer)
                    MilestoneStatus.ACTIVE -> Triple(Icons.Rounded.PlayCircle, NibrasPrimary, NibrasPrimaryFixed)
                    MilestoneStatus.LOCKED -> Triple(Icons.Rounded.Lock, MaterialTheme.colorScheme.outline, NibrasSurfaceContainer)
                    MilestoneStatus.CAPSTONE -> Triple(Icons.Rounded.WorkspacePremium, NibrasTertiary, NibrasTertiaryFixed)
                }
                val linkedCourse = NibrasCatalog.courses.firstOrNull { it.id == milestone.linkedCourseId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = linkedCourse != null) { if (linkedCourse != null) onOpenCourse(linkedCourse) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(
                        width = if (milestone.status == MilestoneStatus.ACTIVE) 1.5.dp else 1.dp,
                        color = if (milestone.status == MilestoneStatus.ACTIVE) NibrasPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = bg, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = tint)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) milestone.titleAr else milestone.titleEn,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (isAr) milestone.subtitleAr else milestone.subtitleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isAr) milestone.durationAr else milestone.durationEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = tint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPlannerSheet) {
        ModalBottomSheet(onDismissRequest = { showPlannerSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = NibrasTertiary)
                    Spacer(Modifier.width(8.dp))
                    Text(bi("مولّد الخطة الدراسية الذكي", "Gemini AI Study Plan Generator"), style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = bi("ساعات الدراسة الأسبوعية المتاحة: ${weeklyHours.toInt()} ساعة", "Available weekly study hours: ${weeklyHours.toInt()} hrs"),
                    style = MaterialTheme.typography.titleSmall
                )
                Slider(
                    value = weeklyHours,
                    onValueChange = { weeklyHours = it },
                    valueRange = 4f..30f,
                    steps = 25
                )
                Text(bi("وتيرة التعلم المفضلة", "Preferred Study Pace"), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "relaxed" to bi("هادئ", "Relaxed"),
                        "balanced" to bi("متوازن", "Balanced"),
                        "intensive" to bi("مكثّف", "Intensive")
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedPace == key,
                            onClick = { selectedPace = key },
                            label = { Text(label) }
                        )
                    }
                }
                Button(
                    onClick = {
                        showPlannerSheet = false
                        onGenerateStudyPlan(activeTrack, weeklyHours.toInt(), selectedPace)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_generate_study_plan"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NibrasPrimary)
                ) {
                    Text(bi("إنشاء ومزامنة الخطة الآن", "Generate & Sync Plan"))
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Screen 16: Progress & Academic Analytics
 */
@Composable
fun ProgressAnalyticsScreen(
    profile: UserProfileDoc?,
    examAttempts: List<ExamAttemptDoc>,
    onBack: () -> Unit
) {
    val weeklyHoursData = listOf(
        bi("سبت", "Sat") to 2.0f,
        bi("أحد", "Sun") to 3.2f,
        bi("إثن", "Mon") to 1.8f,
        bi("ثلا", "Tue") to 3.8f,
        bi("أرب", "Wed") to 2.6f,
        bi("خمي", "Thu") to 4.1f,
        bi("جمع", "Fri") to 1.5f
    )
    val maxDay = 4.5f
    val skills = listOf(
        Triple(bi("الخوارزميات وهياكل البيانات", "Algorithms & DSA"), 88, NibrasPrimary),
        Triple(bi("الذكاء الاصطناعي وبايثون", "AI & Python"), 82, NibrasTertiary),
        Triple(bi("هندسة الأنظمة السحابية", "Cloud Architecture"), 66, NibrasSecondary),
        Triple(bi("الأمن السيبراني وقواعد البيانات", "Cybersecurity & SQL"), 74, NibrasSuccess)
    )
    val avgExamScore = if (examAttempts.isEmpty()) 91 else examAttempts.map { it.scorePercent.toInt() }.average().toInt()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("analytics_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(bi("التقدم الدراسي والتحليلات الأكاديمية", "Progress & Academic Analytics"), style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GPA & Mastery Radial Bento
            item {
                NibrasGradientHeroCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bi("المعدل التراكمي التقديري", "Estimated Academic GPA"), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                            Text("${profile?.gpa ?: 4.85} / 5.0", style = MaterialTheme.typography.displaySmall, color = Color.White)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = bi("متوسط الاختبارات: $avgExamScore% • ${profile?.completedLessonsCount ?: 12} درس مكتمل", "Exam avg: $avgExamScore% • ${profile?.completedLessonsCount ?: 12} lessons completed"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        RadialProgressGauge(
                            progressPercent = 74,
                            size = 80.dp,
                            strokeWidth = 8.dp,
                            trackColor = Color.White.copy(alpha = 0.24f),
                            progressColor = Color.White,
                            centerTextColor = Color.White
                        )
                    }
                }
            }

            // Weekly Study Activity Bar Chart
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(bi("النشاط الدراسي الأسبوعي", "Weekly Study Activity"), style = MaterialTheme.typography.titleMedium)
                            Text(bi("19.0 ساعة", "19.0 hrs"), style = MaterialTheme.typography.labelLarge, color = NibrasPrimary)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyHoursData.forEach { (day, hrs) ->
                                val ratio = (hrs / maxDay).coerceIn(0.15f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${hrs}h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((84f * ratio).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(if (hrs >= 3.5f) NibrasPrimary else NibrasSecondary.copy(alpha = 0.65f))
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(day, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Skills Breakdown Matrix
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(bi("مصفوفة إتقان المهارات التقنية", "Technical Skills Mastery Matrix"), style = MaterialTheme.typography.titleMedium)
                        skills.forEach { (title, pct, color) ->
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(title, style = MaterialTheme.typography.bodyMedium)
                                    Text("$pct%", style = MaterialTheme.typography.labelLarge, color = color)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                                    color = color,
                                    trackColor = NibrasSurfaceContainer,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }

            // Badges Reel
            item {
                NibrasSectionHeader(title = bi("أوسمة التميز الأكاديمي", "Academic Achievement Badges"))
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AchievementBadgeCard(bi("بطل الخوارزميات", "DSA Ace"), NibrasPrimary, NibrasPrimaryFixed, Modifier.weight(1f))
                    AchievementBadgeCard(bi("سلسلة 14 يوماً", "14-Day Streak"), NibrasWarning, Color(0xFFFEF3C7), Modifier.weight(1f))
                    AchievementBadgeCard(bi("باحث ذكي", "AI Scholar"), NibrasTertiary, NibrasTertiaryFixed, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AchievementBadgeCard(title: String, tint: Color, bg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = bg) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}
