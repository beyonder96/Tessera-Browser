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
        id = "chocolate",
        name = "Seda Chocolate",
        drawableRes = com.tessera.browser.R.drawable.wallpaper_chocolate_silk,
        gradientColors = listOf(Color(0xFF241B16), Color(0xFF140F0D)),
        accentColor = Color(0xFFCCA882)
    ),
    WallpaperTheme(
        id = "emerald",
        name = "Ondas Esmeralda",
        gradientColors = listOf(Color(0xFF152A20), Color(0xFF0C1712), Color(0xFF080F0C)),
        accentColor = Color(0xFF4CAF50)
    ),
    WallpaperTheme(
        id = "ocean",
        name = "Dunas Ciano",
        gradientColors = listOf(Color(0xFF122233), Color(0xFF0B141E), Color(0xFF060C12)),
        accentColor = Color(0xFF64B5F6)
    ),
    WallpaperTheme(
        id = "obsidian",
        name = "Obsidiana Pura",
        gradientColors = listOf(Color(0xFF1A1A1E), Color(0xFF101014), Color(0xFF0A0A0C)),
        accentColor = Color(0xFF9E9E9E)
    )
)
