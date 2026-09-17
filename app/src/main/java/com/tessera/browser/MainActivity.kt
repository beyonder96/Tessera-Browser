package com.tessera.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tessera.browser.ui.TesseraBrowserScreen
import com.tessera.browser.ui.theme.TesseraTheme
import com.tessera.browser.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Make system bars fully transparent for immersive feel
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val viewModel: BrowserViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()
            TesseraTheme(isDarkMode = state.isDarkMode) {
                TesseraBrowserScreen(viewModel = viewModel)
            }
        }
    }
}
