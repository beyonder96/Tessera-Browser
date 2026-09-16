package com.tessera.browser.data

import androidx.annotation.DrawableRes

data class SpeedDialItem(
    val id: String,
    val title: String,
    val url: String,
    @DrawableRes val iconRes: Int? = null,
    val iconUrl: String? = null,
    val initial: String? = null,
    val badgeColor: Long = 0xFF2A2522
)
