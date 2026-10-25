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

    // Dark Mode Neutrals (Pure OLED & Solid Elevation)
    val DarkBackground = Color(0xFF000000)        // Pure Deep OLED Black
    val DarkSurface = Color(0xFF141416)           // Solid Opaque Surface
    val DarkElevated = Color(0xFF1E1E22)          // Solid Opaque Elevated
    val DarkCard = Color(0xFF18181C)              // Solid Inset Card
    val DarkLabelPrimary = Color(0xFFFFFFFF)
    val DarkLabelSecondary = Color(0xCCEBEBF5)    // 80% white (alta legibilidade)
    val DarkLabelTertiary = Color(0x80EBEBF5)     // 50% white
    val DarkHairline = Color(0xFF2C2C32)          // Borda sólida nítida 100% opaca
    val OledSurface = DarkSurface                 // Superfície sólida OLED preta pura

    // Light Mode Neutrals — Tema Modo Journal (Papel de Jornal Editorial Quente)
    val LightBackground = Color(0xFFF7F4EB)       // Papel jornal impresso quente (#F7F4EB)
    val LightSurface = Color(0xFFEFECE2)          // Superfície Sólida Journal (#EFECE2)
    val LightElevated = Color(0xFFE8E4D8)         // Elevação Journal (#E8E4D8)
    val LightCard = Color(0xFFEFECE2)             // Cartão Journal (#EFECE2)
    val LightLabelPrimary = Color(0xFF1C1917)     // Preto Tinta Nítido Editorial (#1C1917)
    val LightLabelSecondary = Color(0xFF57534E)   // Tinta Secundária (#57534E)
    val LightLabelTertiary = Color(0xFF78716C)    // Tinta Terciária (#78716C)
    val LightHairline = Color(0xFFDDD8CB)         // Borda fina Journal (#DDD8CB)
    val MatteSurface = LightSurface               // Superfície sólida Journal

    // Editorial / Modo Leitura — Efeito Jornal
    val NewspaperPaper = Color(0xFFF7F4EB)        // Tom de papel jornal impresso quente
    val NewspaperInk = Color(0xFF18181A)          // Tinta escura editorial
    val NewspaperPaperDark = Color(0xFF121214)    // Papel jornal modo noturno OLED
    val NewspaperInkDark = Color(0xFFECEAE2)      // Tinta clara suave

    // Solid Brushes (Fim do vidro translúcido artificial)
    fun liquidGlassBrush(isDarkMode: Boolean): Brush {
        return if (isDarkMode) {
            Brush.verticalGradient(
                listOf(DarkSurface, DarkSurface)
            )
        } else {
            Brush.verticalGradient(
                listOf(LightSurface, LightSurface)
            )
        }
    }

    // Hairline Border Sólido
    fun hairlineBorderBrush(isDarkMode: Boolean): Brush {
        return if (isDarkMode) {
            Brush.verticalGradient(
                listOf(DarkHairline, DarkHairline)
            )
        } else {
            Brush.verticalGradient(
                listOf(LightHairline, LightHairline)
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
    primary = ApplePalette.DarkLabelPrimary,
    onPrimary = Color.Black,
    primaryContainer = ApplePalette.DarkElevated,
    secondary = ApplePalette.DarkLabelSecondary,
    onSecondary = Color.Black,
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
    primary = ApplePalette.LightLabelPrimary,
    onPrimary = Color.White,
    primaryContainer = ApplePalette.LightElevated,
    secondary = ApplePalette.LightLabelSecondary,
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
