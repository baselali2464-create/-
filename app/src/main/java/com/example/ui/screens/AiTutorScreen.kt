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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.AiMessageDoc
import com.example.i18n.bi
import com.example.ui.components.NibrasCodeCard
import com.example.ui.theme.NibrasErrorContainer
import com.example.ui.theme.NibrasOnErrorContainer
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSuccess
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryFixed
import com.example.viewmodel.AiStreamUiState

/**
 * Screen 14: AI Assistant Chat (Live Gemini 3.8 Flash Streaming + 5 Adaptive Modes + Token Quota Meter)
 */
@Composable
fun AiTutorScreen(
    messages: List<AiMessageDoc>,
    aiState: AiStreamUiState,
    selectedMode: String,
    dailyTokensUsed: Long,
    dailyTokenLimit: Long,
    codeModeEnabled: Boolean,
    onSelectMode: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val modes = buildList {
        add("simple" to bi("شرح مبسط", "Simple"))
        add("academic" to bi("أكاديمي جامعي", "Academic"))
        if (codeModeEnabled) add("code" to bi("كود وتطبيق", "Code & Lab"))
        add("summary" to bi("تلخيص مادة", "Summary"))
        add("quiz" to bi("توليد أسئلة", "Quiz Gen"))
    }

    val quickPrompts = listOf(
        bi("اشرح الفرق بين BFS و DFS مع مثال بايثون", "Compare BFS vs DFS with Python code"),
        bi("ما هو التعقيد الزمني لـ QuickSort؟", "Explain QuickSort Big-O complexity"),
        bi("لخّص خصائص ACID في قواعد البيانات", "Summarize ACID properties in SQL"),
        bi("ولّد 3 أسئلة تدريبية عن Transformers", "Generate 3 practice questions on Transformers")
    )

    val quotaRatio = (dailyTokensUsed.toFloat() / dailyTokenLimit.coerceAtLeast(1000L).toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(messages.size, aiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header & Daily Token Quota Meter
        Surface(color = NibrasSurfaceLowest, shadowElevation = 2.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = NibrasTertiaryFixed, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = NibrasTertiary)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(bi("مساعد نبراس الذكي", "Nibras AI Tutor"), style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(NibrasSuccess))
                                Spacer(Modifier.width(5.dp))
                                Text("Gemini 3.8 Flash • Live", style = MaterialTheme.typography.labelSmall, color = NibrasSuccess)
                            }
                        }
                    }
                    Text(
                        text = "$dailyTokensUsed / $dailyTokenLimit",
                        style = MaterialTheme.typography.labelSmall,
                        color = NibrasPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { quotaRatio },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                    color = NibrasTertiary,
                    trackColor = NibrasTertiaryFixed,
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(modes) { (key, label) ->
                        FilterChip(
                            selected = selectedMode == key,
                            onClick = { onSelectMode(key) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NibrasTertiary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("ai_mode_$key")
                        )
                    }
                }
            }
        }

        // Chat Stream
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NibrasSurfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = bi("مرحباً! أنا مساعدك الأكاديمي في نبراس 👋", "Hello! I'm your Nibras Academic AI Tutor 👋"),
                                style = MaterialTheme.typography.titleMedium,
                                color = NibrasPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = bi(
                                    "اختر أسلوب الشرح من الأعلى، أو جرّب أحد الأسئلة السريعة بالأسفل للبدء مباشرةً.",
                                    "Choose an explanation mode above, or tap a quick prompt below to start streaming answers."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    isUser = msg.sender == "user",
                    text = msg.text,
                    modeBadge = msg.mode
                )
            }

            // Streaming / Loading / Error Tail
            when (aiState) {
                AiStreamUiState.Loading -> item {
                    Surface(shape = RoundedCornerShape(16.dp), color = NibrasTertiaryFixed.copy(alpha = 0.6f)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = NibrasTertiary)
                            Spacer(Modifier.width(10.dp))
                            Text(bi("يفكّر مساعد نبراس...", "Nibras AI is thinking..."), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                is AiStreamUiState.Streaming -> item {
                    ChatBubbleItem(isUser = false, text = aiState.text, modeBadge = selectedMode)
                }
                is AiStreamUiState.Error -> item {
                    Surface(shape = RoundedCornerShape(14.dp), color = NibrasErrorContainer, modifier = Modifier.fillMaxWidth()) {
                        Text(aiState.message, color = NibrasOnErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                    }
                }
                else -> {}
            }
        }

        // Proactive Suggestion Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = NibrasPrimaryFixed.copy(alpha = 0.7f),
                    modifier = Modifier.clickable(enabled = aiState !is AiStreamUiState.Loading && aiState !is AiStreamUiState.Streaming) {
                        onSendMessage(prompt)
                    }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = NibrasPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Floating Input Bar
        Surface(color = NibrasSurfaceLowest, shadowElevation = 6.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text(bi("اسأل نبراس عن أي مفهوم أو كود...", "Ask Nibras any CS or AI question...")) },
                    modifier = Modifier.weight(1f).testTag("ai_chat_input"),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = NibrasPrimary,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (draft.isNotBlank()) {
                                onSendMessage(draft)
                                draft = ""
                            }
                        },
                        enabled = draft.isNotBlank() && aiState !is AiStreamUiState.Loading && aiState !is AiStreamUiState.Streaming,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_chat_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(isUser: Boolean, text: String, modeBadge: String) {
    val alignment = if (isUser) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) NibrasPrimary else NibrasSurfaceLowest,
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isUser) {
                    Text(
                        text = "Nibras AI • ${modeBadge.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NibrasTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                val segments = remember(text) { splitMarkdownCodeBlocks(text) }
                segments.forEach { (isCode, content) ->
                    if (isCode) {
                        NibrasCodeCard(code = content, languageLabel = "code")
                    } else if (content.isNotBlank()) {
                        Text(
                            text = content.trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun splitMarkdownCodeBlocks(input: String): List<Pair<Boolean, String>> {
    val parts = input.split("```")
    return parts.mapIndexed { idx, part ->
        val isCode = idx % 2 == 1
        val cleaned = if (isCode) part.substringAfter('\n', part).trim() else part
        isCode to cleaned
    }
}
