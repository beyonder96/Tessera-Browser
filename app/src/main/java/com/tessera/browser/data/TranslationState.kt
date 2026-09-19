package com.tessera.browser.data

data class TranslationState(
    val isBannerVisible: Boolean = false,
    val detectedLanguageCode: String = "",
    val detectedLanguageName: String = "Inglês",
    val targetLanguageCode: String = "pt",
    val targetLanguageName: String = "Português",
    val isTranslating: Boolean = false,
    val isTranslated: Boolean = false
) {
    companion object {
        fun getLanguageDisplayName(code: String): String {
            val clean = code.trim().lowercase().split("-", "_").firstOrNull() ?: code
            return when (clean) {
                "en" -> "Inglês"
                "es" -> "Espanhol"
                "fr" -> "Francês"
                "de" -> "Alemão"
                "it" -> "Italiano"
                "ja" -> "Japonês"
                "zh" -> "Chinês"
                "ru" -> "Russo"
                "pt" -> "Português"
                "ko" -> "Coreano"
                "ar" -> "Árabe"
                "hi" -> "Hindi"
                "tr" -> "Turco"
                "nl" -> "Holandês"
                "pl" -> "Polonês"
                "sv" -> "Sueco"
                else -> code.uppercase()
            }
        }
    }
}
