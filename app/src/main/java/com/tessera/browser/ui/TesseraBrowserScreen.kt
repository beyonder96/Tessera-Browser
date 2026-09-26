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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tessera.browser.ai.AiProvider
import com.tessera.browser.ui.components.FindInPageBar
import com.tessera.browser.ui.components.HistoryBookmarksModal
import com.tessera.browser.ui.components.NotebookModal
import com.tessera.browser.util.WebClipperHelper
import com.tessera.browser.ui.components.PeekPreviewModal
import com.tessera.browser.pip.PipManager
import com.tessera.browser.ui.components.PipPermissionDialog
import com.tessera.browser.ui.components.PodcastFullPlayerModal
import com.tessera.browser.ui.components.PodcastMiniPlayerCapsule
import com.tessera.browser.ui.components.PrivacyDashboardModal
import com.tessera.browser.ui.components.QrCodeShareModal
import com.tessera.browser.ui.components.QuickSettingsPanel
import com.tessera.browser.ui.components.SafeBrowsingWarningView
import com.tessera.browser.ui.components.SiteSettingsModal
import com.tessera.browser.ui.components.SpaceQuickSwitcherModal
import com.tessera.browser.ui.components.TabsModal
import com.tessera.browser.ui.components.TesseraSettingsScreen
import androidx.compose.material.icons.rounded.SmartDisplay
import com.tessera.browser.ui.components.TesseraAiSheet
import com.tessera.browser.ui.components.TesseraAirBar
import com.tessera.browser.ui.components.TesseraReaderScreen
import com.tessera.browser.ui.components.TesseraStartPage
import com.tessera.browser.ui.components.TranslateBar
import com.tessera.browser.viewmodel.BrowserViewModel
import com.tessera.browser.viewmodel.ReaderArticle
import com.tessera.browser.viewmodel.ReaderBlock
import com.tessera.browser.viewmodel.ReaderBlockType
import android.webkit.PermissionRequest
import android.webkit.WebResourceError
import com.tessera.browser.data.PageErrorInfo
import com.tessera.browser.ui.components.DownloadVisualBanner
import com.tessera.browser.ui.components.TesseraOfflineErrorView
import java.io.ByteArrayInputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            var domain = (window.location.hostname || '').replace(/^www\./, '');

            // 1. Title Extraction & Cleaning
            var title = '';
            var metaOg = document.querySelector('meta[property="og:title"]');
            var metaTw = document.querySelector('meta[name="twitter:title"]');
            var h1 = document.querySelector('h1');
            if (metaOg && metaOg.content) {
                title = metaOg.content.trim();
            } else if (metaTw && metaTw.content) {
                title = metaTw.content.trim();
            } else if (h1) {
                title = (h1.innerText || h1.textContent || '').trim();
            } else {
                title = document.title || 'Artigo';
            }
            // Remove common news/encyclopedia branding suffixes
            title = title.replace(/\s*[-–—|]\s*(Wikipédia.*|G1.*|UOL.*|Folha.*|TechCrunch.*|Medium.*|CNN.*|BBC.*|[^-–—|]{3,30})${'$'}/i, '').trim();

            // 2. Author Extraction
            var author = '';
            var authorMeta = document.querySelector('meta[name="author"], meta[property="article:author"], meta[name="byl"], meta[property="og:article:author"]');
            var authorEl = document.querySelector('[rel="author"], .byline, .author, .c-byline__item, .author-name, .article__author, [itemprop="author"]');
            if (authorMeta && authorMeta.content) {
                author = authorMeta.content.trim();
            } else if (authorEl) {
                author = (authorEl.innerText || authorEl.textContent || '').trim();
            }
            if (author.length > 50) author = author.substring(0, 50);

            // 3. Date Extraction
            var dateStr = '';
            var timeMeta = document.querySelector('meta[property="article:published_time"], meta[name="pubdate"], meta[name="date"]');
            var timeEl = document.querySelector('time, [property="article:published_time"], .date, .published, .datetime, [itemprop="datePublished"]');
            if (timeMeta && timeMeta.content) {
                dateStr = timeMeta.content.trim();
            } else if (timeEl) {
                dateStr = (timeEl.getAttribute('datetime') || timeEl.innerText || timeEl.textContent || '').trim();
            }
            if (dateStr.length > 30) dateStr = dateStr.substring(0, 30);

            // 4. Clutter selectors to completely ignore
            var ignoreSelector = [
                '.infobox', '.infobox_v2', 'table.infobox', '.navbox', '.vertical-navbox', '.sidebar',
                '.toc', '.vector-toc', '#toc', '.mw-jump-link', '.mw-editsection', '.reference',
                'sup.reference', '.reflist', '.mw-references-wrap', '.references', '.noprint',
                '.mw-empty-elt', 'nav', 'footer', 'header', 'form', 'button', 'input', 'select',
                'textarea', 'script', 'style', 'noscript', 'iframe', 'svg', 'canvas', '.ad', '.ads',
                '.advertisement', '[id*="google_ads"]', '[class*="google_ads"]', '[id*="banner"]',
                '[class*="banner"]', '.share', '.social', '.share-buttons', '#comments', '.comments',
                '.cookie-banner', '.cookie-notice', '.modal', '.popup', '[role="navigation"]',
                '[role="banner"]', '[role="complementary"]', '[role="dialog"]', '[aria-hidden="true"]',
                '.hatnote', '.shortdescription', '.catlinks', '#catlinks', '.printfooter', '#footer',
                '.license', '.ambox', '.sistersitebox', '.newsletter-signup', '.subscription-prompt',
                '.related-posts', '.recommended', '.aside', 'aside'
            ].join(', ');

            // 5. Select Best Content Container
            var bestContainer = null;
            var maxScore = -1;

            // Prioritize dedicated article container for Wikipedia
            var wikiMain = document.querySelector('#mw-content-text .mw-parser-output, #mw-content-text, #bodyContent');
            if (wikiMain && (wikiMain.innerText || wikiMain.textContent || '').length > 200) {
                bestContainer = wikiMain;
            } else {
                var candidateSelectors = [
                    'article', '[itemprop="articleBody"]', 'main article', '[role="main"] article',
                    '.article-body', '.article__body', '.post-content', '.entry-content', '.story-body',
                    '.content-article', '.caas-body', '#article-body', '#story', '.noticia-texto',
                    '.materia-conteudo', '.post_content', '.articleContent', 'main', '[role="main"]',
                    '#main-content', '.main-content', '#content'
                ];

                for (var c = 0; c < candidateSelectors.length; c++) {
                    var els = document.querySelectorAll(candidateSelectors[c]);
                    for (var e = 0; e < els.length; e++) {
                        var el = els[e];
                        if (el.offsetWidth === 0 && el.offsetHeight === 0 && !el.getClientRects().length) continue;
                        if (el.closest(ignoreSelector)) continue;

                        var pList = el.querySelectorAll('p');
                        var fullText = (el.innerText || el.textContent || '').trim();
                        var links = el.querySelectorAll('a');
                        var linkTextLen = 0;
                        for (var l = 0; l < links.length; l++) {
                            linkTextLen += (links[l].innerText || links[l].textContent || '').length;
                        }
                        var substantiveTextLen = Math.max(0, fullText.length - linkTextLen);
                        var score = pList.length * 250 + substantiveTextLen;
                        if (score > maxScore && substantiveTextLen > 150) {
                            maxScore = score;
                            bestContainer = el;
                        }
                    }
                }
            }

            if (!bestContainer) {
                bestContainer = document.body || document.documentElement;
            }

            // 6. Extract Structured Blocks (Headings, Paragraphs, Blockquotes, Lists, Images)
            var blocks = [];
            var seenTexts = new Set();
            var stopSections = /^(ver também|referências|referencias|fontes|bibliografia|ligações externas|links externos|notas|créditos|leia também|mais sobre|compartilhe|comentários)${'$'}/i;
            var stopped = false;

            var items = bestContainer.querySelectorAll('h1, h2, h3, h4, h5, h6, p, blockquote, ul, ol, img');

            for (var i = 0; i < items.length; i++) {
                if (stopped) break;
                var node = items[i];
                if (node.closest(ignoreSelector)) continue;

                var tag = node.tagName.toLowerCase();

                // Images with Captions
                if (tag === 'img') {
                    var rawSrc = node.currentSrc || node.getAttribute('data-src') || node.getAttribute('data-lazy-src') || node.getAttribute('data-original') || node.src || '';
                    if (rawSrc && !rawSrc.startsWith('data:') && !rawSrc.includes('icon') && !rawSrc.includes('logo') && !rawSrc.includes('avatar') && !rawSrc.includes('pixel') && !rawSrc.includes('tracking')) {
                        try {
                            var absUrl = new URL(rawSrc, document.baseURI).href;
                            var caption = (node.getAttribute('alt') || node.getAttribute('title') || '').trim();
                            if (!caption) {
                                var fig = node.closest('figure');
                                if (fig) {
                                    var figCap = fig.querySelector('figcaption');
                                    if (figCap) caption = (figCap.innerText || figCap.textContent || '').trim();
                                }
                            }
                            if (!seenTexts.has(absUrl)) {
                                seenTexts.add(absUrl);
                                blocks.push({
                                    type: 'IMAGE',
                                    text: '',
                                    imageUrl: absUrl,
                                    caption: caption
                                });
                            }
                        } catch(uErr) {}
                    }
                    continue;
                }

                // Clean Structured Lists
                if (tag === 'ul' || tag === 'ol') {
                    if (node.parentElement && (node.parentElement.tagName.toLowerCase() === 'li' || node.parentElement.closest('ul, ol'))) continue;
                    var listItems = node.querySelectorAll(':scope > li');
                    for (var liIdx = 0; liIdx < listItems.length; liIdx++) {
                        var liNode = listItems[liIdx];
                        if (liNode.closest(ignoreSelector)) continue;
                        var liText = (liNode.innerText || liNode.textContent || '').trim();
                        liText = liText.replace(/\[\d+\]|\[editar.*?\]/g, '').trim();
                        if (liText.length > 5 && !seenTexts.has(liText)) {
                            seenTexts.add(liText);
                            blocks.push({
                                type: 'LIST_ITEM',
                                text: '•  ' + liText
                            });
                        }
                    }
                    continue;
                }

                // Clean Headings, Paragraphs, Quotes
                var rawText = (node.innerText || node.textContent || '').trim();
                var cleanText = rawText.replace(/\[editar.*?\]/gi, '')
                                       .replace(/\[\d+\]/g, '')
                                       .replace(/\[nota\s*\d+\]/gi, '')
                                       .replace(/\[carece\s*de\s*fontes.*?\]/gi, '')
                                       .trim();

                if (!cleanText || cleanText.length < 10) continue;

                // Stop extraction once End Sections are reached
                if ((tag === 'h2' || tag === 'h3' || tag === 'h1') && stopSections.test(cleanText)) {
                    stopped = true;
                    break;
                }

                if (seenTexts.has(cleanText)) continue;
                seenTexts.add(cleanText);

                if (tag === 'h1') {
                    if (cleanText.toLowerCase() !== title.toLowerCase()) {
                        blocks.push({ type: 'H1', text: cleanText });
                    }
                } else if (tag === 'h2') {
                    blocks.push({ type: 'H2', text: cleanText });
                } else if (tag === 'h3' || tag === 'h4' || tag === 'h5' || tag === 'h6') {
                    blocks.push({ type: 'H3', text: cleanText });
                } else if (tag === 'blockquote') {
                    blocks.push({ type: 'BLOCKQUOTE', text: cleanText });
                } else if (tag === 'p') {
                    blocks.push({ type: 'PARAGRAPH', text: cleanText });
                }
            }

            // Fallback: extract substantive paragraphs if container search yielded < 2 blocks
            if (blocks.length < 2) {
                var allPs = (document.body || document.documentElement).querySelectorAll('p');
                for (var pIdx = 0; pIdx < allPs.length; pIdx++) {
                    var pNode = allPs[pIdx];
                    if (pNode.closest(ignoreSelector)) continue;
                    var pText = (pNode.innerText || pNode.textContent || '').trim().replace(/\[\d+\]/g, '');
                    if (pText.length > 40 && !seenTexts.has(pText)) {
                        seenTexts.add(pText);
                        blocks.push({ type: 'PARAGRAPH', text: pText });
                    }
                }
            }

            // Calculate reading time
            var totalWords = blocks.reduce(function(acc, b) {
                return acc + (b.text ? (b.text.match(/\S+/g) || []).length : 0);
            }, 0);
            var readTime = Math.max(1, Math.round(totalWords / 190));

            var articleData = {
                title: title || document.title || 'Artigo',
                author: author,
                publishDate: dateStr,
                domain: domain,
                readingTimeMinutes: readTime,
                blocks: blocks
            };

            var jsonStr = JSON.stringify(articleData);
            if (window.TesseraBridge && window.TesseraBridge.onArticleExtracted) {
                window.TesseraBridge.onArticleExtracted(jsonStr);
            }
            return jsonStr;
        } catch(err) {
            if (window.TesseraBridge && window.TesseraBridge.onExtractionFailed) {
                window.TesseraBridge.onExtractionFailed();
            }
            return JSON.stringify({ error: err.toString(), blocks: [] });
        }
    })();
