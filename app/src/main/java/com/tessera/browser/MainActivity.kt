package com.tessera.browser

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
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
