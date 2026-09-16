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
    background = Color(0xFF141110),         // Dark chocolate base
    onBackground = Color(0xFFE8E0D8),
    surface = Color(0xFF1A1A22),            // Glassmorphic surface
    onSurface = Color(0xFFE8E0D8),
    surfaceVariant = Color(0xFF2A2520),     // Slightly lighter chocolate
    onSurfaceVariant = Color(0xFFC4B8A8),
    outline = Color.White.copy(alpha = 0.12f),
    outlineVariant = Color.White.copy(alpha = 0.06f)
)

@Composable
fun TesseraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TesseraDarkColorScheme,
        content = content
    )
}
