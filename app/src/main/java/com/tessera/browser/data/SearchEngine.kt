package com.tessera.browser.data

import androidx.compose.ui.graphics.Color
import com.tessera.browser.R
import java.net.URLEncoder

enum class SearchEngine(
    val id: String,
    val displayName: String,
    val searchUrlTemplate: String,
    val homeUrl: String,
    val suggestUrlTemplate: String,
    val iconRes: Int,
    val accentColor: Color
) {
    GOOGLE(
        id = "google",
        displayName = "Google",
        searchUrlTemplate = "https://www.google.com/search?q=%s",
        homeUrl = "https://www.google.com",
        suggestUrlTemplate = "https://suggestqueries.google.com/complete/search?client=firefox&q=%s",
        iconRes = R.drawable.ic_brand_google,
        accentColor = Color(0xFF4285F4)
    ),
    DUCKDUCKGO(
        id = "duckduckgo",
        displayName = "DuckDuckGo",
        searchUrlTemplate = "https://duckduckgo.com/?q=%s",
        homeUrl = "https://duckduckgo.com",
        suggestUrlTemplate = "https://duckduckgo.com/ac/?q=%s&type=list",
        iconRes = R.drawable.ic_brand_duckduckgo,
        accentColor = Color(0xFFDE5833)
    ),
    BING(
        id = "bing",
        displayName = "Bing",
        searchUrlTemplate = "https://www.bing.com/search?q=%s",
        homeUrl = "https://www.bing.com",
        suggestUrlTemplate = "https://api.bing.com/osjson.aspx?query=%s",
        iconRes = R.drawable.ic_brand_bing,
        accentColor = Color(0xFF008394)
    ),
    BRAVE(
        id = "brave",
        displayName = "Brave Search",
        searchUrlTemplate = "https://search.brave.com/search?q=%s",
        homeUrl = "https://search.brave.com",
        suggestUrlTemplate = "https://search.brave.com/api/suggest?q=%s",
        iconRes = R.drawable.ic_brand_brave,
        accentColor = Color(0xFFFB542B)
    ),
    ECOSIA(
        id = "ecosia",
        displayName = "Ecosia",
        searchUrlTemplate = "https://www.ecosia.org/search?q=%s",
        homeUrl = "https://www.ecosia.org",
        suggestUrlTemplate = "https://ac.ecosia.org/autocomplete?q=%s&type=list",
        iconRes = R.drawable.ic_brand_ecosia,
        accentColor = Color(0xFF00A562)
    );

    fun buildSearchUrl(query: String): String {
        return try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            searchUrlTemplate.replace("%s", encoded)
        } catch (e: Exception) {
            searchUrlTemplate.replace("%s", query.trim().replace(" ", "+"))
        }
    }

    fun buildSuggestUrl(query: String): String {
        return try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            suggestUrlTemplate.replace("%s", encoded)
        } catch (e: Exception) {
            suggestUrlTemplate.replace("%s", query.trim().replace(" ", "+"))
        }
    }

    companion object {
        val DEFAULT = GOOGLE

        fun fromId(id: String?): SearchEngine {
            if (id.isNullOrBlank()) return DEFAULT
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
