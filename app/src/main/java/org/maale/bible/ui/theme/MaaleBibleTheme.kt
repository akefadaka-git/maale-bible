package org.maale.bible.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MaaleBibleDarkColorScheme = darkColorScheme(
    primary = MaaleBibleColors.RoyalGold,
    onPrimary = MaaleBibleColors.Midnight,
    primaryContainer = Color(0xFF3C321F),
    onPrimaryContainer = MaaleBibleColors.ScriptureWhite,
    secondary = MaaleBibleColors.LivingCyan,
    onSecondary = Color(0xFF062B2A),
    secondaryContainer = Color(0xFF123B3A),
    onSecondaryContainer = Color(0xFFC4F5F1),
    tertiary = Color(0xFFB9A6E8),
    onTertiary = Color(0xFF211A35),
    background = MaaleBibleColors.DarkBackground,
    onBackground = MaaleBibleColors.ScriptureWhite,
    surface = MaaleBibleColors.DarkSurface,
    onSurface = MaaleBibleColors.ScriptureWhite,
    surfaceVariant = MaaleBibleColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD2DF),
    outline = MaaleBibleColors.DarkOutline,
    outlineVariant = Color(0xFF35445D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val MaaleBibleLightColorScheme = lightColorScheme(
    primary = MaaleBibleColors.LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E2BE),
    onPrimaryContainer = Color(0xFF261A05),
    secondary = MaaleBibleColors.LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBCECE7),
    onSecondaryContainer = Color(0xFF00201E),
    tertiary = Color(0xFF62518A),
    onTertiary = Color.White,
    background = MaaleBibleColors.LightBackground,
    onBackground = MaaleBibleColors.LightOnSurface,
    surface = MaaleBibleColors.LightSurface,
    onSurface = MaaleBibleColors.LightOnSurface,
    surfaceVariant = MaaleBibleColors.LightSurfaceVariant,
    onSurfaceVariant = MaaleBibleColors.LightOnSurfaceVariant,
    outline = MaaleBibleColors.LightOutline,
    outlineVariant = Color(0xFFC9C3B5),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

/** Shared Maale Bible Material 3 theme; respects the device appearance by default. */
@Composable
fun MaaleBibleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) MaaleBibleDarkColorScheme else MaaleBibleLightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = MaaleBibleShapes,
        content = content
    )
}
