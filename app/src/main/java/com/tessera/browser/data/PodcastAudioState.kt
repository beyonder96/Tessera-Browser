package com.tessera.browser.data

enum class PodcastVoice(
    val id: String,
    val displayName: String,
    val description: String,
    val isNeural: Boolean
) {
    KORE("Kore", "Kore (Neural)", "Voz feminina natural e expressiva", true),
    FENRIR("Fenrir", "Fenrir (Neural)", "Voz masculina profunda e calma", true),
    PUCK("Puck", "Puck (Neural)", "Voz jovem dinâmica", true),
    AOEDE("Aoede", "Aoede (Neural)", "Voz feminina suave e melódica", true),
    SYSTEM("system", "Sistema Android", "Voz local offline do aparelho", false)
}

data class PodcastAudioState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val selectedVoice: PodcastVoice = PodcastVoice.KORE,
    val isMiniPlayerVisible: Boolean = false,
    val isFullPlayerOpen: Boolean = false,
    val isNeuralVoiceActive: Boolean = false,
    val error: String? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
