package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    primaryContainer = DeepIndigo,
    onPrimaryContainer = SoftLavender,
    secondary = RadiantMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A154B),
    onSecondaryContainer = SoftPink,
    tertiary = NeonCyan,
    onTertiary = Color.Black,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderDark,
    error = CoralWarning,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = DeepIndigo,
    secondary = RadiantMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE7F3),
    onSecondaryContainer = Color(0xFF831843),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlassSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    error = CoralWarning,
    onError = Color.White
)

@Composable
fun SayraTheme(
    darkTheme: Boolean = true, // Default to premium dark mode as specified
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
