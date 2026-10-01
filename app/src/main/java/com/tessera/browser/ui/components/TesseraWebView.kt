package com.tessera.browser.ui.components

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView

/**
 * WebView especializado para o Tessera Browser com integração de privacidade para teclados (IMEs).
 * Quando o Modo Anônimo está ativo, intercepta o onCreateInputConnection de qualquer campo de texto
 * da web (Google, redes sociais, formulários) e injeta a flag oficial IME_FLAG_NO_PERSONALIZED_LEARNING
 * e metadados dedicados para comunicação nativa com o Tessera-Keyboard e o Google Gboard.
 */
class TesseraWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var isIncognitoMode: Boolean = false

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val inputConnection = super.onCreateInputConnection(outAttrs)
        if (isIncognitoMode) {
            // Flag oficial da plataforma Android (API 26+) para desativar aprendizado e telemetria do teclado
            outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING

            // privateImeOptions reconhecido pelo Gboard e pelo ecossistema Tessera
            val currentPrivate = outAttrs.privateImeOptions
            outAttrs.privateImeOptions = if (!currentPrivate.isNullOrBlank()) {
                "$currentPrivate,com.tessera.browser.INCOGNITO,com.google.android.inputmethod.latin.noSuggestions"
            } else {
                "com.tessera.browser.INCOGNITO,com.google.android.inputmethod.latin.noSuggestions"
            }

            // Extras customizados de alto nível para integração com o Tessera-Keyboard
            if (outAttrs.extras == null) {
                outAttrs.extras = Bundle()
            }
            outAttrs.extras?.apply {
                putBoolean("com.tessera.browser.INCOGNITO_MODE", true)
                putBoolean("isIncognito", true)
                putString("tessera_theme", "incognito_stealth")
            }
        }
        return inputConnection
    }
}
