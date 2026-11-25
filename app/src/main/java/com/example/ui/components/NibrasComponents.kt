package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.i18n.bi
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NibrasCodeAccent
import com.example.ui.theme.NibrasCodeBg
import com.example.ui.theme.NibrasCodeHeader
import com.example.ui.theme.NibrasCodeText
import com.example.ui.theme.NibrasPrimary
import com.example.ui.theme.NibrasPrimaryContainer
import com.example.ui.theme.NibrasPrimaryFixed
import com.example.ui.theme.NibrasSecondary
import com.example.ui.theme.NibrasSecondaryContainer
import com.example.ui.theme.NibrasSurfaceContainer
import com.example.ui.theme.NibrasSurfaceLowest
import com.example.ui.theme.NibrasTertiary
import com.example.ui.theme.NibrasTertiaryContainer
import com.example.ui.theme.NibrasTertiaryFixed

@Composable
fun NibrasNetworkImage(
    url: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(NibrasSurfaceContainer, NibrasPrimaryFixed)
                        )
                    )
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(NibrasPrimary, NibrasTertiaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    )
}

@Composable
fun RadialProgressGauge(
    progressPercent: Int,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    strokeWidth: Dp = 9.dp,
    trackColor: Color = NibrasPrimaryFixed,
    progressColor: Color = NibrasPrimary,
    centerTextColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progressPercent.coerceIn(0, 100) / 100f),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 260f),
        label = "radial_progress"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(progressColor, NibrasSecondaryContainer, NibrasTertiaryContainer, progressColor)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$progressPercent%",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = centerTextColor
            )
        )
    }
}

@Composable
fun NibrasCodeCard(
    code: String,
    languageLabel: String = "python",
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = NibrasCodeBg
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NibrasCodeHeader)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = languageLabel.uppercase(),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NibrasCodeAccent
                        )
                    }
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(code)) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy code",
                            tint = NibrasCodeText.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = code,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    color = NibrasCodeText,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun BinaryTreeDiagramCanvas(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NibrasSurfaceLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = bi("مخطط شجرة البحث الثنائية المتوازنة (Balanced BST)", "Balanced Binary Search Tree (BST)"),
                style = MaterialTheme.typography.labelLarge,
                color = NibrasPrimary
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val root = Offset(w * 0.50f, h * 0.20f)
                    val left = Offset(w * 0.28f, h * 0.54f)
                    val right = Offset(w * 0.72f, h * 0.54f)
                    val ll = Offset(w * 0.16f, h * 0.84f)
                    val lr = Offset(w * 0.40f, h * 0.84f)
                    val rl = Offset(w * 0.60f, h * 0.84f)
                    val rr = Offset(w * 0.84f, h * 0.84f)

                    val edges = listOf(
                        root to left, root to right,
                        left to ll, left to lr,
                        right to rl, right to rr
                    )
                    edges.forEach { (start, end) ->
                        drawLine(
                            color = NibrasSecondary.copy(alpha = 0.45f),
                            start = start,
                            end = end,
                            strokeWidth = 4f
                        )
                    }
                    val radius = 22f
                    listOf(
                        root to NibrasPrimary,
                        left to NibrasTertiaryContainer,
                        right to NibrasTertiaryContainer,
                        ll to NibrasSecondary,
                        lr to NibrasSecondary,
                        rl to NibrasSecondary,
                        rr to NibrasSecondary
                    ).forEach { (pos, color) ->
                        drawCircle(color = color, radius = radius, center = pos)
                        drawCircle(color = Color.White, radius = radius - 6f, center = pos, style = Stroke(2.5f))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("12", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = NibrasSecondary)
                    Text("35", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = NibrasSecondary)
                    Text("62", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = NibrasSecondary)
                    Text("91", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = NibrasSecondary)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = bi("In-Order: 12 → 25 → 35 → 50 → 62 → 75 → 91  |  O(log n)", "In-Order: 12 -> 25 -> 35 -> 50 -> 62 -> 75 -> 91  |  O(log n)"),
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NibrasAiBadge(
    label: String = bi("ذكاء نبراس", "Nibras AI"),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = NibrasTertiaryFixed
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = NibrasTertiary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NibrasTertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NibrasGradientHeroCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(NibrasPrimary, NibrasPrimaryContainer, NibrasTertiaryContainer),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun NibrasSectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = NibrasPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
