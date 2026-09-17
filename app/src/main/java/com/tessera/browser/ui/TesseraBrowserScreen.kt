package com.tessera.browser.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.tessera.browser.ui.components.AiActionsModal
import com.tessera.browser.ui.components.HistoryBookmarksModal
import com.tessera.browser.ui.components.QuickSettingsPanel
import com.tessera.browser.ui.components.TabsModal
import com.tessera.browser.ui.components.TesseraAirBar
import com.tessera.browser.ui.components.TesseraStartPage
import com.tessera.browser.viewmodel.BrowserViewModel
import java.io.ByteArrayInputStream

private val AdBlockHosts = setOf(
    "doubleclick.net", "googleadservices.com", "googlesyndication.com",
    "pagead2.googlesyndication.com", "adservice.google.com", "admob.com",
    "taboola.com", "outbrain.com", "popads.net", "adnxs.com", "criteo.com",
    "amazon-adsystem.com", "scorecardresearch.com", "quantserve.com",
    "zedo.com", "advertising.com", "rubiconproject.com", "pubmatic.com"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TesseraBrowserScreen(viewModel: BrowserViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Fetch initial weather and financial quotes for home widgets
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(context)
        viewModel.fetchQuotes()
    }

    // Synchronize navigation requests from state to WebView safely
    LaunchedEffect(state.currentUrl, state.isHomePage, state.activeTabId) {
        if (!state.isHomePage && state.currentUrl.isNotBlank() && state.currentUrl != lastLoadedUrl) {
            lastLoadedUrl = state.currentUrl
            webViewInstance?.loadUrl(state.currentUrl)
        }
    }

    // File Upload (<input type="file">) Callback & Activity Result Launcher
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent?.clipData != null) {
                val clipData = intent.clipData!!
                Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
            } else if (intent?.data != null) {
                arrayOf(intent.data!!)
            } else {
                null
            }
        } else {
            null
        }
        fileUploadCallback?.onReceiveValue(uris)
        fileUploadCallback = null
    }

    var isAirBarExpanded by remember { mutableStateOf(false) }

    // System Back navigation priority:
    // 1. Dismiss Quick Settings
    // 2. Dismiss Tabs Modal
    // 3. Dismiss History/Bookmarks Modal
    // 4. Dismiss AI Actions Modal
    // 5. Collapse expanded AirBar
    // 6. WebView history back
    // 7. Go Home
    BackHandler(enabled = !state.isHomePage || state.showQuickSettings || state.showTabsModal || state.showHistoryModal || state.showAiActionModal || isAirBarExpanded) {
        if (state.showQuickSettings) {
            viewModel.dismissQuickSettings()
        } else if (state.showTabsModal) {
            viewModel.dismissTabsModal()
        } else if (state.showHistoryModal) {
            viewModel.dismissHistoryModal()
        } else if (state.showAiActionModal) {
            viewModel.dismissAiActionModal()
        } else if (isAirBarExpanded) {
            isAirBarExpanded = false
        } else if (state.canGoBack) {
            webViewInstance?.goBack()
        } else {
            viewModel.goHome()
        }
    }

    // Scroll detector to show/hide AirBar when browsing
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y < -15f) viewModel.setBarVisibility(false)
                else if (available.y > 15f) viewModel.setBarVisibility(true)
                return Offset.Zero
            }
        }
    }

    val rootBg = if (state.isDarkMode) Color(0xFF120E0D) else Color(0xFFF7F8FA)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBg)
    ) {
        if (state.isHomePage) {
            // NATIVE START PAGE / LUPA CENTRAL & BARRA DE FAVORITOS
            TesseraStartPage(
                activeWallpaper = state.activeWallpaper,
                showWallpaper = state.showWallpaper,
                customWallpaperUri = state.customWallpaperUri,
                showCatInara = state.showCatInara,
                isDarkMode = state.isDarkMode,
                favorites = state.speedDialItems,
                searchSuggestions = state.searchSuggestions,
                trendingTopics = state.trendingTopics,
                showWeatherWidget = state.showWeatherWidget,
                showQuotesWidget = state.showQuotesWidget,
                weatherData = state.weatherData,
                quotesData = state.quotesData,
                onRefreshWeather = { viewModel.fetchWeather(context, forceRefresh = true) },
                onSearchQueryChange = { query -> viewModel.fetchSearchSuggestions(query) },
                onSearch = { query -> viewModel.openUrl(query) },
                onOpenAi = { query -> viewModel.openAiQuery(query) },
                onOpenUrl = { url -> viewModel.openUrl(url) },
                onOpenSettings = { viewModel.toggleQuickSettings() }
            )
        } else {
            // WEBVIEW BROWSER VIEW
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(top = 6.dp),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            isNestedScrollingEnabled = true

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                                // Remove '; wv' and 'Version/4.0 ' so modern AI and web pages don't block mobile browser
                                val rawUa = userAgentString
                                userAgentString = rawUa.replace("; wv", "").replace("Version/4.0 ", "")

                                applyForceDark(this, state.forceDarkPages)
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val urlString = request?.url?.toString() ?: return false
                                    return if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                                            context.startActivity(intent)
                                            true
                                        } catch (e: Exception) {
                                            true
                                        }
                                    } else {
                                        false
                                    }
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    if (state.adBlockEnabled) {
                                        val host = request?.url?.host?.lowercase() ?: ""
                                        val urlString = request?.url?.toString()?.lowercase() ?: ""
                                        val isAd = AdBlockHosts.any { host.endsWith(it) } ||
                                                urlString.contains("/pagead/") ||
                                                urlString.contains("/adservice/") ||
                                                urlString.contains("/ads/")
                                        if (isAd) {
                                            return WebResourceResponse(
                                                "text/plain",
                                                "UTF-8",
                                                ByteArrayInputStream(ByteArray(0))
                                            )
                                        }
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    if (!url.isNullOrBlank()) {
                                        lastLoadedUrl = url
                                        viewModel.onPageStarted(url)
                                        viewModel.setReaderModeAvailable(false)
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (!url.isNullOrBlank()) {
                                        lastLoadedUrl = url
                                        viewModel.onPageFinished(url, canGoBack(), canGoForward(), view?.title)

                                        // Detect Reader Mode availability on page
                                        view?.evaluateJavascript(
                                            "(function() { " +
                                            "  var article = document.querySelector('article, [itemprop=\"articleBody\"], main, .post-content, .entry-content'); " +
                                            "  var pCount = document.querySelectorAll('p').length; " +
                                            "  var textLength = document.body ? (document.body.innerText || '').length : 0; " +
                                            "  return (article !== null || pCount >= 4 || textLength > 1200) ? 'true' : 'false'; " +
                                            "})()"
                                        ) { result ->
                                            val isAvailable = result?.replace("\"", "")?.trim() == "true"
                                            viewModel.setReaderModeAvailable(isAvailable)
                                        }
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    viewModel.updateProgress(newProgress / 100f)
                                }

                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    filePathCallback: ValueCallback<Array<Uri>>?,
                                    fileChooserParams: FileChooserParams?
                                ): Boolean {
                                    fileUploadCallback?.onReceiveValue(null)
                                    fileUploadCallback = filePathCallback

                                    return try {
                                        val intent = fileChooserParams?.createIntent()
                                            ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                                type = "*/*"
                                                addCategory(Intent.CATEGORY_OPENABLE)
                                            }
                                        fileChooserLauncher.launch(intent)
                                        true
                                    } catch (e: Exception) {
                                        fileUploadCallback?.onReceiveValue(null)
                                        fileUploadCallback = null
                                        false
                                    }
                                }
                            }

                            // File Downloads Handler
                            setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                                try {
                                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                                        setMimeType(mimetype)
                                        val cookies = CookieManager.getInstance().getCookie(url)
                                        if (cookies != null) {
                                            addRequestHeader("cookie", cookies)
                                        }
                                        addRequestHeader("User-Agent", userAgent)
                                        setDescription("Baixando com Tessera Browser...")
                                        setTitle(fileName)
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                    }

                                    val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    downloadManager.enqueue(request)
                                    Toast.makeText(ctx, "Iniciando download: $fileName", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Falha ao iniciar download", Toast.LENGTH_SHORT).show()
                                }
                            }

                            loadUrl(state.currentUrl)
                            lastLoadedUrl = state.currentUrl
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        applyForceDark(view.settings, state.forceDarkPages)
                    }
                )

                // Scrim when AirBar is expanded over web content to collapse easily
                if (isAirBarExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                isAirBarExpanded = false
                            }
                    )
                }
            }
        }

        // Floating TesseraAirBar (Docked at bottom on BOTH Home and Web browsing modes)
        AnimatedVisibility(
            visible = state.isBarVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(280)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(280)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            TesseraAirBar(
                progress = state.progress,
                displayUrl = if (state.isHomePage) "" else state.displayUrl,
                canGoBack = if (state.isHomePage) false else state.canGoBack,
                canGoForward = if (state.isHomePage) false else state.canGoForward,
                tabCount = state.tabs.size,
                isBookmarked = if (state.isHomePage) false else state.isCurrentPageBookmarked,
                isIncognito = state.isIncognitoMode,
                isDarkMode = state.isDarkMode,
                isReaderModeActive = state.isReaderModeActive,
                isReaderModeAvailable = state.isReaderModeAvailable,
                favorites = state.speedDialItems,
                onBack = { webViewInstance?.goBack() },
                onForward = { webViewInstance?.goForward() },
                onHome = { viewModel.goHome() },
                onReload = { webViewInstance?.reload() },
                onSearch = { query -> viewModel.openUrl(query) },
                onFastAction = {
                    if (state.isHomePage) {
                        viewModel.openAiQuery("")
                    } else {
                        webViewInstance?.reload()
                    }
                },
                onOpenAiAction = { viewModel.toggleAiActionModal() },
                onToggleBookmark = {
                    if (state.isHomePage) {
                        viewModel.toggleHistoryModal()
                    } else {
                        viewModel.toggleBookmark(
                            title = webViewInstance?.title ?: "",
                            url = state.displayUrl
                        )
                    }
                },
                onToggleIncognito = { viewModel.toggleIncognitoMode() },
                onToggleReaderMode = {
                    val willBeActive = !state.isReaderModeActive
                    viewModel.toggleReaderMode()
                    if (willBeActive) {
                        val bg = if (state.isDarkMode) "#141414" else "#FBF9F5"
                        val fg = if (state.isDarkMode) "#E6E6E6" else "#202020"
                        val css = """
                            (function() {
                                var existing = document.getElementById('tessera-reader-style');
                                if (existing) existing.remove();
                                var style = document.createElement('style');
                                style.id = 'tessera-reader-style';
                                style.innerHTML = `
                                    header, footer, nav, aside, iframe, .advertisement, .ad, .ads, .sidebar, .comments, #comments, .social-share, .cookie-banner {
                                        display: none !important;
                                    }
                                    body {
                                        background-color: $bg !important;
                                        color: $fg !important;
                                        max-width: 720px !important;
                                        margin: 0 auto !important;
                                        padding: 28px 20px !important;
                                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif !important;
                                        font-size: 20px !important;
                                        line-height: 1.75 !important;
                                        letter-spacing: 0.01em !important;
                                    }
                                    p, li {
                                        font-size: 20px !important;
                                        line-height: 1.75 !important;
                                        color: $fg !important;
                                        margin-bottom: 1.5em !important;
                                    }
                                    h1, h2, h3, h4 {
                                        color: $fg !important;
                                        line-height: 1.3 !important;
                                        margin-top: 1.6em !important;
                                        margin-bottom: 0.7em !important;
                                    }
                                    img {
                                        max-width: 100% !important;
                                        height: auto !important;
                                        border-radius: 12px !important;
                                        margin: 16px 0 !important;
                                    }
                                `;
                                document.head.appendChild(style);
                            })()
                        """.trimIndent()
                        webViewInstance?.evaluateJavascript(css, null)
                    } else {
                        webViewInstance?.evaluateJavascript(
                            "(function() { var el = document.getElementById('tessera-reader-style'); if (el) el.remove(); })()",
                            null
                        )
                    }
                },
                onOpenTabs = { viewModel.toggleTabsModal() },
                onOpenHistory = { viewModel.toggleHistoryModal() },
                onOpenSettings = { viewModel.toggleQuickSettings() },
                onOpenFavorite = { url -> viewModel.openUrl(url) },
                accentColor = state.activeWallpaper.accentColor
            )
        }

        // TABS MODAL OVERLAY
        if (state.showTabsModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.dismissTabsModal()
                    }
            )
        }

        AnimatedVisibility(
            visible = state.showTabsModal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            TabsModal(
                tabs = state.tabs,
                activeTabId = state.activeTabId,
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.addNewTab() },
                onDismiss = { viewModel.dismissTabsModal() },
                onTogglePin = { viewModel.togglePinTab(it) },
                onCloseAllTabs = { viewModel.closeAllTabs() },
                onOpenHistory = {
                    viewModel.dismissTabsModal()
                    viewModel.toggleHistoryModal()
                },
                accentColor = state.activeWallpaper.accentColor,
                isDarkMode = state.isDarkMode
            )
        }

        // HISTORY & BOOKMARKS MODAL OVERLAY
        if (state.showHistoryModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.dismissHistoryModal()
                    }
            )
        }

        AnimatedVisibility(
            visible = state.showHistoryModal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HistoryBookmarksModal(
                bookmarks = state.speedDialItems,
                history = state.history,
                onSelectUrl = {
                    viewModel.dismissHistoryModal()
                    viewModel.openUrl(it)
                },
                onRemoveBookmark = { viewModel.removeSpeedDialItem(it) },
                onClearHistory = { viewModel.clearHistory() },
                onDismiss = { viewModel.dismissHistoryModal() },
                accentColor = state.activeWallpaper.accentColor
            )
        }

        // QUICK AI ACTIONS MODAL OVERLAY
        if (state.showAiActionModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.dismissAiActionModal()
                    }
            )
        }

        AnimatedVisibility(
            visible = state.showAiActionModal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AiActionsModal(
                pageUrl = state.displayUrl,
                onAction = { action -> viewModel.openAiAction(action) },
                onDismiss = { viewModel.dismissAiActionModal() }
            )
        }

        // QUICK SETTINGS SCRIM OVERLAY
        AnimatedVisibility(
            visible = state.showQuickSettings,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.dismissQuickSettings()
                    }
            )
        }

        // QUICK SETTINGS BOTTOM SHEET PANEL
        AnimatedVisibility(
            visible = state.showQuickSettings,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(320)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(280)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            QuickSettingsPanel(
                isDarkMode = state.isDarkMode,
                forceDarkPages = state.forceDarkPages,
                showWallpaper = state.showWallpaper,
                selectedWallpaperId = state.selectedWallpaperId,
                showFavoritesBar = state.showFavoritesBar,
                showCatInara = state.showCatInara,
                tesseraAiEnabled = state.tesseraAiEnabled,
                aiToolbarButton = state.aiToolbarButton,
                aiTextHighlightPrompts = state.aiTextHighlightPrompts,
                showSidebar = state.showSidebar,
                autoHideSidebar = state.autoHideSidebar,
                adBlockEnabled = state.adBlockEnabled,
                showWeatherWidget = state.showWeatherWidget,
                showQuotesWidget = state.showQuotesWidget,
                onDarkModeChanged = { viewModel.setDarkMode(it) },
                onForceDarkPagesChanged = { viewModel.setForceDarkPages(it) },
                onShowWallpaperChanged = { viewModel.setShowWallpaper(it) },
                onSelectWallpaper = { viewModel.selectWallpaper(it) },
                onShowFavoritesBarChanged = { viewModel.setShowFavoritesBar(it) },
                onShowCatInaraChanged = { viewModel.setShowCatInara(it) },
                onShowWeatherWidgetChanged = { viewModel.setShowWeatherWidget(it) },
                onShowQuotesWidgetChanged = { viewModel.setShowQuotesWidget(it) },
                onTesseraAiChanged = { viewModel.setTesseraAiEnabled(it) },
                onAiToolbarButtonChanged = { viewModel.setAiToolbarButton(it) },
                onAiTextHighlightPromptsChanged = { viewModel.setAiTextHighlightPrompts(it) },
                onShowSidebarChanged = { viewModel.setShowSidebar(it) },
                onAutoHideSidebarChanged = { viewModel.setAutoHideSidebar(it) },
                onAdBlockChanged = { viewModel.setAdBlockEnabled(it) },
                onOpenHistory = {
                    viewModel.dismissQuickSettings()
                    viewModel.toggleHistoryModal()
                },
                onDismiss = { viewModel.dismissQuickSettings() }
            )
        }
    }
}

private fun applyForceDark(settings: android.webkit.WebSettings, forceDark: Boolean) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, forceDark)
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        @Suppress("DEPRECATION")
        WebSettingsCompat.setForceDark(
            settings,
            if (forceDark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
        )
    }
}
