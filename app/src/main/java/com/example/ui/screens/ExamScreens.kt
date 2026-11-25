package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.OutlinedFlag
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExamAttemptDoc
import com.example.data.NibrasCatalog
import com.example.data.TechnicalExam
import com.example.i18n.bi
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasCodeCard
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.components.RadialProgressGauge
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasError
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
import com.example.ui.theme.NibrasWarning
import com.example.ui.theme.NibrasWarningContainer
import com.example.viewmodel.ActiveExamSession
import com.example.viewmodel.AiStreamUiState
import com.example.viewmodel.CompletedExamResult

/**
 * Screen 11: Assessments Hub
 */
@Composable
fun AssessmentsHubScreen(
    examAttempts: List<ExamAttemptDoc>,
    onStartExam: (TechnicalExam) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var selectedTab by remember { mutableIntStateOf(0) }
    val readiness = if (examAttempts.isEmpty()) 86 else examAttempts.map { it.scorePercent.toInt() }.average().toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(bi("قسم الاختبارات والتقييم الذكي", "Assessments & Adaptive Exams"), style = MaterialTheme.typography.headlineMedium)
        }

        // Readiness KPIs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReadinessKpiCard(bi("جاهزية الاختبار", "Readiness"), "$readiness%", NibrasPrimary, Modifier.weight(1f))
                ReadinessKpiCard(bi("المكتملة", "Completed"), "${examAttempts.size}", NibrasSuccess, Modifier.weight(1f))
                ReadinessKpiCard(bi("متاحة الآن", "Available"), "${NibrasCatalog.exams.size}", NibrasTertiary, Modifier.weight(1f))
            }
        }

        // AI Adaptive Challenge Card
        item {
            val adaptiveExam = NibrasCatalog.exams.first()
            NibrasGradientHeroCard {
                Column {
                    NibrasAiBadge(label = bi("تحدي الذكاء الاصطناعي التكيّفي", "Adaptive Gemini Challenge"))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isAr) adaptiveExam.titleAr else adaptiveExam.titleEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bi("${adaptiveExam.questions.size} أسئلة تقنية • ${adaptiveExam.durationMinutes} دقيقة • تلميح AI 50/50", "${adaptiveExam.questions.size} coding questions • ${adaptiveExam.durationMinutes} mins • Gemini 50/50 Hints"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { onStartExam(adaptiveExam) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NibrasPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("start_adaptive_exam_button")
                    ) {
                        Text(bi("ابدأ الاختبار الآن", "Start Exam Now"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(bi("الاختبارات المتاحة", "Available")) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(bi("السجل المكتمل (${examAttempts.size})", "Completed (${examAttempts.size})")) })
            }
        }

        if (selectedTab == 0) {
            items(NibrasCatalog.exams, key = { it.id }) { exam ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("exam_card_${exam.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isAr) exam.categoryAr else exam.categoryEn, style = MaterialTheme.typography.labelMedium, color = NibrasSecondary)
                            Text(if (isAr) exam.difficultyAr else exam.difficultyEn, style = MaterialTheme.typography.labelSmall, color = NibrasPrimary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(if (isAr) exam.titleAr else exam.titleEn, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bi("${exam.questions.size} أسئلة • ${exam.durationMinutes} دقيقة", "${exam.questions.size} questions • ${exam.durationMinutes} mins"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onStartExam(exam) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NibrasPrimary)
                            ) {
                                Text(bi("دخول", "Start"))
                            }
                        }
                    }
                }
            }
        } else {
            if (examAttempts.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = NibrasSurfaceContainer, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Quiz, contentDescription = null, tint = NibrasPrimary, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(bi("لم تكمل أي اختبار بعد", "No completed exams yet"), style = MaterialTheme.typography.titleSmall)
                            Text(bi("ابدأ اختبارك الأول للحصول على تشخيص فوري من Gemini", "Take your first exam for live Gemini diagnostics"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(examAttempts, key = { it.id }) { attempt ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(attempt.examTitle, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = bi("${attempt.correctCount}/${attempt.totalQuestions} إجابة صحيحة • ${attempt.durationSeconds} ثانية", "${attempt.correctCount}/${attempt.totalQuestions} correct • ${attempt.durationSeconds}s"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(shape = RoundedCornerShape(10.dp), color = NibrasSuccessContainer) {
                                    Text(
                                        "${attempt.scorePercent}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NibrasSuccess,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            if (!attempt.aiDiagnostics.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(attempt.aiDiagnostics, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessKpiCard(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = NibrasSurfaceLowest, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = tint)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Screen 12: Active Interactive Exam (Timer, Pills, Gemini 50/50 Hint, Flag, Submit Confirm)
 */
@Composable
fun ActiveExamScreen(
    session: ActiveExamSession,
    examHintsEnabled: Boolean,
    onSelectQuestion: (Int) -> Unit,
    onAnswerQuestion: (questionIndex: Int, optionIndex: Int) -> Unit,
    onToggleFlag: (Int) -> Unit,
    onRequest5050Hint: () -> Unit,
    onSubmitExam: () -> Unit,
    onExitExam: () -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var showConfirmSubmit by remember { mutableStateOf(false) }

    val qIdx = session.currentQuestionIndex
    val question = session.exam.questions[qIdx]
    val options = if (isAr) question.optionsAr else question.optionsEn
    val selectedOpt = session.selectedAnswers[qIdx]
    val isFlagged = qIdx in session.flaggedIndices
    val hint = session.hintsByQuestion[qIdx]
    val eliminated = hint?.eliminateIndices ?: emptyList()

    val min = session.remainingSeconds / 60
    val sec = session.remainingSeconds % 60
    val timerText = "%02d:%02d".format(min, sec)

    Column(modifier = Modifier.fillMaxSize()) {
        // Exam Header with Countdown Timer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onExitExam, modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Exit exam")
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (session.remainingSeconds < 120) NibrasErrorContainer else NibrasPrimaryFixed
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = if (session.remainingSeconds < 120) NibrasError else NibrasPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (session.remainingSeconds < 120) NibrasError else NibrasPrimary
                    )
                }
            }
            IconButton(
                onClick = { onToggleFlag(qIdx) },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("exam_flag_button")
            ) {
                Icon(
                    imageVector = if (isFlagged) Icons.Rounded.Flag else Icons.Rounded.OutlinedFlag,
                    contentDescription = "Flag question",
                    tint = if (isFlagged) NibrasWarning else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LinearProgressIndicator(
            progress = { (session.selectedAnswers.size.toFloat() / session.exam.questions.size.toFloat()).coerceIn(0.05f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(6.dp).clip(RoundedCornerShape(4.dp)),
            color = NibrasPrimary,
            trackColor = NibrasPrimaryFixed,
            strokeCap = StrokeCap.Round
        )

        Spacer(Modifier.height(10.dp))

        // Horizontal Question Number Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(session.exam.questions) { idx, _ ->
                val active = idx == qIdx
                val answered = session.selectedAnswers.containsKey(idx)
                val flagged = idx in session.flaggedIndices
                val bg = when {
                    active -> NibrasPrimary
                    flagged -> NibrasWarningContainer
                    answered -> NibrasSuccessContainer
                    else -> NibrasSurfaceContainer
                }
                val fg = when {
                    active -> Color.White
                    flagged -> NibrasWarning
                    answered -> NibrasSuccess
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Surface(
                    shape = CircleShape,
                    color = bg,
                    modifier = Modifier.size(40.dp).clickable { onSelectQuestion(idx) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${idx + 1}", style = MaterialTheme.typography.labelLarge, color = fg)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = bi("سؤال ${qIdx + 1} من ${session.exam.questions.size} • ${question.topicAr}", "Question ${qIdx + 1} of ${session.exam.questions.size} • ${question.topicEn}"),
                    style = MaterialTheme.typography.labelMedium,
                    color = NibrasSecondary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isAr) question.promptAr else question.promptEn,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!question.codeSnippet.isNullOrBlank()) {
                item {
                    NibrasCodeCard(code = question.codeSnippet, languageLabel = "python")
                }
            }

            // Gemini 50/50 Hint Section
            if (examHintsEnabled) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NibrasTertiaryFixed.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = NibrasTertiary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(bi("تلميح Gemini الذكي (50/50)", "Gemini 50/50 Smart Hint"), style = MaterialTheme.typography.labelLarge, color = NibrasTertiary)
                                }
                                if (hint == null) {
                                    Button(
                                        onClick = onRequest5050Hint,
                                        enabled = !session.isHintLoading,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NibrasTertiary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("exam_5050_hint_button")
                                    ) {
                                        if (session.isHintLoading) {
                                            CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text(bi("تفعيل 50/50", "Use 50/50"), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            if (hint != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(hint.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            if (session.hintError != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(session.hintError, style = MaterialTheme.typography.bodySmall, color = NibrasError)
                            }
                        }
                    }
                }
            }

            // MCQ Options
            itemsIndexed(options) { optIdx, optText ->
                val isEliminated = optIdx in eliminated
                val isSelected = selectedOpt == optIdx
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isSelected -> NibrasPrimaryFixed
                        isEliminated -> NibrasSurfaceContainer.copy(alpha = 0.5f)
                        else -> NibrasSurfaceLowest
                    },
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) NibrasPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isEliminated) 0.42f else 1f)
                        .clickable(enabled = !isEliminated) { onAnswerQuestion(qIdx, optIdx) }
                        .testTag("exam_option_$optIdx")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) NibrasPrimary else NibrasSurfaceContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ('A' + optIdx).toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(optText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Bottom Exam Navigation Bar
        Surface(color = NibrasSurfaceLowest, shadowElevation = 6.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onSelectQuestion(qIdx - 1) },
                    enabled = qIdx > 0,
                    shape = RoundedCornerShape(12.dp)
                ) { Text(bi("السابق", "Prev")) }

                if (qIdx < session.exam.questions.lastIndex) {
                    Button(
                        onClick = { onSelectQuestion(qIdx + 1) },
                        modifier = Modifier.weight(1f).testTag("exam_next_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(bi("السؤال التالي", "Next Question")) }
                } else {
                    Button(
                        onClick = { showConfirmSubmit = true },
                        modifier = Modifier.weight(1f).testTag("exam_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NibrasSuccess)
                    ) { Text(bi("تسليم الاختبار", "Submit Exam")) }
                }
            }
        }
    }

    if (showConfirmSubmit) {
        val unanswered = session.exam.questions.size - session.selectedAnswers.size
        AlertDialog(
            onDismissRequest = { showConfirmSubmit = false },
            title = { Text(bi("تأكيد تسليم الاختبار", "Confirm Exam Submission")) },
            text = {
                Text(
                    if (unanswered > 0) {
                        bi("لديك $unanswered أسئلة بدون إجابة. هل ترغب في تسليم الاختبار الآن؟", "You have $unanswered unanswered questions. Submit anyway?")
                    } else {
                        bi("أجبت على جميع الأسئلة بنجاح. هل تريد التسليم وتوليد التشخيص الذكي؟", "All questions answered. Submit and generate live Gemini diagnostics?")
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSubmit = false
                        onSubmitExam()
                    },
                    modifier = Modifier.testTag("exam_confirm_submit_button")
                ) { Text(bi("تسليم الآن", "Submit")) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmit = false }) { Text(bi("مراجعة", "Review")) }
            }
        )
    }
}

/**
 * Screen 13: Exam Result & Live Gemini Post-Exam Diagnostic Evaluation
 */
@Composable
fun ExamResultScreen(
    result: CompletedExamResult,
    diagnosticsState: AiStreamUiState,
    onDone: () -> Unit,
    onRetake: (TechnicalExam) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    val passed = result.scorePercent >= result.exam.passingPercent

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Celebration / Outcome Hero
        item {
            NibrasGradientHeroCard(
                colors = if (passed) listOf(NibrasPrimary, NibrasSuccess) else listOf(NibrasPrimary, NibrasWarning)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (passed) bi("أداء متميّز! 🎉", "Excellent Mastery! 🎉") else bi("فرصة للتحسين 💡", "Keep Practicing 💡"),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isAr) result.exam.titleAr else result.exam.titleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = bi("${result.correctCount} من ${result.totalQuestions} صحيحة • الزمن ${result.durationSeconds} ثانية", "${result.correctCount}/${result.totalQuestions} correct • Time ${result.durationSeconds}s"),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    RadialProgressGauge(
                        progressPercent = result.scorePercent,
                        size = 86.dp,
                        strokeWidth = 8.dp,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        progressColor = Color.White,
                        centerTextColor = Color.White
                    )
                }
            }
        }

        // Live Gemini Post-Exam Diagnostic Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasTertiaryFixed.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NibrasAiBadge(label = bi("تشخيص نبراس الذكي بعد الاختبار", "Gemini Post-Exam Diagnostics"))
                    Spacer(Modifier.height(10.dp))
                    when (diagnosticsState) {
                        AiStreamUiState.Idle, AiStreamUiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = NibrasTertiary)
                            Spacer(Modifier.width(10.dp))
                            Text(bi("جاري تحليل نقاط القوة والضعف عبر Gemini...", "Analyzing mastery gaps via Gemini..."))
                        }
                        is AiStreamUiState.Streaming -> Text(diagnosticsState.text, style = MaterialTheme.typography.bodyMedium)
                        is AiStreamUiState.Success -> Text(diagnosticsState.text, style = MaterialTheme.typography.bodyMedium)
                        is AiStreamUiState.Error -> Surface(color = NibrasErrorContainer, shape = RoundedCornerShape(10.dp)) {
                            Text(diagnosticsState.message, color = NibrasOnErrorContainer, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }
        }

        // Per-Question Review
        item {
            Text(bi("مراجعة الإجابات والتفسيرات", "Question-by-Question Review"), style = MaterialTheme.typography.titleMedium)
        }

        itemsIndexed(result.exam.questions) { idx, q ->
            val userAns = result.selectedAnswers[idx]
            val correct = userAns == q.correctIndex
            val opts = if (isAr) q.optionsAr else q.optionsEn
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, if (correct) NibrasSuccess.copy(alpha = 0.4f) else NibrasError.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = if (correct) NibrasSuccess else NibrasError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAr) q.promptAr else q.promptEn, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = bi("الإجابة الصحيحة: ${opts[q.correctIndex]}", "Correct answer: ${opts[q.correctIndex]}"),
                        style = MaterialTheme.typography.labelMedium,
                        color = NibrasSuccess
                    )
                    Text(
                        text = if (isAr) q.explanationAr else q.explanationEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onRetake(result.exam) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(bi("إعادة المحاولة", "Retake")) }
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f).testTag("exam_result_done_button"),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(bi("العودة للاختبارات", "Back to Hub")) }
            }
        }
    }
}
