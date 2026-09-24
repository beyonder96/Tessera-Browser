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
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import com.tessera.browser.pip.PipManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.BrowserSpace
import com.tessera.browser.data.BrowseForMeResult
import com.tessera.browser.data.BrowseForMeState
import com.tessera.browser.data.BrowseSection
import com.tessera.browser.data.BrowseSource
import com.tessera.browser.data.PodcastAudioState
import com.tessera.browser.data.PodcastVoice
import com.tessera.browser.audio.PodcastAudioManager
import com.tessera.browser.data.DownloadItem
import com.tessera.browser.data.DownloadNotice
import com.tessera.browser.data.DownloadStatus
import com.tessera.browser.data.PageErrorInfo
import com.tessera.browser.data.TabGroup
import com.tessera.browser.data.SiteSettings
import com.tessera.browser.data.TranslationState
import com.tessera.browser.data.SafeBrowsingThreatInfo
import com.tessera.browser.data.SavedPageItem
import com.tessera.browser.data.SearchEngine
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.data.WallpaperTheme
import com.tessera.browser.data.NoteItem
import com.tessera.browser.data.NoteType
import com.tessera.browser.data.PrivacyDashboardState
import com.tessera.browser.data.BlockedTrackerItem
import com.tessera.browser.privacy.PrivacyTrackerEngine
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
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
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),
    val groupId: String? = null,
    val spaceId: String = "space_general"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("url", url)
        put("title", title)
        put("isHomePage", isHomePage)
        put("canGoBack", canGoBack)
        put("canGoForward", canGoForward)
        put("isPinned", isPinned)
        put("lastAccessedTimestamp", lastAccessedTimestamp)
        put("groupId", groupId ?: "")
        put("spaceId", spaceId)
    }

    companion object {
        fun fromJson(json: JSONObject): BrowserTab = BrowserTab(
            id = json.optString("id", UUID.randomUUID().toString()),
            url = json.optString("url", "https://duckduckgo.com"),
            title = json.optString("title", "Nova Guia"),
            isHomePage = json.optBoolean("isHomePage", true),
            canGoBack = json.optBoolean("canGoBack", false),
            canGoForward = json.optBoolean("canGoForward", false),
            isPinned = json.optBoolean("isPinned", false),
            lastAccessedTimestamp = json.optLong("lastAccessedTimestamp", System.currentTimeMillis()),
            groupId = json.optString("groupId", "").let { if (it.isNotBlank()) it else null },
            spaceId = json.optString("spaceId", "space_general").ifBlank { "space_general" }
        )
    }
}

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val spaceId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("url", url)
        put("timestamp", timestamp)
        put("spaceId", spaceId ?: "")
    }

    companion object {
        fun fromJson(json: JSONObject): HistoryEntry = HistoryEntry(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", ""),
            url = json.optString("url", ""),
            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
            spaceId = json.optString("spaceId", "").let { if (it.isNotBlank()) it else null }
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
    val showFullSettings: Boolean = false,
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

    // Podcastify & Áudio em Segundo Plano (Estilo Bot Tessera)
    val podcastAudioState: PodcastAudioState = PodcastAudioState(),

    // Recursos Avançados (Chrome, Opera & Arc)
    val isDesktopMode: Boolean = false,
    val cookieBlockerEnabled: Boolean = true,
    val isInFullscreenVideo: Boolean = false,
    val isInPipMode: Boolean = false,
    val isAutoPipEnabled: Boolean = true,
    val isVideoPlaying: Boolean = false,
    val videoWidth: Int = 16,
    val videoHeight: Int = 9,
    val showPipPermissionDialog: Boolean = false,
    val showFindInPage: Boolean = false,
    val findQuery: String = "",
    val findMatchIndex: Int = 0,
    val findMatchCount: Int = 0,
    val peekUrl: String? = null,
    val peekTitle: String? = null,
    val showPeekModal: Boolean = false,

    // Espaços de Navegação Isolados (Arc Spaces) & Multi-tabs
    val spaces: List<BrowserSpace> = BrowserSpace.DEFAULT_SPACES,
    val activeSpaceId: String = "space_general",
    val showSpaceSwitcherModal: Boolean = false,
    val tabs: List<BrowserTab> = listOf(
        BrowserTab(id = "default-tab", url = "https://duckduckgo.com", title = "Início", isHomePage = true, spaceId = "space_general")
    ),
    val activeTabId: String = "default-tab",
    val showTabsModal: Boolean = false,
    val tabGroups: List<TabGroup> = emptyList(),
    val selectedTabGroupId: String? = null,

    // Google Tradutor em Tempo Real (Inline DOM)
    val translationState: TranslationState = TranslationState(),

    // Permissões de Sites Granulares (Site Settings)
    val showSiteSettingsModal: Boolean = false,
    val siteSettingsOrigin: String = "",
    val siteSettings: Map<String, SiteSettings> = emptyMap(),

    // History, Bookmarks & Downloads
    val history: List<HistoryEntry> = emptyList(),
    val showHistoryModal: Boolean = false,
    val activeHubTab: Int = 0, // 0 = Favoritos, 1 = Histórico, 2 = Downloads, 3 = Salvos
    val downloads: List<DownloadItem> = emptyList(),
    val savedPages: List<SavedPageItem> = emptyList(),

    // Caderno de Notas & Web Clipper
    val notes: List<NoteItem> = emptyList(),
    val showNotebookModal: Boolean = false,
    val editingNote: NoteItem? = null,
    val notebookFilterTag: String? = null,
    val notebookSearchQuery: String = "",
    val summarizingNoteId: String? = null,

    // Modal de Compartilhamento QR Code
    val showQrCodeModal: Boolean = false,

    // Google Safe Browsing Ameaça
    val safeBrowsingThreat: SafeBrowsingThreatInfo? = null,

    // Quick AI Actions Modal
    val showAiActionModal: Boolean = false,

    // Browse for Me (Arc Search Style Editorial Synthesis)
    val browseForMeState: BrowseForMeState = BrowseForMeState(),

    // Arc Page Summary (IA Gratuita & Efeito Arc)
    val showArcSummary: Boolean = false,
    val isGeneratingArcSummary: Boolean = false,
    val arcSummaryContent: String? = null,
    val arcSummaryTitle: String = "",
    val arcSummaryDomain: String = "",
    val arcSummaryReadTimeSaved: Int = 1,
    val arcSummaryError: String? = null,
    val geminiApiKey: String? = null,

    // AdBlocker & Privacy Shield
    val adBlockEnabled: Boolean = true,
    val privacyState: PrivacyDashboardState = PrivacyDashboardState(),

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
    val siteThemeColor: Color? = null,
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

    val currentSpace: BrowserSpace
        get() = spaces.find { it.id == activeSpaceId } ?: spaces.firstOrNull() ?: BrowserSpace.DEFAULT_SPACES.first()

    val currentSpaceTabs: List<BrowserTab>
        get() = tabs.filter { it.spaceId == activeSpaceId }

    val currentSpaceFavorites: List<SpeedDialItem>
        get() = speedDialItems.filter { it.spaceId == null || it.spaceId == activeSpaceId }

    val currentSpaceHistory: List<HistoryEntry>
        get() = history.filter { it.spaceId == null || it.spaceId == activeSpaceId }

    val currentSpaceNotes: List<NoteItem>
        get() = notes.filter { it.spaceId == null || it.spaceId == activeSpaceId }
}

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var suggestionJob: Job? = null
    private var appContext: Context? = null

    init {
        viewModelScope.launch {
            PodcastAudioManager.audioState.collect { audioState ->
                _uiState.update {
                    it.copy(
                        podcastAudioState = audioState,
                        isReaderTtsPlaying = audioState.isPlaying
                    )
                }
            }
        }
    }

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
                pageError = null,
                siteThemeColor = null
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
                    pageError = null,
                    siteThemeColor = null,
                    translationState = state.translationState.copy(
                        isBannerVisible = false,
                        isTranslating = false,
                        isTranslated = false
                    )
                )
            }
        }
    }

    fun setSiteThemeColor(color: Color?) {
        if (_uiState.value.siteThemeColor != color) {
            _uiState.update { it.copy(siteThemeColor = color) }
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

    // PODCASTIFY & ÁUDIO EM SEGUNDO PLANO (ESTILO BOT TESSERA)
    fun playArticleAsPodcast(context: Context, article: ReaderArticle) {
        PodcastAudioManager.initialize(context) { _uiState.value.geminiApiKey }
        val subtitle = if (!article.author.isNullOrBlank()) "${article.domain} • Por ${article.author}" else article.domain
        PodcastAudioManager.playArticleOrContent(
            context = context,
            title = article.title,
            subtitle = subtitle,
            content = "${article.title}. ${article.plainText}"
        )
    }

    fun playSummaryAsPodcast(context: Context, title: String, domain: String, summaryText: String) {
        PodcastAudioManager.initialize(context) { _uiState.value.geminiApiKey }
        PodcastAudioManager.playArticleOrContent(
            context = context,
            title = title.ifBlank { "Resumo da Página" },
            subtitle = "Síntese Arc • $domain",
            content = summaryText
        )
    }

    fun playBrowseForMeAsPodcast(context: Context, result: BrowseForMeResult) {
        PodcastAudioManager.initialize(context) { _uiState.value.geminiApiKey }
        val script = buildString {
            append(result.headline).append(". ")
            append(result.quickAnswer).append(". ")
            if (result.keyTakeaways.isNotEmpty()) {
                append("Pontos principais: ")
                result.keyTakeaways.forEach { append(it).append(". ") }
            }
            result.sections.forEach { sec ->
                append(sec.title).append(". ")
                append(sec.content).append(". ")
            }
        }
        PodcastAudioManager.playArticleOrContent(
            context = context,
            title = result.headline,
            subtitle = "Síntese Editorial • ${result.sources.size} fontes",
            content = script
        )
    }

    fun toggleReaderTts(context: Context) {
        val article = _uiState.value.readerArticle
        if (_uiState.value.podcastAudioState.isPlaying) {
            PodcastAudioManager.pause(context)
        } else if (article != null) {
            playArticleAsPodcast(context, article)
        }
    }

    fun startReaderTts(context: Context, article: ReaderArticle) {
        playArticleAsPodcast(context, article)
    }

    fun stopReaderTts() {
        _uiState.update { it.copy(isReaderTtsPlaying = false) }
    }

    fun speakText(context: Context, text: String) {
        val title = _uiState.value.arcSummaryTitle.ifBlank { "Resumo Arc" }
        val domain = _uiState.value.arcSummaryDomain.ifBlank { "Tessera AI" }
        playSummaryAsPodcast(context, title, domain, text)
    }

    fun togglePodcastPlayPause(context: Context) {
        PodcastAudioManager.togglePlayPause(context)
    }

    fun seekPodcastBy(context: Context, deltaMs: Long) {
        PodcastAudioManager.seekBy(context, deltaMs)
    }

    fun seekPodcastToFraction(fraction: Float) {
        val duration = _uiState.value.podcastAudioState.durationMs
        if (duration > 0) {
            PodcastAudioManager.seekToPosition((duration * fraction).toLong())
        }
    }

    fun setPodcastSpeed(speed: Float) {
        PodcastAudioManager.setSpeed(speed)
    }

    fun cyclePodcastSpeed() {
        val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.75f)
        val current = _uiState.value.podcastAudioState.playbackSpeed
        val next = speeds.getOrNull(speeds.indexOf(current) + 1) ?: speeds.first()
        PodcastAudioManager.setSpeed(next)
    }

    fun selectPodcastVoice(context: Context, voice: PodcastVoice) {
        PodcastAudioManager.selectVoice(context, voice)
    }

    fun openPodcastFullPlayer() {
        PodcastAudioManager.setFullPlayerOpen(true)
    }

    fun dismissPodcastFullPlayer() {
        PodcastAudioManager.setFullPlayerOpen(false)
    }

    fun dismissPodcastPlayer(context: Context) {
        PodcastAudioManager.dismissPlayer(context)
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

    // MULTI-TABS & SPACES MANAGEMENT
    fun addNewTab(url: String = "https://duckduckgo.com", isHome: Boolean = true, spaceId: String? = null) {
        val effectiveSpaceId = spaceId ?: _uiState.value.activeSpaceId
        val newId = UUID.randomUUID().toString()
        val newTab = BrowserTab(
            id = newId,
            url = url,
            title = if (isHome) "Nova Guia" else extractDomain(url),
            isHomePage = isHome,
            spaceId = effectiveSpaceId
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newId,
                activeSpaceId = effectiveSpaceId,
                isHomePage = isHome,
                currentUrl = url,
                displayUrl = if (isHome) "" else url,
                canGoBack = false,
                showTabsModal = false
            )
        }
        saveTabs()
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
                activeSpaceId = targetTab.spaceId,
                isHomePage = targetTab.isHomePage,
                currentUrl = targetTab.url,
                displayUrl = if (targetTab.isHomePage) "" else targetTab.url,
                canGoBack = targetTab.canGoBack,
                showTabsModal = false
            )
        }
        saveTabs()
    }

    fun selectNextTab() {
        val currentTabs = _uiState.value.currentSpaceTabs
        if (currentTabs.size <= 1) return
        val currentIndex = currentTabs.indexOfFirst { it.id == _uiState.value.activeTabId }
        val nextIndex = if (currentIndex == -1 || currentIndex >= currentTabs.size - 1) 0 else currentIndex + 1
        selectTab(currentTabs[nextIndex].id)
    }

    fun selectPreviousTab() {
        val currentTabs = _uiState.value.currentSpaceTabs
        if (currentTabs.size <= 1) return
        val currentIndex = currentTabs.indexOfFirst { it.id == _uiState.value.activeTabId }
        val prevIndex = if (currentIndex <= 0) currentTabs.size - 1 else currentIndex - 1
        selectTab(currentTabs[prevIndex].id)
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        val tabToClose = currentTabs.find { it.id == tabId }
        val effectiveSpaceId = tabToClose?.spaceId ?: _uiState.value.activeSpaceId
        val remainingInSpace = currentTabs.filter { it.spaceId == effectiveSpaceId && it.id != tabId }

        val remainingTabs = currentTabs.filterNot { it.id == tabId }

        if (remainingInSpace.isEmpty()) {
            val newId = UUID.randomUUID().toString()
            val freshTab = BrowserTab(id = newId, isHomePage = true, spaceId = effectiveSpaceId)
            val updatedTabs = remainingTabs + freshTab
            _uiState.update { state ->
                state.copy(
                    tabs = updatedTabs,
                    activeTabId = newId,
                    isHomePage = true,
                    currentUrl = "https://duckduckgo.com",
                    displayUrl = "",
                    canGoBack = false
                )
            }
        } else {
            val nextActiveTab = if (_uiState.value.activeTabId == tabId) {
                remainingInSpace.last()
            } else {
                remainingTabs.find { it.id == _uiState.value.activeTabId } ?: remainingInSpace.last()
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
        saveTabs()
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

    // SPACES MANAGEMENT SUBSYSTEM (Arc Spaces)
    fun selectSpace(spaceId: String) {
        val targetSpace = _uiState.value.spaces.find { it.id == spaceId } ?: return
        if (_uiState.value.activeSpaceId == spaceId) return

        _uiState.update { state ->
            val spaceTabs = state.tabs.filter { it.spaceId == spaceId }
            val (updatedTabs, activeTab) = if (spaceTabs.isEmpty()) {
                val newTab = BrowserTab(
                    id = UUID.randomUUID().toString(),
                    url = "https://duckduckgo.com",
                    title = "Início",
                    isHomePage = true,
                    spaceId = spaceId
                )
                Pair(state.tabs + newTab, newTab)
            } else {
                val mostRecent = spaceTabs.maxByOrNull { it.lastAccessedTimestamp } ?: spaceTabs.first()
                Pair(state.tabs, mostRecent)
            }

            state.copy(
                activeSpaceId = spaceId,
                tabs = updatedTabs,
                activeTabId = activeTab.id,
                isHomePage = activeTab.isHomePage,
                currentUrl = activeTab.url,
                displayUrl = if (activeTab.isHomePage) "" else activeTab.url,
                canGoBack = activeTab.canGoBack,
                canGoForward = activeTab.canGoForward,
                showSpaceSwitcherModal = false
            )
        }
        saveSpaces()
        saveTabs()
    }

    fun createSpace(name: String, iconEmoji: String, colorArgb: Long) {
        val newSpace = BrowserSpace(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Novo Espaço" },
            iconEmoji = iconEmoji.ifBlank { "🌐" },
            colorArgb = colorArgb,
            isDefault = false
        )
        val initialTab = BrowserTab(
            id = UUID.randomUUID().toString(),
            url = "https://duckduckgo.com",
            title = "Início",
            isHomePage = true,
            spaceId = newSpace.id
        )
        _uiState.update { state ->
            state.copy(
                spaces = state.spaces + newSpace,
                activeSpaceId = newSpace.id,
                tabs = state.tabs + initialTab,
                activeTabId = initialTab.id,
                isHomePage = true,
                currentUrl = "https://duckduckgo.com",
                displayUrl = "",
                showSpaceSwitcherModal = false
            )
        }
        saveSpaces()
        saveTabs()
    }

    fun updateSpace(spaceId: String, name: String, iconEmoji: String, colorArgb: Long) {
        _uiState.update { state ->
            val updated = state.spaces.map { s ->
                if (s.id == spaceId) {
                    s.copy(
                        name = name.ifBlank { s.name },
                        iconEmoji = iconEmoji.ifBlank { s.iconEmoji },
                        colorArgb = colorArgb
                    )
                } else s
            }
            state.copy(spaces = updated)
        }
        saveSpaces()
    }

    fun deleteSpace(spaceId: String) {
        val currentSpaces = _uiState.value.spaces
        if (currentSpaces.size <= 1) return // Do not delete last remaining space

        val remainingSpaces = currentSpaces.filterNot { it.id == spaceId }
        val fallbackSpace = remainingSpaces.first()

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.spaceId == spaceId) tab.copy(spaceId = fallbackSpace.id) else tab
            }
            val needSwitch = state.activeSpaceId == spaceId
            val nextSpaceId = if (needSwitch) fallbackSpace.id else state.activeSpaceId
            val nextTabs = updatedTabs.filter { it.spaceId == nextSpaceId }
            val nextActive = nextTabs.maxByOrNull { it.lastAccessedTimestamp } ?: nextTabs.firstOrNull()

            state.copy(
                spaces = remainingSpaces,
                activeSpaceId = nextSpaceId,
                tabs = updatedTabs,
                activeTabId = nextActive?.id ?: state.activeTabId,
                isHomePage = nextActive?.isHomePage ?: state.isHomePage,
                currentUrl = nextActive?.url ?: state.currentUrl,
                displayUrl = if (nextActive?.isHomePage == true) "" else nextActive?.url ?: state.displayUrl
            )
        }
        saveSpaces()
        saveTabs()
    }

    fun moveTabToSpace(tabId: String, targetSpaceId: String) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == tabId) tab.copy(spaceId = targetSpaceId) else tab
            }

            val isCurrentTab = state.activeTabId == tabId
            val currentSpaceTabs = updatedTabs.filter { it.spaceId == state.activeSpaceId }

            if (isCurrentTab) {
                if (currentSpaceTabs.isNotEmpty()) {
                    val nextActive = currentSpaceTabs.last()
                    state.copy(
                        tabs = updatedTabs,
                        activeTabId = nextActive.id,
                        isHomePage = nextActive.isHomePage,
                        currentUrl = nextActive.url,
                        displayUrl = if (nextActive.isHomePage) "" else nextActive.url
                    )
                } else {
                    val newTab = BrowserTab(
                        id = UUID.randomUUID().toString(),
                        url = "https://duckduckgo.com",
                        title = "Início",
                        isHomePage = true,
                        spaceId = state.activeSpaceId
                    )
                    state.copy(
                        tabs = updatedTabs + newTab,
                        activeTabId = newTab.id,
                        isHomePage = true,
                        currentUrl = "https://duckduckgo.com",
                        displayUrl = ""
                    )
                }
            } else {
                state.copy(tabs = updatedTabs)
            }
        }
        saveTabs()
    }

    fun cycleNextSpace() {
        val spaces = _uiState.value.spaces
        if (spaces.size <= 1) return
        val currentIdx = spaces.indexOfFirst { it.id == _uiState.value.activeSpaceId }
        val nextIdx = if (currentIdx == -1 || currentIdx >= spaces.size - 1) 0 else currentIdx + 1
        selectSpace(spaces[nextIdx].id)
    }

    fun cyclePreviousSpace() {
        val spaces = _uiState.value.spaces
        if (spaces.size <= 1) return
        val currentIdx = spaces.indexOfFirst { it.id == _uiState.value.activeSpaceId }
        val prevIdx = if (currentIdx <= 0) spaces.size - 1 else currentIdx - 1
        selectSpace(spaces[prevIdx].id)
    }

    fun toggleSpaceSwitcherModal(visible: Boolean? = null) {
        _uiState.update { it.copy(showSpaceSwitcherModal = visible ?: !it.showSpaceSwitcherModal) }
    }

    // TAB GROUPS SUBSYSTEM
    fun createTabGroup(title: String, colorArgb: Long, tabIds: List<String> = emptyList()) {
        val newGroup = TabGroup(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Grupo" },
            colorArgb = colorArgb
        )
        _uiState.update { state ->
            val updatedTabs = if (tabIds.isNotEmpty()) {
                state.tabs.map { if (it.id in tabIds) it.copy(groupId = newGroup.id) else it }
            } else state.tabs
            state.copy(
                tabGroups = state.tabGroups + newGroup,
                tabs = updatedTabs,
                selectedTabGroupId = newGroup.id
            )
        }
        saveTabGroups()
    }

    fun updateTabGroup(group: TabGroup) {
        _uiState.update { state ->
            state.copy(tabGroups = state.tabGroups.map { if (it.id == group.id) group else it })
        }
        saveTabGroups()
    }

    fun deleteTabGroup(groupId: String, closeTabs: Boolean = false) {
        _uiState.update { state ->
            val updatedTabs = if (closeTabs) {
                state.tabs.filterNot { it.groupId == groupId }
            } else {
                state.tabs.map { if (it.groupId == groupId) it.copy(groupId = null) else it }
            }
            val remainingTabs = if (updatedTabs.isEmpty()) {
                val newId = UUID.randomUUID().toString()
                listOf(BrowserTab(id = newId, isHomePage = true))
            } else updatedTabs

            val activeTabStillExists = remainingTabs.any { it.id == state.activeTabId }
            val nextActiveTab = if (activeTabStillExists) state.activeTabId else remainingTabs.last().id
            val nextActiveTabObj = remainingTabs.find { it.id == nextActiveTab } ?: remainingTabs.last()

            state.copy(
                tabGroups = state.tabGroups.filterNot { it.id == groupId },
                tabs = remainingTabs,
                activeTabId = nextActiveTab,
                isHomePage = nextActiveTabObj.isHomePage,
                currentUrl = nextActiveTabObj.url,
                displayUrl = if (nextActiveTabObj.isHomePage) "" else nextActiveTabObj.url,
                selectedTabGroupId = if (state.selectedTabGroupId == groupId) null else state.selectedTabGroupId
            )
        }
        saveTabGroups()
    }

    fun addTabToGroup(tabId: String, groupId: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { if (it.id == tabId) it.copy(groupId = groupId) else it }
            )
        }
        saveTabGroups()
    }

    fun removeTabFromGroup(tabId: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { if (it.id == tabId) it.copy(groupId = null) else it }
            )
        }
        saveTabGroups()
    }

    fun toggleGroupCollapsed(groupId: String) {
        _uiState.update { state ->
            state.copy(
                tabGroups = state.tabGroups.map {
                    if (it.id == groupId) it.copy(isCollapsed = !it.isCollapsed) else it
                }
            )
        }
        saveTabGroups()
    }

    fun setSelectedTabGroupId(groupId: String?) {
        _uiState.update { it.copy(selectedTabGroupId = groupId) }
    }

    // SITE SETTINGS & PERMISSIONS SUBSYSTEM
    private fun extractOrigin(raw: String): String {
        return try {
            val parsed = Uri.parse(if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw")
            val host = parsed.host ?: raw
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            raw.replace("https://", "").replace("http://", "").split("/").firstOrNull()?.let {
                if (it.startsWith("www.")) it.substring(4) else it
            } ?: raw
        }
    }

    fun openSiteSettings(origin: String) {
        val cleanOrigin = extractOrigin(origin)
        val current = _uiState.value.siteSettings[cleanOrigin] ?: SiteSettings(origin = cleanOrigin)
        val updatedMap = _uiState.value.siteSettings.toMutableMap()
        updatedMap[cleanOrigin] = current
        _uiState.update {
            it.copy(
                showSiteSettingsModal = true,
                siteSettingsOrigin = cleanOrigin,
                siteSettings = updatedMap
            )
        }
    }

    fun dismissSiteSettings() {
        _uiState.update { it.copy(showSiteSettingsModal = false) }
    }

    fun getSiteSettings(origin: String): SiteSettings {
        val clean = extractOrigin(origin)
        return _uiState.value.siteSettings[clean] ?: SiteSettings(origin = clean)
    }

    fun updateSitePermission(origin: String, update: (SiteSettings) -> SiteSettings) {
        val clean = extractOrigin(origin)
        val current = _uiState.value.siteSettings[clean] ?: SiteSettings(origin = clean)
        val newSettings = update(current).copy(lastModified = System.currentTimeMillis())
        _uiState.update { state ->
            val updated = state.siteSettings.toMutableMap()
            updated[clean] = newSettings
            state.copy(siteSettings = updated)
        }
        saveSiteSettings()
    }

    fun clearSiteData(
        context: Context,
        origin: String,
        webView: WebView?,
        onComplete: () -> Unit = {}
    ) {
        val clean = extractOrigin(origin)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                // 1. Delete WebStorage for this origin
                try {
                    WebStorage.getInstance().deleteOrigin(origin)
                    WebStorage.getInstance().deleteOrigin(clean)
                    if (!origin.startsWith("http")) {
                        WebStorage.getInstance().deleteOrigin("https://$clean")
                        WebStorage.getInstance().deleteOrigin("http://$clean")
                    }
                } catch (e: Exception) {
                    Log.w("BrowserViewModel", "Erro ao deletar origin WebStorage", e)
                }

                // 2. Clear cookies for this host/origin
                try {
                    val cookieManager = CookieManager.getInstance()
                    val cookieString = cookieManager.getCookie(origin) ?: cookieManager.getCookie(clean)
                    if (!cookieString.isNullOrBlank()) {
                        val cookies = cookieString.split(";")
                        for (cookie in cookies) {
                            val parts = cookie.split("=")
                            if (parts.isNotEmpty()) {
                                val name = parts[0].trim()
                                cookieManager.setCookie(origin, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/")
                                cookieManager.setCookie(clean, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/")
                            }
                        }
                        cookieManager.flush()
                    }
                } catch (e: Exception) {
                    Log.w("BrowserViewModel", "Erro ao limpar cookies do site", e)
                }

                // 3. Clear permission state for origin
                _uiState.update { state ->
                    val resetSettings = SiteSettings(origin = clean)
                    val updated = state.siteSettings.toMutableMap()
                    updated[clean] = resetSettings
                    state.copy(siteSettings = updated)
                }
                saveSiteSettings()

                // 4. Reload if current page matches this origin
                if (extractOrigin(_uiState.value.currentUrl) == clean) {
                    webView?.reload()
                }

                Toast.makeText(context, "Dados de $clean limpos com sucesso", Toast.LENGTH_SHORT).show()
                onComplete()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao limpar dados do site $clean", e)
                onComplete()
            }
        }
    }

    // GOOGLE REAL-TIME DOM TRANSLATION SUBSYSTEM
    fun onPageLanguageDetected(lang: String, url: String) {
        val cleanLang = lang.trim().lowercase()
        if (cleanLang.isBlank() || cleanLang.startsWith("pt") || !url.startsWith("http")) {
            if (_uiState.value.translationState.isBannerVisible && !_uiState.value.translationState.isTranslated) {
                _uiState.update { it.copy(translationState = it.translationState.copy(isBannerVisible = false)) }
            }
            return
        }

        val langName = TranslationState.getLanguageDisplayName(cleanLang)
        _uiState.update { state ->
            state.copy(
                translationState = state.translationState.copy(
                    isBannerVisible = true,
                    detectedLanguageCode = cleanLang,
                    detectedLanguageName = langName,
                    targetLanguageCode = "pt",
                    targetLanguageName = "Português",
                    isTranslating = false,
                    isTranslated = false
                )
            )
        }
    }

    fun showTranslationBanner(force: Boolean = true) {
        _uiState.update { state ->
            val detected = state.translationState.detectedLanguageCode.ifBlank { "en" }
            val detectedName = TranslationState.getLanguageDisplayName(detected)
            state.copy(
                translationState = state.translationState.copy(
                    isBannerVisible = true,
                    detectedLanguageCode = detected,
                    detectedLanguageName = detectedName,
                    targetLanguageCode = "pt",
                    targetLanguageName = "Português"
                )
            )
        }
    }

    fun dismissTranslationBanner() {
        _uiState.update { state ->
            state.copy(translationState = state.translationState.copy(isBannerVisible = false))
        }
    }

    fun setTranslationProgress(isTranslating: Boolean, isTranslated: Boolean) {
        _uiState.update { state ->
            state.copy(
                translationState = state.translationState.copy(
                    isTranslating = isTranslating,
                    isTranslated = isTranslated
                )
            )
        }
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
                val autoPip = if (prefs.contains("auto_pip_enabled")) prefs.getBoolean("auto_pip_enabled", true) else null
                val loadedGeminiKey = prefs.getString("gemini_api_key", null)
                val savedTotalBlocked = prefs.getInt("total_blocked_trackers", 0)

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

                // Tab Groups
                val groupsJson = prefs.getString("tab_groups_list", null)
                val loadedGroups = if (!groupsJson.isNullOrBlank()) {
                    val arr = JSONArray(groupsJson)
                    val list = mutableListOf<TabGroup>()
                    for (i in 0 until arr.length()) {
                        list.add(TabGroup.fromJson(arr.getJSONObject(i)))
                    }
                    list
                } else null

                // Spaces
                val spacesJson = prefs.getString("spaces_list", null)
                val loadedSpaces = if (!spacesJson.isNullOrBlank()) {
                    val arr = JSONArray(spacesJson)
                    val list = mutableListOf<BrowserSpace>()
                    for (i in 0 until arr.length()) {
                        list.add(BrowserSpace.fromJson(arr.getJSONObject(i)))
                    }
                    if (list.isNotEmpty()) list else null
                } else null
                val savedActiveSpaceId = prefs.getString("active_space_id", null)

                // Tabs
                val tabsJson = prefs.getString("tabs_list", null)
                val loadedTabs = if (!tabsJson.isNullOrBlank()) {
                    val arr = JSONArray(tabsJson)
                    val list = mutableListOf<BrowserTab>()
                    for (i in 0 until arr.length()) {
                        list.add(BrowserTab.fromJson(arr.getJSONObject(i)))
                    }
                    if (list.isNotEmpty()) list else null
                } else null

                // Site Settings
                val siteSettingsJson = prefs.getString("site_settings_list", null)
                val loadedSiteSettings = if (!siteSettingsJson.isNullOrBlank()) {
                    val arr = JSONArray(siteSettingsJson)
                    val map = mutableMapOf<String, SiteSettings>()
                    for (i in 0 until arr.length()) {
                        val s = SiteSettings.fromJson(arr.getJSONObject(i))
                        map[s.origin] = s
                    }
                    map
                } else null

                // Caderno de Notas & Web Clipper
                val notesJson = prefs.getString("notebook_notes_list", null)
                val loadedNotes = if (!notesJson.isNullOrBlank()) {
                    val arr = JSONArray(notesJson)
                    val list = mutableListOf<NoteItem>()
                    for (i in 0 until arr.length()) {
                        list.add(NoteItem.fromJson(arr.getJSONObject(i)))
                    }
                    list
                } else null

                _uiState.update { current ->
                    current.copy(
                        spaces = loadedSpaces ?: current.spaces,
                        activeSpaceId = savedActiveSpaceId ?: current.activeSpaceId,
                        tabs = loadedTabs ?: current.tabs,
                        speedDialItems = loadedBookmarks ?: current.speedDialItems,
                        history = loadedHistory ?: current.history,
                        savedPages = loadedSavedPages ?: current.savedPages,
                        notes = loadedNotes ?: current.notes,
                        tabGroups = loadedGroups ?: current.tabGroups,
                        siteSettings = loadedSiteSettings ?: current.siteSettings,
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
                        readerShowImages = readerShowImages ?: current.readerShowImages,
                        isAutoPipEnabled = autoPip ?: current.isAutoPipEnabled,
                        geminiApiKey = loadedGeminiKey ?: current.geminiApiKey,
                        privacyState = current.privacyState.copy(
                            totalBlockedCount = savedTotalBlocked,
                            dataSavedBytes = savedTotalBlocked.toLong() * PrivacyTrackerEngine.BYTES_PER_BLOCKED_REQUEST,
                            estimatedTimeSavedMs = savedTotalBlocked.toLong() * PrivacyTrackerEngine.TIME_SAVED_PER_BLOCKED_MS
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao carregar preferências", e)
            }
        }
    }

    private fun saveSpaces() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.spaces.forEach { s -> arr.put(s.toJson()) }
                prefs.edit()
                    .putString("spaces_list", arr.toString())
                    .putString("active_space_id", _uiState.value.activeSpaceId)
                    .apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar espaços", e)
            }
        }
    }

    private fun saveTabs() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.tabs.forEach { t -> arr.put(t.toJson()) }
                prefs.edit().putString("tabs_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar abas", e)
            }
        }
    }

    private fun saveTabGroups() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.tabGroups.forEach { group ->
                    arr.put(group.toJson())
                }
                prefs.edit().putString("tab_groups_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar grupos de abas", e)
            }
        }
    }

    private fun saveSiteSettings() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.siteSettings.values.forEach { settings ->
                    arr.put(settings.toJson())
                }
                prefs.edit().putString("site_settings_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar permissões de sites", e)
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

    private fun saveNotes() {
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                val arr = JSONArray()
                _uiState.value.notes.forEach { item ->
                    arr.put(item.toJson())
                }
                prefs.edit().putString("notebook_notes_list", arr.toString()).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar anotações do caderno", e)
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
                    .putString("gemini_api_key", s.geminiApiKey)
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

    fun setInPictureInPictureMode(inPip: Boolean) {
        _uiState.update { it.copy(isInPipMode = inPip) }
    }

    fun setVideoPlaybackState(isPlaying: Boolean, width: Int, height: Int) {
        _uiState.update {
            it.copy(
                isVideoPlaying = isPlaying,
                videoWidth = if (width > 0) width else it.videoWidth,
                videoHeight = if (height > 0) height else it.videoHeight
            )
        }
    }

    fun setAutoPipEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAutoPipEnabled = enabled) }
        val app = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("auto_pip_enabled", enabled).apply()
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao salvar auto_pip_enabled", e)
            }
        }
    }

    fun toggleAutoPip() {
        setAutoPipEnabled(!_uiState.value.isAutoPipEnabled)
    }

    fun showPipPermissionDialog(show: Boolean = true) {
        _uiState.update { it.copy(showPipPermissionDialog = show) }
    }

    fun requestEnterPip(activity: Activity) {
        if (PipManager.hasOverlayOrPipPermission(activity)) {
            val success = PipManager.enterPip(
                activity = activity,
                width = _uiState.value.videoWidth,
                height = _uiState.value.videoHeight
            )
            if (!success) {
                _uiState.update { it.copy(showPipPermissionDialog = true) }
            }
        } else {
            _uiState.update { it.copy(showPipPermissionDialog = true) }
        }
    }

    // --- CADERNO DE NOTAS & WEB CLIPPER ---

    fun openNotebook(noteToEdit: NoteItem? = null) {
        _uiState.update { it.copy(showNotebookModal = true, editingNote = noteToEdit) }
    }

    fun dismissNotebook() {
        _uiState.update { it.copy(showNotebookModal = false, editingNote = null) }
    }

    fun setEditingNote(note: NoteItem?) {
        _uiState.update { it.copy(editingNote = note) }
    }

    fun setNotebookSearchQuery(query: String) {
        _uiState.update { it.copy(notebookSearchQuery = query) }
    }

    fun setNotebookFilterTag(tag: String?) {
        _uiState.update { it.copy(notebookFilterTag = tag) }
    }

    fun saveNote(note: NoteItem) {
        _uiState.update { state ->
            val existingIndex = state.notes.indexOfFirst { it.id == note.id }
            val updated = if (existingIndex >= 0) {
                state.notes.toMutableList().apply {
                    this[existingIndex] = note.copy(updatedAt = System.currentTimeMillis())
                }
            } else {
                listOf(note) + state.notes
            }
            state.copy(notes = updated, editingNote = null)
        }
        saveNotes()
    }

    fun deleteNote(noteId: String) {
        _uiState.update { state ->
            state.copy(notes = state.notes.filter { it.id != noteId })
        }
        saveNotes()
    }

    fun togglePinNote(noteId: String) {
        _uiState.update { state ->
            val updated = state.notes.map {
                if (it.id == noteId) it.copy(isPinned = !it.isPinned, updatedAt = System.currentTimeMillis()) else it
            }
            state.copy(notes = updated)
        }
        saveNotes()
    }

    fun clipPageContent(
        title: String,
        url: String,
        content: String,
        isCode: Boolean = false,
        withAiSummary: Boolean = false
    ) {
        if (content.isBlank()) return
        val currentSpaceId = _uiState.value.activeSpaceId
        val noteType = if (isCode) NoteType.CODE_SNIPPET else NoteType.TEXT_CLIP
        val defaultTag = if (isCode) "Código" else "Pesquisa"

        val newNote = NoteItem(
            title = title.ifBlank { "Clipe da Web" },
            content = content,
            sourceUrl = url,
            sourceTitle = title,
            type = noteType,
            tags = listOf(defaultTag),
            spaceId = currentSpaceId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        saveNote(newNote)

        if (withAiSummary) {
            summarizeNote(newNote.id)
        }
    }

    fun summarizeNote(noteId: String) {
        val note = _uiState.value.notes.find { it.id == noteId } ?: return
        _uiState.update { it.copy(summarizingNoteId = noteId) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentToSummarize = note.content
                val cleanContent = if (contentToSummarize.length > 5000) contentToSummarize.take(5000) + "..." else contentToSummarize
                val domain = try { Uri.parse(note.sourceUrl).host.orEmpty() } catch (e: Exception) { "" }

                var summaryResult: String? = null

                // Tier 1: Gemini API
                val geminiKey = _uiState.value.geminiApiKey?.trim().orEmpty()
                if (geminiKey.isNotBlank()) {
                    try {
                        summaryResult = callGeminiApi(geminiKey, note.title, domain, cleanContent)
                    } catch (e: Exception) {
                        Log.w("BrowserViewModel", "Gemini summary fallback", e)
                    }
                }

                // Tier 2: Free AI API
                if (summaryResult.isNullOrBlank() || !isValidAiSummary(summaryResult)) {
                    try {
                        summaryResult = callFreeAiApi(note.title, domain, cleanContent)
                    } catch (e: Exception) {
                        Log.w("BrowserViewModel", "Free AI summary fallback", e)
                    }
                }

                // Tier 3: Local synthesis
                if (summaryResult.isNullOrBlank() || !isValidAiSummary(summaryResult)) {
                    summaryResult = generateLocalArcSummary(note.title, domain, cleanContent)
                }

                if (!summaryResult.isNullOrBlank()) {
                    val finalSummary = summaryResult.trim()
                    _uiState.update { state ->
                        val updatedNotes = state.notes.map {
                            if (it.id == noteId) {
                                it.copy(
                                    aiSummary = finalSummary,
                                    type = if (it.type == NoteType.TEXT_CLIP) NoteType.AI_SUMMARY else it.type,
                                    updatedAt = System.currentTimeMillis()
                                )
                            } else it
                        }
                        state.copy(notes = updatedNotes, summarizingNoteId = null)
                    }
                    saveNotes()
                } else {
                    _uiState.update { it.copy(summarizingNoteId = null) }
                }
            } catch (e: Exception) {
                Log.e("BrowserViewModel", "Erro ao resumir anotação", e)
                _uiState.update { it.copy(summarizingNoteId = null) }
            }
        }
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

    // BROWSE FOR ME (ARC SEARCH STYLE EDITORIAL SYNTHESIS)
    private var browseForMeJob: Job? = null

    fun dismissBrowseForMe() {
        browseForMeJob?.cancel()
        _uiState.update {
            it.copy(browseForMeState = it.browseForMeState.copy(isVisible = false))
        }
    }

    fun browseForMe(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return

        browseForMeJob?.cancel()
        _uiState.update {
            it.copy(
                browseForMeState = BrowseForMeState(
                    isVisible = true,
                    isLoading = true,
                    currentQuery = cleanQuery,
                    loadingStep = "Consultando fontes e índices...",
                    result = null,
                    error = null
                ),
                showAiActionModal = false
            )
        }

        browseForMeJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Step 1: Extração de Fontes e Conteúdo Real da Web (DuckDuckGo Instant Answer + Wikipedia)
                val (extractedSources, rawContentText) = fetchWebSourcesForQuery(cleanQuery)

                _uiState.update {
                    it.copy(
                        browseForMeState = it.browseForMeState.copy(
                            loadingStep = "Sintetizando inteligência editorial..."
                        )
                    )
                }

                var finalResult: BrowseForMeResult? = null

                // TIER 1: Gemini Oficial (se API key configurada)
                val geminiKey = _uiState.value.geminiApiKey?.trim().orEmpty()
                if (geminiKey.isNotBlank()) {
                    try {
                        val aiJson = callGeminiForBrowseForMe(geminiKey, cleanQuery, rawContentText)
                        if (aiJson != null) {
                            finalResult = parseBrowseForMeJson(cleanQuery, aiJson, extractedSources)
                        }
                    } catch (e: Exception) {
                        Log.w("TesseraBrowser", "Falha no Gemini para BrowseForMe, tentando fallback", e)
                    }
                }

                // TIER 2: Free AI Endpoint (se Tier 1 não configurado ou falhou)
                if (finalResult == null) {
                    try {
                        val freeAiJson = callFreeAiForBrowseForMe(cleanQuery, rawContentText)
                        if (freeAiJson != null) {
                            finalResult = parseBrowseForMeJson(cleanQuery, freeAiJson, extractedSources)
                        }
                    } catch (e: Exception) {
                        Log.w("TesseraBrowser", "Falha no Free AI para BrowseForMe, usando fallback local resiliente", e)
                    }
                }

                // TIER 3: Motor Editorial Local Resiliente (Garante funcionamento 100% offline / sem API)
                if (finalResult == null) {
                    finalResult = buildLocalEditorialResult(cleanQuery, rawContentText, extractedSources)
                }

                val resultToEmit = finalResult
                _uiState.update {
                    it.copy(
                        browseForMeState = it.browseForMeState.copy(
                            isLoading = false,
                            result = resultToEmit,
                            error = null
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("TesseraBrowser", "Erro ao executar Browse for Me", e)
                _uiState.update {
                    it.copy(
                        browseForMeState = it.browseForMeState.copy(
                            isLoading = false,
                            error = "Não foi possível sintetizar fontes para esta pesquisa. Tente novamente ou abra na busca web."
                        )
                    )
                }
            }
        }
    }

    private fun fetchWebSourcesForQuery(query: String): Pair<List<BrowseSource>, String> {
        val sources = mutableListOf<BrowseSource>()
        val contentParts = mutableListOf<String>()

        // 1. DuckDuckGo Instant Answer API
        try {
            val ddgUrl = URL("https://api.duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}&format=json&no_html=1&skip_disambig=1")
            val conn = (ddgUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "TesseraBrowser/1.5.0")
            }
            if (conn.responseCode in 200..299) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val heading = root.optString("Heading", "")
                val abstractText = root.optString("AbstractText", "")
                val abstractSource = root.optString("AbstractSource", "")
                val abstractUrl = root.optString("AbstractURL", "")

                if (abstractText.isNotBlank()) {
                    contentParts.add(abstractText)
                    if (abstractUrl.isNotBlank()) {
                        val domain = try { Uri.parse(abstractUrl).host?.replace("www.", "") ?: abstractSource } catch (e: Exception) { abstractSource }
                        sources.add(
                            BrowseSource(
                                title = heading.ifBlank { abstractSource }.ifBlank { "Fonte Principal" },
                                url = abstractUrl,
                                domain = domain.ifBlank { "duckduckgo.com" },
                                snippet = abstractText.take(140) + "..."
                            )
                        )
                    }
                }

                // Related topics
                val related = root.optJSONArray("RelatedTopics")
                if (related != null) {
                    for (i in 0 until minOf(related.length(), 6)) {
                        val item = related.optJSONObject(i) ?: continue
                        val text = item.optString("Text", "")
                        val firstUrl = item.optString("FirstURL", "")
                        if (text.isNotBlank()) {
                            contentParts.add(text)
                            if (firstUrl.isNotBlank() && sources.none { it.url == firstUrl }) {
                                val domain = try { Uri.parse(firstUrl).host?.replace("www.", "") ?: "web" } catch (e: Exception) { "web" }
                                val title = text.substringBefore(" - ").substringBefore(" – ").take(45)
                                sources.add(
                                    BrowseSource(
                                        title = title.ifBlank { "Referência ${i + 1}" },
                                        url = firstUrl,
                                        domain = domain,
                                        snippet = text.take(130)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("TesseraBrowser", "DDG API query falhou", e)
        }

        // 2. Wikipedia Search API
        try {
            val wikiUrl = URL("https://pt.wikipedia.org/w/api.php?action=query&list=search&srsearch=${URLEncoder.encode(query, "UTF-8")}&utf8=&format=json&srlimit=4")
            val conn = (wikiUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "TesseraBrowser/1.5.0")
            }
            if (conn.responseCode in 200..299) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val search = root.optJSONObject("query")?.optJSONArray("search")
                if (search != null) {
                    for (i in 0 until search.length()) {
                        val item = search.getJSONObject(i)
                        val title = item.optString("title", "")
                        val rawSnippet = item.optString("snippet", "")
                        val cleanSnippet = rawSnippet.replace(Regex("<[^>]*>"), "").replace("&quot;", "\"")
                        if (title.isNotBlank()) {
                            val pageUrl = "https://pt.wikipedia.org/wiki/${URLEncoder.encode(title.replace(" ", "_"), "UTF-8")}"
                            if (sources.none { it.url == pageUrl }) {
                                sources.add(
                                    BrowseSource(
                                        title = "$title — Wikipédia",
                                        url = pageUrl,
                                        domain = "pt.wikipedia.org",
                                        snippet = cleanSnippet.take(130)
                                    )
                                )
                            }
                            if (cleanSnippet.isNotBlank()) {
                                contentParts.add(cleanSnippet)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("TesseraBrowser", "Wikipedia API query falhou", e)
        }

        // Fontes de fallback de busca web direta
        if (sources.none { it.domain.contains("google") }) {
            sources.add(
                BrowseSource(
                    title = "Resultados Google: \"$query\"",
                    url = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}",
                    domain = "google.com",
                    snippet = "Índice web abrangente e resultados em tempo real"
                )
            )
        }
        if (sources.none { it.domain.contains("duckduckgo") }) {
            sources.add(
                BrowseSource(
                    title = "Resultados DuckDuckGo: \"$query\"",
                    url = "https://duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}",
                    domain = "duckduckgo.com",
                    snippet = "Navegação segura e resultados globais"
                )
            )
        }

        val consolidatedContent = contentParts.joinToString("\n\n").ifBlank {
            "Assunto pesquisado: $query. Coletando síntese dos principais resultados da web."
        }

        return Pair(sources.take(6), consolidatedContent)
    }

    private fun callGeminiForBrowseForMe(apiKey: String, query: String, contextText: String): String? {
        val systemPrompt = "Você é o motor editorial 'Browse for Me' do navegador Tessera estilo Arc Search. Crie uma síntese inteligente, visual e completa sobre a consulta em Português do Brasil.\n" +
                "Responda SOMENTE em JSON válido, sem bloco de código markdown e sem texto antes ou depois. Estrutura:\n" +
                "{\n" +
                "  \"headline\": \"Título jornalístico conciso e atraente\",\n" +
                "  \"quickAnswer\": \"Resposta direta e completa em 2 a 3 frases explicando o cerne.\",\n" +
                "  \"keyTakeaways\": [\"💡 Ponto 1...\", \"🚀 Ponto 2...\", \"📌 Ponto 3...\"],\n" +
                "  \"sections\": [\n" +
                "    {\"title\": \"Subtítulo 1\", \"content\": \"Texto explicativo...\", \"bulletPoints\": [\"Detalhe A\", \"Detalhe B\"]},\n" +
                "    {\"title\": \"Subtítulo 2\", \"content\": \"Mais informações...\", \"bulletPoints\": []}\n" +
                "  ]\n" +
                "}"

        val userPrompt = "Consulta: $query\n\nInformações coletadas da web:\n$contextText"

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\n$userPrompt")
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 1200)
                put("responseMimeType", "application/json")
            })
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload.toString()) }

        if (conn.responseCode in 200..299) {
            val responseStr = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val root = JSONObject(responseStr)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val contentObj = first.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return null
    }

    private fun callFreeAiForBrowseForMe(query: String, contextText: String): String? {
        val systemPrompt = "Você é o motor editorial 'Browse for Me' do navegador Tessera estilo Arc Search. Crie uma síntese inteligente e visual em Português do Brasil.\n" +
                "Responda SOMENTE em formato JSON válido:\n" +
                "{\n" +
                "  \"headline\": \"Título elegante sobre o tema\",\n" +
                "  \"quickAnswer\": \"Resposta direta e concisa em 2 a 3 frases.\",\n" +
                "  \"keyTakeaways\": [\"💡 Ponto 1...\", \"🚀 Ponto 2...\", \"📌 Ponto 3...\"],\n" +
                "  \"sections\": [\n" +
                "    {\"title\": \"Subtítulo 1\", \"content\": \"Texto explicativo...\", \"bulletPoints\": [\"Detalhe A\"]},\n" +
                "    {\"title\": \"Subtítulo 2\", \"content\": \"Mais detalhes...\", \"bulletPoints\": []}\n" +
                "  ]\n" +
                "}"

        val userPrompt = "Consulta: $query\nInformações:\n$contextText"

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
            connectTimeout = 7000
            readTimeout = 14000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("User-Agent", "TesseraBrowser/1.5.0")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(reqJson.toString()) }

        if (conn.responseCode in 200..299) {
            val response = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val clean = response.trim()
            if (clean.isNotBlank() && isValidAiSummary(clean)) {
                return clean
            }
        }
        return null
    }

    private fun parseBrowseForMeJson(query: String, rawJson: String, sources: List<BrowseSource>): BrowseForMeResult? {
        return try {
            val trimmed = rawJson.trim()
            val jsonStr = if (trimmed.startsWith("```")) {
                trimmed.substringAfter("\n").substringBeforeLast("```").trim()
            } else trimmed

            val root = JSONObject(jsonStr)
            val headline = root.optString("headline", "").ifBlank {
                query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            val quickAnswer = root.optString("quickAnswer", "")
            if (quickAnswer.isBlank()) return null

            val takeaways = mutableListOf<String>()
            val takeawaysArr = root.optJSONArray("keyTakeaways")
            if (takeawaysArr != null) {
                for (i in 0 until takeawaysArr.length()) {
                    val item = takeawaysArr.optString(i, "").trim()
                    if (item.isNotBlank()) takeaways.add(item)
                }
            }

            val sections = mutableListOf<BrowseSection>()
            val sectionsArr = root.optJSONArray("sections")
            if (sectionsArr != null) {
                for (i in 0 until sectionsArr.length()) {
                    val secObj = sectionsArr.getJSONObject(i)
                    val secTitle = secObj.optString("title", "Seção ${i + 1}")
                    val secContent = secObj.optString("content", "")
                    val secBullets = mutableListOf<String>()
                    val secBulletsArr = secObj.optJSONArray("bulletPoints")
                    if (secBulletsArr != null) {
                        for (b in 0 until secBulletsArr.length()) {
                            val bText = secBulletsArr.optString(b, "").trim()
                            if (bText.isNotBlank()) secBullets.add(bText)
                        }
                    }
                    sections.add(BrowseSection(title = secTitle, content = secContent, bulletPoints = secBullets))
                }
            }

            BrowseForMeResult(
                query = query,
                headline = headline,
                quickAnswer = quickAnswer,
                sections = sections,
                keyTakeaways = takeaways,
                sources = sources
            )
        } catch (e: Exception) {
            Log.w("TesseraBrowser", "Falha ao fazer parse do JSON do Browse for Me", e)
            null
        }
    }

    private fun buildLocalEditorialResult(
        query: String,
        rawContentText: String,
        sources: List<BrowseSource>
    ): BrowseForMeResult {
        val formattedHeadline = query.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        val paragraphs = rawContentText.split("\n\n")
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 20 }

        val quickAnswer = if (paragraphs.isNotEmpty()) {
            paragraphs.first()
        } else {
            "Síntese consolidada das principais referências e artigos sobre \"$query\" extraídos das principais fontes da web."
        }

        val keyTakeaways = mutableListOf<String>()
        if (paragraphs.size > 1) {
            for (i in 1 until minOf(paragraphs.size, 4)) {
                val sentence = paragraphs[i].split(".").firstOrNull { it.trim().length > 15 }?.trim()
                if (!sentence.isNullOrBlank()) {
                    val emoji = when (i) {
                        1 -> "💡"
                        2 -> "📌"
                        else -> "⚡"
                    }
                    keyTakeaways.add("$emoji $sentence.")
                }
            }
        }
        if (keyTakeaways.isEmpty()) {
            keyTakeaways.add("💡 Principais tópicos sobre \"$query\" consolidados para leitura rápida.")
            keyTakeaways.add("📌 Fontes verificadas listadas abaixo para conferência integral.")
            keyTakeaways.add("⚡ Navegue diretamente pelas fontes ou expanda na busca web.")
        }

        val sections = mutableListOf<BrowseSection>()
        if (paragraphs.size > 2) {
            sections.add(
                BrowseSection(
                    title = "Visão Geral & Conceitos",
                    content = paragraphs.getOrNull(1) ?: quickAnswer,
                    bulletPoints = sources.take(3).map { "Fonte: ${it.title} (${it.domain})" }
                )
            )
            if (paragraphs.size > 3) {
                sections.add(
                    BrowseSection(
                        title = "Detalhes & Contexto",
                        content = paragraphs.drop(2).take(2).joinToString(" "),
                        bulletPoints = emptyList()
                    )
                )
            }
        } else {
            sections.add(
                BrowseSection(
                    title = "Resumo dos Resultados",
                    content = "Informações extraídas das principais plataformas e bancos de dados da internet sobre $query.",
                    bulletPoints = listOf(
                        "Artigos e referências consolidadas",
                        "Acesso rápido às fontes originais abaixo",
                        "Pesquisa contínua através do Tessera Browser"
                    )
                )
            )
        }

        return BrowseForMeResult(
            query = query,
            headline = formattedHeadline,
            quickAnswer = quickAnswer,
            sections = sections,
            keyTakeaways = keyTakeaways,
            sources = sources
        )
    }

    fun setGeminiApiKey(key: String) {
        _uiState.update { it.copy(geminiApiKey = key.trim().takeIf { k -> k.isNotBlank() }) }
        saveSettings()
    }

    // ARC PAGE SUMMARY (IA GEMINI / GRATUITA & FALLBACK LOCAL RESILIENTE)
    private var arcSummaryJob: Job? = null
    private var lastSummaryTitle: String = ""
    private var lastSummaryDomain: String = ""
    private var lastSummaryContent: String = ""

    fun prepareArcSummary(title: String, domain: String) {
        arcSummaryJob?.cancel()
        _uiState.update {
            it.copy(
                showArcSummary = true,
                isGeneratingArcSummary = true,
                arcSummaryTitle = title.ifBlank { "Página Atual" },
                arcSummaryDomain = domain,
                arcSummaryContent = null,
                arcSummaryError = null,
                showAiActionModal = false
            )
        }
    }

    fun requestArcSummary(title: String, domain: String, content: String) {
        if (content.isBlank()) {
            _uiState.update {
                it.copy(
                    showArcSummary = true,
                    isGeneratingArcSummary = false,
                    arcSummaryError = "Conteúdo insuficiente na página para gerar resumo."
                )
            }
            return
        }

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
            val cleanContent = if (content.length > 7000) content.take(7000) + "..." else content

            // TIER 1: Gemini API Oficial (se chave configurada pelo usuário)
            val geminiKey = _uiState.value.geminiApiKey?.trim().orEmpty()
            if (geminiKey.isNotBlank()) {
                try {
                    val geminiResult = callGeminiApi(geminiKey, title, domain, cleanContent)
                    if (!geminiResult.isNullOrBlank() && isValidAiSummary(geminiResult)) {
                        _uiState.update {
                            it.copy(
                                isGeneratingArcSummary = false,
                                arcSummaryContent = geminiResult.trim(),
                                arcSummaryError = null
                            )
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.w("TesseraBrowser", "Falha na chamada Gemini API oficial, tentando fallback", e)
                }
            }

            // TIER 2: Provedor IA Gratuito com validação de créditos/erros
            try {
                val freeAiResult = callFreeAiApi(title, domain, cleanContent)
                if (!freeAiResult.isNullOrBlank() && isValidAiSummary(freeAiResult)) {
                    _uiState.update {
                        it.copy(
                            isGeneratingArcSummary = false,
                            arcSummaryContent = freeAiResult.trim(),
                            arcSummaryError = null
                        )
                    }
                    return@launch
                }
            } catch (e: Exception) {
                Log.w("TesseraBrowser", "Falha no provedor IA gratuito, ativando fallback local", e)
            }

            // TIER 3: Motor de Resumo Extrativo Local (GARANTIA 100% FUNCIONAMENTO / OFFLINE)
            try {
                val localSummary = generateLocalArcSummary(title, domain, content)
                _uiState.update {
                    it.copy(
                        isGeneratingArcSummary = false,
                        arcSummaryContent = localSummary,
                        arcSummaryError = null
                    )
                }
            } catch (e: Exception) {
                Log.e("TesseraBrowser", "Falha no resumo local", e)
                _uiState.update {
                    it.copy(
                        isGeneratingArcSummary = false,
                        arcSummaryError = "Não foi possível sintetizar o resumo desta página."
                    )
                }
            }
        }
    }

    private fun callGeminiApi(apiKey: String, title: String, domain: String, content: String): String? {
        val systemPrompt = "Você é o assistente de síntese do navegador Tessera estilo Arc Search. Resuma a página de forma elegante, precisa e direta ao ponto em Português do Brasil.\n" +
                "Estrutura obrigatória:\n" +
                "1. Um parágrafo curto de Visão Geral (2 linhas).\n" +
                "2. De 3 a 5 pontos-chave principais, cada um iniciado por '-' e um emoji adequado (ex: '- 💡 Ponto importante...').\n" +
                "3. Uma conclusão curta e objetiva (1 linha).\n" +
                "Evite enrolação. Use Markdown limpo."

        val userPrompt = "Título: $title\nDomínio: $domain\n\nConteúdo da página:\n$content"

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\n$userPrompt")
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 800)
            })
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload.toString()) }

        if (conn.responseCode in 200..299) {
            val responseStr = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val root = JSONObject(responseStr)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val contentObj = first.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return null
    }

    private fun callFreeAiApi(title: String, domain: String, content: String): String? {
        val systemPrompt = "Você é a inteligência artificial do navegador Tessera estilo Arc Search. Resuma a página com alta precisão, elegância e foco no essencial em Português do Brasil.\n" +
                "Estrutura obrigatória:\n" +
                "1. Um parágrafo curto de Visão Geral (2 linhas).\n" +
                "2. De 3 a 5 pontos-chave principais, cada um iniciado por '-' e um emoji adequado (ex: '- 💡 Ponto importante...').\n" +
                "3. Uma conclusão curta e objetiva (1 a 2 linhas).\n" +
                "Evite enrolação. Use Markdown limpo."

        val userPrompt = "Título: $title\nDomínio: $domain\n\nConteúdo da página:\n$content"

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
            connectTimeout = 7000
            readTimeout = 12000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("User-Agent", "TesseraBrowser/1.5.0")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(reqJson.toString()) }

        if (conn.responseCode in 200..299) {
            val response = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            return response.trim()
        }
        return null
    }

    private fun isValidAiSummary(text: String): Boolean {
        val lower = text.lowercase()
        if (lower.contains("enough credits") ||
            lower.contains("top up") ||
            lower.contains("queue full") ||
            lower.contains("rate limit") ||
            lower.contains("error:") ||
            lower.contains("<!doctype") ||
            lower.contains("<html") ||
            text.length < 50
        ) {
            return false
        }
        return true
    }

    private fun generateLocalArcSummary(title: String, domain: String, content: String): String {
        val rawParagraphs = content.split("\n\n")
            .map { it.replace("\n", " ").trim() }
            .filter { it.length > 30 && !it.startsWith("http") && !it.contains("cookie", ignoreCase = true) }

        val allSentences = mutableListOf<String>()
        rawParagraphs.forEach { p ->
            val sentences = p.split(Regex("(?<=[.!?])\\s+"))
                .map { it.trim() }
                .filter { it.length in 35..250 && !it.contains("http") }
            allSentences.addAll(sentences)
        }

        val overview = if (rawParagraphs.isNotEmpty()) {
            val firstP = rawParagraphs.first()
            if (firstP.length > 220) firstP.take(217) + "..." else firstP
        } else if (title.isNotBlank()) {
            "Artigo publicado em $domain abordando os tópicos e novidades sobre $title."
        } else {
            "Síntese dos tópicos principais apresentados na página."
        }

        val candidates = allSentences.drop(1).distinct().filter { s ->
            s != overview && !s.startsWith("-") && !s.startsWith("http")
        }.sortedByDescending { s ->
            var score = s.length.coerceAtMost(160)
            if (s.any { it.isDigit() }) score += 20
            if (s.contains("%") || s.contains("R$") || s.contains("$")) score += 15
            if (s.contains("importante", ignoreCase = true) || s.contains("destaque", ignoreCase = true) || s.contains("principal", ignoreCase = true) || s.contains("novo", ignoreCase = true)) score += 20
            score
        }

        val arcEmojis = listOf("💡", "📌", "⚡", "🎯", "🔍")
        val selectedPoints = candidates.take(5)

        val bulletSection = buildString {
            if (selectedPoints.isNotEmpty()) {
                selectedPoints.forEachIndexed { idx, pt ->
                    val emoji = arcEmojis.getOrElse(idx) { "✨" }
                    val cleanPt = pt.replace(Regex("^[-*•\\s]+"), "").trim()
                    append("- $emoji $cleanPt\n")
                }
            } else {
                append("- 💡 ${title.ifBlank { "Tópico central identificado na página" }}\n")
                append("- 📌 Conteúdo textual compilado diretamente da página para leitura otimizada.\n")
                append("- ⚡ Navegação focada sem anúncios e sem distrações visuais.\n")
            }
        }.trimEnd()

        val conclusion = if (allSentences.size > 4) {
            val last = allSentences.last()
            if (last.length > 180) last.take(177) + "..." else last
        } else {
            "Artigo completo disponível para leitura imersiva no Tessera Browser."
        }

        return buildString {
            append("## Visão Geral\n")
            append(overview)
            append("\n\n")
            append("## Destaques Principais\n")
            append(bulletSection)
            append("\n\n")
            append("## Conclusão\n")
            append(conclusion)
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

    // PRIVACY & TRACKER SHIELD SUBSYSTEM
    fun recordBlockedTracker(host: String, url: String) {
        val (entityName, category) = PrivacyTrackerEngine.identifyTracker(host, url)
        _uiState.update { current ->
            val prevItems = current.privacyState.pageBlockedItems
            val existingIndex = prevItems.indexOfFirst { it.domain.equals(host, ignoreCase = true) }
            val updatedItems = if (existingIndex != -1) {
                prevItems.mapIndexed { idx, item ->
                    if (idx == existingIndex) item.copy(count = item.count + 1) else item
                }
            } else {
                listOf(BlockedTrackerItem(domain = host, entityName = entityName, category = category, count = 1)) + prevItems
            }

            val newPageCount = current.privacyState.pageBlockedCount + 1
            val newSessionCount = current.privacyState.sessionBlockedCount + 1
            val newTotalCount = current.privacyState.totalBlockedCount + 1
            val newDataSaved = current.privacyState.dataSavedBytes + PrivacyTrackerEngine.BYTES_PER_BLOCKED_REQUEST
            val newTimeSaved = current.privacyState.estimatedTimeSavedMs + PrivacyTrackerEngine.TIME_SAVED_PER_BLOCKED_MS

            current.copy(
                privacyState = current.privacyState.copy(
                    pageBlockedCount = newPageCount,
                    sessionBlockedCount = newSessionCount,
                    totalBlockedCount = newTotalCount,
                    pageBlockedItems = updatedItems,
                    dataSavedBytes = newDataSaved,
                    estimatedTimeSavedMs = newTimeSaved
                )
            )
        }

        val app = appContext
        if (app != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val total = _uiState.value.privacyState.totalBlockedCount
                app.getSharedPreferences("tessera_browser_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("total_blocked_trackers", total)
                    .apply()
            }
        }
    }

    fun resetPageTrackers(url: String) {
        val domain = try {
            val uri = java.net.URI(url)
            val h = uri.host ?: url
            if (h.startsWith("www.")) h.substring(4) else h
        } catch (e: Exception) {
            url
        }
        val isSecure = url.startsWith("https://", ignoreCase = true)
        _uiState.update { current ->
            current.copy(
                privacyState = current.privacyState.copy(
                    currentDomain = domain,
                    isCurrentSiteSecure = isSecure,
                    pageBlockedCount = 0,
                    pageBlockedItems = emptyList()
                )
            )
        }
    }

    fun togglePrivacyDashboard(visible: Boolean? = null) {
        _uiState.update { current ->
            val newVis = visible ?: !current.privacyState.isVisible
            current.copy(privacyState = current.privacyState.copy(isVisible = newVis))
        }
    }

    fun dismissPrivacyDashboard() {
        togglePrivacyDashboard(false)
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

    fun openFullSettings() {
        _uiState.update { it.copy(showFullSettings = true, showQuickSettings = false) }
    }

    fun dismissFullSettings() {
        _uiState.update { it.copy(showFullSettings = false) }
    }

    fun toggleFullSettings() {
        _uiState.update { it.copy(showFullSettings = !it.showFullSettings, showQuickSettings = false) }
    }

    fun openQuickSettings() {
        _uiState.update { it.copy(showQuickSettings = true, showFullSettings = false) }
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
    }
}
