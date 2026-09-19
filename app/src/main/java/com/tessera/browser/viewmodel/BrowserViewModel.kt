package com.tessera.browser.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.DownloadItem
import com.tessera.browser.data.DownloadNotice
import com.tessera.browser.data.DownloadStatus
import com.tessera.browser.data.PageErrorInfo
import com.tessera.browser.data.SafeBrowsingThreatInfo
import com.tessera.browser.data.SavedPageItem
import com.tessera.browser.data.SearchEngine
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.data.WallpaperTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

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
import java.io.OutputStreamWriter
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
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isPinned: Boolean = false,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("url", url)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(json: JSONObject): HistoryEntry = HistoryEntry(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", ""),
            url = json.optString("url", ""),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }
}

enum class ReaderBlockType {
    H1, H2, H3, PARAGRAPH, BLOCKQUOTE, IMAGE
}

data class ReaderBlock(
    val type: ReaderBlockType,
    val text: String = "",
    val imageUrl: String? = null,
    val caption: String? = null
)

enum class ReaderTheme {
    LIGHT, SEPIA, DARK, AMOLED
}

enum class ReaderFontFamily {
    SERIF, SANS_SERIF, MONOSPACE
}

data class ReaderArticle(
    val title: String,
    val author: String? = null,
    val publishDate: String? = null,
    val domain: String = "",
    val readingTimeMinutes: Int = 1,
    val blocks: List<ReaderBlock> = emptyList(),
    val plainText: String = ""
)

