package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.core.onGoogleSignInClicked
import com.example.data.NibrasAssets
import com.example.data.NibrasCatalog
import com.example.i18n.bi
import com.example.ui.components.NibrasAiBadge
import com.example.ui.components.NibrasNetworkImage
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.NibrasErrorContainer
import com.example.ui.theme.NibrasOnErrorContainer
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryContainer
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSurface
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryContainer

/**
 * Screen 1: Splash Screen
 */
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NibrasPrimary, NibrasPrimaryContainer, NibrasTertiaryContainer)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.size(104.dp)
            ) {
                NibrasNetworkImage(
                    url = NibrasAssets.LOGO_URL,
                    contentDescription = "Nibras Logo",
                    modifier = Modifier.fillMaxSize().padding(14.dp).clip(RoundedCornerShape(18.dp))
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "نبراس • Nibras",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = bi(
                    "منصة التعليم التقني والذكاء الاصطناعي الجامعي",
                    "Luminous Technical Education & AI Learning Platform"
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.86f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Screen 2: Login Screen (Google Sign-In exclusively via Jetpack Credential Manager)
 */
@Composable
fun LoginScreen(
    onToggleLanguage: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NibrasSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NibrasAiBadge(label = bi("الجيل الجديد للتعليم", "Next-Gen CS Learning"))
                OutlinedButton(
                    onClick = onToggleLanguage,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("login_language_toggle")
                ) {
                    Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (LocalAppLanguage.current == AppLanguage.AR) "English" else "العربية")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Brand Hero Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(NibrasPrimary, NibrasPrimaryContainer, NibrasTertiary)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.size(84.dp)
                        ) {
                            NibrasNetworkImage(
                                url = NibrasAssets.LOGO_URL,
                                contentDescription = "Nibras Brand Emblem",
                                modifier = Modifier.fillMaxSize().padding(10.dp).clip(RoundedCornerShape(14.dp))
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = bi("أهلاً بك في نبراس", "Welcome to Nibras"),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = bi(
                                "دورات تقنية متقدمة، مكتبة ذكية، اختبارات تكيّفية ومساعد ذكاء اصطناعي متكامل",
                                "Technical courses, smart textbooks, adaptive exams, and live Gemini AI mentoring"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Feature highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureHighlightPill(
                    icon = Icons.Rounded.AutoAwesome,
                    label = bi("مساعد Gemini ذكي", "Gemini Tutor"),
                    modifier = Modifier.weight(1f)
                )
                FeatureHighlightPill(
                    icon = Icons.Rounded.CloudDone,
                    label = bi("مزامنة سحابية", "Cloud Sync"),
                    modifier = Modifier.weight(1f)
                )
                FeatureHighlightPill(
                    icon = Icons.Rounded.Security,
                    label = bi("حماية صفرية", "Zero-Trust"),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NibrasSurfaceLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bi("تسجيل الدخول الآمن", "Secure Sign-In"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = bi(
                            "استخدم حساب Google لحفظ تقدمك الأكاديمي، ملاحظاتك، ونتائج اختباراتك في السحابة.",
                            "Continue with Google to sync your courses, book highlights, exam results, and AI chat."
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    if (errorMessage != null) {
                        Surface(
                            color = NibrasErrorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = NibrasOnErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NibrasPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(bi("جاري التحقق...", "Signing in..."))
                        } else {
                            Icon(Icons.Rounded.Login, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = bi("المتابعة باستخدام حساب Google", "Sign in with Google"),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = NibrasSurfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = NibrasPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Screen 3: Profile Onboarding Screen
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    userName: String,
    photoUrl: String?,
    onComplete: (major: String, studyYear: String, skills: List<String>) -> Unit
) {
    val isAr = LocalAppLanguage.current == AppLanguage.AR
    var selectedMajorIdx by remember { mutableStateOf(0) }
    var selectedYearIdx by remember { mutableStateOf(2) }
    val selectedSkills = remember { mutableStateListOf("Python", "Algorithms", "SQL") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NibrasSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NibrasNetworkImage(
                    url = photoUrl ?: NibrasAssets.STUDENT_BASEL,
                    contentDescription = "Student Avatar",
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = bi("استكمال الملف الأكاديمي", "Complete Academic Profile"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = bi("مرحباً $userName — لنخصّص خطتك بالذكاء الاصطناعي", "Hi $userName — let's personalize your AI learning track"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = bi("اختر تخصصك التقني", "Select Your Technical Major"),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NibrasCatalog.majors.forEachIndexed { idx, (ar, en) ->
                    val selected = selectedMajorIdx == idx
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) NibrasPrimaryFixed else NibrasSurfaceLowest,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) NibrasPrimary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMajorIdx = idx }
                            .testTag("onboarding_major_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = if (selected) NibrasPrimary else NibrasSecondary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = if (isAr) ar else en,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (selected) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = NibrasPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = bi("المستوى الدراسي", "Academic Year"),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NibrasCatalog.studyYears.forEachIndexed { idx, (ar, en) ->
                    FilterChip(
                        selected = selectedYearIdx == idx,
                        onClick = { selectedYearIdx = idx },
                        label = { Text(if (isAr) ar else en) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NibrasPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = bi("مهاراتك واهتماماتك الحالية", "Your Skills & Interests"),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NibrasCatalog.skillTags.forEach { tag ->
                    val active = tag in selectedSkills
                    FilterChip(
                        selected = active,
                        onClick = {
                            if (active) selectedSkills.remove(tag)
                            else if (selectedSkills.size < 10) selectedSkills.add(tag)
                        },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NibrasTertiaryContainer,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    val major = NibrasCatalog.majors[selectedMajorIdx].first
                    val year = NibrasCatalog.studyYears[selectedYearIdx].first
                    onComplete(major, year, selectedSkills.toList())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_continue_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NibrasPrimary)
            ) {
                Text(
                    text = bi("ابدأ رحلتك التعليمية في نبراس", "Start Learning with Nibras"),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
