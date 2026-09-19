package com.tessera.browser.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.JsResult
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
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import android.webkit.SafeBrowsingResponse
import com.tessera.browser.data.SafeBrowsingThreatInfo
import com.tessera.browser.ui.components.AiActionsModal
import com.tessera.browser.ui.components.ArcSummarySheet
import com.tessera.browser.ui.components.FindInPageBar
import com.tessera.browser.ui.components.HistoryBookmarksModal
import com.tessera.browser.ui.components.PeekPreviewModal
import com.tessera.browser.ui.components.QrCodeShareModal
import com.tessera.browser.ui.components.QuickSettingsPanel
import com.tessera.browser.ui.components.SafeBrowsingWarningView
import com.tessera.browser.ui.components.TabsModal
import com.tessera.browser.ui.components.TesseraAirBar
import com.tessera.browser.ui.components.TesseraReaderScreen
import com.tessera.browser.ui.components.TesseraStartPage
import com.tessera.browser.viewmodel.BrowserViewModel
import com.tessera.browser.viewmodel.ReaderArticle
import com.tessera.browser.viewmodel.ReaderBlock
import com.tessera.browser.viewmodel.ReaderBlockType
import android.webkit.WebResourceError
import com.tessera.browser.data.PageErrorInfo
import com.tessera.browser.ui.components.DownloadVisualBanner
import com.tessera.browser.ui.components.TesseraOfflineErrorView
import java.io.ByteArrayInputStream
import org.json.JSONArray
import org.json.JSONObject

private const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.107 Mobile Safari/537.36"
private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

private val AdBlockHosts = setOf(
    "doubleclick.net", "googleadservices.com", "googlesyndication.com",
    "pagead2.googlesyndication.com", "adservice.google.com", "admob.com",
    "taboola.com", "outbrain.com", "popads.net", "adnxs.com", "criteo.com",
    "amazon-adsystem.com", "scorecardresearch.com", "quantserve.com",
    "zedo.com", "advertising.com", "rubiconproject.com", "pubmatic.com",
    "googletagservices.com", "adcolony.com", "appsflyer.com", "branch.io",
    "chartbeat.com", "smartadserver.com", "casalemedia.com", "criteo.net",
    "yieldmo.com", "adroll.com", "inmobi.com", "unityads.unity3d.com"
)

private val COSMETIC_ADBLOCK_SCRIPT = """
    (function() {
        var selectors = [
            'ins.adsbygoogle', '.adsbygoogle',
            'div[id^="google_ads"]', 'div[id^="div-gpt-ad"]',
            '.ad-container', '.ad-box', '.ad-wrapper', '.ad-banner',
            '.advertisement', '.advertising', '.ad-slot', '.ad_unit',
            '[class*="sponsored-post"]', '[data-ad-unit]', '[data-ad-client]',
            '.taboola', '.outbrain', '.trc_related_container',
            'iframe[src*="doubleclick"]', 'iframe[src*="googleads"]',
            'iframe[src*="amazon-adsystem"]', 'iframe[id*="google_ads"]'
        ];
        var style = document.getElementById('tessera-cosmetic-adblock');
        if (!style) {
            style = document.createElement('style');
            style.id = 'tessera-cosmetic-adblock';
            style.textContent = selectors.join(', ') + ' { display: none !important; height: 0 !important; min-height: 0 !important; visibility: hidden !important; margin: 0 !important; padding: 0 !important; }';
            (document.head || document.documentElement).appendChild(style);
        }
        selectors.forEach(function(sel) {
            try {
                var els = document.querySelectorAll(sel);
                for (var i = 0; i < els.length; i++) {
                    els[i].style.setProperty('display', 'none', 'important');
                    els[i].style.setProperty('height', '0px', 'important');
                    els[i].style.setProperty('visibility', 'hidden', 'important');
                }
            } catch (e) {}
        });
    })();
""".trimIndent()


