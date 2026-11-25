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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.data.CommunityPostDoc
import com.example.data.NibrasAssets
import com.example.data.UserProfileDoc
import com.example.i18n.bi
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasCodeCard
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.components.NibrasNetworkImage
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasError
import com.example.ui.theme.NibrasErrorContainer
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSuccess
import com.example.ui.theme.NibrasSuccessContainer
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryFixed

/**
 * Screen 15: Student Communities & Academic Discussions (Pomodoro Room + Live Gemini Thread Digest + New Post Sheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCommunitiesScreen(
    posts: List<CommunityPostDoc>,
    localDigests: Map<String, String>,
    digestLoadingPostId: String?,
    pomodoroJoined: Boolean,
    onBack: () -> Unit,
    onTogglePomodoro: () -> Unit,
    onUpvotePost: (CommunityPostDoc) -> Unit,
    onRequestAiDigest: (CommunityPostDoc) -> Unit,
    onCreatePost: (category: String, title: String, body: String, codeSnippet: String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showNewPostSheet by remember { mutableStateOf(false) }
    var postTitle by remember { mutableStateOf("") }
    var postBody by remember { mutableStateOf("") }
    var postCode by remember { mutableStateOf("") }
    var postCategory by remember { mutableStateOf("خوارزميات") }

    val categories = listOf(
        "ALL" to bi("الكل", "All"),
        "خوارزميات" to bi("خوارزميات", "Algorithms"),
        "السحابة" to bi("السحابة", "Cloud"),
        "ذكاء اصطناعي" to bi("ذكاء اصطناعي", "AI")
    )

    val filtered = remember(posts, selectedCategory) {
        if (selectedCategory == "ALL") posts
        else posts.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPostSheet = true },
                containerColor = NibrasPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("community_new_post_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "New Discussion")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("community_back")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(bi("المجتمعات الطلابية والنقاشات", "Student Communities"), style = MaterialTheme.typography.titleMedium)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Pomodoro Study Room Pill
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (pomodoroJoined) NibrasSuccessContainer else NibrasPrimaryFixed,
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onTogglePomodoro).testTag("pomodoro_room_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.Headphones, contentDescription = null, tint = if (pomodoroJoined) NibrasSuccess else NibrasPrimary)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (pomodoroJoined) bi("أنت متصل في غرفة بومودورو (25:00)", "Connected to Pomodoro Room (25:00)") else bi("غرفة مذاكرة بومودورو مباشرة • 24 طالب", "Live Pomodoro Study Room • 24 online"),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (pomodoroJoined) NibrasSuccess else NibrasPrimary
                                    )
                                    Text(bi("جلسة تركيز خوارزميات + مراجعة الاختبار", "Algorithms deep-focus session"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = if (pomodoroJoined) bi("مغادرة", "Leave") else bi("انضم", "Join"),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (pomodoroJoined) NibrasSuccess else NibrasPrimary
                            )
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { (key, label) ->
                            FilterChip(
                                selected = selectedCategory == key,
                                onClick = { selectedCategory = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                items(filtered, key = { it.id }) { post ->
                    val digest = post.aiDigest ?: localDigests[post.id]
                    val loadingDigest = digestLoadingPostId == post.id
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NibrasNetworkImage(
                                    url = post.authorAvatar ?: NibrasAssets.STUDENT_BASEL,
                                    contentDescription = post.authorName,
                                    modifier = Modifier.size(38.dp).clip(CircleShape)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.authorName, style = MaterialTheme.typography.titleSmall)
                                    Text(post.authorRole, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = NibrasSurfaceContainer) {
                                    Text(post.category, style = MaterialTheme.typography.labelSmall, color = NibrasSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            Text(post.title, style = MaterialTheme.typography.titleMedium)
                            Text(post.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (!post.codeSnippet.isNullOrBlank()) {
                                NibrasCodeCard(code = post.codeSnippet, languageLabel = "snippet")
                            }

                            if (!digest.isNullOrBlank()) {
                                Surface(shape = RoundedCornerShape(14.dp), color = NibrasTertiaryFixed.copy(alpha = 0.55f)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        NibrasAiBadge(label = bi("خلاصة نبراس الفورية", "Nibras AI Digest"))
                                        Spacer(Modifier.height(6.dp))
                                        Text(digest, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onUpvotePost(post) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("${post.upvotes}")
                                }
                                Button(
                                    onClick = { onRequestAiDigest(post) },
                                    enabled = !loadingDigest,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NibrasTertiary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    if (loadingDigest) {
                                        CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(bi("تحليل AI", "AI Digest"), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewPostSheet) {
        ModalBottomSheet(onDismissRequest = { showNewPostSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(bi("بدء نقاش أكاديمي جديد", "Start New Academic Discussion"), style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("خوارزميات", "السحابة", "ذكاء اصطناعي").forEach { cat ->
                        FilterChip(selected = postCategory == cat, onClick = { postCategory = cat }, label = { Text(cat) })
                    }
                }
                OutlinedTextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    label = { Text(bi("عنوان السؤال أو النقاش", "Discussion Title")) },
                    modifier = Modifier.fillMaxWidth().testTag("community_post_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = postBody,
                    onValueChange = { postBody = it },
                    label = { Text(bi("التفاصيل الأكاديمية", "Details")) },
                    modifier = Modifier.fillMaxWidth().testTag("community_post_body_input"),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = postCode,
                    onValueChange = { postCode = it },
                    label = { Text(bi("مقطع كود (اختياري)", "Code Snippet (optional)")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (postTitle.trim().length >= 3 && postBody.trim().length >= 3) {
                            onCreatePost(postCategory, postTitle, postBody, postCode.ifBlank { null })
                            postTitle = ""
                            postBody = ""
                            postCode = ""
                            showNewPostSheet = false
                        }
                    },
                    enabled = postTitle.trim().length >= 3 && postBody.trim().length >= 3,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("community_publish_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(bi("نشر في المجتمع", "Publish to Community"))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Screen 17: Account Settings & Privacy (Bilingual Toggle, AI Style, Admin Portal Switch, Safe Sign-Out)
 */