""".trimIndent()

private val SUMMARY_EXTRACTION_SCRIPT = """
    (function() {
        try {
            var domain = (window.location.hostname || '').replace(/^www\./, '');
            var title = '';
            var metaOg = document.querySelector('meta[property="og:title"]');
            var metaTw = document.querySelector('meta[name="twitter:title"]');
            var h1 = document.querySelector('h1');
            if (metaOg && metaOg.content) title = metaOg.content.trim();
            else if (metaTw && metaTw.content) title = metaTw.content.trim();
            else if (h1 && (h1.innerText || h1.textContent || '').trim().length > 3) title = (h1.innerText || h1.textContent).trim();
            else title = document.title || '';
            title = title.replace(/\s*[-–—|]\s*[^-–—|]+$/, '').trim();

            var ignoreSelector = 'nav, footer, header, form, button, input, select, textarea, script, style, noscript, iframe, svg, canvas, .ad, .ads, .advertisement, [id*="google_ads"], [class*="google_ads"], [id*="banner"], [class*="banner"], .sidebar, .widget, .share, .social, #comments, .comments, .cookie-banner, .cookie-notice, .modal, .popup';

            var candidateSelectors = [
                'article', '[itemprop="articleBody"]', 'main article', '[role="main"] article',
                '.article-body', '.article__body', '.post-content', '.entry-content', '.story-body',
                '.content-article', '.caas-body', '#article-body', '#story', '.noticia-texto',
                '.materia-conteudo', 'main', '[role="main"]', '#main-content', '.main-content', '#content'
            ];

            var bestContainer = null;
            var maxScore = -1;
            for (var c = 0; c < candidateSelectors.length; c++) {
                var els = document.querySelectorAll(candidateSelectors[c]);
                for (var e = 0; e < els.length; e++) {
                    var el = els[e];
                    if (el.offsetWidth === 0 && el.offsetHeight === 0 && !el.getClientRects().length) continue;
                    var pList = el.querySelectorAll('p');
                    var text = (el.innerText || el.textContent || '').trim();
                    var score = pList.length * 100 + text.length;
                    if (score > maxScore && text.length > 150) {
                        maxScore = score;
                        bestContainer = el;
                    }
                }
                if (bestContainer && maxScore > 500) break;
            }
            if (!bestContainer) bestContainer = document.body;

            var parts = [];
            var seen = new Set();
            var elements = bestContainer.querySelectorAll('h1, h2, h3, h4, p, blockquote, li');
            for (var i = 0; i < elements.length; i++) {
                var node = elements[i];
                if (node.closest(ignoreSelector)) continue;
                var t = (node.innerText || node.textContent || '').trim();
                if (t.length < 15 || seen.has(t)) continue;
                seen.add(t);
                parts.push(t);
            }

            if (parts.length < 2) {
                var allP = document.body.querySelectorAll('p');
                for (var p = 0; p < allP.length; p++) {
                    var pel = allP[p];
                    if (pel.closest(ignoreSelector)) continue;
                    var pt = (pel.innerText || pel.textContent || '').trim();
                    if (pt.length > 35 && !seen.has(pt)) {
                        seen.add(pt);
                        parts.push(pt);
                    }
                }
            }

            var fullText = parts.join('\n\n');
            if (fullText.length > 8000) fullText = fullText.substring(0, 8000);

            var payload = {
                title: title.trim() || document.title || 'Página Atual',
                domain: domain,
                content: fullText
            };

            if (window.TesseraBridge && window.TesseraBridge.onSummaryExtracted) {
                window.TesseraBridge.onSummaryExtracted(JSON.stringify(payload));
            }
        } catch(e) {
            if (window.TesseraBridge && window.TesseraBridge.onSummaryExtracted) {
                window.TesseraBridge.onSummaryExtracted(JSON.stringify({
                    title: document.title || 'Página Atual',
                    domain: (window.location.hostname || '').replace(/^www\./, ''),
                    content: (document.body ? (document.body.innerText || document.body.textContent || '').substring(0, 5000) : '')
                }));
            }
        }
    })();