private val READER_EXTRACTION_SCRIPT = """
    (function() {
        try {
            var domain = (window.location.hostname || '').replace('www.', '');

            var title = '';
            var metaOgTitle = document.querySelector('meta[property="og:title"]');
            var metaTwitterTitle = document.querySelector('meta[name="twitter:title"]');
            var h1 = document.querySelector('h1');
            if (metaOgTitle && metaOgTitle.content) {
                title = metaOgTitle.content.trim();
            } else if (metaTwitterTitle && metaTwitterTitle.content) {
                title = metaTwitterTitle.content.trim();
            } else if (h1 && (h1.innerText || '').trim().length > 5) {
                title = h1.innerText.trim();
            } else {
                title = document.title || '';
            }

            var author = '';
            var authorMeta = document.querySelector('meta[name="author"], meta[property="article:author"], meta[name="byl"]');
            var authorEl = document.querySelector('[rel="author"], .byline, .author, .c-byline__item, .author-name, .article__author');
            if (authorMeta && authorMeta.content) {
                author = authorMeta.content.trim();
            } else if (authorEl && (authorEl.innerText || '').trim()) {
                author = authorEl.innerText.trim();
            }

            var dateStr = '';
            var timeMeta = document.querySelector('meta[property="article:published_time"], meta[name="pubdate"], meta[name="date"]');
            var timeEl = document.querySelector('time, [property="article:published_time"], .date, .published, .datetime');
            if (timeMeta && timeMeta.content) {
                dateStr = timeMeta.content.trim();
            } else if (timeEl) {
                dateStr = (timeEl.getAttribute('datetime') || timeEl.innerText || '').trim();
            }
            if (dateStr.length > 35) dateStr = dateStr.substring(0, 35);

            var clone = document.body.cloneNode(true);

            var unwantedSelectors = [
                'script', 'style', 'noscript', 'iframe', 'svg', 'canvas', 'nav', 'footer', 'header',
                'form', 'button', 'input', 'select', 'textarea',
                '.ad', '.ads', '.advertisement', '[id*="google_ads"]', '[class*="google_ads"]',
                '[id*="banner"]', '[class*="banner"]', '.sidebar', '.widget',
                '.share', '.social', '.share-buttons', '#comments', '.comments',
                '.cookie-banner', '.cookie-notice', '.cookie-consent',
                '.modal', '.popup', '[role="navigation"]', '[role="banner"]',
                '[role="complementary"]', '[role="dialog"]', '[aria-hidden="true"]'
            ];
            var badNodes = clone.querySelectorAll(unwantedSelectors.join(','));
            for (var b = 0; b < badNodes.length; b++) {
                badNodes[b].remove();
            }

            var candidateSelectors = [
                'article', '[itemprop="articleBody"]', 'main article', '.article-body',
                '.post-content', '.entry-content', '.story-body', '.content-article',
                '#article-body', '#story', '.noticia-texto', '[role="main"] article',
                'main', '[role="main"]', '.main-content', '#main-content', '#content'
            ];

            var bestContainer = null;
            var maxScore = 0;

            for (var c = 0; c < candidateSelectors.length; c++) {
                var el = clone.querySelector(candidateSelectors[c]);
                if (el) {
                    var pCount = el.querySelectorAll('p').length;
                    var textLen = (el.innerText || '').trim().length;
                    var score = pCount * 100 + textLen;
                    if (score > maxScore && textLen > 150) {
                        maxScore = score;
                        bestContainer = el;
                    }
                }
            }

            if (!bestContainer) {
                bestContainer = clone;
            }

            var blocks = [];
            var plainTextParts = [];
            var elements = bestContainer.querySelectorAll('h1, h2, h3, h4, p, blockquote, li, img');
            var seenTexts = new Set();

            for (var i = 0; i < elements.length; i++) {
                var node = elements[i];
                var tag = node.tagName.toLowerCase();

                if (tag === 'img') {
                    var src = node.getAttribute('src') || node.getAttribute('data-src') || node.getAttribute('data-lazy-src') || '';
                    if (src && !src.startsWith('data:') && !src.includes('icon') && !src.includes('logo') && !src.includes('avatar') && !src.includes('pixel')) {
                        var alt = node.getAttribute('alt') || '';
                        blocks.push({
                            type: 'IMAGE',
                            text: '',
                            imageUrl: src,
                            caption: alt
                        });
                    }
                    continue;
                }

                var text = (node.innerText || '').trim();
                if (text.length < 15) continue;
                if (seenTexts.has(text)) continue;
                seenTexts.add(text);

                if (tag === 'h1' && blocks.length > 0) {
                    blocks.push({ type: 'H1', text: text });
                    plainTextParts.push(text);
                } else if (tag === 'h2') {
                    blocks.push({ type: 'H2', text: text });
                    plainTextParts.push(text);
                } else if (tag === 'h3' || tag === 'h4') {
                    blocks.push({ type: 'H3', text: text });
                    plainTextParts.push(text);
                } else if (tag === 'blockquote') {
                    blocks.push({ type: 'BLOCKQUOTE', text: text });
                    plainTextParts.push(text);
                } else {
                    blocks.push({ type: 'PARAGRAPH', text: text });
                    plainTextParts.push(text);
                }
            }

            if (blocks.length < 2) {
                var paragraphs = clone.querySelectorAll('p, div');
                for (var p = 0; p < paragraphs.length; p++) {
                    var pel = paragraphs[p];
                    if (pel.querySelectorAll('p').length > 0) continue;
                    var pText = (pel.innerText || '').trim();
                    if (pText.length > 40 && !seenTexts.has(pText)) {
                        seenTexts.add(pText);
                        blocks.push({ type: 'PARAGRAPH', text: pText });
                        plainTextParts.push(pText);
                    }
                }
            }

            var fullText = plainTextParts.join('\n\n');
            var words = (fullText.match(/\S+/g) || []).length;
            var readTime = Math.max(1, Math.round(words / 200));

            var articleData = {
                title: title,
                author: author,
                publishDate: dateStr,
                domain: domain,
                readingTimeMinutes: readTime,
                blocks: blocks,
                plainText: fullText
            };

            if (window.TesseraBridge && window.TesseraBridge.onArticleExtracted) {
                window.TesseraBridge.onArticleExtracted(JSON.stringify(articleData));
            }
        } catch(err) {
            if (window.TesseraBridge && window.TesseraBridge.onExtractionFailed) {
                window.TesseraBridge.onExtractionFailed();
            }
        }
    })();
""".trimIndent()

