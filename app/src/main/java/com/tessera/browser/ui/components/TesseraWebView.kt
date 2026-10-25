package com.tessera.browser.ui.components

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView
import kotlin.math.abs

/**
 * WebView especializado para o Tessera Browser com suporte a Pull-to-Refresh e integração de privacidade para teclados.
 */
class TesseraWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var isIncognitoMode: Boolean = false
    var onPullToRefresh: (() -> Unit)? = null
    var onPullDistanceChange: ((Float) -> Unit)? = null

    private var startY = 0f
    private var startX = 0f
    private var isAtTopAtStart = false
    private var isPulling = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (onPullToRefresh != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    startX = event.rawX
                    isAtTopAtStart = (scrollY == 0 || !canScrollVertically(-1))
                    isPulling = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - startY
                    val deltaX = event.rawX - startX

                    if (isAtTopAtStart && (scrollY == 0 || !canScrollVertically(-1)) && deltaY > 0 && deltaY > abs(deltaX) * 1.2f) {
                        val distance = (deltaY - 12f).coerceAtLeast(0f) * 0.45f
                        if (distance > 0f) {
                            isPulling = true
                            onPullDistanceChange?.invoke(distance)
                        }
                    } else if (isPulling) {
                        val distance = (event.rawY - startY).coerceAtLeast(0f) * 0.45f
                        onPullDistanceChange?.invoke(distance)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isPulling) {
                        val distance = (event.rawY - startY).coerceAtLeast(0f) * 0.45f
                        if (distance > 130f) {
                            onPullToRefresh?.invoke()
                        }
                        isPulling = false
                        onPullDistanceChange?.invoke(0f)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

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
