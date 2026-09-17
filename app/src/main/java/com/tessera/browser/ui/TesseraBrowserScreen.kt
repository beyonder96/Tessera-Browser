package com.tessera.browser.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.DisposableEffect
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

    // Fetch initial weather and financial quotes for home widgets, and init downloads
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(context)
        viewModel.fetchQuotes()
        viewModel.initDownloads(context)
    }

    // Listen for system download completions to update status in real time
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id != -1L && ctx != null) {
                        viewModel.onDownloadCompleted(ctx, id)
                    }
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // ignore
            }
        }
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
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                                // Standard modern Chrome mobile UA so AI pages (Duck.ai, Perplexity, etc.) load flawlessly
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.107 Mobile Safari/537.36"

                                applyForceDark(this, state.forceDarkPages)
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val urlString = request?.url?.toString() ?: return false
                                    if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                                        return try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                                            context.startActivity(intent)
                                            true
                                        } catch (e: Exception) {
                                            true
                                        }
                                    }

                                    // Intercept direct downloadable files
                                    val cleanUrl = urlString.split("?").firstOrNull()?.lowercase() ?: ""
                                    val isDirectDownload = cleanUrl.endsWith(".apk") || cleanUrl.endsWith(".pdf") ||
                                            cleanUrl.endsWith(".zip") || cleanUrl.endsWith(".rar") || cleanUrl.endsWith(".7z") ||
                                            cleanUrl.endsWith(".tar") || cleanUrl.endsWith(".gz") || cleanUrl.endsWith(".mp3") ||
                                            cleanUrl.endsWith(".mp4") || cleanUrl.endsWith(".wav") || cleanUrl.endsWith(".docx") ||
                                            cleanUrl.endsWith(".xlsx") || cleanUrl.endsWith(".pptx") || cleanUrl.endsWith(".csv")

                                    if (isDirectDownload) {
                                        val downloadCtx = view?.context ?: context
                                        val downloadId = viewModel.enqueueDownload(
                                            context = downloadCtx,
                                            url = urlString,
                                            userAgent = view?.settings?.userAgentString ?: ""
                                        )
                                        if (downloadId != -1L) {
                                            val guessedName = URLUtil.guessFileName(urlString, null, null)
                                            Toast.makeText(downloadCtx, "Iniciando download: $guessedName", Toast.LENGTH_SHORT).show()
                                        }
                                        return true
                                    }

                                    return false
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
                                        if (state.isReaderModeActive) {
                                            viewModel.toggleReaderMode()
                                        }
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (!url.isNullOrBlank()) {
                                        lastLoadedUrl = url
                                        viewModel.onPageFinished(url, canGoBack(), canGoForward(), view?.title)

                                        // Detect Reader Mode availability on actual articles/content pages
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                var host = (window.location.hostname || '').toLowerCase();
                                                if (host.includes('google.') || host.includes('duckduckgo.') || host.includes('bing.') || host.includes('duck.ai') || host.includes('youtube.com')) {
                                                    return 'false';
                                                }
                                                var article = document.querySelector('article, [itemprop="articleBody"], main article, .article-body, .post-content, .entry-content, [role="main"] article');
                                                if (article && (article.innerText || '').trim().length > 350) {
                                                    return 'true';
                                                }
                                                var paragraphs = document.querySelectorAll('p');
                                                var substantialP = 0;
                                                for (var i = 0; i < paragraphs.length; i++) {
                                                    if ((paragraphs[i].innerText || '').trim().length > 70) {
                                                        substantialP++;
                                                    }
                                                }
                                                return (substantialP >= 3) ? 'true' : 'false';
                                            })()
                                            """.trimIndent()
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
                                    val downloadId = viewModel.enqueueDownload(
                                        context = ctx,
                                        url = url,
                                        userAgent = userAgent,
                                        contentDisposition = contentDisposition,
                                        mimeType = mimetype
                                    )
                                    if (downloadId != -1L) {
                                        Toast.makeText(ctx, "Iniciando download: $fileName", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(ctx, "Falha ao iniciar download", Toast.LENGTH_SHORT).show()
                                    }
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
                        val isDark = state.isDarkMode
                        val js = """
                            (function() {
                                var existing = document.getElementById('tessera-immersive-reader');
                                if (existing) existing.remove();

                                var domain = (window.location.hostname || '').replace('www.', '');
                                var titleEl = document.querySelector('h1, [property="og:title"], .article-title, .entry-title');
                                var title = titleEl ? (titleEl.getAttribute('content') || titleEl.innerText || document.title) : document.title;

                                var authorEl = document.querySelector('[rel="author"], .byline, .author, meta[name="author"], meta[property="article:author"]');
                                var author = authorEl ? (authorEl.getAttribute('content') || authorEl.innerText || '').trim() : '';

                                var timeEl = document.querySelector('time, [property="article:published_time"], .date, .published');
                                var dateStr = timeEl ? (timeEl.getAttribute('datetime') || timeEl.innerText || '').trim() : '';
                                if (dateStr.length > 35) dateStr = dateStr.substring(0, 35);

                                var candidate = document.querySelector('article, [itemprop="articleBody"], main article, .article-body, .post-content, .entry-content, [role="main"] article, [role="main"]');
                                var paragraphs = [];
                                if (candidate) {
                                    var nodes = candidate.querySelectorAll('h1, h2, h3, h4, p, blockquote, ul, ol, img');
                                    for (var i = 0; i < nodes.length; i++) {
                                        var n = nodes[i];
                                        if (n.closest('.ad, .ads, .sidebar, .comments, #comments, nav, header, footer, .share, .social')) continue;
                                        paragraphs.push(n.outerHTML);
                                    }
                                }
                                if (paragraphs.length < 2) {
                                    var allP = document.querySelectorAll('p');
                                    for (var i = 0; i < allP.length; i++) {
                                        var p = allP[i];
                                        if ((p.innerText || '').trim().length > 35 && !p.closest('.ad, .comments, nav, footer, aside, header')) {
                                            paragraphs.push(p.outerHTML);
                                        }
                                    }
                                }

                                var contentHtml = paragraphs.join('');
                                var words = (contentHtml.replace(/<[^>]*>/g, ' ').match(/\S+/g) || []).length;
                                var minutes = Math.max(1, Math.round(words / 200));

                                var isDarkTheme = $isDark;
                                var bg = isDarkTheme ? '#141414' : '#FBF9F5';
                                var text = isDarkTheme ? '#E2E2E2' : '#222222';
                                var sub = isDarkTheme ? '#9E9E9E' : '#666666';
                                var toolbarBg = isDarkTheme ? 'rgba(26, 26, 28, 0.94)' : 'rgba(255, 255, 255, 0.94)';

                                var reader = document.createElement('div');
                                reader.id = 'tessera-immersive-reader';
                                reader.style.cssText = 'position:fixed!important;top:0!important;left:0!important;right:0!important;bottom:0!important;width:100vw!important;height:100vh!important;z-index:2147483647!important;background:' + bg + '!important;overflow-y:auto!important;-webkit-overflow-scrolling:touch!important;margin:0!important;padding:0!important;';

                                var bylineHtml = '';
                                if (author || dateStr) {
                                    bylineHtml = '<div style="font-size:14px;color:' + sub + ';margin-bottom:24px;font-family:-apple-system,sans-serif;">' +
                                        (author ? 'Por <b>' + author + '</b> ' : '') +
                                        (dateStr ? '• ' + dateStr : '') + '</div>';
                                }

                                reader.innerHTML = '' +
                                    '<div id="tir-toolbar" style="position:sticky;top:0;z-index:1000;display:flex;align-items:center;justify-content:space-between;padding:12px 18px;background:' + toolbarBg + ';backdrop-filter:blur(16px);-webkit-backdrop-filter:blur(16px);border-bottom:1px solid ' + (isDarkTheme ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)') + ';font-family:-apple-system,sans-serif;">' +
                                    '  <div style="display:flex;align-items:center;gap:8px;font-size:13px;color:' + sub + ';">' +
                                    '    <span style="font-weight:700;color:' + text + ';">' + domain + '</span>' +
                                    '    <span>•</span>' +
                                    '    <span>' + minutes + ' min de leitura</span>' +
                                    '  </div>' +
                                    '  <div style="display:flex;align-items:center;gap:8px;">' +
                                    '    <button id="tir-font-dec" style="background:none;border:1px solid ' + sub + ';color:' + text + ';border-radius:6px;padding:3px 7px;font-size:12px;font-weight:bold;cursor:pointer;">A-</button>' +
                                    '    <button id="tir-font-inc" style="background:none;border:1px solid ' + sub + ';color:' + text + ';border-radius:6px;padding:3px 7px;font-size:13px;font-weight:bold;cursor:pointer;">A+</button>' +
                                    '    <span id="tir-theme-light" style="width:20px;height:20px;border-radius:50%;background:#FFFFFF;border:1px solid #CCC;cursor:pointer;display:inline-block;"></span>' +
                                    '    <span id="tir-theme-sepia" style="width:20px;height:20px;border-radius:50%;background:#F8F1E3;border:1px solid #D6C7A8;cursor:pointer;display:inline-block;"></span>' +
                                    '    <span id="tir-theme-dark" style="width:20px;height:20px;border-radius:50%;background:#141414;border:1px solid #555;cursor:pointer;display:inline-block;"></span>' +
                                    '    <button id="tir-exit" style="background:' + (isDarkTheme ? '#333333' : '#E8E8E8') + ';border:none;color:' + text + ';border-radius:14px;padding:5px 12px;margin-left:6px;font-size:12.5px;font-weight:600;cursor:pointer;">✕ Sair</button>' +
                                    '  </div>' +
                                    '</div>' +
                                    '<div id="tir-article-container" style="max-width:680px;margin:0 auto;padding:28px 20px 90px;font-family:Georgia,Cambria,\'Times New Roman\',serif;font-size:19px;line-height:1.8;color:' + text + ';letter-spacing:0.01em;">' +
                                    '  <h1 id="tir-title" style="font-family:-apple-system,BlinkMacSystemFont,\'Segoe UI\',Roboto,sans-serif;font-size:28px;line-height:1.25;font-weight:800;margin-bottom:12px;color:' + text + ';">' + title + '</h1>' +
                                    bylineHtml +
                                    '  <div id="tir-content">' + contentHtml + '</div>' +
                                    '</div>' +
                                    '<style>' +
                                    '#tir-content img { max-width:100%!important; height:auto!important; border-radius:10px!important; margin:18px 0!important; display:block!important; }' +
                                    '#tir-content p { margin-bottom:1.55em!important; }' +
                                    '#tir-content h2, #tir-content h3 { margin-top:1.6em!important; margin-bottom:0.6em!important; font-family:-apple-system,sans-serif!important; line-height:1.35!important; }' +
                                    '#tir-content blockquote { border-left:4px solid #64B5F6!important; margin:1.6em 0!important; padding-left:16px!important; font-style:italic!important; opacity:0.9!important; }' +
                                    '#tir-content a { color:#64B5F6!important; text-decoration:underline!important; }' +
                                    '</style>';

                                document.body.appendChild(reader);
                                document.documentElement.style.overflow = 'hidden';

                                var currentFontSize = 19;
                                var container = document.getElementById('tir-article-container');
                                document.getElementById('tir-font-inc').onclick = function() {
                                    if (currentFontSize < 28) {
                                        currentFontSize += 2;
                                        container.style.fontSize = currentFontSize + 'px';
                                    }
                                };
                                document.getElementById('tir-font-dec').onclick = function() {
                                    if (currentFontSize > 14) {
                                        currentFontSize -= 2;
                                        container.style.fontSize = currentFontSize + 'px';
                                    }
                                };
                                function applyTheme(newBg, newText, newSub, newTb) {
                                    reader.style.backgroundColor = newBg;
                                    container.style.color = newText;
                                    var t = document.getElementById('tir-title');
                                    if (t) t.style.color = newText;
                                    var tb = document.getElementById('tir-toolbar');
                                    if (tb) {
                                        tb.style.backgroundColor = newTb;
                                        tb.style.borderColor = (newBg === '#141414') ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)';
                                    }
                                }
                                document.getElementById('tir-theme-light').onclick = function() {
                                    applyTheme('#FFFFFF', '#1A1A1A', '#666666', 'rgba(255,255,255,0.96)');
                                };
                                document.getElementById('tir-theme-sepia').onclick = function() {
                                    applyTheme('#F8F1E3', '#2C2218', '#736151', 'rgba(248,241,227,0.96)');
                                };
                                document.getElementById('tir-theme-dark').onclick = function() {
                                    applyTheme('#141414', '#E2E2E2', '#9E9E9E', 'rgba(26,26,28,0.96)');
                                };
                                document.getElementById('tir-exit').onclick = function() {
                                    reader.remove();
                                    document.documentElement.style.overflow = '';
                                };
                            })();
                        """.trimIndent()
                        webViewInstance?.evaluateJavascript(js, null)
                    } else {
                        webViewInstance?.evaluateJavascript(
                            "(function() { var el = document.getElementById('tessera-immersive-reader'); if (el) el.remove(); document.documentElement.style.overflow = ''; })()",
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
                downloads = state.downloads,
                initialTab = state.activeHubTab,
                onTabSelected = { viewModel.setActiveHubTab(it) },
                onSelectUrl = {
                    viewModel.dismissHistoryModal()
                    viewModel.openUrl(it)
                },
                onRemoveBookmark = { viewModel.removeSpeedDialItem(it) },
                onClearHistory = { viewModel.clearHistory() },
                onOpenDownload = { viewModel.openDownloadedFile(context, it) },
                onShareDownload = { viewModel.shareDownloadedFile(context, it) },
                onRemoveDownload = { viewModel.removeDownload(context, it) },
                onClearDownloads = { viewModel.clearDownloads(context) },
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
                    viewModel.openHistoryModal(0)
                },
                onOpenDownloads = {
                    viewModel.dismissQuickSettings()
                    viewModel.openDownloadsModal()
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
