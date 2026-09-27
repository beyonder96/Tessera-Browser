package com.tessera.browser.data

import android.net.Uri

/**
 * Representa os dados capturados durante um toque longo (long-press) em elementos do WebView.
 * Suporta Links, Imagens, Imagens com Link (âncoras), Telefones, E-mails e Geolocalização.
 */
data class ContextMenuTarget(
    val linkUrl: String? = null,
    val imageUrl: String? = null,
    val title: String? = null,
    val hitType: Int = 0
) {
    val isLink: Boolean get() = !linkUrl.isNullOrBlank() && !isSpecialIntent
    val isImage: Boolean get() = !imageUrl.isNullOrBlank()
    val isBothLinkAndImage: Boolean get() = isLink && isImage

    val isPhone: Boolean get() = linkUrl?.startsWith("tel:", ignoreCase = true) == true
    val isEmail: Boolean get() = linkUrl?.startsWith("mailto:", ignoreCase = true) == true
    val isGeo: Boolean get() = linkUrl?.startsWith("geo:", ignoreCase = true) == true
    val isSpecialIntent: Boolean get() = isPhone || isEmail || isGeo

    val cleanPhoneNumber: String get() = linkUrl?.substringAfter("tel:") ?: ""
    val cleanEmailAddress: String get() = linkUrl?.substringAfter("mailto:") ?: ""

    val displayHost: String get() {
        val target = linkUrl ?: imageUrl ?: return ""
        return try {
            val uri = Uri.parse(target)
            uri.host?.replace("www.", "") ?: target
        } catch (e: Exception) {
            target
        }
    }
}
