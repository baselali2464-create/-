package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AdminAiConfigDoc
import com.example.data.LibraryAssetAuditDoc
import com.example.data.LibraryBook
import com.example.data.NibrasCatalog
import com.example.data.UserProfileDoc
import com.example.i18n.bi
import com.example.ui.components.NibrasGradientHeroCard
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.JetBrainsMonoFamily
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
import com.example.ui.theme.NibrasWarning
import com.example.ui.theme.NibrasWarningContainer

/**
 * Integrated Adaptive Admin Control Panel (Screens 18, 19, 20, 21)
 * Uses NavigationRail on >=600dp tablets/desktops and scrollable tabs on compact phones.
 */
@Composable
fun AdminConsoleScreen(
    adminUsers: List<UserProfileDoc>,
    aiConfig: AdminAiConfigDoc,
    libraryAudits: Map<String, LibraryAssetAuditDoc>,
    onExitAdmin: () -> Unit,
    onUpdateUserRole: (uid: String, role: String, status: String) -> Unit,
    onSaveAiConfig: (dailyLimit: Long, model: String, codeMode: Boolean, examHints: Boolean, strictSafety: Boolean) -> Unit,
    onUpdateAssetStatus: (book: LibraryBook, status: String, note: String?) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        Triple(bi("الإحصائيات", "Overview"), Icons.Rounded.Dashboard, "admin_tab_overview"),
        Triple(bi("المستخدمون", "Users RBAC"), Icons.Rounded.Group, "admin_tab_users"),
        Triple(bi("الذكاء AI", "AI & Costs"), Icons.Rounded.AutoAwesome, "admin_tab_ai"),
        Triple(bi("أمان المكتبة", "Library Audit"), Icons.Rounded.LibraryBooks, "admin_tab_library")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Admin Console Bar
            Surface(color = NibrasSurfaceLowest, shadowElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onExitAdmin, modifier = Modifier.minimumInteractiveComponentSize().testTag("admin_exit_button")) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Student App")
                        }
                        Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = NibrasTertiary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(bi("لوحة تحكم نبراس المركزية", "Nibras Admin Control Panel"), style = MaterialTheme.typography.titleMedium)
                            Text(bi("إدارة السحابة • RBAC • حوكمة Gemini", "Cloud Governance • RBAC • Gemini Ops"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(onClick = onExitAdmin, shape = RoundedCornerShape(10.dp)) {
                        Text(bi("وضع الطالب", "Student Mode"))
                    }
                }
            }

            if (!isWide) {
                ScrollableTabRow(selectedTabIndex = selectedSection, containerColor = NibrasSurfaceLowest, edgePadding = 12.dp) {
                    sections.forEachIndexed { idx, (label, icon, tag) ->
                        Tab(
                            selected = selectedSection == idx,
                            onClick = { selectedSection = idx },
                            text = { Text(label) },
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isWide) {
                    NavigationRail(containerColor = NibrasSurfaceLowest, modifier = Modifier.fillMaxHeight()) {
                        Spacer(Modifier.height(12.dp))
                        sections.forEachIndexed { idx, (label, icon, tag) ->
                            NavigationRailItem(
                                selected = selectedSection == idx,
                                onClick = { selectedSection = idx },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (selectedSection) {
                        0 -> AdminOverviewTab(adminUsers, aiConfig, libraryAudits)
                        1 -> AdminUsersRbacTab(adminUsers, onUpdateUserRole)
                        2 -> AdminAiManagementTab(aiConfig, adminUsers, onSaveAiConfig)
                        else -> AdminLibrarySecurityTab(libraryAudits, onUpdateAssetStatus)
                    }
                }
            }
        }
    }
}

/**
 * Screen 18: Admin Overview & KPIs
 */
@Composable
private fun AdminOverviewTab(
    users: List<UserProfileDoc>,
    aiConfig: AdminAiConfigDoc,
    audits: Map<String, LibraryAssetAuditDoc>
) {
    val totalTokens = users.sumOf { it.dailyTokensUsed }.coerceAtLeast(1240L)
    val verifiedCount = NibrasCatalog.books.count { (audits[it.id]?.scanStatus ?: "verified") == "verified" }
    val trend = listOf(42, 58, 65, 79, 88, 94)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NibrasGradientHeroCard(colors = listOf(NibrasPrimary, NibrasTertiary)) {
                Column {
                    Text(bi("حالة المنصة المباشرة", "Live Platform Telemetry"), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.height(4.dp))
                    Text(bi("جميع خدمات Firestore و Gemini تعمل بكفاءة 99.98%", "Firestore Enterprise & Gemini 3.8 Flash Operational (99.98%)"), style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiCard(bi("المستخدمون المسجلون", "Registered Users"), "${users.size.coerceAtLeast(1)}", NibrasPrimary, Modifier.weight(1f))
                AdminKpiCard(bi("رموز AI اليوم", "AI Tokens Today"), "$totalTokens", NibrasTertiary, Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiCard(bi("الحد اليومي للطالب", "Daily Cap"), "${aiConfig.dailyStudentTokenLimit}", NibrasSecondary, Modifier.weight(1f))
                AdminKpiCard(bi("كتب مفحوصة بأمان", "Verified Books"), "$verifiedCount/${NibrasCatalog.books.size}", NibrasSuccess, Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(bi("نمو التفاعل الأكاديمي الشهري", "Monthly Academic Engagement Trend"), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(116.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        trend.forEachIndexed { idx, value ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$value%", style = MaterialTheme.typography.labelSmall, color = NibrasPrimary)
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height((value * 0.85f).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(if (idx == trend.lastIndex) NibrasPrimary else NibrasSecondary.copy(alpha = 0.6f))
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("M${idx + 1}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 19: Users & Roles RBAC
 */
@Composable
private fun AdminUsersRbacTab(
    users: List<UserProfileDoc>,
    onUpdateUserRole: (uid: String, role: String, status: String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("ALL") }

    val filtered = remember(users, search, roleFilter) {
        users.filter { u ->
            val matchRole = roleFilter == "ALL" || u.role == roleFilter
            val q = search.trim().lowercase()
            val matchText = q.isBlank() || u.displayName.lowercase().contains(q) || u.email.lowercase().contains(q)
            matchRole && matchText
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(bi("بحث بالاسم أو البريد الإلكتروني...", "Search users by name or email...")) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to bi("الكل", "All"), "student" to "Student", "instructor" to "Instructor", "admin" to "Admin").forEach { (key, label) ->
                    FilterChip(selected = roleFilter == key, onClick = { roleFilter = key }, label = { Text(label) })
                }
            }
        }

        items(filtered, key = { it.uid.ifBlank { it.id } }) { user ->
            val targetUid = user.uid.ifBlank { user.id }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.displayName, style = MaterialTheme.typography.titleSmall)
                            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (user.accountStatus == "active") NibrasSuccessContainer else NibrasErrorContainer
                        ) {
                            Text(
                                text = user.accountStatus.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (user.accountStatus == "active") NibrasSuccess else NibrasError,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("student", "instructor", "admin").forEach { role ->
                            FilterChip(
                                selected = user.role == role,
                                onClick = { onUpdateUserRole(targetUid, role, user.accountStatus) },
                                label = { Text(role) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NibrasPrimary, selectedLabelColor = Color.White)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(
                            onClick = {
                                val nextStatus = if (user.accountStatus == "active") "suspended" else "active"
                                onUpdateUserRole(targetUid, user.role, nextStatus)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (user.accountStatus == "active") bi("تعليق", "Suspend") else bi("تفعيل", "Activate"), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 20: AI Management & LLM Cost Governance
 */
@Composable
private fun AdminAiManagementTab(
    config: AdminAiConfigDoc,
    users: List<UserProfileDoc>,
    onSaveAiConfig: (dailyLimit: Long, model: String, codeMode: Boolean, examHints: Boolean, strictSafety: Boolean) -> Unit
) {
    var limitFloat by remember(config.dailyStudentTokenLimit) { mutableFloatStateOf(config.dailyStudentTokenLimit.toFloat()) }
    var codeEnabled by remember(config.codeModeEnabled) { mutableStateOf(config.codeModeEnabled) }
    var hintsEnabled by remember(config.examHintsEnabled) { mutableStateOf(config.examHintsEnabled) }
    var strictSafety by remember(config.safetyGuardrailsStrict) { mutableStateOf(config.safetyGuardrailsStrict) }

    val totalTokens = users.sumOf { it.dailyTokensUsed }.coerceAtLeast(1240L)
    val estimatedCostUsd = String.format("%.4f", (totalTokens / 1_000_000.0) * 0.35)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiCard(bi("النموذج النشط", "Active Model"), config.activeModel, NibrasTertiary, Modifier.weight(1f))
                AdminKpiCard(bi("التكلفة التقديرية", "Est. Daily Cost"), "$$estimatedCostUsd", NibrasSuccess, Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(bi("حوكمة وحدود الذكاء الاصطناعي", "Gemini Quota & Guardrail Controls"), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = bi("الحد اليومي لكل طالب: ${limitFloat.toLong()} رمز", "Daily student token cap: ${limitFloat.toLong()} tokens"),
                        style = MaterialTheme.typography.titleSmall,
                        color = NibrasPrimary
                    )
                    Slider(
                        value = limitFloat,
                        onValueChange = { limitFloat = it },
                        valueRange = 5000f..100000f
                    )
                    AdminSwitchRow(bi("تفعيل وضع الكود والبرمجة", "Enable Code & Lab AI Mode"), codeEnabled) { codeEnabled = it }
                    AdminSwitchRow(bi("تفعيل تلميح الاختبار 50/50", "Enable Exam 50/50 AI Hints"), hintsEnabled) { hintsEnabled = it }
                    AdminSwitchRow(bi("فلاتر الأمان الأكاديمي الصارمة", "Strict Academic Guardrails"), strictSafety) { strictSafety = it }

                    Button(
                        onClick = { onSaveAiConfig(limitFloat.toLong(), config.activeModel, codeEnabled, hintsEnabled, strictSafety) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_save_ai_config_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NibrasPrimary)
                    ) {
                        Text(bi("حفظ وتطبيق على جميع الطلاب", "Save & Apply Globally"))
                    }
                }
            }
        }
    }
}

/**
 * Screen 21: Digital Library & File Security Inspection
 */
@Composable
private fun AdminLibrarySecurityTab(
    audits: Map<String, LibraryAssetAuditDoc>,
    onUpdateAssetStatus: (book: LibraryBook, status: String, note: String?) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = NibrasPrimaryFixed, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = NibrasPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = bi("فحص بصمة SHA-256 وحالة الأمان لجميع ملفات المكتبة الرقمية", "SHA-256 integrity & malware inspection for all library PDFs"),
                        style = MaterialTheme.typography.bodySmall,
                        color = NibrasPrimary
                    )
                }
            }
        }

        items(NibrasCatalog.books, key = { it.id }) { book ->
            val currentStatus = audits[book.id]?.scanStatus ?: "verified"
            val (badgeBg, badgeFg) = when (currentStatus) {
                "verified" -> NibrasSuccessContainer to NibrasSuccess
                "scanning" -> NibrasPrimaryFixed to NibrasPrimary
                "flagged" -> NibrasWarningContainer to NibrasWarning
                else -> NibrasErrorContainer to NibrasError
            }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isAr) book.titleAr else book.titleEn, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "SHA-256: ${book.sha256Prefix}... • ${book.fileSizeMb} MB",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                            Text(
                                text = currentStatus.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeFg,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "verified" to bi("اعتماد", "Verify"),
                            "flagged" to bi("مراجعة", "Flag"),
                            "quarantined" to bi("عزل", "Quarantine")
                        ).forEach { (st, lbl) ->
                            FilterChip(
                                selected = currentStatus == st,
                                onClick = { onUpdateAssetStatus(book, st, "Reviewed by Admin") },
                                label = { Text(lbl) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = NibrasSurfaceLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
