package com.tessera.browser

import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.tessera.browser.pip.PipManager
import com.tessera.browser.ui.TesseraBrowserScreen
import com.tessera.browser.ui.theme.TesseraTheme
import com.tessera.browser.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by lazy {
        ViewModelProvider(this)[BrowserViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Make system bars fully transparent for immersive feel
        WindowCompat.setDecorFitsSystemWindows(window, false)

        handleIntent(intent)

        setContent {
            val state by viewModel.uiState.collectAsState()

            // On Android 12+, keep autoEnterEnabled in sync with video playback state
            LaunchedEffect(state.isVideoPlaying, state.isInFullscreenVideo, state.isAutoPipEnabled, state.videoWidth, state.videoHeight) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && PipManager.isPipSupported(this@MainActivity)) {
                    val hasPerm = PipManager.hasOverlayOrPipPermission(this@MainActivity)
                    val shouldAutoEnter = state.isAutoPipEnabled && (state.isInFullscreenVideo || state.isVideoPlaying) && hasPerm
                    val params = PipManager.buildPipParams(
                        width = state.videoWidth,
                        height = state.videoHeight,
                        autoEnter = shouldAutoEnter
                    )
                    if (params != null) {
                        try {
                            setPictureInPictureParams(params)
                        } catch (e: Exception) {
                            // Ignora se não puder atualizar no momento
                        }
                    }
                }
            }

            TesseraTheme(isDarkMode = state.isDarkMode) {
                TesseraBrowserScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        viewModel.setInPictureInPictureMode(isInPictureInPictureMode)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val state = viewModel.uiState.value
        val shouldPip = state.isAutoPipEnabled && (state.isInFullscreenVideo || state.isVideoPlaying)

        if (shouldPip && PipManager.isPipSupported(this)) {
            if (PipManager.hasOverlayOrPipPermission(this)) {
                PipManager.enterPip(
                    activity = this,
                    width = state.videoWidth,
                    height = state.videoHeight
                )
            } else {
                viewModel.showPipPermissionDialog(true)
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val data = intent.dataString ?: intent.data?.toString()
                if (!data.isNullOrBlank()) {
                    viewModel.openFromExternal(data)
                    return
                }
            }
            Intent.ACTION_WEB_SEARCH, Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY)
                if (!query.isNullOrBlank()) {
                    viewModel.openFromExternal(query)
                    return
                }
            }
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.openFromExternal(sharedText)
                        return
                    }
                }
            }
        }

        // Fallback para qualquer intent com URL válida nos dados
        val fallbackData = intent.dataString ?: intent.data?.toString()
        if (!fallbackData.isNullOrBlank() && (fallbackData.startsWith("http://") || fallbackData.startsWith("https://"))) {
            viewModel.openFromExternal(fallbackData)
        }
    }
}
