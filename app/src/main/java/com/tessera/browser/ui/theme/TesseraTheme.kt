package com.tessera.browser.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TesseraDarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),          // Cyan-blue accent
    onPrimary = Color(0xFF0A0A0E),
    primaryContainer = Color(0xFF1A3A5C),
    secondary = Color(0xFFCCA882),         // Bronze accent
    onSecondary = Color(0xFF0A0A0E),
    background = Color(0xFF120E0D),         // Dark base
    onBackground = Color(0xFFE8E0D8),
    surface = Color(0xFF1A1715),            // Glassmorphic dark surface
    onSurface = Color(0xFFE8E0D8),
    surfaceVariant = Color(0xFF261F1B),
    onSurfaceVariant = Color(0xFFC4B8A8),
    outline = Color.White.copy(alpha = 0.12f),
    outlineVariant = Color.White.copy(alpha = 0.06f)
)

private val TesseraLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF0277BD),          // Deep cyan-blue accent
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F5FE),
    secondary = Color(0xFF8D6E63),
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),         // Light neutral porcelain
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFFFFFF),            // Pure white glassmorphic surface
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFEEF0F3),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color.Black.copy(alpha = 0.12f),
    outlineVariant = Color.Black.copy(alpha = 0.06f)
)

@Composable
fun TesseraTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkMode) TesseraDarkColorScheme else TesseraLightColorScheme,
        content = content
    )
}
