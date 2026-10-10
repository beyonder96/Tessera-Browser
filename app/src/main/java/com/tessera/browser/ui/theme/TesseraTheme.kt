package com.tessera.browser.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Apple iOS 18 Design System — Authentic System Colors
object ApplePalette {
    // Primary Accents
    val Blue = Color(0xFF007AFF)           // Apple System Blue (Light)
    val BlueDark = Color(0xFF0A84FF)       // Apple System Blue (Dark)
    val Indigo = Color(0xFF5856D6)
    val Purple = Color(0xFFAF52DE)
    val Pink = Color(0xFFFF2D55)
    val Red = Color(0xFFFF3B30)
    val Orange = Color(0xFFFF9500)
    val Yellow = Color(0xFFFFCC00)
    val Green = Color(0xFF34C759)
    val Mint = Color(0xFF00C7BE)
    val Teal = Color(0xFF30B0C7)
    val Cyan = Color(0xFF32ADE6)

    // Dark Mode Neutrals (Pure OLED & Elevation)
    val DarkBackground = Color(0xFF000000)        // Pure Deep OLED Black
    val DarkSurface = Color(0xFF1C1C1E)           // Secondary System Background
    val DarkElevated = Color(0xFF2C2C2E)          // Tertiary System Background
    val DarkCard = Color(0xFF242426)              // Inset Grouped Card
    val DarkLabelPrimary = Color(0xFFFFFFFF)
    val DarkLabelSecondary = Color(0x99EBEBF5)    // 60% white
    val DarkLabelTertiary = Color(0x4DEBEBF5)     // 30% white
    val DarkHairline = Color(0x26FFFFFF)          // 15% specular white border

    // Light Mode Neutrals (Porcelain & System Gray)
    val LightBackground = Color(0xFFF2F2F7)       // System Grouped Background
    val LightSurface = Color(0xFFFFFFFF)          // Secondary System Background
    val LightElevated = Color(0xFFE5E5EA)         // Tertiary System Background
    val LightCard = Color(0xFFFFFFFF)
    val LightLabelPrimary = Color(0xFF000000)
    val LightLabelSecondary = Color(0x993C3C43)   // 60% black
    val LightLabelTertiary = Color(0x4D3C3C43)    // 30% black
    val LightHairline = Color(0x1A000000)         // 10% black hairline border

    // Frosted Glass Gradients
    fun liquidGlassBrush(isDarkMode: Boolean): Brush {
        return if (isDarkMode) {
            Brush.verticalGradient(
                listOf(
                    Color(0xD91C1C1E),
                    Color(0xEA161618)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xF0FFFFFF),
                    Color(0xE6F8F8FA)
                )
            )
        }
    }

    // Hairline Border Gradients (Apple Specular Highlights)
    fun hairlineBorderBrush(isDarkMode: Boolean): Brush {
        return if (isDarkMode) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.06f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.80f),
                    Color.Black.copy(alpha = 0.08f)
                )
            )
        }
    }
}

// Apple Squircle Geometries
object AppleShapes {
    val SquircleSmall = RoundedCornerShape(12.dp)
    val SquircleMedium = RoundedCornerShape(16.dp)
    val SquircleLarge = RoundedCornerShape(22.dp)
    val Capsule = RoundedCornerShape(26.dp)
    val FullCapsule = RoundedCornerShape(32.dp)
}

private val TesseraDarkColorScheme = darkColorScheme(
    primary = ApplePalette.BlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F325E),
    secondary = ApplePalette.Teal,
    onSecondary = Color.White,
    background = ApplePalette.DarkBackground,
    onBackground = ApplePalette.DarkLabelPrimary,
    surface = ApplePalette.DarkSurface,
    onSurface = ApplePalette.DarkLabelPrimary,
    surfaceVariant = ApplePalette.DarkElevated,
    onSurfaceVariant = ApplePalette.DarkLabelSecondary,
    outline = ApplePalette.DarkHairline,
    outlineVariant = Color.White.copy(alpha = 0.08f)
)

private val TesseraLightColorScheme = lightColorScheme(
    primary = ApplePalette.Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E6FF),
    secondary = ApplePalette.Teal,
    onSecondary = Color.White,
    background = ApplePalette.LightBackground,
    onBackground = ApplePalette.LightLabelPrimary,
    surface = ApplePalette.LightSurface,
    onSurface = ApplePalette.LightLabelPrimary,
    surfaceVariant = ApplePalette.LightElevated,
    onSurfaceVariant = ApplePalette.LightLabelSecondary,
    outline = ApplePalette.LightHairline,
    outlineVariant = Color.Black.copy(alpha = 0.05f)
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
