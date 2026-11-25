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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Translate
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookHighlightDoc
import com.example.data.LibraryBook
import com.example.data.NibrasCatalog
import com.example.data.ReadingStateDoc
import com.example.i18n.bi
import com.example.ui.components.BinaryTreeDiagramCanvas
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasCodeCard
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.components.NibrasNetworkImage
import com.example.ui.components.NibrasSectionHeader
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasErrorContainer
import com.example.ui.theme.NibrasOnErrorContainer
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryFixed
import com.example.ui.theme.NibrasWarningContainer
import com.example.viewmodel.AiStreamUiState

/**
 * Screen 9: Digital Library
 */
@Composable
fun DigitalLibraryScreen(
    readingStatesMap: Map<String, ReadingStateDoc>,
    onOpenBook: (LibraryBook) -> Unit,
    onOpenAiTutor: () -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL" to bi("الكل", "All"),
        "Software Engineering" to bi("هندسة البرمجيات", "Software Eng"),
        "Algorithms" to bi("الخوارزميات", "Algorithms"),
        "Artificial Intelligence" to bi("الذكاء الاصطناعي", "AI"),
        "Databases" to bi("قواعد البيانات", "Databases"),
        "Cybersecurity" to bi("الأمن السيبراني", "Security")
    )

    val filteredBooks = remember(query, selectedCategory) {
        NibrasCatalog.books.filter { b ->
            val catOk = selectedCategory == "ALL" || b.categoryEn == selectedCategory
            val q = query.trim().lowercase()
            val textOk = q.isBlank() ||
                b.titleAr.lowercase().contains(q) ||
                b.titleEn.lowercase().contains(q) ||
                b.authorAr.lowercase().contains(q)
            catOk && textOk
        }
    }

    val continueBook = NibrasCatalog.books.first()
    val continueState = readingStatesMap[continueBook.id]
    val continuePage = continueState?.currentPage?.toInt() ?: continueBook.defaultPage
    val continuePct = continueState?.progressPercent?.toInt() ?: ((continuePage * 100) / continueBook.totalPages)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = bi("المكتبة الرقمية الذكية", "Smart Digital Library"),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(bi("ابحث عن كتاب، مؤلف، أو مرجع...", "Search textbooks, authors...")) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("library_search_input")
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

        // Continue Reading Hero Card (Clean Code)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenBook(continueBook) }
                    .testTag("library_continue_reading_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    NibrasNetworkImage(
                        url = continueBook.coverUrl,
                        contentDescription = continueBook.titleEn,
                        modifier = Modifier.width(86.dp).height(118.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        NibrasAiBadge(label = bi("متابعة القراءة", "Continue Reading"))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isAr) continueBook.titleAr else continueBook.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2
                        )
                        Text(
                            text = if (isAr) continueBook.authorAr else continueBook.authorEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bi("صفحة $continuePage من ${continueBook.totalPages}", "Page $continuePage / ${continueBook.totalPages}"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("$continuePct%", style = MaterialTheme.typography.labelSmall, color = NibrasPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { continuePct / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = NibrasPrimary,
                            trackColor = NibrasPrimaryFixed,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Textbooks Horizontal Shelf
        item {
            NibrasSectionHeader(title = bi("المراجع الجامعية الأساسية", "Core University Textbooks"))
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredBooks, key = { it.id }) { book ->
                    Column(
                        modifier = Modifier
                            .width(134.dp)
                            .clickable { onOpenBook(book) }
                            .testTag("library_book_${book.id}")
                    ) {
                        NibrasNetworkImage(
                            url = book.coverUrl,
                            contentDescription = book.titleEn,
                            modifier = Modifier.fillMaxWidth().height(178.dp).clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isAr) book.titleAr else book.titleEn,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${book.rating}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // AI Recommended Books with Smart Summaries
        item {
            NibrasSectionHeader(title = bi("مختارات الذكاء الاصطناعي مع الملخص", "AI Recommended with Smart Summaries"))
        }

        items(filteredBooks.take(3), key = { "rec_${it.id}" }) { book ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenBook(book) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NibrasNetworkImage(
                            url = book.coverUrl,
                            contentDescription = book.titleEn,
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isAr) book.titleAr else book.titleEn, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (isAr) book.categoryAr else book.categoryEn, style = MaterialTheme.typography.labelSmall, color = NibrasSecondary)
                        }
                        NibrasAiBadge(label = bi("قراءة ذكية", "Smart Read"))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isAr) book.aiSummaryAr else book.aiSummaryEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            NibrasGradientHeroCard(modifier = Modifier.clickable(onClick = onOpenAiTutor)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bi("مساعد البحث في المكتبة", "AI Research Companion"), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(bi("اسأل نبراس عن أي مفهوم في كتبك الدراسية واحصل على شرح وتلخيص فوري.", "Ask Nibras about any concept in your textbooks for instant Q&A."), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
                    }
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

/**
 * Screen 10: Smart Book / PDF Reader (with interactive passage toolbar + live Gemini Copilot + BST diagram)
 */
@Composable
fun SmartBookReaderScreen(
    book: LibraryBook,
    readingState: ReadingStateDoc?,
    highlights: List<BookHighlightDoc>,
    copilotState: AiStreamUiState,
    onBack: () -> Unit,
    onUpdatePage: (page: Int, bookmarked: Boolean) -> Unit,
    onRunCopilot: (passage: String, action: String) -> Unit,
    onSaveHighlight: (page: Int, quote: String, note: String, aiExplanation: String?) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    val clipboard = LocalClipboardManager.current
    var pageIdx by remember { mutableIntStateOf(0) }
    var fontScale by remember { mutableFloatStateOf(1.0f) }
    var focusMode by remember { mutableStateOf(false) }
    var bookmarked by remember(readingState) { mutableStateOf(readingState?.bookmarked ?: false) }
    var showSelectionToolbar by remember { mutableStateOf(true) }
    var noteInput by remember { mutableStateOf("") }
    var showNoteBox by remember { mutableStateOf(false) }

    val pageData = book.pages[pageIdx.coerceIn(0, book.pages.lastIndex)]
    val quote = if (isAr) pageData.highlightableQuoteAr else pageData.highlightableQuoteEn
    val bookHighlights = remember(highlights, book.id) { highlights.filter { it.bookId == book.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (focusMode) Color(0xFFFFFDF7) else MaterialTheme.colorScheme.background)
    ) {
        // Top Reader Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAr) book.titleAr else book.titleEn,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bi("صفحة ${pageData.pageNumber} من ${book.totalPages}", "Page ${pageData.pageNumber} of ${book.totalPages}"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { fontScale = if (fontScale >= 1.25f) 0.95f else fontScale + 0.15f },
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Rounded.FormatSize, contentDescription = "Font size")
            }
            IconButton(
                onClick = { focusMode = !focusMode },
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Rounded.CenterFocusStrong, contentDescription = "Focus mode", tint = if (focusMode) NibrasPrimary else MaterialTheme.colorScheme.onSurface)
            }
            IconButton(
                onClick = {
                    bookmarked = !bookmarked
                    onUpdatePage(pageData.pageNumber, bookmarked)
                },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_bookmark_button")
            ) {
                Icon(
                    imageVector = if (bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (bookmarked) NibrasPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LinearProgressIndicator(
            progress = { (pageData.pageNumber.toFloat() / book.totalPages.toFloat()).coerceIn(0.05f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = NibrasPrimary,
            trackColor = NibrasPrimaryFixed
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (isAr) pageData.chapterTitleAr else pageData.chapterTitleEn,
                    style = MaterialTheme.typography.headlineSmall,
                    color = NibrasPrimary
                )
            }

            items(if (isAr) pageData.paragraphsAr else pageData.paragraphsEn) { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (15.5f * fontScale).sp,
                        lineHeight = (26f * fontScale).sp
                    )
                )
            }

            // Interactive Highlightable Academic Passage + Floating Contextual Toolbar
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NibrasWarningContainer.copy(alpha = 0.72f),
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSelectionToolbar = !showSelectionToolbar }
                        .testTag("reader_highlighted_passage")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = (15f * fontScale).sp
                            )
                        )
                        AnimatedVisibility(visible = showSelectionToolbar) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ReaderActionPill(
                                    icon = Icons.Rounded.AutoAwesome,
                                    label = bi("اشرح بـ AI", "AI Explain"),
                                    tint = NibrasTertiary,
                                    onClick = { onRunCopilot(quote, "explain") },
                                    modifier = Modifier.weight(1f).testTag("reader_ai_explain")
                                )
                                ReaderActionPill(
                                    icon = Icons.Rounded.Translate,
                                    label = bi("ترجمة", "Translate"),
                                    tint = NibrasSecondary,
                                    onClick = { onRunCopilot(quote, "translate") },
                                    modifier = Modifier.weight(1f).testTag("reader_ai_translate")
                                )
                                ReaderActionPill(
                                    icon = Icons.Rounded.NoteAlt,
                                    label = bi("ملاحظة", "Note"),
                                    tint = NibrasPrimary,
                                    onClick = { showNoteBox = !showNoteBox },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(quote)) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Optional Save Highlight Note Box
            if (showNoteBox) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = noteInput,
                                onValueChange = { noteInput = it },
                                placeholder = { Text(bi("أضف تعليقك على المقطع المظلّل...", "Add note for this highlight...")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val aiExp = (copilotState as? AiStreamUiState.Success)?.text
                                    onSaveHighlight(pageData.pageNumber, quote, noteInput.ifBlank { "Saved highlight" }, aiExp)
                                    noteInput = ""
                                    showNoteBox = false
                                },
                                modifier = Modifier.align(Alignment.End).testTag("reader_save_highlight")
                            ) {
                                Text(bi("حفظ التظليل سحابياً", "Save Highlight"))
                            }
                        }
                    }
                }
            }

            // Nibras AI Reader Copilot Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasTertiaryFixed.copy(alpha = 0.52f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NibrasAiBadge(label = bi("مرافق القراءة الذكي Gemini", "Nibras AI Reader Copilot"))
                            Text(
                                text = bi("سؤال سريع", "Quick Quiz"),
                                style = MaterialTheme.typography.labelLarge,
                                color = NibrasTertiary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onRunCopilot(quote, "quiz") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        when (copilotState) {
                            AiStreamUiState.Idle -> Text(
                                text = bi(
                                    "اختر «اشرح بـ AI» أو «ترجمة» على المقطع المظلّل أعلاه للحصول على شرح أكاديمي مباشر من Gemini.",
                                    "Tap AI Explain or Translate on the highlighted passage above for live Gemini insights."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AiStreamUiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = NibrasTertiary)
                                Spacer(Modifier.width(8.dp))
                                Text(bi("جاري تحليل النص عبر Gemini...", "Analyzing passage with Gemini..."))
                            }
                            is AiStreamUiState.Streaming -> Text(copilotState.text, style = MaterialTheme.typography.bodyMedium)
                            is AiStreamUiState.Success -> Text(copilotState.text, style = MaterialTheme.typography.bodyMedium)
                            is AiStreamUiState.Error -> Surface(color = NibrasErrorContainer, shape = RoundedCornerShape(10.dp)) {
                                Text(copilotState.message, color = NibrasOnErrorContainer, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            }

            // Optional Code Block
            if (!pageData.codeSnippet.isNullOrBlank()) {
                item {
                    NibrasCodeCard(code = pageData.codeSnippet, languageLabel = "code")
                }
            }

            // Optional Binary Tree Vector Diagram
            if (pageData.showBinaryTreeDiagram) {
                item {
                    BinaryTreeDiagramCanvas()
                }
            }

            // Saved Cloud Highlights for this book
            if (bookHighlights.isNotEmpty()) {
                item {
                    NibrasSectionHeader(title = bi("تظليلاتك المحفوظة (${bookHighlights.size})", "Saved Highlights (${bookHighlights.size})"))
                }
                items(bookHighlights, key = { it.id }) { hl ->
                    Surface(shape = RoundedCornerShape(14.dp), color = NibrasSurfaceContainer, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(bi("صفحة ${hl.page}", "Page ${hl.page}"), style = MaterialTheme.typography.labelSmall, color = NibrasPrimary, fontWeight = FontWeight.Bold)
                            Text(hl.quote, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (hl.note.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(hl.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Page Slider Controls
        if (book.pages.size > 1) {
            Surface(color = NibrasSurfaceLowest, shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(bi("الفصل ${pageIdx + 1}/${book.pages.size}", "Section ${pageIdx + 1}/${book.pages.size}"), style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = pageIdx.toFloat(),
                        onValueChange = {
                            val newIdx = it.toInt().coerceIn(0, book.pages.lastIndex)
                            if (newIdx != pageIdx) {
                                pageIdx = newIdx
                                onUpdatePage(book.pages[newIdx].pageNumber, bookmarked)
                            }
                        },
                        valueRange = 0f..book.pages.lastIndex.toFloat(),
                        steps = (book.pages.size - 2).coerceAtLeast(0),
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                    Text("p.${pageData.pageNumber}", style = MaterialTheme.typography.labelMedium, color = NibrasPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReaderActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = NibrasSurfaceLowest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1)
        }
    }
}
