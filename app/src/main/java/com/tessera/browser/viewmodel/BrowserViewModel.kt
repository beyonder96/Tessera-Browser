package com.tessera.browser.viewmodel

import androidx.lifecycle.ViewModel
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.data.WallpaperTheme
import com.tessera.browser.data.AvailableWallpapers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class BrowserUiState(
    val isHomePage: Boolean = true,
    val currentUrl: String = "https://duckduckgo.com",
    val displayUrl: String = "",
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val isBarVisible: Boolean = true,
    val showQuickSettings: Boolean = false,

    // Configuração Fácil (Matching Reference Screenshot 3)
    val isDarkMode: Boolean = true,
    val forceDarkPages: Boolean = false,
    val showWallpaper: Boolean = true,
    val selectedWallpaperId: String = "chocolate",
    val showFavoritesBar: Boolean = false,
    val showCatInara: Boolean = false,
    val tesseraAiEnabled: Boolean = true,
    val aiToolbarButton: Boolean = true,
    val aiTextHighlightPrompts: Boolean = true,
    val showSidebar: Boolean = false,
    val autoHideSidebar: Boolean = true,

    // Speed Dial Shortcuts
    val speedDialItems: List<SpeedDialItem> = listOf(
        SpeedDialItem(
            id = "1",
            title = "DuckDuckGo",
            url = "https://duckduckgo.com",
            iconEmoji = "🦆",
            badgeColor = 0xFFDE5833
        ),
        SpeedDialItem(
            id = "2",
            title = "Google",
            url = "https://www.google.com",
            iconEmoji = "🔍",
            badgeColor = 0xFF4285F4
        ),
        SpeedDialItem(
            id = "3",
            title = "YouTube",
            url = "https://www.youtube.com",
            iconEmoji = "▶️",
            badgeColor = 0xFFFF0000
        ),
        SpeedDialItem(
            id = "4",
            title = "Wikipedia",
            url = "https://pt.wikipedia.org",
            iconEmoji = "📖",
            badgeColor = 0xFF333333
        ),
        SpeedDialItem(
            id = "5",
            title = "GitHub",
            url = "https://github.com",
            iconEmoji = "🐙",
            badgeColor = 0xFF24292E
        ),
        SpeedDialItem(
            id = "6",
            title = "Reddit",
            url = "https://reddit.com",
            iconEmoji = "🤖",
            badgeColor = 0xFFFF4500
        )
    )
) {
    val activeWallpaper: WallpaperTheme
        get() = AvailableWallpapers.find { it.id == selectedWallpaperId }
            ?: AvailableWallpapers.first()
}

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun openUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return

        val formattedUrl = formatInputAsUrl(trimmed)
        _uiState.update {
            it.copy(
                isHomePage = false,
                currentUrl = formattedUrl,
                displayUrl = formattedUrl,
                isBarVisible = true
            )
        }
    }

    fun goHome() {
        _uiState.update {
            it.copy(
                isHomePage = true,
                progress = 0f
            )
        }
    }

    fun onPageStarted(url: String?) {
        if (!url.isNullOrBlank() && !_uiState.value.isHomePage) {
            _uiState.update { it.copy(displayUrl = url) }
        }
    }

    fun onPageFinished(url: String?, canBack: Boolean) {
        _uiState.update {
            it.copy(
                progress = 0f,
                canGoBack = canBack,
                displayUrl = url ?: it.displayUrl
            )
        }
    }

    fun updateProgress(progress: Float) {
        _uiState.update { it.copy(progress = progress) }
    }

    fun setBarVisibility(visible: Boolean) {
        if (_uiState.value.isBarVisible != visible) {
            _uiState.update { it.copy(isBarVisible = visible) }
        }
    }

    fun toggleQuickSettings() {
        _uiState.update { it.copy(showQuickSettings = !it.showQuickSettings) }
    }

    fun dismissQuickSettings() {
        _uiState.update { it.copy(showQuickSettings = false) }
    }

    // Settings mutators
    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun setForceDarkPages(enabled: Boolean) {
        _uiState.update { it.copy(forceDarkPages = enabled) }
    }

    fun setShowWallpaper(enabled: Boolean) {
        _uiState.update { it.copy(showWallpaper = enabled) }
    }

    fun selectWallpaper(wallpaperId: String) {
        _uiState.update { it.copy(selectedWallpaperId = wallpaperId, showWallpaper = true) }
    }

    fun setShowFavoritesBar(enabled: Boolean) {
        _uiState.update { it.copy(showFavoritesBar = enabled) }
    }

    fun setShowCatInara(enabled: Boolean) {
        _uiState.update { it.copy(showCatInara = enabled) }
    }

    fun setTesseraAiEnabled(enabled: Boolean) {
        _uiState.update { it.copy(tesseraAiEnabled = enabled) }
    }

    fun setAiToolbarButton(enabled: Boolean) {
        _uiState.update { it.copy(aiToolbarButton = enabled) }
    }

    fun setAiTextHighlightPrompts(enabled: Boolean) {
        _uiState.update { it.copy(aiTextHighlightPrompts = enabled) }
    }

    fun setShowSidebar(enabled: Boolean) {
        _uiState.update { it.copy(showSidebar = enabled) }
    }

    fun setAutoHideSidebar(enabled: Boolean) {
        _uiState.update { it.copy(autoHideSidebar = enabled) }
    }

    // Speed Dial Management
    fun addSpeedDialItem(title: String, rawUrl: String) {
        val url = formatInputAsUrl(rawUrl.trim())
        val initialLetter = title.firstOrNull()?.uppercase() ?: "W"
        val newItem = SpeedDialItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            url = url,
            initial = initialLetter,
            badgeColor = 0xFF3D322B
        )
        _uiState.update {
            it.copy(speedDialItems = it.speedDialItems + newItem)
        }
    }

    fun removeSpeedDialItem(id: String) {
        _uiState.update {
            it.copy(speedDialItems = it.speedDialItems.filterNot { item -> item.id == id })
        }
    }

    private fun formatInputAsUrl(rawInput: String): String {
        return when {
            rawInput.startsWith("http://") || rawInput.startsWith("https://") -> rawInput
            rawInput.contains(".") && !rawInput.contains(" ") -> "https://$rawInput"
            else -> "https://duckduckgo.com/?q=${rawInput.replace(" ", "+")}"
        }
    }
}
