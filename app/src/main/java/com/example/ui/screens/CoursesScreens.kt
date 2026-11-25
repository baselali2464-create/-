package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Course
import com.example.data.CourseProgressDoc
import com.example.data.Lesson
import com.example.data.LessonNoteDoc
import com.example.data.NibrasCatalog
import com.example.i18n.bi
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasCodeCard
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.components.NibrasNetworkImage
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasErrorContainer
import com.example.ui.theme.NibrasOnErrorContainer
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSuccess
import com.example.ui.theme.NibrasSuccessContainer
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryFixed
import com.example.viewmodel.AiStreamUiState
import kotlinx.coroutines.delay

/**
 * Screen 5: Courses Catalog
 */
@Composable
fun CoursesCatalogScreen(
    courseProgressMap: Map<String, CourseProgressDoc>,
    onOpenCourse: (Course) -> Unit,
    onOpenAiTutor: () -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL" to bi("الكل", "All"),
        "Cloud Computing" to bi("الحوسبة السحابية", "Cloud"),
        "Artificial Intelligence" to bi("الذكاء الاصطناعي", "AI"),
        "Cybersecurity" to bi("الأمن السيبراني", "Security"),
        "Algorithms" to bi("الخوارزميات", "Algorithms")
    )

    val filteredCourses = remember(query, selectedCategory, isAr) {
        NibrasCatalog.courses.filter { c ->
            val matchCat = selectedCategory == "ALL" || c.categoryEn == selectedCategory
            val q = query.trim().lowercase()
            val matchText = q.isBlank() ||
                c.titleAr.lowercase().contains(q) ||
                c.titleEn.lowercase().contains(q) ||
                c.categoryAr.lowercase().contains(q)
            matchCat && matchText
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = bi("قسم الدورات التعليمية", "Technical Courses Catalog"),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(bi("ابحث في الدورات والمحاضرات...", "Search courses, topics...")) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("courses_search_input")
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NibrasPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // AI Adaptive Recommendation Banner
        item {
            NibrasGradientHeroCard(
                modifier = Modifier.clickable(onClick = onOpenAiTutor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bi("توصية نبراس التكيّفية", "Nibras AI Recommendation"),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bi("بناءً على أدائك في بايثون، نوصي بإكمال دورة الأنظمة السحابية", "Based on your Python mastery, continue Cloud Architecture next"),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }

        items(filteredCourses, key = { it.id }) { course ->
            val progress = courseProgressMap[course.id]?.progressPercent?.toInt() ?: course.defaultProgress
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCourse(course) }
                    .testTag("course_card_${course.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column {
                    Box {
                        NibrasNetworkImage(
                            url = course.bannerUrl,
                            contentDescription = if (isAr) course.titleAr else course.titleEn,
                            modifier = Modifier.fillMaxWidth().height(152.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = NibrasPrimary.copy(alpha = 0.9f),
                            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                        ) {
                            Text(
                                text = if (isAr) course.levelAr else course.levelEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAr) course.categoryAr else course.categoryEn,
                                style = MaterialTheme.typography.labelMedium,
                                color = NibrasSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("${course.rating}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isAr) course.titleAr else course.titleEn,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isAr) course.summaryAr else course.summaryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bi("${course.durationHours} ساعة • ${course.units.sumOf { it.lessons.size }} دروس", "${course.durationHours}h • ${course.units.sumOf { it.lessons.size }} lessons"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.labelSmall,
                                color = NibrasPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
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

/**
 * Screen 6: Course Details & Curriculum
 */
@Composable
fun CourseDetailsScreen(
    course: Course,
    progressDoc: CourseProgressDoc?,
    onBack: () -> Unit,
    onOpenLesson: (Course, Lesson) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var selectedTab by remember { mutableIntStateOf(0) }
    var expandedUnitId by remember { mutableStateOf(course.units.firstOrNull()?.id) }
    val completedIds = progressDoc?.completedLessonIds ?: emptyList()
    val progressPct = progressDoc?.progressPercent?.toInt() ?: course.defaultProgress
    val firstLesson = course.units.firstOrNull()?.lessons?.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("course_detail_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = bi("تفاصيل الدورة الأكاديمية", "Course Details"),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            NibrasAiBadge(label = "$progressPct%")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero preview
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { if (firstLesson != null) onOpenLesson(course, firstLesson) }
                ) {
                    NibrasNetworkImage(
                        url = course.bannerUrl,
                        contentDescription = if (isAr) course.titleAr else course.titleEn,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.32f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f),
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play Preview", tint = NibrasPrimary, modifier = Modifier.size(34.dp))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isAr) course.titleAr else course.titleEn,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isAr) course.summaryAr else course.summaryEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChipCard(bi("المدة", "Duration"), "${course.durationHours}h", Modifier.weight(1f))
                    MetricChipCard(bi("الدروس", "Lessons"), "${course.units.sumOf { it.lessons.size }}", Modifier.weight(1f))
                    MetricChipCard(bi("التقييم", "Rating"), "${course.rating} ★", Modifier.weight(1f))
                }
            }

            // Segmented Tabs
            item {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(bi("المنهج", "Curriculum")) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(bi("المدرّس", "Instructor")) })
                }
            }

            if (selectedTab == 0) {
                items(course.units, key = { it.id }) { unit ->
                    val expanded = expandedUnitId == unit.id
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedUnitId = if (expanded) null else unit.id },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isAr) unit.titleAr else unit.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    unit.lessons.forEach { lesson ->
                                        val isDone = lesson.id in completedIds
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isDone) NibrasSuccessContainer.copy(alpha = 0.55f) else NibrasSurfaceContainer,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenLesson(course, lesson) }
                                                .testTag("lesson_item_${lesson.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
                                                    contentDescription = null,
                                                    tint = if (isDone) NibrasSuccess else NibrasPrimary
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (isAr) lesson.titleAr else lesson.titleEn,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = bi("${lesson.durationMin} دقيقة • كود ${lesson.codeLanguage.uppercase()}", "${lesson.durationMin} min • ${lesson.codeLanguage.uppercase()} lab"),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            NibrasNetworkImage(
                                url = course.instructorAvatar,
                                contentDescription = course.instructorNameEn,
                                modifier = Modifier.size(64.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isAr) course.instructorNameAr else course.instructorNameEn,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isAr) course.instructorTitleAr else course.instructorTitleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Bar
        Surface(
            color = NibrasSurfaceLowest,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(bi("تقدمك الحالي", "Current Progress"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$progressPct%", style = MaterialTheme.typography.titleMedium, color = NibrasPrimary)
                }
                Button(
                    onClick = { if (firstLesson != null) onOpenLesson(course, firstLesson) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NibrasPrimary),
                    modifier = Modifier.testTag("start_course_lesson_button")
                ) {
                    Text(bi("متابعة الدرس الآن", "Continue Lesson"))
                }
            }
        }
    }
}

@Composable
private fun MetricChipCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = NibrasSurfaceContainer
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = NibrasPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Screen 7: Interactive Lesson Player (with live Gemini Smart Summary & Firestore Timestamped Notes)
 */
@Composable
fun LessonPlayerScreen(
    course: Course,
    lesson: Lesson,
    isCompleted: Boolean,
    notes: List<LessonNoteDoc>,
    aiSummaryState: AiStreamUiState,
    onBack: () -> Unit,
    onMarkComplete: () -> Unit,
    onSaveNote: (timestampLabel: String, content: String) -> Unit,
    onGenerateAiSummary: () -> Unit,
    onSelectLesson: (Lesson) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0.28f) }
    var speedIdx by remember { mutableIntStateOf(0) }
    val speeds = listOf("1.0x", "1.25x", "1.5x", "2.0x")
    var selectedTab by remember { mutableIntStateOf(0) }
    var noteDraft by remember { mutableStateOf("") }

    val allLessons = remember(course) { course.units.flatMap { it.lessons } }
    val currentIndex = allLessons.indexOfFirst { it.id == lesson.id }.coerceAtLeast(0)
    val lessonSpecificNotes = remember(notes, lesson.id) { notes.filter { it.lessonId == lesson.id } }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000L)
            playbackProgress = (playbackProgress + 0.015f).coerceAtMost(1f)
            if (playbackProgress >= 1f) isPlaying = false
        }
    }

    val totalSec = lesson.durationMin * 60
    val currentSec = (totalSec * playbackProgress).toInt()
    val timestampLabel = "%02d:%02d".format(currentSec / 60, currentSec % 60)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("lesson_player_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAr) lesson.titleAr else lesson.titleEn,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isAr) course.titleAr else course.titleEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 16:9 Interactive Player Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
        ) {
            NibrasNetworkImage(
                url = lesson.videoPosterUrl,
                contentDescription = "Lesson video frame",
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NibrasPrimary.copy(alpha = 0.9f))
                    .testTag("lesson_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Slider(
                    value = playbackProgress,
                    onValueChange = { playbackProgress = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF5BB8FE),
                        inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$timestampLabel / ${lesson.durationMin}:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { speedIdx = (speedIdx + 1) % speeds.size }
                    ) {
                        Text(
                            text = speeds[speedIdx],
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(bi("الشرح والكود", "Lesson & Code")) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(bi("ملخص AI", "AI Summary")) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(bi("ملاحظاتي (${lessonSpecificNotes.size})", "Notes (${lessonSpecificNotes.size})")) })
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Text(
                            text = if (isAr) lesson.contentAr else lesson.contentEn,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (lesson.codeSnippet.isNotBlank()) {
                        item {
                            NibrasCodeCard(
                                code = lesson.codeSnippet,
                                languageLabel = lesson.codeLanguage
                            )
                        }
                    }
                }
                1 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = NibrasTertiaryFixed.copy(alpha = 0.55f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NibrasAiBadge(label = bi("تلخيص Gemini الفوري", "Live Gemini Summary"))
                                    Button(
                                        onClick = onGenerateAiSummary,
                                        enabled = aiSummaryState !is AiStreamUiState.Loading && aiSummaryState !is AiStreamUiState.Streaming,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NibrasTertiary),
                                        modifier = Modifier.testTag("lesson_ai_summary_button")
                                    ) {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(bi("ولّد الملخص", "Summarize"))
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                when (aiSummaryState) {
                                    AiStreamUiState.Idle -> Text(
                                        text = bi(
                                            "اضغط على «ولّد الملخص» ليقوم مساعد نبراس بتلخيص النقاط الجوهرية وأسئلة الاختبار المتوقعة لهذا الدرس.",
                                            "Tap Summarize to generate structured lecture takeaways and exam tips via Gemini."
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AiStreamUiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = NibrasTertiary)
                                        Spacer(Modifier.width(10.dp))
                                        Text(bi("جاري تحليل الدرس عبر Gemini...", "Analyzing lesson with Gemini..."))
                                    }
                                    is AiStreamUiState.Streaming -> Text(aiSummaryState.text, style = MaterialTheme.typography.bodyMedium)
                                    is AiStreamUiState.Success -> Text(aiSummaryState.text, style = MaterialTheme.typography.bodyMedium)
                                    is AiStreamUiState.Error -> Surface(color = NibrasErrorContainer, shape = RoundedCornerShape(10.dp)) {
                                        Text(aiSummaryState.message, color = NibrasOnErrorContainer, modifier = Modifier.padding(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = NibrasPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = bi("إضافة ملاحظة عند الدقيقة $timestampLabel", "Add timestamped note at $timestampLabel"),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = noteDraft,
                                    onValueChange = { noteDraft = it },
                                    placeholder = { Text(bi("اكتب ملاحظتك الأكاديمية هنا...", "Write your study note...")) },
                                    modifier = Modifier.fillMaxWidth().testTag("lesson_note_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    minLines = 2
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (noteDraft.isNotBlank()) {
                                            onSaveNote(timestampLabel, noteDraft)
                                            noteDraft = ""
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End).testTag("lesson_save_note_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(bi("حفظ سحابياً", "Save Note"))
                                }
                            }
                        }
                    }
                    items(lessonSpecificNotes, key = { it.id }) { note ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NibrasSurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = note.timestampLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NibrasPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(note.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Sticky Prev / Complete / Next bar
        Surface(color = NibrasSurfaceLowest, shadowElevation = 6.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentIndex > 0) onSelectLesson(allLessons[currentIndex - 1]) },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(bi("السابق", "Prev"))
                }
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier.weight(1f).testTag("lesson_complete_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) NibrasSuccess else NibrasPrimary
                    )
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isCompleted) bi("مكتمل ✓", "Completed ✓") else bi("إتمام الدرس", "Mark Complete"))
                }
                OutlinedButton(
                    onClick = { if (currentIndex < allLessons.lastIndex) onSelectLesson(allLessons[currentIndex + 1]) },
                    enabled = currentIndex < allLessons.lastIndex,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(bi("التالي", "Next"))
                }
            }
        }
    }
}
