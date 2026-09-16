package com.tessera.browser.data

import androidx.compose.ui.graphics.Color

data class SpeedDialItem(
    val id: String,
    val title: String,
    val url: String,
    val iconEmoji: String? = null,
    val initial: String? = null,
    val badgeColor: Long = 0xFF2A2522
)