""".trimIndent()

class TesseraWebBridge(
    private val onReaderExit: () -> Unit,
    private val onArticleExtracted: (String) -> Unit,
    private val onExtractionFailed: () -> Unit,
    private val onSummaryExtracted: (String) -> Unit = {},
    private val onVideoPlaybackChanged: (Boolean, Int, Int) -> Unit = { _, _, _ -> }
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

    @JavascriptInterface
    fun onVideoPlaybackStateChanged(isPlaying: Boolean, width: Int, height: Int) {
        onVideoPlaybackChanged(isPlaying, width, height)
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
    val coroutineScope = rememberCoroutineScope()

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
    BackHandler(enabled = state.safeBrowsingThreat != null || state.showPipPermissionDialog || state.showNotebookModal || state.showQrCodeModal || state.pageError != null || isSearchEditing || state.showFindInPage || state.showPeekModal || customView != null || !state.isHomePage || state.showFullSettings || state.showQuickSettings || state.showTabsModal || state.showSpaceSwitcherModal || state.showHistoryModal || state.aiAssistantState.isVisible || state.showSiteSettingsModal || state.translationState.isBannerVisible || isAirBarExpanded) {
        if (state.safeBrowsingThreat != null) {
            safeBrowsingCallback?.backToSafety(true)
            viewModel.dismissSafeBrowsingThreat()
            if (state.canGoBack) webViewInstance?.goBack() else viewModel.goHome()
        } else if (state.showPipPermissionDialog) {
            viewModel.showPipPermissionDialog(false)
        } else if (state.showNotebookModal) {
            viewModel.dismissNotebook()
        } else if (state.showFullSettings) {
            viewModel.dismissFullSettings()
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
        } else if (state.showSiteSettingsModal) {
            viewModel.dismissSiteSettings()
        } else if (state.translationState.isBannerVisible) {
            viewModel.dismissTranslationBanner()
        } else if (state.showQuickSettings) {
            viewModel.dismissQuickSettings()
        } else if (state.showTabsModal) {
            viewModel.dismissTabsModal()
        } else if (state.showSpaceSwitcherModal) {
            viewModel.toggleSpaceSwitcherModal(false)
        } else if (state.showHistoryModal) {
            viewModel.dismissHistoryModal()
        } else if (state.aiAssistantState.isVisible) {
            viewModel.dismissAiAssistant()
        } else if (isAirBarExpanded) {
            isAirBarExpanded = false
        } else if (state.canGoBack) {
            webViewInstance?.goBack()
        } else {
            viewModel.goHome()
        }
    }


    val launchReaderMode: () -> Unit = {
        if (state.isReaderModeActive) {
            viewModel.closeReaderMode()
        } else if (state.isHomePage) {
            Toast.makeText(context, "Abra uma página ou artigo para ativar o Modo Leitura.", Toast.LENGTH_SHORT).show()
        } else {
            val pageTitle = webViewInstance?.title?.takeIf { it.isNotBlank() } ?: "Artigo"
            val pageDomain = try {
                Uri.parse(state.displayUrl.ifBlank { state.currentUrl }).host?.replace("www.", "") ?: ""
            } catch (e: Exception) { "" }

            // 1. Ativa o estado de carregamento do leitor imersivo
            viewModel.startReaderLoading(
                initialTitle = pageTitle,
                initialDomain = pageDomain,
                initialUrl = state.displayUrl.ifBlank { state.currentUrl }
            )

            // 2. Extrai dados via script resiliente com retorno direto e ponte nativa
            webViewInstance?.evaluateJavascript(READER_EXTRACTION_SCRIPT) { result ->
                if (!result.isNullOrBlank() && result != "null" && result != "\"\"") {
                    try {
                        val cleanJson = if (result.length >= 2 && result.startsWith("\"") && result.endsWith("\"")) {
                            try {
                                org.json.JSONTokener(result).nextValue().toString()
                            } catch (e: Exception) {
                                result.substring(1, result.length - 1)
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\")
                                    .replace("\\n", "\n")
                                    .replace("\\r", "\r")
                                    .replace("\\t", "\t")
                            }
                        } else {
                            result
                        }
                        viewModel.processExtractedArticleJson(cleanJson)
                    } catch (e: Exception) {
                        Log.e("TesseraBrowser", "Falha ao processar retorno direto do leitor", e)
                    }
                }
            }
        }
    }

    val clipCurrentPageAction: (Boolean) -> Unit = { withAiSummary ->
        if (state.isHomePage) {
            Toast.makeText(context, "Abra uma página web para clipar conteúdos!", Toast.LENGTH_SHORT).show()
        } else {
            webViewInstance?.evaluateJavascript(WebClipperHelper.CLIP_PAGE_SCRIPT) { rawResult ->
                val note = WebClipperHelper.parseClippingResult(rawResult, state.activeSpaceId)
                if (note != null) {
                    viewModel.saveNote(note)
                    if (withAiSummary) {
                        viewModel.summarizeNote(note.id)
                        Toast.makeText(context, "Clipado e gerando resumo com IA ✨", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Salvo no Caderno de Notas 📝", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Não foi possível extrair o conteúdo da página.", Toast.LENGTH_SHORT).show()
                }
            }
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
                favorites = state.currentSpaceFavorites,
                searchSuggestions = state.searchSuggestions,
                trendingTopics = state.trendingTopics,
                digitalMinimalismMode = state.digitalMinimalismMode,
                showWeatherWidget = state.showWeatherWidget,
                showQuotesWidget = state.showQuotesWidget,
                weatherData = state.weatherData,
                quotesData = state.quotesData,
                onRefreshWeather = { viewModel.fetchWeather(context, forceRefresh = true) },
                onSearchQueryChange = { query -> viewModel.fetchSearchSuggestions(query) },
                onSearch = { query -> viewModel.openUrl(query) },
                onOpenAi = { query -> viewModel.openAiAssistant("Tessera AI", "", "", query) },
                onOpenUrl = { url -> viewModel.openUrl(url) },
                onOpenSettings = { viewModel.toggleQuickSettings() },
                onSearchClick = { isSearchEditing = true },
                totalBlockedCount = state.privacyState.totalBlockedCount,
                onOpenPrivacyDashboard = { viewModel.togglePrivacyDashboard(true) },
                currentSpaceEmoji = state.currentSpace.iconEmoji,
                currentSpaceName = state.currentSpace.name,
                currentSpaceColor = Color(state.currentSpace.colorArgb),
                onOpenSpaces = { viewModel.toggleSpaceSwitcherModal(true) },
                notesCount = state.notes.size,
                onOpenNotebook = { viewModel.openNotebook() }
            )
        } else {
            // WEBVIEW BROWSER VIEW
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                AndroidView(
                    modifier = if (state.isInPipMode) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .displayCutoutPadding()
                            .padding(top = 6.dp)
                    },
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
                                            viewModel.processExtractedArticleJson(jsonStr)
                                        }
                                    },
                                    onExtractionFailed = {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            val currentTitle = webViewInstance?.title ?: "Documento"
                                            val currentDomain = try { Uri.parse(state.displayUrl).host?.replace("www.", "") ?: "" } catch (e: Exception) { "" }
                                            viewModel.fallbackReaderArticle(currentTitle, currentDomain)
                                        }
                                    },
                                    onSummaryExtracted = { jsonStr ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            try {
                                                val json = JSONObject(jsonStr)
                                                val title = json.optString("title", webViewInstance?.title ?: "Página Atual")
                                                val domain = json.optString("domain", "")
                                                val content = json.optString("content", "")
                                                viewModel.openAiAssistant(
                                                    title = title,
                                                    domain = domain,
                                                    url = state.displayUrl,
                                                    content = content
                                                )
                                            } catch (e: Exception) {
                                                Log.e("TesseraBrowser", "Erro ao processar extração de resumo", e)
                                                Toast.makeText(context, "Falha ao extrair texto da página.", Toast.LENGTH_SHORT).show()
                                                viewModel.dismissAiAssistant()
                                            }
                                        }
                                    },
                                    onVideoPlaybackChanged = { isPlaying, width, height ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            viewModel.setVideoPlaybackState(isPlaying, width, height)
                                        }
                                    }
                                ),
                                "TesseraBridge"
                            )
                            addJavascriptInterface(
                                TesseraWebBridge(
                                    onReaderExit = {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            viewModel.closeReaderMode()
                                        }
                                    },
                                    onArticleExtracted = {},
                                    onExtractionFailed = {},
                                    onVideoPlaybackChanged = { isPlaying, width, height ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            viewModel.setVideoPlaybackState(isPlaying, width, height)
                                        }
                                    }
                                ),
                                "TesseraNativeBridge"
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
                                            viewModel.recordBlockedTracker(host, urlString)
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
                                        viewModel.resetPageTrackers(url)
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

                                        // Detect Reader Mode availability on actual articles/content pages (Immediate + SPA delayed)
                                        val checkReaderScript = """
                                            (function() {
                                                var host = (window.location.hostname || '').toLowerCase();
                                                if (host.includes('google.') || host.includes('duckduckgo.') || host.includes('bing.') || host.includes('duck.ai') || host.includes('youtube.com')) {
                                                    return 'false';
                                                }
                                                var article = document.querySelector('article, [itemprop="articleBody"], main article, .article-body, .post-content, .entry-content, [role="main"] article');
                                                if (article && (article.innerText || article.textContent || '').trim().length > 180) {
                                                    return 'true';
                                                }
                                                var paragraphs = document.querySelectorAll('p');
                                                var substantialP = 0;
                                                for (var i = 0; i < paragraphs.length; i++) {
                                                    if ((paragraphs[i].innerText || paragraphs[i].textContent || '').trim().length > 45) {
                                                        substantialP++;
                                                    }
                                                }
                                                return (substantialP >= 2) ? 'true' : 'false';
                                            })()
                                        """.trimIndent()

                                        view?.evaluateJavascript(checkReaderScript) { result ->
                                            val isAvailable = result?.replace("\"", "")?.trim() == "true"
                                            viewModel.setReaderModeAvailable(isAvailable)
                                        }

                                        // Re-check after 1s to capture client-side hydrated SPA articles (Medium, Substack, News SPAs)
                                        view?.postDelayed({
                                            view.evaluateJavascript(checkReaderScript) { result ->
                                                val isAvailable = result?.replace("\"", "")?.trim() == "true"
                                                if (isAvailable) {
                                                    viewModel.setReaderModeAvailable(true)
                                                }
                                            }
                                        }, 1000)

                                        // Detect Foreign Language for Real-Time Google Translation
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                var l = (document.documentElement.lang || (document.body && document.body.parentElement ? document.body.parentElement.lang : '') || '').trim().toLowerCase();
                                                if (!l) {
                                                    var meta = document.querySelector('meta[http-equiv="content-language"], meta[name="language"]');
                                                    if (meta && meta.content) l = meta.content.trim().toLowerCase();
                                                }
                                                return l;
                                            })()
                                            """.trimIndent()
                                        ) { langResult ->
                                            val detected = langResult?.replace("\"", "")?.trim()?.lowercase() ?: ""
                                            if (detected.isNotBlank() && detected != "null" && url != null) {
                                                viewModel.onPageLanguageDetected(detected, url)
                                            }
                                        }

                                        // Extract dynamic site theme-color / brand accent for Dynamic Tinted Glass
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                var meta = document.querySelector('meta[name="theme-color"]') || 
                                                           document.querySelector('meta[name="msapplication-TileColor"]') ||
                                                           document.querySelector('meta[name="apple-mobile-web-app-status-bar-style"]');
                                                if (meta && meta.content) return meta.content.trim();
                                                var header = document.querySelector('header') || document.querySelector('nav');
                                                if (header) {
                                                    var bg = window.getComputedStyle(header).backgroundColor;
                                                    if (bg && bg !== 'rgba(0, 0, 0, 0)' && bg !== 'transparent') return bg;
                                                }
                                                return '';
                                            })()
                                            """.trimIndent()
                                        ) { colorResult ->
                                            val parsedColor = parseCssColor(colorResult)
                                            viewModel.setSiteThemeColor(parsedColor)
                                        }
                                        // Inject Picture-in-Picture HTML5 Video Observer
                                        view?.evaluateJavascript(PipManager.VIDEO_DETECTION_SCRIPT, null)
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
                                    val safeOrigin = origin ?: ""
                                    val siteSetting = viewModel.getSiteSettings(safeOrigin)
                                    if (siteSetting.locationConfigured) {
                                        callback?.invoke(origin, siteSetting.locationGranted, true)
                                        return
                                    }

                                    AlertDialog.Builder(context)
                                        .setTitle("Permissão de Localização")
                                        .setMessage("$safeOrigin gostaria de acessar sua localização.")
                                        .setPositiveButton("Permitir") { _, _ ->
                                            viewModel.updateSitePermission(safeOrigin) {
                                                it.copy(locationGranted = true, locationConfigured = true)
                                            }
                                            callback?.invoke(origin, true, true)
                                        }
                                        .setNegativeButton("Bloquear") { _, _ ->
                                            viewModel.updateSitePermission(safeOrigin) {
                                                it.copy(locationGranted = false, locationConfigured = true)
                                            }
                                            callback?.invoke(origin, false, false)
                                        }
                                        .setOnCancelListener {
                                            callback?.invoke(origin, false, false)
                                        }
                                        .show()
                                }

                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    if (request == null) return
                                    val reqOrigin = request.origin?.toString() ?: ""
                                    val cleanReqOrigin = try { java.net.URI(reqOrigin).host ?: reqOrigin } catch (e: Exception) { reqOrigin }
                                    val currentSetting = viewModel.getSiteSettings(cleanReqOrigin)

                                    val needsAudio = request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                                    val needsVideo = request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)

                                    if ((needsAudio && currentSetting.micConfigured) || (needsVideo && currentSetting.cameraConfigured)) {
                                        val grantedResources = mutableListOf<String>()
                                        if (needsAudio && currentSetting.micGranted) grantedResources.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                                        if (needsVideo && currentSetting.cameraGranted) grantedResources.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)

                                        if (grantedResources.isNotEmpty()) {
                                            request.grant(grantedResources.toTypedArray())
                                        } else {
                                            request.deny()
                                        }
                                        return
                                    }

                                    AlertDialog.Builder(context)
                                        .setTitle("Permissão de Câmera/Microfone")
                                        .setMessage("$cleanReqOrigin gostaria de acessar seus dispositivos de áudio/vídeo.")
                                        .setPositiveButton("Permitir") { _, _ ->
                                            viewModel.updateSitePermission(cleanReqOrigin) {
                                                it.copy(
                                                    cameraGranted = true,
                                                    cameraConfigured = true,
                                                    micGranted = true,
                                                    micConfigured = true
                                                )
                                            }
                                            request.grant(request.resources)
                                        }
                                        .setNegativeButton("Bloquear") { _, _ ->
                                            viewModel.updateSitePermission(cleanReqOrigin) {
                                                it.copy(
                                                    cameraGranted = false,
                                                    cameraConfigured = true,
                                                    micGranted = false,
                                                    micConfigured = true
                                                )
                                            }
                                            request.deny()
                                        }
                                        .setOnCancelListener { request.deny() }
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
                visible = state.isBarVisible && !state.isInPipMode,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(280)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(280)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // GOOGLE REAL-TIME INLINE DOM TRANSLATE BAR
                    if (!state.isHomePage && state.translationState.isBannerVisible) {
                        TranslateBar(
                            state = state.translationState,
                            isDarkMode = state.isDarkMode,
                            accentColor = state.activeWallpaper.accentColor,
                            onTranslate = {
                                viewModel.setTranslationProgress(isTranslating = true, isTranslated = false)
                                webViewInstance?.evaluateJavascript(
                                    """
                                    (function() {
                                        function doTranslate() {
                                            var combo = document.querySelector('.goog-te-combo');
                                            if (combo) {
                                                combo.value = 'pt';
                                                combo.dispatchEvent(new Event('change'));
                                                return true;
                                            }
                                            return false;
                                        }
                                        if (doTranslate()) return;

                                        document.cookie = 'googtrans=/auto/pt; path=/; domain=' + window.location.hostname;
                                        document.cookie = 'googtrans=/auto/pt; path=/;';

                                        var hideStyle = document.getElementById('tessera-translate-style');
                                        if (!hideStyle) {
                                            hideStyle = document.createElement('style');
                                            hideStyle.id = 'tessera-translate-style';
                                            hideStyle.textContent = '.goog-te-banner-frame, .skiptranslate, #goog-gt-tt, .goog-te-balloon-frame { display: none !important; } body { top: 0px !important; }';
                                            (document.head || document.documentElement).appendChild(hideStyle);
                                        }

                                        window.googleTranslateElementInit = function() {
                                            new google.translate.TranslateElement({
                                                pageLanguage: 'auto',
                                                includedLanguages: 'pt,en,es,fr,de,it,ja,zh-CN,ru',
                                                autoDisplay: false
                                            }, 'google_translate_element');
                                            setTimeout(function() {
                                                doTranslate();
                                            }, 400);
                                        };

                                        var elem = document.getElementById('google_translate_element');
                                        if (!elem) {
                                            elem = document.createElement('div');
                                            elem.id = 'google_translate_element';
                                            elem.style.display = 'none';
                                            (document.body || document.documentElement).appendChild(elem);
                                        }

                                        var s = document.createElement('script');
                                        s.type = 'text/javascript';
                                        s.src = 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
                                        (document.head || document.documentElement).appendChild(s);
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                                coroutineScope.launch {
                                    delay(1200)
                                    viewModel.setTranslationProgress(isTranslating = false, isTranslated = true)
                                }
                            },
                            onRevert = {
                                viewModel.setTranslationProgress(isTranslating = false, isTranslated = false)
                                webViewInstance?.evaluateJavascript(
                                    """
                                    (function() {
                                        document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=' + window.location.hostname;
                                        document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
                                        var combo = document.querySelector('.goog-te-combo');
                                        if (combo) {
                                            combo.value = '';
                                            combo.dispatchEvent(new Event('change'));
                                        }
                                        var frame = document.querySelector('iframe.goog-te-banner-frame');
                                        if (frame) {
                                            try {
                                                var doc = frame.contentDocument || frame.contentWindow.document;
                                                var btn = doc.querySelector('.goog-te-banner-frame button');
                                                if (btn) btn.click();
                                            } catch(e) {}
                                        }
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            },
                            onDismiss = { viewModel.dismissTranslationBanner() },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Podcastify & Áudio em Segundo Plano Mini Player Capsule
                    if (state.podcastAudioState.isMiniPlayerVisible) {
                        PodcastMiniPlayerCapsule(
                            state = state.podcastAudioState,
                            isDarkMode = state.isDarkMode,
                            accentColor = state.activeWallpaper.accentColor,
                            onTogglePlayPause = { viewModel.togglePodcastPlayPause(context) },
                            onOpenFullPlayer = { viewModel.openPodcastFullPlayer() },
                            onCycleSpeed = { viewModel.cyclePodcastSpeed() },
                            onClose = { viewModel.dismissPodcastPlayer(context) },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    TesseraAirBar(
                        progress = state.progress,
                        displayUrl = if (state.isHomePage) "" else state.displayUrl,
                        canGoBack = if (state.isHomePage) false else state.canGoBack,
                        canGoForward = if (state.isHomePage) false else state.canGoForward,
                        tabCount = state.currentSpaceTabs.size,
                        isBookmarked = if (state.isHomePage) false else state.isCurrentPageBookmarked,
                        isIncognito = state.isIncognitoMode,
                        isDarkMode = state.isDarkMode,
                        isHomePage = state.isHomePage,
                        isReaderModeActive = state.isReaderModeActive,
                        isReaderModeAvailable = state.isReaderModeAvailable,
                        favorites = state.currentSpaceFavorites,
                        searchSuggestions = state.searchSuggestions,
                        onBack = { webViewInstance?.goBack() },
                        onForward = { webViewInstance?.goForward() },
                        onHome = { viewModel.goHome() },
                        onReload = { webViewInstance?.reload() },
                        onSearch = { query -> viewModel.openUrl(query) },
                        onQueryChange = { query -> viewModel.fetchSearchSuggestions(query) },
                        onOpenAi = { query ->
                            viewModel.openAiAssistant(
                                title = if (query.isNotBlank()) "Consulta IA" else "Tessera AI",
                                domain = "",
                                url = "",
                                content = query
                            )
                        },
                        onBrowseForMe = { query ->
                            viewModel.openAiAssistant(
                                title = "Tessera AI",
                                domain = "",
                                url = "",
                                content = query
                            )
                        },
                        isEditingExternal = isSearchEditing,
                        onEditingChange = { isSearchEditing = it },
                        onFastAction = {
                            if (state.isHomePage) {
                                viewModel.openAiAssistant("Tessera AI", "", "", "")
                            } else {
                                webViewInstance?.reload()
                            }
                        },
                        onOpenAiAction = {
                            if (state.isReaderModeActive && state.readerArticle != null) {
                                val art = state.readerArticle!!
                                viewModel.openAiAssistant(art.title, art.domain, state.displayUrl, art.plainText)
                            } else if (!state.isHomePage) {
                                val currentTitle = webViewInstance?.title ?: ""
                                val currentDomain = try { Uri.parse(state.displayUrl).host?.replace("www.", "") ?: "" } catch (e: Exception) { "" }
                                viewModel.prepareAiAssistant(currentTitle, currentDomain, state.displayUrl)
                                webViewInstance?.evaluateJavascript(SUMMARY_EXTRACTION_SCRIPT, null)
                            } else {
                                viewModel.openAiAssistant("Tessera AI", "", "", "")
                            }
                        },
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
                            launchReaderMode()
                        },
                        onOpenTabs = { viewModel.toggleTabsModal() },
                        onOpenHistory = { viewModel.toggleHistoryModal() },
                        onOpenSettings = { viewModel.toggleQuickSettings() },
                        onOpenSiteSettings = { viewModel.openSiteSettings(state.displayUrl) },
                        privacyBlockedCount = state.privacyState.pageBlockedCount,
                        onOpenPrivacyDashboard = { viewModel.togglePrivacyDashboard(true) },
                        onOpenFavorite = { url -> viewModel.openUrl(url) },
                        onNextTab = { viewModel.selectNextTab() },
                        onPreviousTab = { viewModel.selectPreviousTab() },
                        accentColor = state.activeWallpaper.accentColor,
                        siteThemeColor = state.siteThemeColor,
                        currentSpaceEmoji = state.currentSpace.iconEmoji,
                        currentSpaceName = state.currentSpace.name,
                        currentSpaceColor = Color(state.currentSpace.colorArgb),
                        onOpenSpaces = { viewModel.toggleSpaceSwitcherModal(true) }
                    )
                }
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
                tabGroups = state.tabGroups,
                spaces = state.spaces,
                activeSpaceId = state.activeSpaceId,
                onSelectSpace = { viewModel.selectSpace(it) },
                onCreateSpace = { name, emoji, color -> viewModel.createSpace(name, emoji, color) },
                onUpdateSpace = { id, name, emoji, color -> viewModel.updateSpace(id, name, emoji, color) },
                onDeleteSpace = { viewModel.deleteSpace(it) },
                onMoveTabToSpace = { tabId, targetSpaceId -> viewModel.moveTabToSpace(tabId, targetSpaceId) },
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.addNewTab() },
                onDismiss = { viewModel.dismissTabsModal() },
                onCreateGroup = { title, colorArgb -> viewModel.createTabGroup(title, colorArgb) },
                onDeleteGroup = { gid, closeTabs -> viewModel.deleteTabGroup(gid, closeTabs) },
                onAddTabToGroup = { tabId, gid -> viewModel.addTabToGroup(tabId, gid) },
                onRemoveTabFromGroup = { tabId -> viewModel.removeTabFromGroup(tabId) },
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
                notes = state.notes,
                onOpenSavedPage = {
                    viewModel.dismissHistoryModal()
                    viewModel.openSavedPage(it)
                },
                onDeleteSavedPage = { viewModel.deleteSavedPage(it) },
                onOpenNotebook = {
                    viewModel.dismissHistoryModal()
                    viewModel.openNotebook()
                },
                onDeleteNote = { viewModel.deleteNote(it) },
                onDismiss = { viewModel.dismissHistoryModal() },
                accentColor = state.activeWallpaper.accentColor
            )
        }

        // TESSERA AI ASSISTANT SHEET (GROQ / GEMINI - SUMARIZAÇÃO & DÚVIDAS)
        if (state.aiAssistantState.isVisible) {
            TesseraAiSheet(
                state = state.aiAssistantState,
                geminiApiKey = state.geminiApiKey,
                groqApiKey = state.groqApiKey,
                isDarkMode = state.isDarkMode,
                isSpeaking = state.isReaderTtsPlaying,
                onSpeakText = { text -> viewModel.speakText(context, text) },
                onStopSpeaking = { viewModel.stopReaderTts() },
                onSelectProvider = { provider -> viewModel.setAiProvider(provider) },
                onSaveKey = { provider, key ->
                    if (provider == AiProvider.GROQ) {
                        viewModel.setGroqApiKey(key)
                    } else {
                        viewModel.setGeminiApiKey(key)
                    }
                },
                onRegenerateSummary = { viewModel.regenerateAiSummary() },
                onAskQuestion = { q -> viewModel.askAiAssistantQuestion(q) },
                onSaveToNotebook = { title, content ->
                    viewModel.saveNote(
                        com.tessera.browser.data.NoteItem(
                            title = title,
                            content = content,
                            sourceUrl = state.aiAssistantState.pageUrl,
                            sourceTitle = state.aiAssistantState.pageTitle,
                            type = com.tessera.browser.data.NoteType.AI_SUMMARY,
                            spaceId = state.activeSpaceId,
                            aiSummary = content
                        )
                    )
                },
                onDismiss = { viewModel.dismissAiAssistant() }
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
                onTranslatePage = {
                    viewModel.dismissQuickSettings()
                    viewModel.showTranslationBanner(true)
                },
                onOpenSiteSettings = {
                    viewModel.dismissQuickSettings()
                    viewModel.openSiteSettings(state.displayUrl)
                },
                onOpenPrivacyDashboard = {
                    viewModel.dismissQuickSettings()
                    viewModel.togglePrivacyDashboard(true)
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
                    viewModel.dismissQuickSettings()
                    launchReaderMode()
                },
                onOpenFullSettings = {
                    viewModel.openFullSettings()
                },
                isAutoPipEnabled = state.isAutoPipEnabled,
                onAutoPipChanged = { viewModel.setAutoPipEnabled(it) },
                onEnterPip = {
                    val activity = context as? Activity
                    if (activity != null) {
                        viewModel.requestEnterPip(activity)
                    }
                },
                onOpenNotebook = {
                    viewModel.dismissQuickSettings()
                    viewModel.openNotebook()
                },
                onClipPage = {
                    viewModel.dismissQuickSettings()
                    clipCurrentPageAction(false)
                },
                geminiApiKey = state.geminiApiKey,
                onGeminiApiKeyChanged = { viewModel.setGeminiApiKey(it) },
                onDismiss = { viewModel.dismissQuickSettings() }
            )
        }

        // FULL SETTINGS SCREEN OVERLAY
        AnimatedVisibility(
            visible = state.showFullSettings,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            TesseraSettingsScreen(
                isDarkMode = state.isDarkMode,
                forceDarkPages = state.forceDarkPages,
                showWallpaper = state.showWallpaper,
                selectedWallpaperId = state.selectedWallpaperId,
                customWallpaperUri = state.customWallpaperUri,
                showFavoritesBar = state.showFavoritesBar,
                showWeatherWidget = state.showWeatherWidget,
                showQuotesWidget = state.showQuotesWidget,
                tesseraAiEnabled = state.tesseraAiEnabled,
                aiToolbarButton = state.aiToolbarButton,
                aiTextHighlightPrompts = state.aiTextHighlightPrompts,
                showSidebar = state.showSidebar,
                autoHideSidebar = state.autoHideSidebar,
                adBlockEnabled = state.adBlockEnabled,
                cookieBlockerEnabled = state.cookieBlockerEnabled,
                selectedSearchEngine = state.searchEngine,
                geminiApiKey = state.geminiApiKey,
                groqApiKey = state.groqApiKey,
                aiProvider = state.aiProvider,
                digitalMinimalismMode = state.digitalMinimalismMode,
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
                onCookieBlockerChanged = { viewModel.toggleCookieBlocker() },
                onSearchEngineSelected = { viewModel.setSearchEngine(it) },
                onGeminiApiKeyChanged = { viewModel.setGeminiApiKey(it) },
                onGroqApiKeyChanged = { viewModel.setGroqApiKey(it) },
                onAiProviderChanged = { viewModel.setAiProvider(it) },
                onDigitalMinimalismModeChanged = { viewModel.setDigitalMinimalismMode(it) },
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
                onOpenDownloads = {
                    viewModel.dismissFullSettings()
                    viewModel.openDownloadsModal()
                },
                onOpenHistory = {
                    viewModel.dismissFullSettings()
                    viewModel.openHistoryModal(0)
                },
                onOpenSiteSettings = {
                    viewModel.dismissFullSettings()
                    viewModel.openSiteSettings(state.displayUrl)
                },
                onBack = { viewModel.dismissFullSettings() }
            )
        }

        // SITE SETTINGS & PERMISSIONS MODAL
        if (state.showSiteSettingsModal) {
            val siteOrigin = state.siteSettingsOrigin.ifBlank {
                try {
                    val uri = java.net.URI(state.displayUrl)
                    val host = uri.host ?: state.displayUrl
                    if (host.startsWith("www.")) host.substring(4) else host
                } catch (e: Exception) {
                    state.displayUrl
                }
            }
            val currentSiteSettings = viewModel.getSiteSettings(siteOrigin)

            SiteSettingsModal(
                origin = siteOrigin,
                currentUrl = state.displayUrl,
                settings = currentSiteSettings,
                isDarkMode = state.isDarkMode,
                accentColor = state.activeWallpaper.accentColor,
                onUpdatePermission = { update ->
                    viewModel.updateSitePermission(siteOrigin, update)
                },
                onClearSiteData = {
                    viewModel.clearSiteData(context, siteOrigin, webViewInstance)
                },
                onDismiss = { viewModel.dismissSiteSettings() }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { customView!! },
                    modifier = Modifier.fillMaxSize()
                )

                // Quick Floating PiP button in fullscreen video
                if (!state.isInPipMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 16.dp, end = 16.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable {
                                val activity = context as? Activity
                                if (activity != null) {
                                    viewModel.requestEnterPip(activity)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SmartDisplay,
                            contentDescription = "Janela Flutuante (PiP)",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // NATIVE IMMERSIVE PDF / E-BOOK DOCUMENT READER SCREEN
        if (state.isReaderModeActive && state.readerArticle != null) {
            TesseraReaderScreen(
                article = state.readerArticle!!,
                isLoading = state.isReaderLoading,
                fontSizeSp = state.readerFontSizeSp,
                theme = state.readerTheme,
                fontFamily = state.readerFontFamily,
                showImages = state.readerShowImages,
                isTtsPlaying = state.isReaderTtsPlaying,
                isSettingsOpen = state.isReaderSettingsOpen,
                readerHighlights = state.readerHighlights,
                isHighlighterActive = state.isReaderHighlighterActive,
                activeHighlightColor = state.activeHighlightColor,
                isAudioBarVisible = state.isReaderAudioBarVisible,
                podcastAudioState = state.podcastAudioState,
                onClose = { viewModel.closeReaderMode() },
                onToggleSettings = { viewModel.toggleReaderSettings() },
                onUpdateFontSize = { delta -> viewModel.updateReaderFontSize(delta) },
                onSelectTheme = { theme -> viewModel.setReaderTheme(theme) },
                onSelectFontFamily = { family -> viewModel.setReaderFontFamily(family) },
                onToggleShowImages = { viewModel.toggleReaderShowImages() },
                onToggleTts = { viewModel.toggleReaderTts(context) },
                onToggleHighlighterActive = { viewModel.toggleReaderHighlighterActive() },
                onSelectHighlightColor = { color -> viewModel.setActiveHighlightColor(color) },
                onToggleHighlightBlock = { idx, color -> viewModel.toggleHighlightBlock(idx, color) },
                onClearHighlights = { viewModel.clearAllReaderHighlights() },
                onToggleAudioBar = { viewModel.toggleReaderAudioBar() },
                onSeekAudio = { deltaMs -> viewModel.seekPodcastBy(context, deltaMs) },
                onCycleSpeed = { viewModel.cyclePodcastSpeed() },
                onOpenArcSummary = {
                    val art = state.readerArticle
                    if (art != null) {
                        viewModel.openAiAssistant(art.title, art.domain, state.displayUrl, art.plainText)
                    }
                }
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

        // PODCASTIFY FULL PLAYER MODAL (ESTILO BOT TESSERA)
        if (state.podcastAudioState.isFullPlayerOpen) {
            PodcastFullPlayerModal(
                state = state.podcastAudioState,
                isDarkMode = state.isDarkMode,
                accentColor = state.activeWallpaper.accentColor,
                onTogglePlayPause = { viewModel.togglePodcastPlayPause(context) },
                onSeekBy = { delta -> viewModel.seekPodcastBy(context, delta) },
                onSeekToFraction = { fraction -> viewModel.seekPodcastToFraction(fraction) },
                onSelectSpeed = { speed -> viewModel.setPodcastSpeed(speed) },
                onSelectVoice = { voice -> viewModel.selectPodcastVoice(context, voice) },
                onDismiss = { viewModel.dismissPodcastFullPlayer() }
            )
        }

        // PRIVACY DASHBOARD SCRIM OVERLAY
        AnimatedVisibility(
            visible = state.privacyState.isVisible,
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
                        viewModel.dismissPrivacyDashboard()
                    }
            )
        }

        // PRIVACY DASHBOARD MODAL (ESCUDO ATIVO & DETALHAMENTO DE RASTREADORES)
        PrivacyDashboardModal(
            state = state.privacyState,
            adBlockEnabled = state.adBlockEnabled,
            cookieBlockerEnabled = state.cookieBlockerEnabled,
            isDarkMode = state.isDarkMode,
            accentColor = state.activeWallpaper.accentColor,
            onToggleAdBlock = { viewModel.setAdBlockEnabled(it) },
            onToggleCookieBlocker = { viewModel.setCookieBlockerEnabled(it) },
            onDismiss = { viewModel.dismissPrivacyDashboard() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // SPACE QUICK SWITCHER MODAL (ARC-STYLE BOTTOM SHEET)
        AnimatedVisibility(
            visible = state.showSpaceSwitcherModal,
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
                        viewModel.toggleSpaceSwitcherModal(false)
                    }
            )
        }

        AnimatedVisibility(
            visible = state.showSpaceSwitcherModal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SpaceQuickSwitcherModal(
                spaces = state.spaces,
                activeSpaceId = state.activeSpaceId,
                tabs = state.tabs,
                isDarkMode = state.isDarkMode,
                onSelectSpace = { viewModel.selectSpace(it) },
                onCreateSpace = { name, emoji, color -> viewModel.createSpace(name, emoji, color) },
                onCreateNewSpace = {
                    viewModel.toggleSpaceSwitcherModal(false)
                    viewModel.toggleTabsModal()
                },
                onDismiss = { viewModel.toggleSpaceSwitcherModal(false) }
            )
        }

        // NOTEBOOK & WEB CLIPPER MODAL
        AnimatedVisibility(
            visible = state.showNotebookModal,
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
                        viewModel.dismissNotebook()
                    }
            )
        }

        AnimatedVisibility(
            visible = state.showNotebookModal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(320)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NotebookModal(
                notes = state.notes,
                spaces = state.spaces,
                activeSpaceId = state.activeSpaceId,
                searchQuery = state.notebookSearchQuery,
                selectedTag = state.notebookFilterTag,
                isDarkMode = state.isDarkMode,
                accentColor = Color(state.currentSpace.colorArgb),
                summarizingNoteId = state.summarizingNoteId,
                onSearchQueryChanged = { viewModel.setNotebookSearchQuery(it) },
                onTagSelected = { viewModel.setNotebookFilterTag(it) },
                onOpenSourceUrl = { url ->
                    viewModel.openUrl(url)
                    viewModel.dismissNotebook()
                },
                onTogglePinNote = { id -> viewModel.togglePinNote(id) },
                onDeleteNote = { id -> viewModel.deleteNote(id) },
                onSaveNote = { note -> viewModel.saveNote(note) },
                onSummarizeNote = { id -> viewModel.summarizeNote(id) },
                onDismiss = { viewModel.dismissNotebook() }
            )
        }

        // PICTURE-IN-PICTURE & OVERLAY PERMISSION DIALOG
        if (state.showPipPermissionDialog) {
            PipPermissionDialog(
                isDarkMode = state.isDarkMode,
                accentColor = state.activeWallpaper.accentColor,
                onOpenSettings = {
                    PipManager.openPipOrOverlaySettings(context)
                },
                onDismiss = {
                    viewModel.showPipPermissionDialog(false)
                }
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

private fun parseCssColor(cssColor: String?): Color? {
    if (cssColor.isNullOrBlank()) return null
    val trimmed = cssColor.trim().removeSurrounding("\"").removeSurrounding("'").trim()
    if (trimmed.isEmpty() || trimmed.equals("null", ignoreCase = true) || trimmed.equals("transparent", ignoreCase = true)) return null

    return try {
        if (trimmed.startsWith("#")) {
            val hex = when (trimmed.length) {
                4 -> "#${trimmed[1]}${trimmed[1]}${trimmed[2]}${trimmed[2]}${trimmed[3]}${trimmed[3]}"
                5 -> "#${trimmed[1]}${trimmed[1]}${trimmed[2]}${trimmed[2]}${trimmed[3]}${trimmed[3]}${trimmed[4]}${trimmed[4]}"
                else -> trimmed
            }
            Color(android.graphics.Color.parseColor(hex))
        } else if (trimmed.startsWith("rgb", ignoreCase = true)) {
            val inner = trimmed.substringAfter("(").substringBefore(")")
            val parts = if (inner.contains(",")) inner.split(",") else inner.split(" ")
            val nums = parts.mapNotNull { it.trim().removeSuffix("%").toFloatOrNull() }
            if (nums.size >= 3) {
                val r = nums[0].toInt().coerceIn(0, 255)
                val g = nums[1].toInt().coerceIn(0, 255)
                val b = nums[2].toInt().coerceIn(0, 255)
                val a = if (nums.size >= 4) {
                    val alphaVal = nums[3]
                    if (alphaVal <= 1.0f) (alphaVal * 255).toInt().coerceIn(0, 255) else alphaVal.toInt().coerceIn(0, 255)
                } else 255
                if (a == 0) return null
                Color(android.graphics.Color.argb(a, r, g, b))
            } else null
        } else {
            Color(android.graphics.Color.parseColor(trimmed))
        }
    } catch (e: Exception) {
        null
    }
}

