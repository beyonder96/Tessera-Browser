package com.tessera.browser.data

import androidx.compose.ui.graphics.Color
import com.tessera.browser.R

data class WallpaperTheme(
    val id: String,
    val name: String,
    val drawableRes: Int? = null,
    val gradientColors: List<Color>,
    val accentColor: Color
)

val AvailableWallpapers = listOf(
    WallpaperTheme(
        id = "summer_villa",
        name = "Villa Mediterrânea",
        drawableRes = R.drawable.wallpaper_summer_villa,
        gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF1E88E5), Color(0xFF0D47A1)),
        accentColor = Color(0xFF0288D1)
    ),
    WallpaperTheme(
        id = "neon_horizon",
        name = "Neon Horizon",
        gradientColors = listOf(Color(0xFF240046), Color(0xFF3C096C), Color(0xFF5A189A), Color(0xFF7B2CBF)),
        accentColor = Color(0xFF00E5FF)
    ),
    WallpaperTheme(
        id = "emerald_forest",
        name = "Floresta Esmeralda",
        gradientColors = listOf(Color(0xFF0B2E24), Color(0xFF144D3E), Color(0xFF1C6B56), Color(0xFF071F18)),
        accentColor = Color(0xFF00E676)
    ),
    WallpaperTheme(
        id = "violet_galaxy",
        name = "Galáxia Violeta",
        gradientColors = listOf(Color(0xFF180A2E), Color(0xFF2D1254), Color(0xFF4A1E8A), Color(0xFF120524)),
        accentColor = Color(0xFFB388FF)
    ),
    WallpaperTheme(
        id = "sunset_dunes",
        name = "Duna Dourada",
        gradientColors = listOf(Color(0xFF3E1E14), Color(0xFF5D2E1F), Color(0xFF7E3F2B), Color(0xFF2A120B)),
        accentColor = Color(0xFFFFB74D)
    ),
    WallpaperTheme(
        id = "deep_ocean",
        name = "Oceano Profundo",
        gradientColors = listOf(Color(0xFF071E3D), Color(0xFF0B2E5C), Color(0xFF134582), Color(0xFF030E1F)),
        accentColor = Color(0xFF40C4FF)
    ),
    WallpaperTheme(
        id = "amoled_black",
        name = "Black AMOLED",
        gradientColors = listOf(Color(0xFF000000), Color(0xFF0A0A0C), Color(0xFF121216), Color(0xFF000000)),
        accentColor = Color(0xFFE0E0E0)
    )
)
