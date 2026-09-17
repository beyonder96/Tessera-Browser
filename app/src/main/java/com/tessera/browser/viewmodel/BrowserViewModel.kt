package com.tessera.browser.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.data.WallpaperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID

data class WeatherData(
    val cityName: String = "São Paulo",
    val temperature: Int = 24,
    val apparentTemperature: Int = 24,
    val humidity: Int = 65,
    val conditionText: String = "Parcialmente nublado",
    val weatherCode: Int = 1,
    val isDay: Boolean = true,
    val isLoading: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class QuoteItem(
    val symbol: String, // "USD", "EUR", "BTC"
    val name: String,   // "Dólar", "Euro", "Bitcoin"
    val value: String,  // "R$ 5,64"
    val change: String, // "+0,42%"
    val isPositive: Boolean
)

data class QuotesData(
    val items: List<QuoteItem> = emptyList(),
    val isLoading: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://duckduckgo.com",
    val title: String = "Nova Guia",
    val isHomePage: Boolean = true,
    val canGoBack: Boolean = false
)

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BrowserUiState(
    val isHomePage: Boolean = true,
    val currentUrl: String = "https://duckduckgo.com",
    val displayUrl: String = "",
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val isBarVisible: Boolean = true,
    val showQuickSettings: Boolean = false,

    // Multi-tabs
    val tabs: List<BrowserTab> = listOf(
        BrowserTab(id = "default-tab", url = "https://duckduckgo.com", title = "Início", isHomePage = true)
    ),
    val activeTabId: String = "default-tab",
    val showTabsModal: Boolean = false,

    // History & Bookmarks
    val history: List<HistoryEntry> = emptyList(),
    val showHistoryModal: Boolean = false,

    // Quick AI Actions Modal
    val showAiActionModal: Boolean = false,

    // AdBlocker
    val adBlockEnabled: Boolean = true,

    // Search Autocomplete & Trends
    val searchSuggestions: List<String> = emptyList(),
    val trendingTopics: List<String> = listOf(
        "🔥 Inteligência Artificial",
        "Notícias do Dia",
        "Clima & Previsão",
        "Lançamentos de Jogos",
        "Cotação do Dólar",
        "Cinema & Séries"
    ),

    // Widgets da Home
    val showWeatherWidget: Boolean = true,
    val showQuotesWidget: Boolean = true,
    val weatherData: WeatherData? = null,
    val quotesData: QuotesData? = null,

    // Configuração Fácil
    val isDarkMode: Boolean = true,
    val forceDarkPages: Boolean = false,
    val showWallpaper: Boolean = true,
    val selectedWallpaperId: String = "nebula",
    val showFavoritesBar: Boolean = true,
    val showCatInara: Boolean = false,
    val tesseraAiEnabled: Boolean = true,
    val aiToolbarButton: Boolean = true,
    val aiTextHighlightPrompts: Boolean = true,
    val showSidebar: Boolean = false,
    val autoHideSidebar: Boolean = true,

    // Speed Dial / Bookmarks
    val speedDialItems: List<SpeedDialItem> = listOf(
        SpeedDialItem(
            id = "1",
            title = "DuckDuckGo",
            url = "https://duckduckgo.com",
            iconRes = com.tessera.browser.R.drawable.ic_brand_duckduckgo,
            badgeColor = 0xFFDE5833
        ),
        SpeedDialItem(
            id = "2",
            title = "Google",
            url = "https://www.google.com",
            iconRes = com.tessera.browser.R.drawable.ic_brand_google,
            badgeColor = 0xFF4285F4
        ),
        SpeedDialItem(
            id = "3",
            title = "YouTube",
            url = "https://www.youtube.com",
            iconRes = com.tessera.browser.R.drawable.ic_brand_youtube,
            badgeColor = 0xFFFF0000
        ),
        SpeedDialItem(
            id = "4",
            title = "Wikipedia",
            url = "https://pt.wikipedia.org",
            iconRes = com.tessera.browser.R.drawable.ic_brand_wikipedia,
            badgeColor = 0xFF2C3238
        ),
        SpeedDialItem(
            id = "5",
            title = "GitHub",
            url = "https://github.com",
            iconRes = com.tessera.browser.R.drawable.ic_brand_github,
            badgeColor = 0xFF24292E
        ),
        SpeedDialItem(
            id = "6",
            title = "Reddit",
            url = "https://reddit.com",
            iconRes = com.tessera.browser.R.drawable.ic_brand_reddit,
            badgeColor = 0xFFFF4500
        )
    )
) {
    val activeWallpaper: WallpaperTheme
        get() = AvailableWallpapers.find { it.id == selectedWallpaperId }
            ?: AvailableWallpapers.first()

    val isCurrentPageBookmarked: Boolean
        get() = speedDialItems.any { it.url.equals(displayUrl, ignoreCase = true) || it.url.equals(currentUrl, ignoreCase = true) }
}

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var suggestionJob: Job? = null

    fun openUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return

        val formattedUrl = formatInputAsUrl(trimmed)
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(url = formattedUrl, isHomePage = false)
                } else tab
            }
            state.copy(
                isHomePage = false,
                currentUrl = formattedUrl,
                displayUrl = formattedUrl,
                isBarVisible = true,
                tabs = updatedTabs,
                searchSuggestions = emptyList()
            )
        }
    }

    fun openAiQuery(query: String) {
        val trimmed = query.trim()
        val aiUrl = if (trimmed.isNotBlank()) {
            "https://duckduckgo.com/?q=${URLEncoder.encode(trimmed, "UTF-8")}&ia=chat"
        } else {
            "https://duckduckgo.com/chat"
        }
        openUrl(aiUrl)
    }

    fun openAiAction(action: String, pageUrl: String = "") {
        dismissAiActionModal()
        val target = if (pageUrl.isNotBlank()) pageUrl else _uiState.value.displayUrl
        when (action) {
            "summarize" -> {
                openAiQuery("Faça um resumo dos pontos mais importantes da página: $target")
            }
            "explain" -> {
                openAiQuery("Explique de maneira simples e didática o assunto de: $target")
            }
            "chat" -> {
                openAiQuery("")
            }
            else -> {
                openAiQuery(action)
            }
        }
    }

    fun goHome() {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(isHomePage = true, url = "https://duckduckgo.com")
                } else tab
            }
            state.copy(
                isHomePage = true,
                progress = 0f,
                tabs = updatedTabs,
                searchSuggestions = emptyList()
            )
        }
    }

    fun onPageStarted(url: String?) {
        if (!url.isNullOrBlank() && !_uiState.value.isHomePage) {
            _uiState.update { state ->
                val updatedTabs = state.tabs.map { tab ->
                    if (tab.id == state.activeTabId) {
                        tab.copy(url = url, isHomePage = false)
                    } else tab
                }
                state.copy(
                    currentUrl = url,
                    displayUrl = url,
                    tabs = updatedTabs
                )
            }
        }
    }

    fun onPageFinished(url: String?, canBack: Boolean, title: String? = null) {
        val effectiveUrl = url ?: _uiState.value.currentUrl
        val effectiveTitle = if (!title.isNullOrBlank()) title else extractDomain(effectiveUrl)

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        url = effectiveUrl,
                        title = effectiveTitle,
                        canGoBack = canBack,
                        isHomePage = false
                    )
                } else tab
            }

            // Register in history if viewing a website
            val newHistory = if (!state.isHomePage && effectiveUrl.isNotBlank() && effectiveUrl.startsWith("http")) {
                val entry = HistoryEntry(title = effectiveTitle, url = effectiveUrl)
                (listOf(entry) + state.history.filterNot { it.url == effectiveUrl }).take(100)
            } else {
                state.history
            }

            state.copy(
                progress = 0f,
                canGoBack = canBack,
                currentUrl = effectiveUrl,
                displayUrl = effectiveUrl,
                tabs = updatedTabs,
                history = newHistory
            )
        }
    }

    fun updateProgress(progress: Float) {
        _uiState.update { it.copy(progress = progress) }
    }

    fun setBarVisibility(visible: Boolean) {
        if (_uiState.value.isBarVisible != visible) {
            _uiState.update { it.copy(isBarVisible = visible) }
        }
    }

    // MULTI-TABS MANAGEMENT
    fun addNewTab(url: String = "https://duckduckgo.com", isHome: Boolean = true) {
        val newId = UUID.randomUUID().toString()
        val newTab = BrowserTab(
            id = newId,
            url = url,
            title = if (isHome) "Nova Guia" else extractDomain(url),
            isHomePage = isHome
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newId,
                isHomePage = isHome,
                currentUrl = url,
                displayUrl = if (isHome) "" else url,
                canGoBack = false,
                showTabsModal = false
            )
        }
    }

    fun selectTab(tabId: String) {
        val targetTab = _uiState.value.tabs.find { it.id == tabId } ?: return
        _uiState.update { state ->
            state.copy(
                activeTabId = tabId,
                isHomePage = targetTab.isHomePage,
                currentUrl = targetTab.url,
                displayUrl = if (targetTab.isHomePage) "" else targetTab.url,
                canGoBack = targetTab.canGoBack,
                showTabsModal = false
            )
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            // If closing only tab, reset to home
            goHome()
            return
        }

        val remainingTabs = currentTabs.filterNot { it.id == tabId }
        val nextActiveTab = if (_uiState.value.activeTabId == tabId) {
            remainingTabs.last()
        } else {
            remainingTabs.find { it.id == _uiState.value.activeTabId } ?: remainingTabs.last()
        }

        _uiState.update { state ->
            state.copy(
                tabs = remainingTabs,
                activeTabId = nextActiveTab.id,
                isHomePage = nextActiveTab.isHomePage,
                currentUrl = nextActiveTab.url,
                displayUrl = if (nextActiveTab.isHomePage) "" else nextActiveTab.url,
                canGoBack = nextActiveTab.canGoBack
            )
        }
    }

    fun toggleTabsModal() {
        _uiState.update { it.copy(showTabsModal = !it.showTabsModal) }
    }

    fun dismissTabsModal() {
        _uiState.update { it.copy(showTabsModal = false) }
    }

    // HISTORY & BOOKMARKS MANAGEMENT
    fun toggleBookmark(title: String, url: String) {
        val targetUrl = formatInputAsUrl(url)
        val isBookmarked = _uiState.value.speedDialItems.any { it.url.equals(targetUrl, ignoreCase = true) }

        if (isBookmarked) {
            _uiState.update { state ->
                state.copy(speedDialItems = state.speedDialItems.filterNot { it.url.equals(targetUrl, ignoreCase = true) })
            }
        } else {
            addSpeedDialItem(title.ifBlank { extractDomain(targetUrl) }, targetUrl)
        }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun toggleHistoryModal() {
        _uiState.update { it.copy(showHistoryModal = !it.showHistoryModal) }
    }

    fun dismissHistoryModal() {
        _uiState.update { it.copy(showHistoryModal = false) }
    }

    // QUICK AI ACTIONS MODAL
    fun toggleAiActionModal() {
        _uiState.update { it.copy(showAiActionModal = !it.showAiActionModal) }
    }

    fun dismissAiActionModal() {
        _uiState.update { it.copy(showAiActionModal = false) }
    }

    // SEARCH AUTOCOMPLETE & SUGGESTIONS
    fun fetchSearchSuggestions(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _uiState.update { it.copy(searchSuggestions = emptyList()) }
            return
        }

        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch(Dispatchers.IO) {
            delay(150) // Small debounce
            try {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                val url = URL("https://duckduckgo.com/ac/?q=$encoded&type=list")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONArray(response)
                    if (json.length() > 1) {
                        val arr = json.getJSONArray(1)
                        val suggestions = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            val item = arr.optString(i)
                            if (!item.isNullOrBlank()) suggestions.add(item)
                        }
                        _uiState.update { it.copy(searchSuggestions = suggestions.take(5)) }
                    }
                }
            } catch (e: Exception) {
                // Silently ignore network failures for suggestions
            }
        }
    }

    fun clearSearchSuggestions() {
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
    }

    // Settings mutators
    fun setAdBlockEnabled(enabled: Boolean) {
        _uiState.update { it.copy(adBlockEnabled = enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun setForceDarkPages(enabled: Boolean) {
        _uiState.update { it.copy(forceDarkPages = enabled) }
    }

    fun setShowWallpaper(enabled: Boolean) {
        _uiState.update { it.copy(showWallpaper = enabled) }
    }

    fun selectWallpaper(wallpaperId: String) {
        _uiState.update { it.copy(selectedWallpaperId = wallpaperId, showWallpaper = true) }
    }

    fun setShowFavoritesBar(enabled: Boolean) {
        _uiState.update { it.copy(showFavoritesBar = enabled) }
    }

    fun setShowCatInara(enabled: Boolean) {
        _uiState.update { it.copy(showCatInara = enabled) }
    }

    fun setTesseraAiEnabled(enabled: Boolean) {
        _uiState.update { it.copy(tesseraAiEnabled = enabled) }
    }

    fun setAiToolbarButton(enabled: Boolean) {
        _uiState.update { it.copy(aiToolbarButton = enabled) }
    }

    fun setAiTextHighlightPrompts(enabled: Boolean) {
        _uiState.update { it.copy(aiTextHighlightPrompts = enabled) }
    }

    fun setShowSidebar(enabled: Boolean) {
        _uiState.update { it.copy(showSidebar = enabled) }
    }

    fun setAutoHideSidebar(enabled: Boolean) {
        _uiState.update { it.copy(autoHideSidebar = enabled) }
    }

    fun toggleQuickSettings() {
        _uiState.update { it.copy(showQuickSettings = !it.showQuickSettings) }
    }

    fun setShowWeatherWidget(enabled: Boolean) {
        _uiState.update { it.copy(showWeatherWidget = enabled) }
    }

    fun setShowQuotesWidget(enabled: Boolean) {
        _uiState.update { it.copy(showQuotesWidget = enabled) }
    }

    private fun getWeatherConditionText(code: Int, isDay: Boolean): String {
        return when (code) {
            0 -> if (isDay) "Céu limpo" else "Noite limpa"
            1 -> "Predominantemente ensolarado"
            2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Nevoeiro"
            51, 53, 55 -> "Garoa leve"
            56, 57 -> "Garoa congelante"
            61, 63 -> "Chuva moderada"
            65 -> "Chuva forte"
            66, 67 -> "Chuva congelante"
            71, 73, 75 -> "Neve"
            77 -> "Grãos de neve"
            80, 81, 82 -> "Pancadas de chuva"
            85, 86 -> "Pancadas de neve"
            95 -> "Tempestade"
            96, 99 -> "Tempestade com granizo"
            else -> if (isDay) "Ensolarado" else "Céu limpo"
        }
    }

    fun fetchWeather(context: Context, forceRefresh: Boolean = false) {
        val current = _uiState.value.weatherData
        if (!forceRefresh && current != null && (System.currentTimeMillis() - current.lastUpdated < 15 * 60 * 1000)) {
            return
        }

        _uiState.update {
            it.copy(weatherData = it.weatherData?.copy(isLoading = true) ?: WeatherData(isLoading = true))
        }

        viewModelScope.launch(Dispatchers.IO) {
            var lat = -23.5505
            var lon = -46.6333
            var cityName = "São Paulo"
            var hasGpsLocation = false

            // 1. Try Android LocationManager if coarse location is granted
            try {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    if (locationManager != null) {
                        val providers = listOf(
                            LocationManager.NETWORK_PROVIDER,
                            LocationManager.GPS_PROVIDER,
                            LocationManager.PASSIVE_PROVIDER
                        )
                        for (provider in providers) {
                            try {
                                if (locationManager.isProviderEnabled(provider)) {
                                    val loc = locationManager.getLastKnownLocation(provider)
                                    if (loc != null) {
                                        lat = loc.latitude
                                        lon = loc.longitude
                                        hasGpsLocation = true
                                        break
                                    }
                                }
                            } catch (e: SecurityException) {
                                // Ignore permission races
                            }
                        }
                    }

                    if (hasGpsLocation) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            val address = addresses?.firstOrNull()
                            if (address != null) {
                                val detectedCity = address.locality ?: address.subAdminArea ?: address.adminArea
                                if (!detectedCity.isNullOrBlank()) {
                                    cityName = detectedCity
                                }
                            }
                        } catch (e: Exception) {
                            // Geocoder may fail on offline or emulators
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore location errors
            }

            // 2. If no GPS location, fallback to IP-based Geolocation
            if (!hasGpsLocation) {
                try {
                    val ipUrl = URL("https://ipapi.co/json/")
                    val conn = ipUrl.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    if (conn.responseCode == 200) {
                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        if (json.has("latitude") && json.has("longitude")) {
                            lat = json.optDouble("latitude", lat)
                            lon = json.optDouble("longitude", lon)
                            val city = json.optString("city", "")
                            if (city.isNotBlank()) cityName = city
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to default
                }
            }

            // 3. Query Open-Meteo Weather API
            try {
                val weatherUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code&timezone=auto")
                val conn = weatherUrl.openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val currentJson = json.optJSONObject("current")
                    if (currentJson != null) {
                        val temp = currentJson.optDouble("temperature_2m", 22.0).toInt()
                        val apparentTemp = currentJson.optDouble("apparent_temperature", temp.toDouble()).toInt()
                        val humidity = currentJson.optInt("relative_humidity_2m", 60)
                        val isDay = currentJson.optInt("is_day", 1) == 1
                        val weatherCode = currentJson.optInt("weather_code", 1)
                        val condition = getWeatherConditionText(weatherCode, isDay)

                        val weather = WeatherData(
                            cityName = cityName,
                            temperature = temp,
                            apparentTemperature = apparentTemp,
                            humidity = humidity,
                            conditionText = condition,
                            weatherCode = weatherCode,
                            isDay = isDay,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis()
                        )
                        _uiState.update { it.copy(weatherData = weather) }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Weather query failed
            }

            // If fetch failed, use cached or fallback
            _uiState.update {
                it.copy(
                    weatherData = it.weatherData?.copy(isLoading = false) ?: WeatherData(
                        cityName = cityName,
                        temperature = 22,
                        conditionText = "Parcialmente nublado",
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun fetchQuotes(forceRefresh: Boolean = false) {
        val current = _uiState.value.quotesData
        if (!forceRefresh && current != null && (System.currentTimeMillis() - current.lastUpdated < 5 * 60 * 1000)) {
            return
        }

        _uiState.update {
            it.copy(quotesData = it.quotesData?.copy(isLoading = true) ?: QuotesData(isLoading = true))
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://economia.awesomeapi.com.br/last/USD-BRL,EUR-BRL,BTC-BRL")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val items = mutableListOf<QuoteItem>()

                    if (json.has("USDBRL")) {
                        val obj = json.getJSONObject("USDBRL")
                        val bid = obj.optDouble("bid", 5.60)
                        val pct = obj.optDouble("pctChange", 0.0)
                        val formattedVal = String.format(Locale.US, "R$ %.2f", bid).replace('.', ',')
                        val formattedPct = String.format(Locale.US, "%+.2f%%", pct).replace('.', ',')
                        items.add(QuoteItem("USD", "Dólar", formattedVal, formattedPct, pct >= 0))
                    }

                    if (json.has("EURBRL")) {
                        val obj = json.getJSONObject("EURBRL")
                        val bid = obj.optDouble("bid", 6.15)
                        val pct = obj.optDouble("pctChange", 0.0)
                        val formattedVal = String.format(Locale.US, "R$ %.2f", bid).replace('.', ',')
                        val formattedPct = String.format(Locale.US, "%+.2f%%", pct).replace('.', ',')
                        items.add(QuoteItem("EUR", "Euro", formattedVal, formattedPct, pct >= 0))
                    }

                    if (json.has("BTCBRL")) {
                        val obj = json.getJSONObject("BTCBRL")
                        val bid = obj.optDouble("bid", 355000.0)
                        val pct = obj.optDouble("pctChange", 0.0)
                        val valText = if (bid >= 1000) {
                            String.format(Locale.US, "R$ %.0fk", bid / 1000.0).replace('.', ',')
                        } else {
                            String.format(Locale.US, "R$ %.0f", bid)
                        }
                        val formattedPct = String.format(Locale.US, "%+.2f%%", pct).replace('.', ',')
                        items.add(QuoteItem("BTC", "Bitcoin", valText, formattedPct, pct >= 0))
                    }

                    if (items.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                quotesData = QuotesData(
                                    items = items,
                                    isLoading = false,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Ignore network error
            }

            _uiState.update {
                it.copy(
                    quotesData = it.quotesData?.copy(isLoading = false) ?: QuotesData(
                        items = listOf(
                            QuoteItem("USD", "Dólar", "R$ 5,64", "+0,35%", true),
                            QuoteItem("EUR", "Euro", "R$ 6,18", "-0,12%", false),
                            QuoteItem("BTC", "Bitcoin", "R$ 358k", "+1,85%", true)
                        ),
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun dismissQuickSettings() {
        _uiState.update { it.copy(showQuickSettings = false) }
    }

    // Bookmarks Management
    fun addSpeedDialItem(title: String, rawUrl: String) {
        val url = formatInputAsUrl(rawUrl.trim())
        val initialLetter = title.firstOrNull()?.uppercase() ?: "W"
        val lowerUrl = url.lowercase()

        val detectedIconRes: Int? = when {
            lowerUrl.contains("google.com") -> com.tessera.browser.R.drawable.ic_brand_google
            lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be") -> com.tessera.browser.R.drawable.ic_brand_youtube
            lowerUrl.contains("github.com") -> com.tessera.browser.R.drawable.ic_brand_github
            lowerUrl.contains("reddit.com") -> com.tessera.browser.R.drawable.ic_brand_reddit
            lowerUrl.contains("duckduckgo.com") -> com.tessera.browser.R.drawable.ic_brand_duckduckgo
            lowerUrl.contains("wikipedia.org") -> com.tessera.browser.R.drawable.ic_brand_wikipedia
            lowerUrl.contains("twitter.com") || lowerUrl.contains("x.com") -> com.tessera.browser.R.drawable.ic_brand_x_twitter
            else -> null
        }

        val domain = extractDomain(url)
        val iconUrl = if (detectedIconRes == null && domain.isNotBlank()) {
            "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        } else {
            null
        }

        val newItem = SpeedDialItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            url = url,
            iconRes = detectedIconRes,
            iconUrl = iconUrl,
            initial = initialLetter,
            badgeColor = 0xFF2D2420
        )
        _uiState.update {
            it.copy(speedDialItems = it.speedDialItems + newItem)
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host ?: ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            url.replace("https://", "").replace("http://", "").split("/").firstOrNull() ?: ""
        }
    }

    fun removeSpeedDialItem(id: String) {
        _uiState.update {
            it.copy(speedDialItems = it.speedDialItems.filterNot { item -> item.id == id })
        }
    }

    private fun formatInputAsUrl(rawInput: String): String {
        return when {
            rawInput.startsWith("http://") || rawInput.startsWith("https://") -> rawInput
            rawInput.contains(".") && !rawInput.contains(" ") -> "https://$rawInput"
            else -> "https://duckduckgo.com/?q=${rawInput.replace(" ", "+")}"
        }
    }
}
