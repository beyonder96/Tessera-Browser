package com.tessera.browser.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tessera.browser.ui.components.QuickSettingsPanel
import com.tessera.browser.ui.components.TesseraAirBar
import com.tessera.browser.ui.components.TesseraStartPage
import com.tessera.browser.viewmodel.BrowserViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TesseraBrowserScreen(viewModel: BrowserViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current

    // Handle back navigation:
    // 1. If WebView has back history -> goBack()
    // 2. Else if viewing a website -> return to Start Page
    // 3. If already on Start Page -> system back (exit)
    BackHandler(enabled = !state.isHomePage) {
        if (state.canGoBack) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120E0D))
    ) {
        if (state.isHomePage) {
            // NATIVE START PAGE / DISCAGEM RÁPIDA
            TesseraStartPage(
                speedDialItems = state.speedDialItems,
                activeWallpaper = state.activeWallpaper,
                showWallpaper = state.showWallpaper,
                showFavoritesBar = state.showFavoritesBar,
                showCatInara = state.showCatInara,
                showAiButton = state.tesseraAiEnabled && state.aiToolbarButton,
                onSearch = { query -> viewModel.openUrl(query) },
                onOpenUrl = { url -> viewModel.openUrl(url) },
                onAddShortcut = { title, url -> viewModel.addSpeedDialItem(title, url) },
                onRemoveShortcut = { id -> viewModel.removeSpeedDialItem(id) },
                onOpenSettings = { viewModel.toggleQuickSettings() },
                onOpenAi = { viewModel.toggleQuickSettings() }
            )
        } else {
            // WEBVIEW BROWSER VIEW
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            isNestedScrollingEnabled = true

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
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

                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    viewModel.onPageStarted(url)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    viewModel.onPageFinished(url, canGoBack())
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    viewModel.updateProgress(newProgress / 100f)
                                }
                            }

                            loadUrl(state.currentUrl)
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        applyForceDark(view.settings, state.forceDarkPages)
                        if (view.url != state.currentUrl && state.currentUrl.isNotBlank()) {
                            view.loadUrl(state.currentUrl)
                        }
                    }
                )

                // Floating TesseraAirBar
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
                        displayUrl = state.displayUrl,
                        canGoBack = state.canGoBack,
                        onBack = { webViewInstance?.goBack() },
                        onHome = { viewModel.goHome() },
                        onReload = { webViewInstance?.reload() },
                        onSearch = { query -> viewModel.openUrl(query) },
                        onOpenSettings = { viewModel.toggleQuickSettings() }
                    )
                }
            }
        }

        // Quick Settings Scrim Overlay
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

        // Quick Settings Bottom Sheet Panel
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
                onDarkModeChanged = { viewModel.setDarkMode(it) },
                onForceDarkPagesChanged = { viewModel.setForceDarkPages(it) },
                onShowWallpaperChanged = { viewModel.setShowWallpaper(it) },
                onSelectWallpaper = { viewModel.selectWallpaper(it) },
                onShowFavoritesBarChanged = { viewModel.setShowFavoritesBar(it) },
                onShowCatInaraChanged = { viewModel.setShowCatInara(it) },
                onTesseraAiChanged = { viewModel.setTesseraAiEnabled(it) },
                onAiToolbarButtonChanged = { viewModel.setAiToolbarButton(it) },
                onAiTextHighlightPromptsChanged = { viewModel.setAiTextHighlightPrompts(it) },
                onShowSidebarChanged = { viewModel.setShowSidebar(it) },
                onAutoHideSidebarChanged = { viewModel.setAutoHideSidebar(it) },
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
