package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

enum class AppLanguage(val code: String) {
    AR("ar"),
    EN("en")
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.AR }

private val NibrasLightColorScheme = lightColorScheme(
    primary = NibrasPrimary,
    onPrimary = NibrasOnPrimary,
    primaryContainer = NibrasPrimaryContainer,
    onPrimaryContainer = NibrasOnPrimaryContainer,
    secondary = NibrasSecondary,
    onSecondary = NibrasOnSecondary,
    secondaryContainer = NibrasSecondaryContainer,
    onSecondaryContainer = NibrasOnSecondaryContainer,
    tertiary = NibrasTertiary,
    onTertiary = NibrasOnTertiary,
    tertiaryContainer = NibrasTertiaryContainer,
    onTertiaryContainer = NibrasOnTertiaryContainer,
    background = NibrasSurface,
    onBackground = NibrasOnSurface,
    surface = NibrasSurface,
    onSurface = NibrasOnSurface,
    surfaceVariant = NibrasSurfaceHighest,
    onSurfaceVariant = NibrasOnSurfaceVariant,
    surfaceContainerLowest = NibrasSurfaceLowest,
    surfaceContainerLow = NibrasSurfaceLow,
    surfaceContainer = NibrasSurfaceContainer,
    surfaceContainerHigh = NibrasSurfaceHigh,
    surfaceContainerHighest = NibrasSurfaceHighest,
    surfaceDim = NibrasSurfaceDim,
    inverseSurface = NibrasInverseSurface,
    inverseOnSurface = NibrasInverseOnSurface,
    outline = NibrasOutline,
    outlineVariant = NibrasOutlineVariant,
    error = NibrasError,
    onError = NibrasOnError,
    errorContainer = NibrasErrorContainer,
    onErrorContainer = NibrasOnErrorContainer
)

val NibrasShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NibrasTheme(
    language: AppLanguage = AppLanguage.AR,
    content: @Composable () -> Unit
) {
    val direction = if (language == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalLayoutDirection provides direction
    ) {
        MaterialTheme(
            colorScheme = NibrasLightColorScheme,
            typography = NibrasTypography,
            shapes = NibrasShapes,
            content = content
        )
    }
}