private val SUMMARY_EXTRACTION_SCRIPT = """
    (function() {
        try {
            var domain = (window.location.hostname || '').replace('www.', '');
            var metaOgTitle = document.querySelector('meta[property="og:title"]');
            var metaTwitter = document.querySelector('meta[name="twitter:title"]');
            var h1 = document.querySelector('h1');
            var title = (metaOgTitle && metaOgTitle.content) || 
                        (metaTwitter && metaTwitter.content) || 
                        (h1 && h1.innerText) || 
                        document.title || '';

            var clone = document.body.cloneNode(true);
            var unwanted = clone.querySelectorAll('script, style, noscript, iframe, svg, canvas, nav, footer, header, form, button, input, .ad, .ads, [id*="google_ads"], [class*="google_ads"], .sidebar, .widget, .comments, #comments, .cookie-banner, .cookie-notice');
            for (var i = 0; i < unwanted.length; i++) {
                unwanted[i].remove();
            }

            var candidateSelectors = [
                'article', '[itemprop="articleBody"]', 'main article', '.article-body',
                '.post-content', '.entry-content', '.story-body', '.content-article',
                '#article-body', '#story', '.noticia-texto', 'main', '#content'
            ];

            var best = null;
            for (var c = 0; c < candidateSelectors.length; c++) {
                var el = clone.querySelector(candidateSelectors[c]);
                if (el && (el.innerText || '').trim().length > 180) {
                    best = el;
                    break;
                }
            }
            if (!best) best = clone;

            var text = (best.innerText || clone.innerText || '').trim();
            text = text.replace(/\n\s*\n/g, '\n\n');
            if (text.length > 7000) text = text.substring(0, 7000);

            var payload = {
                title: title.trim(),
                domain: domain,
                content: text
            };

            if (window.TesseraBridge && window.TesseraBridge.onSummaryExtracted) {
                window.TesseraBridge.onSummaryExtracted(JSON.stringify(payload));
            }
        } catch(e) {
            if (window.TesseraBridge && window.TesseraBridge.onSummaryExtracted) {
                window.TesseraBridge.onSummaryExtracted(JSON.stringify({
                    title: document.title || '',
                    domain: (window.location.hostname || '').replace('www.', ''),
                    content: (document.body.innerText || '').substring(0, 5000)
                }));
            }
        }
    })();
""".trimIndent()