@Composable
fun AccountSettingsScreen(
    profile: UserProfileDoc?,
    dailyTokenLimit: Long,
    isAdminEligible: Boolean,
    onBack: () -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSelectAiStyle: (String) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onOpenAdminConsole: () -> Unit,
    onSignOut: () -> Unit
) {
    val currentLang = LocalAppLanguage.current
    var nameDraft by remember(profile?.displayName) { mutableStateOf(profile?.displayName.orEmpty()) }
    val used = profile?.dailyTokensUsed ?: 0L
    val ratio = (used.toFloat() / dailyTokenLimit.coerceAtLeast(1000L).toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(bi("إعدادات الحساب والخصوصية", "Account Settings & Privacy"), style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NibrasNetworkImage(
                                url = profile?.photoUrl ?: NibrasAssets.STUDENT_BASEL,
                                contentDescription = "Profile photo",
                                modifier = Modifier.size(60.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile?.displayName ?: "طالب نبراس", style = MaterialTheme.typography.titleMedium)
                                Text(profile?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${profile?.major ?: ""} • ${profile?.role?.uppercase() ?: "STUDENT"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NibrasPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nameDraft,
                                onValueChange = { nameDraft = it },
                                label = { Text(bi("الاسم المعروض", "Display Name")) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = { onUpdateDisplayName(nameDraft) },
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(bi("حفظ", "Save")) }
                        }
                    }
                }
            }

            // Admin Control Panel Switch Card
            if (isAdminEligible) {
                item {
                    NibrasGradientHeroCard(
                        colors = listOf(NibrasTertiary, NibrasPrimary),
                        modifier = Modifier.clickable(onClick = onOpenAdminConsole).testTag("settings_open_admin_console")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bi("لوحة تحكم الإدارة المركزية", "Central Admin Control Panel"), style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text(bi("إدارة المستخدمين RBAC، حصص Gemini، وفحص أمان المكتبة", "Manage RBAC users, Gemini quotas & library security"), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
                            }
                        }
                    }
                }
            }

            // Bilingual Language & Layout Direction Switch
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Language, contentDescription = null, tint = NibrasPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text(bi("اللغة واتجاه الواجهة", "Language & Layout Direction"), style = MaterialTheme.typography.titleSmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = currentLang == AppLanguage.AR,
                                onClick = { onSetLanguage(AppLanguage.AR) },
                                label = { Text("العربية (RTL)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NibrasPrimary, selectedLabelColor = Color.White),
                                modifier = Modifier.testTag("settings_lang_ar")
                            )
                            FilterChip(
                                selected = currentLang == AppLanguage.EN,
                                onClick = { onSetLanguage(AppLanguage.EN) },
                                label = { Text("English (LTR)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NibrasPrimary, selectedLabelColor = Color.White),
                                modifier = Modifier.testTag("settings_lang_en")
                            )
                        }
                    }
                }
            }

            // AI Preferences & Quota
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(bi("حصة الذكاء الاصطناعي اليومية", "Daily AI Token Quota"), style = MaterialTheme.typography.titleSmall)
                            Text("$used / $dailyTokenLimit", style = MaterialTheme.typography.labelLarge, color = NibrasTertiary)
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                            color = NibrasTertiary,
                            trackColor = NibrasTertiaryFixed,
                            strokeCap = StrokeCap.Round
                        )
                        Text(bi("أسلوب الشرح الافتراضي", "Default Explanation Style"), style = MaterialTheme.typography.labelLarge)
                        val styleOptions = listOf(
                            "simple" to bi("مبسّط", "Simple"),
                            "academic" to bi("أكاديمي", "Academic"),
                            "code" to bi("كود", "Code"),
                            "summary" to bi("ملخص", "Summary")
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(styleOptions) { (key, label) ->
                                FilterChip(
                                    selected = (profile?.aiStyle ?: "academic") == key,
                                    onClick = { onSelectAiStyle(key) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // Privacy & Security Info
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = NibrasSurfaceContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = NibrasSuccess)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = bi("حسابك محمي بتوثيق Google و Firebase App Check وقواعد عزل البيانات الصفرية.", "Protected by Google Identity, Firebase App Check & Zero-Trust Firestore rules."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sign Out
            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("settings_sign_out_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NibrasErrorContainer, contentColor = NibrasError)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(bi("تسجيل الخروج الآمن", "Sign Out"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
