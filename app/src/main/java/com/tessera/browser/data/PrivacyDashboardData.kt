package com.tessera.browser.data

enum class TrackerCategory(val displayName: String, val iconEmoji: String) {
    ADVERTISING("Publicidade & Banners", "📢"),
    ANALYTICS("Rastreadores Analíticos", "📊"),
    SOCIAL("Rastreadores Sociais", "👥"),
    FINGERPRINTING("Telemetria & Fingerprinting", "🔍"),
    SECURITY("Ameaça / Desconhecido", "🛡️")
}

data class BlockedTrackerItem(
    val domain: String,
    val entityName: String,
    val category: TrackerCategory,
    val count: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

data class PrivacyDashboardState(
    val isVisible: Boolean = false,
    val currentDomain: String = "",
    val isCurrentSiteSecure: Boolean = true,
    val pageBlockedCount: Int = 0,
    val sessionBlockedCount: Int = 0,
    val totalBlockedCount: Int = 0,
    val pageBlockedItems: List<BlockedTrackerItem> = emptyList(),
    val dataSavedBytes: Long = 0L,
    val estimatedTimeSavedMs: Long = 0L
) {
    val formattedDataSaved: String
        get() {
            val kb = dataSavedBytes / 1024.0
            return if (kb >= 1024.0) {
                String.format(java.util.Locale.getDefault(), "%.1f MB", kb / 1024.0)
            } else {
                String.format(java.util.Locale.getDefault(), "%.0f KB", kb)
            }
        }

    val formattedTimeSaved: String
        get() {
            val seconds = estimatedTimeSavedMs / 1000.0
            return if (seconds >= 1.0) {
                String.format(java.util.Locale.getDefault(), "%.1fs", seconds)
            } else {
                "${estimatedTimeSavedMs}ms"
            }
        }
}
