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
        id = "chocolate_silk",
        name = "Seda Chocolate",
        drawableRes = R.drawable.wallpaper_chocolate_silk,
        gradientColors = listOf(Color(0xFF4E342E), Color(0xFF271A16), Color(0xFF120C0A)),
        accentColor = Color(0xFFFFB74D)
    ),
    WallpaperTheme(
        id = "nebula",
        name = "Nebulosa Cósmica",
        gradientColors = listOf(Color(0xFF2C1654), Color(0xFF1B0B33), Color(0xFF0C0517)),
        accentColor = Color(0xFFBA68C8)
    ),
    WallpaperTheme(
        id = "aurora",
        name = "Aurora Boreal",
        gradientColors = listOf(Color(0xFF0B3328), Color(0xFF09201A), Color(0xFF040F0C)),
        accentColor = Color(0xFF26A69A)
    ),
    WallpaperTheme(
        id = "solar",
        name = "Crepúsculo Solar",
        gradientColors = listOf(Color(0xFF3B1F0B), Color(0xFF241005), Color(0xFF120803)),
        accentColor = Color(0xFFFFA726)
    ),
    WallpaperTheme(
        id = "cyberpunk",
        name = "Cyberpunk Neon",
        gradientColors = listOf(Color(0xFF1E0A3C), Color(0xFF0D153A), Color(0xFF060B1E)),
        accentColor = Color(0xFF00E5FF)
    ),
    WallpaperTheme(
        id = "abyssal",
        name = "Oceano Abissal",
        gradientColors = listOf(Color(0xFF0B2240), Color(0xFF071426), Color(0xFF030A14)),
        accentColor = Color(0xFF29B6F6)
    ),
    WallpaperTheme(
        id = "graphite",
        name = "Grafite Minimalista",
        gradientColors = listOf(Color(0xFF212529), Color(0xFF16181B), Color(0xFF0D0E10)),
        accentColor = Color(0xFFB0BEC5)
    )
)
