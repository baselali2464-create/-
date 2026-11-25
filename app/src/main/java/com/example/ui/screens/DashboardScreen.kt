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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Course
import com.example.data.CourseProgressDoc
import com.example.data.LibraryBook
import com.example.data.NibrasAssets
import com.example.data.NibrasCatalog
import com.example.data.UserProfileDoc
import com.example.i18n.bi
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.components.NibrasNetworkImage
import com.example.ui.components.NibrasSectionHeader
import com.example.ui.components.RadialProgressGauge
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSecondaryContainer
import com.example.ui.theme.NibrasSecondaryFixed
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryContainer
import com.example.ui.theme.NibrasTertiaryFixed
import com.example.ui.theme.NibrasWarning
import com.example.ui.theme.NibrasWarningContainer

/**
 * Screen 4: Student Dashboard
 */
@Composable
fun DashboardScreen(
    profile: UserProfileDoc?,
    courseProgressMap: Map<String, CourseProgressDoc>,
    isAdminEligible: Boolean,
    onToggleLanguage: () -> Unit,
    onOpenCourse: (Course) -> Unit,
    onOpenBook: (LibraryBook) -> Unit,
    onOpenAiTutor: () -> Unit,
    onOpenTracks: () -> Unit,
    onOpenCommunities: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenCoursesCatalog: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAdminConsole: () -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    val studentName = profile?.displayName?.ifBlank { bi("باسل الحربي", "Basel Al-Harbi") }
        ?: bi("باسل الحربي", "Basel Al-Harbi")
    val overallProgress = if (courseProgressMap.isEmpty()) 68 else {
        val avg = courseProgressMap.values.map { it.progressPercent.toInt() }.average().toInt()
        avg.coerceIn(15, 100)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Greeting & Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    NibrasNetworkImage(
                        url = profile?.photoUrl ?: NibrasAssets.STUDENT_BASEL,
                        contentDescription = "Student avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenSettings)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = bi("مرحباً بعودتك،", "Welcome back,"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = studentName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAdminEligible) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NibrasTertiaryFixed,
                            modifier = Modifier
                                .clickable(onClick = onOpenAdminConsole)
                                .testTag("dashboard_admin_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.AdminPanelSettings,
                                    contentDescription = "Admin Console",
                                    tint = NibrasTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = bi("الإدارة", "Admin"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NibrasTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("dashboard_language_toggle")
                    ) {
                        Icon(Icons.Rounded.Language, contentDescription = "Switch Language", tint = NibrasPrimary)
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("dashboard_settings_button")
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Account Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Radial Academic Mastery Bento Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAnalytics)
                    .testTag("dashboard_analytics_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bi("إنجاز الفصل الدراسي", "Semester Mastery"),
                            style = MaterialTheme.typography.labelMedium,
                            color = NibrasSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bi("المعدل ${profile?.gpa ?: 4.85} / 5.0", "GPA ${profile?.gpa ?: 4.85} / 5.0"),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = bi(
                                "${profile?.completedLessonsCount ?: 12} درس مكتمل • ${profile?.studyHoursWeek ?: 14.5} ساعة هذا الأسبوع",
                                "${profile?.completedLessonsCount ?: 12} lessons done • ${profile?.studyHoursWeek ?: 14.5} hrs this week"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    RadialProgressGauge(progressPercent = overallProgress, size = 82.dp)
                }
            }
        }

        // Featured Nibras AI Mentor Hero Card
        item {
            NibrasGradientHeroCard(
                modifier = Modifier
                    .clickable(onClick = onOpenAiTutor)
                    .testTag("dashboard_ai_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = bi("مدعوم بـ Gemini", "Powered by Gemini"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = bi("5 أوضاع ذكية", "5 Adaptive Modes"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = bi("مساعد نبراس الذكي", "Nibras AI Academic Tutor"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bi(
                            "اطرح أسئلة الخوارزميات، راجع الكود، أو لخّص محاضراتك الجامعية فورياً.",
                            "Ask algorithms questions, debug Python/SQL code, or summarize lectures live."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onOpenAiTutor,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NibrasPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(bi("ابدأ محادثة ذكية الآن", "Start AI Session"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Hub Shortcuts (Tracks, Communities, Analytics)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickHubTile(
                    icon = Icons.Rounded.Route,
                    title = bi("المسارات", "Tracks"),
                    subtitle = bi("خطة ذكية", "AI Plan"),
                    tint = NibrasPrimary,
                    bg = NibrasPrimaryFixed,
                    onClick = onOpenTracks,
                    modifier = Modifier.weight(1f).testTag("quick_hub_tracks")
                )
                QuickHubTile(
                    icon = Icons.Rounded.Forum,
                    title = bi("المجتمع", "Community"),
                    subtitle = bi("غرف بومودورو", "Study Rooms"),
                    tint = NibrasSecondary,
                    bg = NibrasSecondaryFixed,
                    onClick = onOpenCommunities,
                    modifier = Modifier.weight(1f).testTag("quick_hub_community")
                )
                QuickHubTile(
                    icon = Icons.Rounded.BarChart,
                    title = bi("التحليلات", "Analytics"),
                    subtitle = bi("المعدل والمهارات", "GPA & Skills"),
                    tint = NibrasTertiary,
                    bg = NibrasTertiaryFixed,
                    onClick = onOpenAnalytics,
                    modifier = Modifier.weight(1f).testTag("quick_hub_analytics")
                )
            }
        }

        // Active Exam Alert Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = NibrasWarningContainer,
                border = BorderStroke(1.dp, NibrasWarning.copy(alpha = 0.28f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenExams)
                    .testTag("dashboard_exam_alert")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NibrasWarning.copy(alpha = 0.16f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Timer, contentDescription = null, tint = NibrasWarning)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bi("اختبار تكيّفي جاهز للتقييم", "Adaptive Assessment Ready"),
                            style = MaterialTheme.typography.titleSmall,
                            color = NibrasWarning
                        )
                        Text(
                            text = bi("الخوارزميات وهياكل البيانات • 15 دقيقة مع تلميح AI 50/50", "DSA Midterm • 15 mins with Gemini 50/50 Hint"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(Icons.Rounded.Quiz, contentDescription = null, tint = NibrasWarning)
                }
            }
        }

        // Continue Learning Carousel
        item {
            NibrasSectionHeader(
                title = bi("متابعة التعلم", "Continue Learning"),
                actionLabel = bi("عرض الكل", "View All"),
                onAction = onOpenCoursesCatalog
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(NibrasCatalog.courses, key = { it.id }) { course ->
                    val progress = courseProgressMap[course.id]?.progressPercent?.toInt() ?: course.defaultProgress
                    Card(
                        modifier = Modifier
                            .width(248.dp)
                            .clickable { onOpenCourse(course) }
                            .testTag("dashboard_course_${course.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column {
                            NibrasNetworkImage(
                                url = course.bannerUrl,
                                contentDescription = if (isAr) course.titleAr else course.titleEn,
                                modifier = Modifier.fillMaxWidth().height(118.dp)
                            )
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isAr) course.categoryAr else course.categoryEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NibrasSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isAr) course.titleAr else course.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = bi("نسبة الإنجاز", "Progress"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$progress%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NibrasPrimary
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                                    color = NibrasPrimary,
                                    trackColor = NibrasPrimaryFixed,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recommended Career Track
        item {
            val activeTrack = NibrasCatalog.tracks.first()
            NibrasSectionHeader(
                title = bi("المسار المهني المقترح", "Active Career Track"),
                actionLabel = bi("المسار الكامل", "Open Track"),
                onAction = onOpenTracks
            )
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenTracks),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NibrasNetworkImage(
                        url = activeTrack.bannerUrl,
                        contentDescription = if (isAr) activeTrack.titleAr else activeTrack.titleEn,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        NibrasAiBadge(label = if (isAr) activeTrack.badgeAr else activeTrack.badgeEn)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isAr) activeTrack.titleAr else activeTrack.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bi("${activeTrack.progressPercent}% مكتمل • ${activeTrack.estimatedWeeks} أسبوع", "${activeTrack.progressPercent}% complete • ${activeTrack.estimatedWeeks} weeks"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Digital Library Latest Shelf
        item {
            NibrasSectionHeader(
                title = bi("أحدث كتب المكتبة الرقمية", "Digital Library Shelf"),
                actionLabel = bi("المكتبة", "Library"),
                onAction = onOpenLibrary
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(NibrasCatalog.books.take(4), key = { it.id }) { book ->
                    Column(
                        modifier = Modifier
                            .width(126.dp)
                            .clickable { onOpenBook(book) }
                            .testTag("dashboard_book_${book.id}")
                    ) {
                        NibrasNetworkImage(
                            url = book.coverUrl,
                            contentDescription = if (isAr) book.titleAr else book.titleEn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(166.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isAr) book.titleAr else book.titleEn,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuickHubTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = NibrasSurfaceLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
