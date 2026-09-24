package com.tessera.browser.data

data class BrowseSource(
    val title: String,
    val url: String,
    val domain: String,
    val snippet: String? = null
)

data class BrowseSection(
    val title: String,
    val content: String,
    val bulletPoints: List<String> = emptyList()
)

data class BrowseForMeResult(
    val query: String,
    val headline: String,
    val quickAnswer: String,
    val sections: List<BrowseSection> = emptyList(),
    val keyTakeaways: List<String> = emptyList(),
    val sources: List<BrowseSource> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class BrowseForMeState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val currentQuery: String = "",
    val loadingStep: String = "Pesquisando na web...",
    val result: BrowseForMeResult? = null,
    val error: String? = null
)
