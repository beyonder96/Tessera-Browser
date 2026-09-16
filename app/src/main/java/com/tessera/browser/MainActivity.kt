package com.tessera.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.tessera.browser.ui.TesseraBrowserScreen
import com.tessera.browser.ui.theme.TesseraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Make system bars fully transparent for immersive feel
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TesseraTheme {
                TesseraBrowserScreen()
            }
        }
    }
}
