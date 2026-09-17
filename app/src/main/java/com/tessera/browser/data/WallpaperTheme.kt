package com.tessera.browser.data

import androidx.compose.ui.graphics.Color

data class WallpaperTheme(
    val id: String,
    val name: String,
    val drawableRes: Int? = null,
    val gradientColors: List<Color>,
    val accentColor: Color
)

val AvailableWallpapers = listOf(
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
