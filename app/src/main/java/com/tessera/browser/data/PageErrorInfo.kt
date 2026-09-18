package com.tessera.browser.data

data class PageErrorInfo(
    val url: String,
    val errorCode: Int = 0,
    val description: String = "",
    val isOffline: Boolean = true
)