data class BrowserUiState(
    val isHomePage: Boolean = true,
    val currentUrl: String = "https://duckduckgo.com",
    val displayUrl: String = "",
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isBarVisible: Boolean = true,
    val showQuickSettings: Boolean = false,
    val isIncognitoMode: Boolean = false,
    val isReaderModeActive: Boolean = false,
    val isReaderModeAvailable: Boolean = false,
    val readerArticle: ReaderArticle? = null,
    val readerFontSizeSp: Int = 18,
    val readerTheme: ReaderTheme = ReaderTheme.SEPIA,
    val readerFontFamily: ReaderFontFamily = ReaderFontFamily.SERIF,
    val readerShowImages: Boolean = false,
    val isReaderTtsPlaying: Boolean = false,
    val isReaderSettingsOpen: Boolean = false,

    // Recursos Avançados (Chrome, Opera & Arc)
    val isDesktopMode: Boolean = false,
    val cookieBlockerEnabled: Boolean = true,
    val isInFullscreenVideo: Boolean = false,
    val showFindInPage: Boolean = false,
    val findQuery: String = "",
    val findMatchIndex: Int = 0,
    val findMatchCount: Int = 0,
    val peekUrl: String? = null,
    val peekTitle: String? = null,
    val showPeekModal: Boolean = false,

    // Multi-tabs
    val tabs: List<BrowserTab> = listOf(
        BrowserTab(id = "default-tab", url = "https://duckduckgo.com", title = "Início", isHomePage = true)
    ),
    val activeTabId: String = "default-tab",
    val showTabsModal: Boolean = false,

    // History, Bookmarks & Downloads
    val history: List<HistoryEntry> = emptyList(),
    val showHistoryModal: Boolean = false,
    val activeHubTab: Int = 0, // 0 = Favoritos, 1 = Histórico, 2 = Downloads, 3 = Salvos
    val downloads: List<DownloadItem> = emptyList(),
    val savedPages: List<SavedPageItem> = emptyList(),

    // Modal de Compartilhamento QR Code
    val showQrCodeModal: Boolean = false,

    // Google Safe Browsing Ameaça
    val safeBrowsingThreat: SafeBrowsingThreatInfo? = null,

    // Quick AI Actions Modal
    val showAiActionModal: Boolean = false,

    // Arc Page Summary (IA Gratuita & Efeito Arc)
    val showArcSummary: Boolean = false,
    val isGeneratingArcSummary: Boolean = false,
    val arcSummaryContent: String? = null,
    val arcSummaryTitle: String = "",
    val arcSummaryDomain: String = "",
    val arcSummaryReadTimeSaved: Int = 1,
    val arcSummaryError: String? = null,

    // AdBlocker
    val adBlockEnabled: Boolean = true,

    // Mecanismo de Busca
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,

    // Notificação Visual de Download
    val activeDownloadNotice: DownloadNotice? = null,

    // Erro Nativo de Página / Offline
    val pageError: PageErrorInfo? = null,


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
    val weatherData: WeatherData? = WeatherData(
        cityName = "São Paulo",
        temperature = 24,
        apparentTemperature = 24,
        humidity = 65,
        conditionText = "Parcialmente nublado",
        weatherCode = 1,
        isDay = true,
        isLoading = false
    ),
    val quotesData: QuotesData? = QuotesData(
        items = listOf(
            QuoteItem("USD", "Dólar", "R$ 5,64", "+0,35%", true),
            QuoteItem("EUR", "Euro", "R$ 6,18", "-0,12%", false),
            QuoteItem("BTC", "Bitcoin", "R$ 358k", "+1,85%", true)
        ),
        isLoading = false
    ),

    // Configuração Fácil
    val isDarkMode: Boolean = false,
    val forceDarkPages: Boolean = false,
    val showWallpaper: Boolean = true,
    val selectedWallpaperId: String = "summer_villa",
    val customWallpaperUri: String? = null,
    val showFavoritesBar: Boolean = true,
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
        get() {
            if (selectedWallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()) {
                return WallpaperTheme(
                    id = "custom",
                    name = "Minha Foto",
                    gradientColors = listOf(androidx.compose.ui.graphics.Color(0xFF1E1E24), androidx.compose.ui.graphics.Color(0xFF121214)),
                    accentColor = androidx.compose.ui.graphics.Color(0xFF00E5FF)
                )
            }
            return AvailableWallpapers.find { it.id == selectedWallpaperId }
                ?: AvailableWallpapers.first()
        }

    val isCurrentPageBookmarked: Boolean
        get() = speedDialItems.any { it.url.equals(displayUrl, ignoreCase = true) || it.url.equals(currentUrl, ignoreCase = true) }
}

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var suggestionJob: Job? = null
    private var appContext: Context? = null

    fun openUrl(rawInput: String) {
        stopReaderTts()
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
                isReaderModeActive = false,
                isReaderModeAvailable = false,
                readerArticle = null,
                isReaderSettingsOpen = false,
                tabs = updatedTabs,
                searchSuggestions = emptyList()
            )
        }
    }

    fun openFromExternal(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return

        val formattedUrl = formatInputAsUrl(trimmed)

        // Dismiss any open modals
        dismissAiActionModal()
        dismissHistoryModal()
        dismissTabsModal()
        dismissQuickSettings()

        _uiState.update { state ->
            val currentTab = state.tabs.find { it.id == state.activeTabId }
            if (currentTab != null && currentTab.isHomePage) {
                // Reutiliza a aba home ativa
                val updatedTabs = state.tabs.map { tab ->
                    if (tab.id == state.activeTabId) {
                        tab.copy(url = formattedUrl, isHomePage = false, title = extractDomain(formattedUrl))
                    } else tab
                }
                state.copy(
                    isHomePage = false,
                    currentUrl = formattedUrl,
                    displayUrl = formattedUrl,
                    isBarVisible = true,
                    isReaderModeActive = false,
                    isReaderModeAvailable = false,
                    tabs = updatedTabs,
                    searchSuggestions = emptyList()
                )
            } else {
                // Abre em uma nova aba
                val newId = UUID.randomUUID().toString()
                val newTab = BrowserTab(
                    id = newId,
                    url = formattedUrl,
                    title = extractDomain(formattedUrl),
                    isHomePage = false
                )
                state.copy(
                    tabs = state.tabs + newTab,
                    activeTabId = newId,
                    isHomePage = false,
                    currentUrl = formattedUrl,
                    displayUrl = formattedUrl,
                    canGoBack = false,
                    canGoForward = false,
                    isBarVisible = true,
                    isReaderModeActive = false,
                    isReaderModeAvailable = false,
                    searchSuggestions = emptyList()
                )
            }
        }
    }

    fun openAiQuery(query: String) {
        val trimmed = query.trim()
        val aiUrl = if (trimmed.isNotBlank()) {
            "https://duck.ai/?q=${URLEncoder.encode(trimmed, "UTF-8")}"
        } else {
            "https://duck.ai/"
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
                    tab.copy(isHomePage = true, url = state.searchEngine.homeUrl)
                } else tab
            }
            state.copy(
                isHomePage = true,
                progress = 0f,
                tabs = updatedTabs,
                searchSuggestions = emptyList(),
                pageError = null
            )
        }
    }

    fun onPageStarted(url: String?) {
        stopReaderTts()
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
                    tabs = updatedTabs,
                    isReaderModeActive = false,
                    readerArticle = null,
                    isReaderSettingsOpen = false,
                    pageError = null
                )
            }
        }
    }

    fun setPageError(error: PageErrorInfo) {
        _uiState.update { it.copy(pageError = error) }
    }

    fun clearPageError() {
        _uiState.update { it.copy(pageError = null) }
    }


    fun onPageFinished(url: String?, canBack: Boolean, canForward: Boolean = false, title: String? = null) {
        val effectiveUrl = url ?: _uiState.value.currentUrl
        val effectiveTitle = if (!title.isNullOrBlank()) title else extractDomain(effectiveUrl)

        val previousHistory = _uiState.value.history
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        url = effectiveUrl,
                        title = effectiveTitle,
                        canGoBack = canBack,
                        canGoForward = canForward,
                        isHomePage = false
                    )
                } else tab
            }

            // Register in history if viewing a website and not in incognito mode
            val newHistory = if (!state.isIncognitoMode && !state.isHomePage && effectiveUrl.isNotBlank() && effectiveUrl.startsWith("http")) {
                val entry = HistoryEntry(title = effectiveTitle, url = effectiveUrl)
                (listOf(entry) + state.history.filterNot { it.url == effectiveUrl }).take(100)
            } else {
                state.history
            }

            state.copy(
                progress = 0f,
                canGoBack = canBack,
                canGoForward = canForward,
                currentUrl = effectiveUrl,
                displayUrl = effectiveUrl,
                tabs = updatedTabs,
                history = newHistory
            )
        }

        if (_uiState.value.history !== previousHistory) {
            saveHistory()
        }
    }

    fun toggleIncognitoMode() {
        _uiState.update { it.copy(isIncognitoMode = !it.isIncognitoMode) }
    }

    fun toggleReaderMode() {
        if (_uiState.value.isReaderModeActive) {
            closeReaderMode()
        } else {
            _uiState.update { it.copy(isReaderModeActive = true) }
        }
    }

    fun setReaderModeAvailable(available: Boolean) {
        _uiState.update { it.copy(isReaderModeAvailable = available) }
    }

    fun setReaderModeActive(active: Boolean) {
        if (!active) {
            stopReaderTts()
        }
        _uiState.update { it.copy(isReaderModeActive = active) }
    }

    fun setReaderArticle(article: ReaderArticle?) {
        _uiState.update {
            it.copy(
                readerArticle = article,
                isReaderModeActive = article != null
            )
        }
    }

    fun closeReaderMode() {
        stopReaderTts()
        _uiState.update {
            it.copy(
                isReaderModeActive = false,
                isReaderSettingsOpen = false
            )
        }
    }

    fun updateReaderFontSize(delta: Int) {
        _uiState.update {
            val newSize = (it.readerFontSizeSp + delta).coerceIn(14, 32)
            it.copy(readerFontSizeSp = newSize)
        }
        saveSettings()
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _uiState.update { it.copy(readerTheme = theme) }
        saveSettings()
    }

    fun setReaderFontFamily(family: ReaderFontFamily) {
        _uiState.update { it.copy(readerFontFamily = family) }
        saveSettings()
    }

    fun toggleReaderShowImages() {
        _uiState.update { it.copy(readerShowImages = !it.readerShowImages) }
        saveSettings()
    }

    fun toggleReaderSettings() {
        _uiState.update { it.copy(isReaderSettingsOpen = !it.isReaderSettingsOpen) }
    }

    // TTS (Text to Speech)
    private var tts: TextToSpeech? = null
    private var isTtsInitialized: Boolean = false

    private fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    try {
                        tts?.language = Locale.getDefault()
                    } catch (e: Exception) {
                        Log.e("BrowserViewModel", "Erro ao configurar idioma TTS", e)
                    }
                }
            }
        }
    }

    fun toggleReaderTts(context: Context) {
        val article = _uiState.value.readerArticle ?: return
        if (_uiState.value.isReaderTtsPlaying) {
            stopReaderTts()
        } else {
            startReaderTts(context, article)
        }
    }

    fun startReaderTts(context: Context, article: ReaderArticle) {
        initTts(context)
        val textToRead = buildString {
            append(article.title)
            append(". ")
            if (!article.author.isNullOrBlank()) {
                append("Por ").append(article.author).append(". ")
            }
            append(article.plainText)
        }
        if (textToRead.isBlank()) return

        _uiState.update { it.copy(isReaderTtsPlaying = true) }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                tts?.stop()
                val chunks = textToRead.chunked(3000)
                chunks.forEachIndexed { index, chunk ->
                    val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    tts?.speak(chunk, queueMode, null, "reader_chunk_$index")
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        val lastId = "reader_chunk_${chunks.size - 1}"
                        if (utteranceId == lastId) {
                            _uiState.update { it.copy(isReaderTtsPlaying = false) }
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        _uiState.update { it.copy(isReaderTtsPlaying = false) }
                    }
                })
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro no TTS", e)
                _uiState.update { it.copy(isReaderTtsPlaying = false) }
            }
        }
    }

    fun stopReaderTts() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("BrowserViewModel", "Erro ao parar TTS", e)
        }
        _uiState.update { it.copy(isReaderTtsPlaying = false) }
    }

    fun speakText(context: Context, text: String) {
        initTts(context)
        val clean = text.replace("**", "").replace(Regex("^#+\\s*", RegexOption.MULTILINE), "").trim()
        if (clean.isBlank()) return

        _uiState.update { it.copy(isReaderTtsPlaying = true) }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                tts?.stop()
                val chunks = clean.chunked(3000)
                chunks.forEachIndexed { index, chunk ->
                    val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    tts?.speak(chunk, queueMode, null, "summary_chunk_$index")
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        val lastId = "summary_chunk_${chunks.size - 1}"
                        if (utteranceId == lastId) {
                            _uiState.update { it.copy(isReaderTtsPlaying = false) }
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        _uiState.update { it.copy(isReaderTtsPlaying = false) }
                    }
                })
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro no TTS", e)
                _uiState.update { it.copy(isReaderTtsPlaying = false) }
            }
        }
    }

    fun togglePinTab(tabId: String) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == tabId) tab.copy(isPinned = !tab.isPinned) else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    fun closeAllTabs() {
        val pinnedTabs = _uiState.value.tabs.filter { it.isPinned }
        if (pinnedTabs.isNotEmpty()) {
            _uiState.update { state ->
                val first = pinnedTabs.first()
                state.copy(
                    tabs = pinnedTabs,
                    activeTabId = first.id,
                    isHomePage = first.isHomePage,
                    currentUrl = first.url,
                    displayUrl = if (first.isHomePage) "" else first.url,
                    showTabsModal = false
                )
            }
        } else {
            val newId = UUID.randomUUID().toString()
            val defaultTab = BrowserTab(id = newId, isHomePage = true)
            _uiState.update { state ->
                state.copy(
                    tabs = listOf(defaultTab),
                    activeTabId = newId,
                    isHomePage = true,
                    currentUrl = "https://duckduckgo.com",
                    displayUrl = "",
                    showTabsModal = false
                )
            }
        }
    }

    fun setCustomWallpaperUri(uri: String?) {
        _uiState.update { it.copy(customWallpaperUri = uri, showWallpaper = true) }
        saveSettings()
    }

    fun importCustomWallpaper(context: Context, sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@launch
                // Remove previous persistent custom wallpapers to save space
                context.filesDir.listFiles { _, name -> name.startsWith("custom_wallpaper_") }?.forEach { it.delete() }

                val file = File(context.filesDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                val persistentUriString = Uri.fromFile(file).toString()
                _uiState.update {
                    it.copy(
                        customWallpaperUri = persistentUriString,
                        selectedWallpaperId = "custom",
                        showWallpaper = true
                    )
                }
                saveSettings()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao importar papel de parede personalizado", e)
            }
        }
    }

    fun clearCustomWallpaper() {
        _uiState.update {
            it.copy(
                customWallpaperUri = null,
                selectedWallpaperId = AvailableWallpapers.first().id
            )
        }
        saveSettings()
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
        val now = System.currentTimeMillis()
        _uiState.update { state ->
            val updatedTabs = state.tabs.map {
                if (it.id == tabId) it.copy(lastAccessedTimestamp = now) else it
            }
            state.copy(
                tabs = updatedTabs,
                activeTabId = tabId,
                isHomePage = targetTab.isHomePage,
                currentUrl = targetTab.url,
                displayUrl = if (targetTab.isHomePage) "" else targetTab.url,
                canGoBack = targetTab.canGoBack,
                showTabsModal = false
            )
        }
    }

    fun selectNextTab() {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) return
        val currentIndex = currentTabs.indexOfFirst { it.id == _uiState.value.activeTabId }
        val nextIndex = if (currentIndex == -1 || currentIndex >= currentTabs.size - 1) 0 else currentIndex + 1
        selectTab(currentTabs[nextIndex].id)
    }

    fun selectPreviousTab() {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) return
        val currentIndex = currentTabs.indexOfFirst { it.id == _uiState.value.activeTabId }
        val prevIndex = if (currentIndex <= 0) currentTabs.size - 1 else currentIndex - 1
        selectTab(currentTabs[prevIndex].id)
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

    fun archiveInactiveTabs(thresholdHours: Long = 24) {
        val now = System.currentTimeMillis()
        val thresholdMillis = thresholdHours * 60 * 60 * 1000L
        _uiState.update { state ->
            val remainingTabs = state.tabs.filter { tab ->
                tab.id == state.activeTabId || tab.isPinned || (now - tab.lastAccessedTimestamp) < thresholdMillis
            }
            if (remainingTabs.isEmpty()) {
                val newId = UUID.randomUUID().toString()
                state.copy(
                    tabs = listOf(BrowserTab(id = newId, isHomePage = true)),
                    activeTabId = newId,
                    isHomePage = true
                )
            } else {
                state.copy(tabs = remainingTabs)
            }
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
            saveBookmarks()
        } else {
            addSpeedDialItem(title.ifBlank { extractDomain(targetUrl) }, targetUrl)
        }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
        saveHistory()
    }

    fun toggleHistoryModal() {
        _uiState.update { it.copy(showHistoryModal = !it.showHistoryModal) }
    }

    fun openHistoryModal(initialTab: Int = 0) {
        _uiState.update { it.copy(showHistoryModal = true, activeHubTab = initialTab) }
    }

    fun openDownloadsModal() {
        _uiState.update { it.copy(showHistoryModal = true, activeHubTab = 2) }
    }

    fun setActiveHubTab(tab: Int) {
        _uiState.update { it.copy(activeHubTab = tab) }
    }

    fun dismissHistoryModal() {
        _uiState.update { it.copy(showHistoryModal = false) }
    }

    fun openSavedPagesModal() {
        _uiState.update { it.copy(showHistoryModal = true, activeHubTab = 3) }
    }

    fun showQrCodeModal() {
        _uiState.update { it.copy(showQrCodeModal = true) }
    }

    fun dismissQrCodeModal() {
        _uiState.update { it.copy(showQrCodeModal = false) }
    }

    fun setSafeBrowsingThreat(threat: SafeBrowsingThreatInfo) {
        _uiState.update { it.copy(safeBrowsingThreat = threat) }
    }

    fun dismissSafeBrowsingThreat() {
        _uiState.update { it.copy(safeBrowsingThreat = null) }
    }

    fun addCurrentPageToHomeScreen(context: Context, webView: WebView?) {
        val url = _uiState.value.currentUrl
        if (url.isBlank() || _uiState.value.isHomePage) {
            Toast.makeText(context, "Navegue para uma página antes de adicionar", Toast.LENGTH_SHORT).show()
            return
        }
        val title = webView?.title?.takeIf { it.isNotBlank() }
            ?: _uiState.value.tabs.find { it.id == _uiState.value.activeTabId }?.title
            ?: extractDomain(url)
        val favicon = webView?.favicon
        com.tessera.browser.util.ShortcutHelper.addPinShortcut(context, url, title, favicon)
    }

    fun saveCurrentPageForOffline(context: Context, webView: WebView?) {
        if (webView == null) return
        val targetUrl = _uiState.value.currentUrl
        if (targetUrl.isBlank() || _uiState.value.isHomePage) {
            Toast.makeText(context, "Navegue para uma página antes de salvar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val savedPagesDir = File(context.filesDir, "saved_pages")
            if (!savedPagesDir.exists()) {
                savedPagesDir.mkdirs()
            }
            val file = File(savedPagesDir, "tessera_page_${System.currentTimeMillis()}.mht")
            webView.saveWebArchive(file.absolutePath, false) { path ->
                if (path != null) {
                    val savedFile = File(path)
                    val title = webView.title?.takeIf { it.isNotBlank() } ?: extractDomain(targetUrl)
                    val newItem = SavedPageItem(
                        title = title,
                        url = targetUrl,
                        filePath = path,
                        fileSize = savedFile.length()
                    )
                    _uiState.update { state ->
                        state.copy(savedPages = listOf(newItem) + state.savedPages)
                    }
                    saveSavedPages()
                    Toast.makeText(context, "Página salva para leitura offline!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Não foi possível salvar esta página", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("BrowserViewModel", "Erro ao salvar página offline", e)
            Toast.makeText(context, "Erro ao salvar página: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSavedPage(item: SavedPageItem) {
        val file = File(item.filePath)
        if (!file.exists()) {
            Toast.makeText(appContext, "Arquivo offline não encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        openUrl("file://${item.filePath}")
    }

    fun deleteSavedPage(item: SavedPageItem) {
        try {
            val file = File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.w("BrowserViewModel", "Erro ao deletar arquivo de página offline", e)
        }
        _uiState.update { state ->
            state.copy(savedPages = state.savedPages.filterNot { it.id == item.id })
        }
        saveSavedPages()
    }

    // PERSISTENCE SUBSYSTEM (Bookmarks, History, Settings & Downloads)
    fun initPersistence(context: Context) {
        val app = context.applicationContext
        appContext = app
        initDownloads(app)
        loadPreferences(app)
    }

    // DOWNLOADS SUBSYSTEM
    fun initDownloads(context: Context) {
        loadDownloadsFromPreferences(context)
    }

    fun enqueueDownload(
        context: Context,
        url: String,
        userAgent: String = "",
        contentDisposition: String = "",
        mimeType: String = ""
    ): Long {
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            var effectiveMime = mimeType
            val ext = fileName.substringAfterLast('.', "").lowercase()
            if (ext == "apk") {
                effectiveMime = "application/vnd.android.package-archive"
            } else if (effectiveMime.isBlank() || effectiveMime == "*/*") {
                if (ext.isNotBlank()) {
                    effectiveMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
                }
            }

            // Garante que o diretório Downloads público exista
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
            } catch (e: Exception) {
                Log.w("BrowserViewModel", "Não foi possível verificar/criar pasta de Downloads", e)
            }

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                if (effectiveMime.isNotBlank() && effectiveMime != "*/*") {
                    setMimeType(effectiveMime)
                }
                try {
                    val cookies = CookieManager.getInstance().getCookie(url)
                    if (!cookies.isNullOrBlank()) {
                        val cleanCookies = cookies.replace("\n", "").replace("\r", "").trim()
                        if (cleanCookies.isNotEmpty()) {
                            addRequestHeader("cookie", cleanCookies)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BrowserViewModel", "Ignorando erro ao adicionar cookies ao download", e)
                }

                if (userAgent.isNotBlank()) {
                    try {
                        val cleanUa = userAgent.replace("\n", "").replace("\r", "").trim()
                        if (cleanUa.isNotEmpty()) {
                            addRequestHeader("User-Agent", cleanUa)
                        }
                    } catch (e: Exception) {
                        Log.w("BrowserViewModel", "Ignorando erro ao adicionar User-Agent ao download", e)
                    }
                }

                setDescription("Baixando com Tessera Browser...")
                setTitle(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val targetFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            val item = DownloadItem(
                id = downloadId,
                fileName = fileName,
                url = url,
                mimeType = if (effectiveMime.isNotBlank()) effectiveMime else "*/*",
                filePath = targetFile.absolutePath,
                status = DownloadStatus.RUNNING,
                timestamp = System.currentTimeMillis()
            )

            val notice = DownloadNotice(
                id = downloadId,
                fileName = fileName,
                status = DownloadStatus.RUNNING,
                message = "Iniciando download..."
            )

            _uiState.update { state ->
                val updated = listOf(item) + state.downloads.filterNot { it.id == downloadId }
                state.copy(downloads = updated, activeDownloadNotice = notice)
            }
            saveDownloadsToPreferences(context)
            return downloadId
        } catch (e: Exception) {
            Log.e("BrowserViewModel", "Erro ao enfileirar download", e)
            return -1L
        }
    }

    fun onDownloadCompleted(context: Context, downloadId: Long) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val bytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val fileUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                val statusVal = if (statusIdx != -1) cursor.getInt(statusIdx) else -1
                val totalBytes = if (bytesIdx != -1) cursor.getLong(bytesIdx) else -1L
                val localUri = if (fileUriIdx != -1) cursor.getString(fileUriIdx) else null

                val isSuccess = statusVal == DownloadManager.STATUS_SUCCESSFUL
                val status = if (isSuccess) DownloadStatus.SUCCESSFUL else DownloadStatus.FAILED

                cursor.close()

                val completedItem = _uiState.value.downloads.find { it.id == downloadId }
                val notice = DownloadNotice(
                    id = downloadId,
                    fileName = completedItem?.fileName ?: "Arquivo",
                    status = status,
                    message = if (isSuccess) "Download concluído com sucesso!" else "Falha no download"
                )

                _uiState.update { state ->
                    val updated = state.downloads.map { item ->
                        if (item.id == downloadId) {
                            val resolvedPath = if (localUri != null && localUri.startsWith("file://")) {
                                Uri.parse(localUri).path ?: item.filePath
                            } else item.filePath
                            item.copy(
                                status = status,
                                totalBytes = if (totalBytes > 0) totalBytes else item.totalBytes,
                                filePath = resolvedPath
                            )
                        } else item
                    }
                    state.copy(downloads = updated, activeDownloadNotice = notice)
                }
                saveDownloadsToPreferences(context)
            }
        } catch (e: Exception) {
            Log.e("BrowserViewModel", "Erro ao processar download completado", e)
        }
    }

    fun openDownloadedFile(context: Context, item: DownloadItem) {
        try {
            val file = if (item.filePath != null) File(item.filePath) else null
            if (file != null && file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val ext = file.extension.lowercase()
                var resolvedMime = item.mimeType
                if (ext == "apk") {
                    resolvedMime = "application/vnd.android.package-archive"
                } else if (resolvedMime.isBlank() || resolvedMime == "*/*" || resolvedMime == "application/octet-stream") {
                    val fromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    if (!fromExt.isNullOrBlank()) {
                        resolvedMime = fromExt
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, resolvedMime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDownloadedFile(context: Context, item: DownloadItem) {
        try {
            val file = if (item.filePath != null) File(item.filePath) else null
            if (file != null && file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val ext = file.extension.lowercase()
                var resolvedMime = item.mimeType
                if (ext == "apk") {
                    resolvedMime = "application/vnd.android.package-archive"
                } else if (resolvedMime.isBlank() || resolvedMime == "*/*" || resolvedMime == "application/octet-stream") {
                    val fromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    if (!fromExt.isNullOrBlank()) {
                        resolvedMime = fromExt
                    }
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = resolvedMime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, item.fileName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Compartilhar ${item.fileName}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } else {
                Toast.makeText(context, "Arquivo não encontrado para compartilhamento", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeDownload(context: Context?, id: Long) {
        _uiState.update { state ->
            val updated = state.downloads.filterNot { it.id == id }
            state.copy(downloads = updated)
        }
        if (context != null) {
            saveDownloadsToPreferences(context)
        }
    }

    fun clearDownloads(context: Context?) {
        _uiState.update { it.copy(downloads = emptyList(), activeDownloadNotice = null) }
        if (context != null) {
            saveDownloadsToPreferences(context)
        }
    }

    fun dismissDownloadNotice() {
        _uiState.update { it.copy(activeDownloadNotice = null) }
    }

    fun setSearchEngine(engine: SearchEngine) {
        _uiState.update { it.copy(searchEngine = engine) }
        saveSettings()
    }

    fun clearBrowsingData(
        context: Context,
        webView: WebView?,
        clearHistory: Boolean = true,
        clearCookies: Boolean = true,
        clearCache: Boolean = true,
        clearStorage: Boolean = true,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (clearHistory) {
                    _uiState.update { it.copy(history = emptyList()) }
                    saveHistory()
                    webView?.clearHistory()
                }
                if (clearCache) {
                    webView?.clearCache(true)
                    withContext(Dispatchers.IO) {
                        try {
                            context.cacheDir.deleteRecursively()
                        } catch (e: Exception) {
                            Log.w("BrowserViewModel", "Erro ao limpar pasta de cache", e)
                        }
                    }
                }
                if (clearCookies) {
                    try {
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.removeAllCookies {
                            cookieManager.flush()
                        }
                    } catch (e: Exception) {
                        Log.w("BrowserViewModel", "Erro ao limpar cookies", e)
                    }
                }
                if (clearStorage) {
                    try {
                        WebStorage.getInstance().deleteAllData()
                        webView?.clearFormData()
                    } catch (e: Exception) {
                        Log.w("BrowserViewModel", "Erro ao limpar WebStorage", e)
                    }
                }
                onComplete()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro geral ao limpar dados de navegação", e)
                onComplete()
            }
        }
    }


    private fun loadDownloadsFromPreferences(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("tessera_downloads", Context.MODE_PRIVATE)
                val jsonStr = prefs.getString("downloads_list", null)
                if (!jsonStr.isNullOrBlank()) {
                    val jsonArr = JSONArray(jsonStr)
                    val list = mutableListOf<DownloadItem>()
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        list.add(DownloadItem.fromJson(obj))
                    }
                    _uiState.update { it.copy(downloads = list) }
                }
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao carregar downloads", e)
            }
        }
    }

    private fun saveDownloadsToPreferences(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("tessera_downloads", Context.MODE_PRIVATE)
                val jsonArr = JSONArray()
                _uiState.value.downloads.take(60).forEach { item ->
                    jsonArr.put(item.toJson())
                }
                prefs.edit().putString("downloads_list", jsonArr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar downloads", e)
            }
        }
    }

    private fun loadPreferences(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)

                // Bookmarks
                val bookmarksJson = prefs.getString("bookmarks_list", null)
                val loadedBookmarks = if (!bookmarksJson.isNullOrBlank()) {
                    val arr = JSONArray(bookmarksJson)
                    val list = mutableListOf<SpeedDialItem>()
                    for (i in 0 until arr.length()) {
                        list.add(SpeedDialItem.fromJson(arr.getJSONObject(i)))
                    }
                    list
                } else null

                // History
                val historyJson = prefs.getString("history_list", null)
                val loadedHistory = if (!historyJson.isNullOrBlank()) {
                    val arr = JSONArray(historyJson)
                    val list = mutableListOf<HistoryEntry>()
                    for (i in 0 until arr.length()) {
                        list.add(HistoryEntry.fromJson(arr.getJSONObject(i)))
                    }
                    list
                } else null

                // Settings
                val isDark = if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
                val forceDark = if (prefs.contains("force_dark_pages")) prefs.getBoolean("force_dark_pages", false) else null
                val showWallpaper = if (prefs.contains("show_wallpaper")) prefs.getBoolean("show_wallpaper", true) else null
                val selectedWallpaper = prefs.getString("selected_wallpaper_id", null)
                val customWallpaper = prefs.getString("custom_wallpaper_uri", null)
                val showFavorites = if (prefs.contains("show_favorites_bar")) prefs.getBoolean("show_favorites_bar", true) else null
                val tesseraAi = if (prefs.contains("tessera_ai_enabled")) prefs.getBoolean("tessera_ai_enabled", true) else null
                val adBlock = if (prefs.contains("ad_block_enabled")) prefs.getBoolean("ad_block_enabled", true) else null
                val showWeather = if (prefs.contains("show_weather_widget")) prefs.getBoolean("show_weather_widget", true) else null
                val showQuotes = if (prefs.contains("show_quotes_widget")) prefs.getBoolean("show_quotes_widget", true) else null
                val cookieBlocker = if (prefs.contains("cookie_blocker_enabled")) prefs.getBoolean("cookie_blocker_enabled", true) else null
                val isDesktop = if (prefs.contains("is_desktop_mode")) prefs.getBoolean("is_desktop_mode", false) else null
                val searchEngineId = prefs.getString("search_engine_id", null)
                val searchEngine = if (searchEngineId != null) SearchEngine.fromId(searchEngineId) else null

                // Reader Mode Preferences
                val readerFontSize = if (prefs.contains("reader_font_size")) prefs.getInt("reader_font_size", 18) else null
                val readerThemeName = prefs.getString("reader_theme", null)
                val readerTheme = if (readerThemeName != null) {
                    try { ReaderTheme.valueOf(readerThemeName) } catch (e: Exception) { null }
                } else null
                val readerFontName = prefs.getString("reader_font_family", null)
                val readerFont = if (readerFontName != null) {
                    try { ReaderFontFamily.valueOf(readerFontName) } catch (e: Exception) { null }
                } else null
                val readerShowImages = if (prefs.contains("reader_show_images")) prefs.getBoolean("reader_show_images", false) else null

                // Saved Pages (Offline)
                val savedPagesJson = prefs.getString("saved_pages_list", null)
                val loadedSavedPages = if (!savedPagesJson.isNullOrBlank()) {
                    val arr = JSONArray(savedPagesJson)
                    val list = mutableListOf<SavedPageItem>()
                    for (i in 0 until arr.length()) {
                        list.add(SavedPageItem.fromJson(arr.getJSONObject(i)))
                    }
                    list
                } else null

                _uiState.update { current ->
                    current.copy(
                        speedDialItems = loadedBookmarks ?: current.speedDialItems,
                        history = loadedHistory ?: current.history,
                        savedPages = loadedSavedPages ?: current.savedPages,
                        isDarkMode = isDark ?: current.isDarkMode,
                        forceDarkPages = forceDark ?: current.forceDarkPages,
                        showWallpaper = showWallpaper ?: current.showWallpaper,
                        selectedWallpaperId = selectedWallpaper ?: current.selectedWallpaperId,
                        customWallpaperUri = customWallpaper ?: current.customWallpaperUri,
                        showFavoritesBar = showFavorites ?: current.showFavoritesBar,
                        tesseraAiEnabled = tesseraAi ?: current.tesseraAiEnabled,
                        adBlockEnabled = adBlock ?: current.adBlockEnabled,
                        cookieBlockerEnabled = cookieBlocker ?: current.cookieBlockerEnabled,
                        isDesktopMode = isDesktop ?: current.isDesktopMode,
                        showWeatherWidget = showWeather ?: current.showWeatherWidget,
                        showQuotesWidget = showQuotes ?: current.showQuotesWidget,
                        searchEngine = searchEngine ?: current.searchEngine,
                        readerFontSizeSp = readerFontSize ?: current.readerFontSizeSp,
                        readerTheme = readerTheme ?: current.readerTheme,
                        readerFontFamily = readerFont ?: current.readerFontFamily,
                        readerShowImages = readerShowImages ?: current.readerShowImages
                    )
                }
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao carregar preferências", e)
            }
        }
    }

    private fun saveSavedPages() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.savedPages.forEach { item ->
                    arr.put(item.toJson())
                }
                prefs.edit().putString("saved_pages_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar páginas offline", e)
            }
        }
    }

    private fun saveBookmarks() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.speedDialItems.forEach { item ->
                    arr.put(item.toJson())
                }
                prefs.edit().putString("bookmarks_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar favoritos", e)
            }
        }
    }

    private fun saveHistory() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.history.take(100).forEach { entry ->
                    arr.put(entry.toJson())
                }
                prefs.edit().putString("history_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar histórico", e)
            }
        }
    }

    private fun saveSettings() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val s = _uiState.value
                prefs.edit()
                    .putBoolean("is_dark_mode", s.isDarkMode)
                    .putBoolean("force_dark_pages", s.forceDarkPages)
                    .putBoolean("show_wallpaper", s.showWallpaper)
                    .putString("selected_wallpaper_id", s.selectedWallpaperId)
                    .putString("custom_wallpaper_uri", s.customWallpaperUri)
                    .putBoolean("show_favorites_bar", s.showFavoritesBar)
                    .putBoolean("tessera_ai_enabled", s.tesseraAiEnabled)
                    .putBoolean("ad_block_enabled", s.adBlockEnabled)
                    .putBoolean("cookie_blocker_enabled", s.cookieBlockerEnabled)
                    .putBoolean("is_desktop_mode", s.isDesktopMode)
                    .putBoolean("show_weather_widget", s.showWeatherWidget)
                    .putBoolean("show_quotes_widget", s.showQuotesWidget)
                    .putString("search_engine_id", s.searchEngine.id)
                    .putInt("reader_font_size", s.readerFontSizeSp)
                    .putString("reader_theme", s.readerTheme.name)
                    .putString("reader_font_family", s.readerFontFamily.name)
                    .putBoolean("reader_show_images", s.readerShowImages)
                    .apply()

            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar configurações", e)
            }
        }
    }

    // ADVANCED BROWSER FEATURES (Chrome, Opera, Arc)
    fun toggleDesktopMode() {
        _uiState.update { it.copy(isDesktopMode = !it.isDesktopMode) }
        saveSettings()
    }

    fun setDesktopMode(enabled: Boolean) {
        _uiState.update { it.copy(isDesktopMode = enabled) }
        saveSettings()
    }

    fun toggleCookieBlocker() {
        _uiState.update { it.copy(cookieBlockerEnabled = !it.cookieBlockerEnabled) }
        saveSettings()
    }

    fun setCookieBlockerEnabled(enabled: Boolean) {
        _uiState.update { it.copy(cookieBlockerEnabled = enabled) }
        saveSettings()
    }

    fun setFullscreenVideo(active: Boolean) {
        _uiState.update { it.copy(isInFullscreenVideo = active) }
    }

    fun openFindInPage() {
        _uiState.update { it.copy(showFindInPage = true) }
    }

    fun closeFindInPage() {
        _uiState.update {
            it.copy(
                showFindInPage = false,
                findQuery = "",
                findMatchIndex = 0,
                findMatchCount = 0
            )
        }
    }

    fun updateFindQuery(query: String) {
        _uiState.update { it.copy(findQuery = query) }
    }

    fun updateFindResults(activeMatchOrdinal: Int, numberOfMatches: Int) {
        _uiState.update {
            it.copy(
                findMatchIndex = if (numberOfMatches > 0) activeMatchOrdinal else 0,
                findMatchCount = numberOfMatches
            )
        }
    }

    fun showPeekModal(url: String, title: String? = null) {
        _uiState.update {
            it.copy(
                peekUrl = url,
                peekTitle = title,
                showPeekModal = true
            )
        }
    }

    fun dismissPeekModal() {
        _uiState.update {
            it.copy(
                showPeekModal = false,
                peekUrl = null,
                peekTitle = null
            )
        }
    }

    fun browseForMe(query: String) {
        // Deprecated - Navegue por mim removido da barra
    }

    // ARC PAGE SUMMARY (IA GRATUITA & EFEITO VISUAL ARC)
    private var arcSummaryJob: Job? = null
    private var lastSummaryTitle: String = ""
    private var lastSummaryDomain: String = ""
    private var lastSummaryContent: String = ""

    fun requestArcSummary(title: String, domain: String, content: String) {
        lastSummaryTitle = title
        lastSummaryDomain = domain
        lastSummaryContent = content

        val wordCount = content.split("\\s+".toRegex()).size
        val savedMinutes = maxOf(1, wordCount / 180)

        _uiState.update {
            it.copy(
                showArcSummary = true,
                isGeneratingArcSummary = true,
                arcSummaryTitle = title.ifBlank { "Página Atual" },
                arcSummaryDomain = domain,
                arcSummaryReadTimeSaved = savedMinutes,
                arcSummaryContent = null,
                arcSummaryError = null,
                showAiActionModal = false
            )
        }

        arcSummaryJob?.cancel()
        arcSummaryJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanContent = if (content.length > 6000) {
                    content.take(6000) + "..."
                } else {
                    content
                }

                val systemPrompt = "Você é a inteligência artificial do navegador Tessera estilo Arc Search. Resuma a página com alta precisão, elegância e foco no essencial em Português do Brasil.\n" +
                        "Estrutura obrigatória:\n" +
                        "1. Um parágrafo curto de Visão Geral (2 linhas).\n" +
                        "2. De 3 a 5 pontos-chave principais, cada um iniciado por '-' e um emoji adequado (ex: '- 💡 Ponto importante...').\n" +
                        "3. Uma conclusão curta e objetiva (1 a 2 linhas).\n" +
                        "Evite enrolação. Use Markdown limpo."

                val userPrompt = "Título: $title\nDomínio: $domain\n\nConteúdo da página:\n$cleanContent"

                val messagesArr = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }

                val reqJson = JSONObject().apply {
                    put("messages", messagesArr)
                    put("model", "openai")
                    put("seed", 42)
                }

                val url = URL("https://text.pollinations.ai/")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 12000
                    readTimeout = 25000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("User-Agent", "TesseraBrowser/1.3.0")
                }

                val writer = OutputStreamWriter(conn.outputStream, "UTF-8")
                writer.write(reqJson.toString())
                writer.flush()
                writer.close()

                val respCode = conn.responseCode
                if (respCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val response = reader.readText()
                    reader.close()

                    if (response.isNotBlank()) {
                        _uiState.update {
                            it.copy(
                                isGeneratingArcSummary = false,
                                arcSummaryContent = response.trim(),
                                arcSummaryError = null
                            )
                        }
                    } else {
                        throw Exception("Resposta vazia da IA")
                    }
                } else {
                    throw Exception("Erro HTTP $respCode")
                }
            } catch (e: Exception) {
                Log.e("TesseraBrowser", "Falha ao gerar resumo Arc com IA", e)
                _uiState.update {
                    it.copy(
                        isGeneratingArcSummary = false,
                        arcSummaryError = "Não foi possível conectar ao assistente de IA. Verifique sua conexão e tente novamente."
                    )
                }
            }
        }
    }

    fun retryArcSummary() {
        if (lastSummaryContent.isNotBlank()) {
            requestArcSummary(lastSummaryTitle, lastSummaryDomain, lastSummaryContent)
        }
    }

    fun dismissArcSummary() {
        arcSummaryJob?.cancel()
        stopReaderTts()
        _uiState.update {
            it.copy(
                showArcSummary = false,
                isGeneratingArcSummary = false
            )
        }
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
                val suggestUrl = _uiState.value.searchEngine.buildSuggestUrl(trimmed)
                val url = URL(suggestUrl)
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
        saveSettings()
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        saveSettings()
    }

    fun setForceDarkPages(enabled: Boolean) {
        _uiState.update { it.copy(forceDarkPages = enabled) }
        saveSettings()
    }

    fun setShowWallpaper(enabled: Boolean) {
        _uiState.update { it.copy(showWallpaper = enabled) }
        saveSettings()
    }

    fun selectWallpaper(wallpaperId: String) {
        _uiState.update { it.copy(selectedWallpaperId = wallpaperId, showWallpaper = true) }
        saveSettings()
    }

    fun setShowFavoritesBar(enabled: Boolean) {
        _uiState.update { it.copy(showFavoritesBar = enabled) }
        saveSettings()
    }

    fun setTesseraAiEnabled(enabled: Boolean) {
        _uiState.update { it.copy(tesseraAiEnabled = enabled) }
        saveSettings()
    }

    fun setAiToolbarButton(enabled: Boolean) {
        _uiState.update { it.copy(aiToolbarButton = enabled) }
        saveSettings()
    }

    fun setAiTextHighlightPrompts(enabled: Boolean) {
        _uiState.update { it.copy(aiTextHighlightPrompts = enabled) }
        saveSettings()
    }

    fun setShowSidebar(enabled: Boolean) {
        _uiState.update { it.copy(showSidebar = enabled) }
        saveSettings()
    }

    fun setAutoHideSidebar(enabled: Boolean) {
        _uiState.update { it.copy(autoHideSidebar = enabled) }
        saveSettings()
    }

    fun toggleQuickSettings() {
        _uiState.update { it.copy(showQuickSettings = !it.showQuickSettings) }
    }

    fun setShowWeatherWidget(enabled: Boolean) {
        _uiState.update { it.copy(showWeatherWidget = enabled) }
        saveSettings()
    }

    fun setShowQuotesWidget(enabled: Boolean) {
        _uiState.update { it.copy(showQuotesWidget = enabled) }
        saveSettings()
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
                    val ipUrl = URL("https://get.geojs.io/v1/ip/geo.json")
                    val conn = ipUrl.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3500
                    conn.readTimeout = 3500
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    if (conn.responseCode == 200) {
                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        lat = json.optDouble("latitude", lat)
                        lon = json.optDouble("longitude", lon)
                        val city = json.optString("city", "")
                        if (city.isNotBlank()) cityName = city
                    }
                } catch (e: Exception) {
                    try {
                        val ipUrl = URL("https://ipwho.is/")
                        val conn = ipUrl.openConnection() as HttpURLConnection
                        conn.connectTimeout = 3500
                        conn.readTimeout = 3500
                        if (conn.responseCode == 200) {
                            val body = conn.inputStream.bufferedReader().use { it.readText() }
                            val json = JSONObject(body)
                            lat = json.optDouble("latitude", lat)
                            lon = json.optDouble("longitude", lon)
                            val city = json.optString("city", "")
                            if (city.isNotBlank()) cityName = city
                        }
                    } catch (ex: Exception) {
                        // Keep defaults
                    }
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
        saveBookmarks()
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
        saveBookmarks()
    }

    private fun formatInputAsUrl(rawInput: String): String {
        return when {
            rawInput.startsWith("http://") || rawInput.startsWith("https://") -> rawInput
            rawInput.contains(".") && !rawInput.contains(" ") -> "https://$rawInput"
            else -> _uiState.value.searchEngine.buildSearchUrl(rawInput)
        }
    }


    override fun onCleared() {
        super.onCleared()
        stopReaderTts()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("BrowserViewModel", "Erro ao finalizar TTS", e)
        }
        tts = null
    }
}