class TesseraWebBridge(
    private val onReaderExit: () -> Unit,
    private val onArticleExtracted: (String) -> Unit,
    private val onExtractionFailed: () -> Unit,
    private val onSummaryExtracted: (String) -> Unit = {}
) {
    @JavascriptInterface
    fun onReaderModeExited() {
        onReaderExit()
    }

    @JavascriptInterface
    fun onArticleExtracted(json: String) {
        onArticleExtracted(json)
    }

    @JavascriptInterface
    fun onExtractionFailed() {
        onExtractionFailed()
    }

    @JavascriptInterface
    fun onSummaryExtracted(json: String) {
        onSummaryExtracted(json)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TesseraBrowserScreen(viewModel: BrowserViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }
    var lastActiveTabId by remember { mutableStateOf<String?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var safeBrowsingCallback by remember { mutableStateOf<SafeBrowsingResponse?>(null) }
    val context = LocalContext.current

    // Request POST_NOTIFICATIONS on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result ignored */ }

    // Fetch initial weather, financial quotes, init persistence, safe browsing, and request notification permission
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(context)
        viewModel.fetchQuotes()
        viewModel.initPersistence(context)

        // Initialize Google Safe Browsing
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
                WebViewCompat.startSafeBrowsing(context) { success ->
                    Log.d("TesseraBrowser", "Google Safe Browsing inicializado: $success")
                }
            }
        } catch (e: Exception) {
            Log.w("TesseraBrowser", "Erro ao inicializar Safe Browsing", e)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
        if (!state.isHomePage && state.currentUrl.isNotBlank()) {
            if (state.currentUrl != lastLoadedUrl || state.activeTabId != lastActiveTabId) {
                lastLoadedUrl = state.currentUrl
                lastActiveTabId = state.activeTabId
                webViewInstance?.loadUrl(state.currentUrl)
            }
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

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importCustomWallpaper(context, uri)
        }
    }

    var isAirBarExpanded by remember { mutableStateOf(false) }
    var isSearchEditing by remember { mutableStateOf(false) }

    // System Back navigation priority:
    // 0. Dismiss search editing (keyboard)
    // 1. Dismiss Find in Page
    // 2. Dismiss Peek Modal
    // 3. Exit Fullscreen Video
    // 4. Dismiss Quick Settings
    // 5. Dismiss Tabs Modal
    // 6. Dismiss History/Bookmarks Modal
    // 7. Dismiss AI Actions Modal
    // 8. Collapse expanded AirBar
    // 9. WebView history back
    // 10. Go Home
    BackHandler(enabled = state.safeBrowsingThreat != null || state.showQrCodeModal || state.pageError != null || isSearchEditing || state.showFindInPage || state.showPeekModal || customView != null || !state.isHomePage || state.showQuickSettings || state.showTabsModal || state.showHistoryModal || state.showAiActionModal || isAirBarExpanded) {
        if (state.safeBrowsingThreat != null) {
            safeBrowsingCallback?.backToSafety(true)
            viewModel.dismissSafeBrowsingThreat()
            if (state.canGoBack) webViewInstance?.goBack() else viewModel.goHome()
        } else if (state.showQrCodeModal) {
            viewModel.dismissQrCodeModal()
        } else if (state.pageError != null) {
            viewModel.clearPageError()
            viewModel.goHome()
        } else if (isSearchEditing) {
            isSearchEditing = false
        } else if (state.showFindInPage) {
            webViewInstance?.clearMatches()
            viewModel.closeFindInPage()
        } else if (state.showPeekModal) {
            viewModel.dismissPeekModal()
        } else if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
            viewModel.setFullscreenVideo(false)
        } else if (state.showQuickSettings) {
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
                onOpenSettings = { viewModel.toggleQuickSettings() },
                onSearchClick = { isSearchEditing = true }
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
                                javaScriptCanOpenWindowsAutomatically = true

                                // Standard modern Chrome mobile/desktop UA
                                userAgentString = if (state.isDesktopMode) DESKTOP_USER_AGENT else MOBILE_USER_AGENT

                                applyForceDark(this, state.forceDarkPages)
                                setGeolocationEnabled(true)
                            }

                            // Find in Page results listener
                            setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                                viewModel.updateFindResults(
                                    if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0,
                                    numberOfMatches
                                )
                            }

                            // Arc Peek / Link Preview on long-press
                            setOnLongClickListener {
                                val result = hitTestResult
                                val type = result.type
                                if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                                    val linkUrl = result.extra
                                    if (!linkUrl.isNullOrBlank()) {
                                        viewModel.showPeekModal(linkUrl)
                                        return@setOnLongClickListener true
                                    }
                                }
                                false
                            }

                            addJavascriptInterface(
                                TesseraWebBridge(
                                    onReaderExit = {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            viewModel.closeReaderMode()
                                        }
                                    },
                                    onArticleExtracted = { jsonStr ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            try {
                                                val json = JSONObject(jsonStr)
                                                val title = json.optString("title", "Sem título")
                                                val author = json.optString("author", "").takeIf { it.isNotBlank() }
                                                val date = json.optString("publishDate", "").takeIf { it.isNotBlank() }
                                                val domain = json.optString("domain", "")
                                                val readTime = json.optInt("readingTimeMinutes", 1)
                                                val plainText = json.optString("plainText", "")

                                                val blocksArr = json.optJSONArray("blocks") ?: JSONArray()
                                                val blocks = mutableListOf<ReaderBlock>()
                                                for (i in 0 until blocksArr.length()) {
                                                    val bObj = blocksArr.getJSONObject(i)
                                                    val typeStr = bObj.optString("type", "PARAGRAPH")
                                                    val type = try { ReaderBlockType.valueOf(typeStr) } catch (e: Exception) { ReaderBlockType.PARAGRAPH }
                                                    val text = bObj.optString("text", "")
                                                    val imgUrl = bObj.optString("imageUrl", "").takeIf { it.isNotBlank() }
                                                    val caption = bObj.optString("caption", "").takeIf { it.isNotBlank() }
                                                    blocks.add(ReaderBlock(type, text, imgUrl, caption))
                                                }

                                                if (blocks.isEmpty() && plainText.isBlank()) {
                                                    Toast.makeText(context, "Não foi possível extrair texto legível desta página.", Toast.LENGTH_SHORT).show()
                                                    viewModel.closeReaderMode()
                                                } else {
                                                    val article = ReaderArticle(
                                                        title = title,
                                                        author = author,
                                                        publishDate = date,
                                                        domain = domain,
                                                        readingTimeMinutes = readTime,
                                                        blocks = blocks,
                                                        plainText = plainText
                                                    )
                                                    viewModel.setReaderArticle(article)
                                                }
                                            } catch (e: Exception) {
                                                Log.e("TesseraBrowser", "Erro ao processar dados do leitor", e)
                                                Toast.makeText(context, "Falha ao processar conteúdo da página.", Toast.LENGTH_SHORT).show()
                                                viewModel.closeReaderMode()
                                            }
                                        }
                                    },
                                    onExtractionFailed = {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            Toast.makeText(context, "Não foi possível ativar o modo de leitura nesta página.", Toast.LENGTH_SHORT).show()
                                            viewModel.closeReaderMode()
                                        }
                                    },
                                    onSummaryExtracted = { jsonStr ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            try {
                                                val json = JSONObject(jsonStr)
                                                val title = json.optString("title", webViewInstance?.title ?: "Página Atual")
                                                val domain = json.optString("domain", "")
                                                val content = json.optString("content", "")
                                                if (content.isNotBlank()) {
                                                    viewModel.requestArcSummary(title, domain, content)
                                                } else {
                                                    Toast.makeText(context, "Conteúdo insuficiente para resumir.", Toast.LENGTH_SHORT).show()
                                                    viewModel.dismissArcSummary()
                                                }
                                            } catch (e: Exception) {
                                                Log.e("TesseraBrowser", "Erro ao processar extração de resumo", e)
                                                Toast.makeText(context, "Falha ao extrair texto da página.", Toast.LENGTH_SHORT).show()
                                                viewModel.dismissArcSummary()
                                            }
                                        }
                                    }
                                ),
                                "TesseraBridge"
                            )

                            webViewClient = object : WebViewClient() {
                                override fun onSafeBrowsingHit(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    threatType: Int,
                                    callback: SafeBrowsingResponse?
                                ) {
                                    val threatUrl = request?.url?.toString() ?: (view?.url ?: "")
                                    safeBrowsingCallback = callback
                                    viewModel.setSafeBrowsingThreat(
                                        SafeBrowsingThreatInfo.create(threatUrl, threatType)
                                    )
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val urlString = request?.url?.toString() ?: return false
                                    if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                                        if (urlString.startsWith("intent://") || urlString.startsWith("intent:")) {
                                            return try {
                                                val intent = Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME)
                                                if (intent != null) {
                                                    val packageManager = context.packageManager
                                                    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                                                    if (resolveInfo != null) {
                                                        context.startActivity(intent)
                                                        return true
                                                    }
                                                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                                    if (!fallbackUrl.isNullOrBlank()) {
                                                        view?.loadUrl(fallbackUrl)
                                                        return true
                                                    }
                                                }
                                                true
                                            } catch (e: Exception) {
                                                true
                                            }
                                        }
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
                                    val queryPart = urlString.substringAfter("?", "").lowercase()
                                    val isDirectDownload = cleanUrl.endsWith(".apk") || cleanUrl.endsWith(".pdf") ||
                                            cleanUrl.endsWith(".zip") || cleanUrl.endsWith(".rar") || cleanUrl.endsWith(".7z") ||
                                            cleanUrl.endsWith(".tar") || cleanUrl.endsWith(".gz") || cleanUrl.endsWith(".mp3") ||
                                            cleanUrl.endsWith(".mp4") || cleanUrl.endsWith(".wav") || cleanUrl.endsWith(".docx") ||
                                            cleanUrl.endsWith(".xlsx") || cleanUrl.endsWith(".pptx") || cleanUrl.endsWith(".csv") ||
                                            cleanUrl.endsWith(".bin") || cleanUrl.endsWith(".dmg") || cleanUrl.endsWith(".iso") ||
                                            queryPart.contains(".apk") || queryPart.contains("filename=")

                                    if (isDirectDownload) {
                                        val downloadCtx = view?.context ?: context
                                        val ext = cleanUrl.substringAfterLast('.', "")
                                        val detectedMime = if (ext == "apk" || queryPart.contains(".apk")) {
                                            "application/vnd.android.package-archive"
                                        } else ""

                                        val downloadId = viewModel.enqueueDownload(
                                            context = downloadCtx,
                                            url = urlString,
                                            userAgent = view?.settings?.userAgentString ?: "",
                                            mimeType = detectedMime
                                        )
                                        if (downloadId != -1L) {
                                            val guessedName = URLUtil.guessFileName(urlString, null, detectedMime)
                                            Toast.makeText(downloadCtx, "Iniciando download: $guessedName", Toast.LENGTH_SHORT).show()
                                            viewModel.openDownloadsModal()
                                        } else {
                                            Toast.makeText(downloadCtx, "Falha ao iniciar download", Toast.LENGTH_SHORT).show()
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

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        val failingUrl = request.url?.toString() ?: ""
                                        val errorCode = error?.errorCode ?: 0
                                        val description = error?.description?.toString() ?: "Falha ao carregar a página"
                                        val isOffline = errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT
                                        viewModel.setPageError(
                                            PageErrorInfo(
                                                url = failingUrl,
                                                errorCode = errorCode,
                                                description = description,
                                                isOffline = isOffline
                                            )
                                        )
                                    }
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

                                        // Injeção de AdBlock Cosmético (Ocultação de espaços vazios)
                                        if (state.adBlockEnabled) {
                                            view?.evaluateJavascript(COSMETIC_ADBLOCK_SCRIPT, null)
                                        }


                                        // Cookie banner blocker injection (Opera-style)
                                        if (state.cookieBlockerEnabled) {
                                            view?.evaluateJavascript(
                                                """
                                                (function() {
                                                    var selectors = [
                                                        '#onetrust-banner-sdk', '#onetrust-consent-sdk', '.onetrust-pc-dark-filter',
                                                        '.cookie-banner', '.cookie-notice', '.cookie-consent', '.cc-banner', '.cc-window',
                                                        '#CybotCookiebotDialog', '#CybotCookiebotDialogBodyUnderlay',
                                                        '[id*="cookie-banner"]', '[id*="cookie-consent"]', '[id*="cookieConsent"]',
                                                        '[class*="cookie-banner"]', '[class*="cookie-consent"]', '[class*="CookieConsent"]',
                                                        '.cmp-container', '#cmp-app', '#cmpbox', '.qc-cmp2-container',
                                                        '#sp_message_container', '.truste_overlay', '.truste_box_overlay'
                                                    ];
                                                    selectors.forEach(function(sel) {
                                                        var els = document.querySelectorAll(sel);
                                                        for (var i = 0; i < els.length; i++) {
                                                            els[i].style.setProperty('display', 'none', 'important');
                                                            els[i].style.setProperty('visibility', 'hidden', 'important');
                                                            els[i].style.setProperty('opacity', '0', 'important');
                                                            els[i].style.setProperty('pointer-events', 'none', 'important');
                                                        }
                                                    });
                                                    if (document.body) {
                                                        document.body.style.setProperty('overflow', 'auto', 'important');
                                                        document.body.style.setProperty('position', 'static', 'important');
                                                    }
                                                    if (document.documentElement) {
                                                        document.documentElement.style.setProperty('overflow', 'auto', 'important');
                                                    }
                                                })();
                                                """.trimIndent(),
                                                null
                                            )
                                        }

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

                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    if (customView != null) {
                                        callback?.onCustomViewHidden()
                                        return
                                    }
                                    customView = view
                                    customViewCallback = callback
                                    viewModel.setFullscreenVideo(true)
                                }

                                override fun onHideCustomView() {
                                    customViewCallback?.onCustomViewHidden()
                                    customView = null
                                    customViewCallback = null
                                    viewModel.setFullscreenVideo(false)
                                }

                                override fun onJsAlert(
                                    view: WebView?,
                                    url: String?,
                                    message: String?,
                                    result: JsResult?
                                ): Boolean {
                                    AlertDialog.Builder(view?.context ?: context)
                                        .setTitle(view?.title ?: "Aviso")
                                        .setMessage(message ?: "")
                                        .setPositiveButton("OK") { _, _ -> result?.confirm() }
                                        .setOnCancelListener { result?.cancel() }
                                        .show()
                                    return true
                                }

                                override fun onJsConfirm(
                                    view: WebView?,
                                    url: String?,
                                    message: String?,
                                    result: JsResult?
                                ): Boolean {
                                    AlertDialog.Builder(view?.context ?: context)
                                        .setTitle(view?.title ?: "Confirmação")
                                        .setMessage(message ?: "")
                                        .setPositiveButton("OK") { _, _ -> result?.confirm() }
                                        .setNegativeButton("Cancelar") { _, _ -> result?.cancel() }
                                        .setOnCancelListener { result?.cancel() }
                                        .show()
                                    return true
                                }

                                override fun onGeolocationPermissionsShowPrompt(
                                    origin: String?,
                                    callback: GeolocationPermissions.Callback?
                                ) {
                                    AlertDialog.Builder(context)
                                        .setTitle("Permissão de Localização")
                                        .setMessage("$origin gostaria de acessar sua localização.")
                                        .setPositiveButton("Permitir") { _, _ ->
                                            callback?.invoke(origin, true, true)
                                        }
                                        .setNegativeButton("Bloquear") { _, _ ->
                                            callback?.invoke(origin, false, false)
                                        }
                                        .setOnCancelListener {
                                            callback?.invoke(origin, false, false)
                                        }
                                        .show()
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
                                    val downloadId = viewModel.enqueueDownload(
                                        context = ctx,
                                        url = url,
                                        userAgent = userAgent,
                                        contentDisposition = contentDisposition,
                                        mimeType = mimetype
                                    )
                                    if (downloadId == -1L) {
                                        Toast.makeText(ctx, "Falha ao iniciar download", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Falha ao iniciar download", Toast.LENGTH_SHORT).show()
                                }
                            }


                            loadUrl(state.currentUrl)
                            lastLoadedUrl = state.currentUrl
                            lastActiveTabId = state.activeTabId
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        applyForceDark(view.settings, state.forceDarkPages)
                        val targetUa = if (state.isDesktopMode) DESKTOP_USER_AGENT else MOBILE_USER_AGENT
                        if (view.settings.userAgentString != targetUa) {
                            view.settings.userAgentString = targetUa
                            view.settings.useWideViewPort = true
                            view.reload()
                        }
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

        // Dim scrim background when user is typing in the search bar
        if (isSearchEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isSearchEditing = false
                    }
            )
        }

        // Native Offline / Network Error View
        if (state.pageError != null && !state.isHomePage) {
            TesseraOfflineErrorView(
                errorInfo = state.pageError!!,
                isDarkMode = state.isDarkMode,
                onRetry = {
                    viewModel.clearPageError()
                    webViewInstance?.reload()
                },
                onGoHome = {
                    viewModel.goHome()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .displayCutoutPadding()
            )
        }

        // Floating Bottom Container (Download Banner + AirBar)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Download Notification Banner
            DownloadVisualBanner(
                notice = state.activeDownloadNotice,
                isDarkMode = state.isDarkMode,
                onOpenFile = {
                    val noticeId = state.activeDownloadNotice?.id
                    val item = state.downloads.find { it.id == noticeId }
                    if (item != null) {
                        viewModel.openDownloadedFile(context, item)
                    } else {
                        viewModel.openDownloadsModal()
                    }
                    viewModel.dismissDownloadNotice()
                },
                onViewDownloads = {
                    viewModel.openDownloadsModal()
                    viewModel.dismissDownloadNotice()
                },
                onDismiss = { viewModel.dismissDownloadNotice() }
            )

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
                )
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
                    searchSuggestions = state.searchSuggestions,
                    onBack = { webViewInstance?.goBack() },
                    onForward = { webViewInstance?.goForward() },
                    onHome = { viewModel.goHome() },
                    onReload = { webViewInstance?.reload() },
                    onSearch = { query -> viewModel.openUrl(query) },
                    onQueryChange = { query -> viewModel.fetchSearchSuggestions(query) },
                    onOpenAi = { query -> viewModel.openAiQuery(query) },
                    onBrowseForMe = { query -> viewModel.browseForMe(query) },
                    isEditingExternal = isSearchEditing,
                    onEditingChange = { isSearchEditing = it },
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
                        if (state.isReaderModeActive) {
                            viewModel.toggleReaderMode()
                        } else {
                            Toast.makeText(context, "Ativando modo leitura...", Toast.LENGTH_SHORT).show()
                            webViewInstance?.evaluateJavascript(READER_EXTRACTION_SCRIPT, null)
                        }
                    },
                    onOpenTabs = { viewModel.toggleTabsModal() },
                    onOpenHistory = { viewModel.toggleHistoryModal() },
                    onOpenSettings = { viewModel.toggleQuickSettings() },
                    onOpenFavorite = { url -> viewModel.openUrl(url) },
                    onNextTab = { viewModel.selectNextTab() },
                    onPreviousTab = { viewModel.selectPreviousTab() },
                    accentColor = state.activeWallpaper.accentColor
                )
            }
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
                onArchiveInactiveTabs = { viewModel.archiveInactiveTabs() },
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
                savedPages = state.savedPages,
                onOpenSavedPage = {
                    viewModel.dismissHistoryModal()
                    viewModel.openSavedPage(it)
                },
                onDeleteSavedPage = { viewModel.deleteSavedPage(it) },
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
                onAction = { action ->
                    if (action == "summarize") {
                        viewModel.dismissAiActionModal()
                        if (state.isHomePage) {
                            Toast.makeText(context, "Abra um site ou artigo para gerar um resumo com IA!", Toast.LENGTH_SHORT).show()
                        } else {
                            val currentTitle = webViewInstance?.title ?: ""
                            val currentDomain = try { Uri.parse(state.displayUrl).host?.replace("www.", "") ?: "" } catch (e: Exception) { "" }
                            viewModel.requestArcSummary(currentTitle, currentDomain, "Extraindo conteúdo da página...")
                            webViewInstance?.evaluateJavascript(SUMMARY_EXTRACTION_SCRIPT, null)
                        }
                    } else {
                        viewModel.openAiAction(action)
                    }
                },
                onDismiss = { viewModel.dismissAiActionModal() }
            )
        }

        // ARC PAGE SUMMARY OVERLAY & SHEET
        AnimatedVisibility(
            visible = state.showArcSummary,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(280)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ArcSummarySheet(
                isVisible = state.showArcSummary,
                isGenerating = state.isGeneratingArcSummary,
                summaryText = state.arcSummaryContent,
                pageTitle = state.arcSummaryTitle,
                pageDomain = state.arcSummaryDomain,
                readingTimeSavedMinutes = state.arcSummaryReadTimeSaved,
                error = state.arcSummaryError,
                isDarkMode = state.isDarkMode,
                isSpeaking = state.isReaderTtsPlaying,
                onSpeakSummary = { text -> viewModel.speakText(context, text) },
                onStopSpeaking = { viewModel.stopReaderTts() },
                onRetry = {
                    val currentTitle = webViewInstance?.title ?: ""
                    val currentDomain = try { Uri.parse(state.displayUrl).host?.replace("www.", "") ?: "" } catch (e: Exception) { "" }
                    viewModel.requestArcSummary(currentTitle, currentDomain, "Recarregando...")
                    webViewInstance?.evaluateJavascript(SUMMARY_EXTRACTION_SCRIPT, null)
                },
                onDismiss = { viewModel.dismissArcSummary() }
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
                customWallpaperUri = state.customWallpaperUri,
                showFavoritesBar = state.showFavoritesBar,
                tesseraAiEnabled = state.tesseraAiEnabled,
                aiToolbarButton = state.aiToolbarButton,
                aiTextHighlightPrompts = state.aiTextHighlightPrompts,
                showSidebar = state.showSidebar,
                autoHideSidebar = state.autoHideSidebar,
                adBlockEnabled = state.adBlockEnabled,
                isDesktopMode = state.isDesktopMode,
                cookieBlockerEnabled = state.cookieBlockerEnabled,
                isWebPageActive = !state.isHomePage,
                showWeatherWidget = state.showWeatherWidget,
                showQuotesWidget = state.showQuotesWidget,
                selectedSearchEngine = state.searchEngine,
                onSearchEngineSelected = { viewModel.setSearchEngine(it) },
                onClearBrowsingData = { clearHistory, clearCookies, clearCache ->
                    viewModel.clearBrowsingData(
                        context = context,
                        webView = webViewInstance,
                        clearHistory = clearHistory,
                        clearCookies = clearCookies,
                        clearCache = clearCache
                    ) {
                        Toast.makeText(context, "Dados de navegação limpos com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                },
                onDarkModeChanged = { viewModel.setDarkMode(it) },

                onForceDarkPagesChanged = { viewModel.setForceDarkPages(it) },
                onShowWallpaperChanged = { viewModel.setShowWallpaper(it) },
                onSelectWallpaper = { viewModel.selectWallpaper(it) },
                onUploadWallpaper = {
                    wallpaperPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onShowFavoritesBarChanged = { viewModel.setShowFavoritesBar(it) },
                onShowWeatherWidgetChanged = { viewModel.setShowWeatherWidget(it) },
                onShowQuotesWidgetChanged = { viewModel.setShowQuotesWidget(it) },
                onTesseraAiChanged = { viewModel.setTesseraAiEnabled(it) },
                onAiToolbarButtonChanged = { viewModel.setAiToolbarButton(it) },
                onAiTextHighlightPromptsChanged = { viewModel.setAiTextHighlightPrompts(it) },
                onShowSidebarChanged = { viewModel.setShowSidebar(it) },
                onAutoHideSidebarChanged = { viewModel.setAutoHideSidebar(it) },
                onAdBlockChanged = { viewModel.setAdBlockEnabled(it) },
                onDesktopModeChanged = { viewModel.toggleDesktopMode() },
                onCookieBlockerChanged = { viewModel.toggleCookieBlocker() },
                onFindInPage = {
                    viewModel.dismissQuickSettings()
                    viewModel.openFindInPage()
                },
                onSharePage = {
                    viewModel.dismissQuickSettings()
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, webViewInstance?.title ?: "Tessera Browser")
                        putExtra(Intent.EXTRA_TEXT, "${webViewInstance?.title.orEmpty()}\n${state.displayUrl}".trim())
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Página"))
                },
                onPrintPage = {
                    viewModel.dismissQuickSettings()
                    webViewInstance?.let { webView ->
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        val printAdapter = webView.createPrintDocumentAdapter("Tessera_${System.currentTimeMillis()}")
                        val jobName = "${webView.title ?: "Documento"}_Tessera"
                        printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
                    }
                },
                onAddToHomeScreen = {
                    viewModel.dismissQuickSettings()
                    viewModel.addCurrentPageToHomeScreen(context, webViewInstance)
                },
                onSavePageOffline = {
                    viewModel.dismissQuickSettings()
                    viewModel.saveCurrentPageForOffline(context, webViewInstance)
                },
                onShowQrCode = {
                    viewModel.dismissQuickSettings()
                    viewModel.showQrCodeModal()
                },
                onOpenHistory = {
                    viewModel.dismissQuickSettings()
                    viewModel.openHistoryModal(0)
                },
                onOpenDownloads = {
                    viewModel.dismissQuickSettings()
                    viewModel.openDownloadsModal()
                },
                onOpenReaderMode = {
                    Toast.makeText(context, "Convertendo página em texto...", Toast.LENGTH_SHORT).show()
                    webViewInstance?.evaluateJavascript(READER_EXTRACTION_SCRIPT, null)
                },
                onDismiss = { viewModel.dismissQuickSettings() }
            )
        }

        // FIND IN PAGE FLOATING OVERLAY (Chrome-style)
        AnimatedVisibility(
            visible = state.showFindInPage && !state.isHomePage,
            enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(260)) + fadeIn(tween(260)),
            exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(220)) + fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .displayCutoutPadding()
        ) {
            FindInPageBar(
                query = state.findQuery,
                matchIndex = state.findMatchIndex,
                matchCount = state.findMatchCount,
                onQueryChange = { query ->
                    viewModel.updateFindQuery(query)
                    webViewInstance?.findAllAsync(query)
                },
                onFindNext = {
                    webViewInstance?.findNext(true)
                },
                onFindPrevious = {
                    webViewInstance?.findNext(false)
                },
                onClose = {
                    webViewInstance?.clearMatches()
                    viewModel.closeFindInPage()
                },
                isDarkMode = state.isDarkMode,
                accentColor = state.activeWallpaper.accentColor
            )
        }

        // ARC SEARCH LINK PEEK PREVIEW MODAL
        if (state.showPeekModal && state.peekUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.dismissPeekModal()
                    }
            )
        }

        AnimatedVisibility(
            visible = state.showPeekModal && state.peekUrl != null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.peekUrl?.let { peekUrl ->
                PeekPreviewModal(
                    url = peekUrl,
                    title = state.peekTitle,
                    isDarkMode = state.isDarkMode,
                    accentColor = state.activeWallpaper.accentColor,
                    onOpenInCurrentTab = {
                        viewModel.dismissPeekModal()
                        viewModel.openUrl(peekUrl)
                    },
                    onOpenInNewTab = {
                        viewModel.dismissPeekModal()
                        viewModel.addNewTab(url = peekUrl, isHome = false)
                    },
                    onDismiss = { viewModel.dismissPeekModal() }
                )
            }
        }

        // HTML5 FULLSCREEN VIDEO OVERLAY
        if (customView != null) {
            AndroidView(
                factory = { customView!! },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // NATIVE IMMERSIVE TEXT-ONLY READER SCREEN
        if (state.isReaderModeActive && state.readerArticle != null) {
            TesseraReaderScreen(
                article = state.readerArticle!!,
                fontSizeSp = state.readerFontSizeSp,
                theme = state.readerTheme,
                fontFamily = state.readerFontFamily,
                showImages = state.readerShowImages,
                isTtsPlaying = state.isReaderTtsPlaying,
                isSettingsOpen = state.isReaderSettingsOpen,
                onClose = { viewModel.closeReaderMode() },
                onToggleSettings = { viewModel.toggleReaderSettings() },
                onUpdateFontSize = { delta -> viewModel.updateReaderFontSize(delta) },
                onSelectTheme = { theme -> viewModel.setReaderTheme(theme) },
                onSelectFontFamily = { family -> viewModel.setReaderFontFamily(family) },
                onToggleShowImages = { viewModel.toggleReaderShowImages() },
                onToggleTts = { viewModel.toggleReaderTts(context) }
            )
        }

        // QR CODE SHARE MODAL
        if (state.showQrCodeModal) {
            QrCodeShareModal(
                url = state.currentUrl,
                title = webViewInstance?.title ?: state.tabs.find { it.id == state.activeTabId }?.title ?: "",
                onDismiss = { viewModel.dismissQrCodeModal() },
                accentColor = state.activeWallpaper.accentColor,
                isDarkMode = state.isDarkMode
            )
        }

        // GOOGLE SAFE BROWSING THREAT WARNING OVERLAY
        if (state.safeBrowsingThreat != null) {
            SafeBrowsingWarningView(
                threatInfo = state.safeBrowsingThreat!!,
                onBackToSafety = {
                    safeBrowsingCallback?.backToSafety(true)
                    viewModel.dismissSafeBrowsingThreat()
                    if (state.canGoBack) {
                        webViewInstance?.goBack()
                    } else {
                        viewModel.goHome()
                    }
                },
                onProceedAnyway = {
                    safeBrowsingCallback?.proceed(false)
                    viewModel.dismissSafeBrowsingThreat()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .displayCutoutPadding()
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
