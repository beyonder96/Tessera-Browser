package com.tessera.browser.util

import com.tessera.browser.data.NoteItem
import com.tessera.browser.data.NoteType
import org.json.JSONObject

object WebClipperHelper {

    /**
     * Script JavaScript injetado para capturar o conteúdo selecionado ou o artigo principal da página.
     */
    val CLIP_PAGE_SCRIPT = """
        (function() {
            try {
                var selection = window.getSelection() ? window.getSelection().toString().trim() : '';
                var isSelected = selection.length > 0;
                var title = document.title || 'Página Web';
                var url = window.location.href;
                var content = '';
                var isCode = false;

                if (isSelected) {
                    content = selection;
                    var anchorNode = window.getSelection().anchorNode;
                    var parentTag = anchorNode && anchorNode.parentElement ? anchorNode.parentElement.tagName.toUpperCase() : '';
                    if (parentTag === 'PRE' || parentTag === 'CODE' || content.indexOf('\n') > 0 && (content.indexOf('{') >= 0 || content.indexOf('function') >= 0 || content.indexOf('def ') >= 0 || content.indexOf('val ') >= 0)) {
                        isCode = true;
                    }
                } else {
                    var article = document.querySelector('article') || document.querySelector('main') || document.querySelector('.article-content') || document.querySelector('.post-content');
                    if (article) {
                        content = article.innerText.trim();
                    } else if (document.body) {
                        content = document.body.innerText.trim();
                    }
                    if (content.length > 4000) {
                        content = content.substring(0, 4000) + '...';
                    }
                }

                return JSON.stringify({
                    title: title,
                    url: url,
                    content: content,
                    isSelection: isSelected,
                    isCode: isCode
                });
            } catch (e) {
                return JSON.stringify({
                    title: document.title || 'Página Web',
                    url: window.location.href,
                    content: '',
                    isSelection: false,
                    isCode: false,
                    error: e.toString()
                });
            }
        })();
    """.trimIndent()

    /**
     * Converte o resultado de evaluateJavascript em um NoteItem.
     */
    fun parseClippingResult(
        rawResult: String?,
        currentSpaceId: String? = null
    ): NoteItem? {
        if (rawResult.isNullOrBlank() || rawResult == "null") return null

        // Se o resultado estiver envelopado em aspas de string JSON
        var jsonStr = rawResult.trim()
        if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
            try {
                jsonStr = org.json.JSONTokener(jsonStr).nextValue().toString()
            } catch (e: Exception) {
                jsonStr = jsonStr.substring(1, jsonStr.length - 1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\\\", "\\")
            }
        }

        return try {
            val json = JSONObject(jsonStr)
            val title = json.optString("title", "Anotação").trim()
            val url = json.optString("url", "")
            val content = json.optString("content", "").trim()
            val isSelection = json.optBoolean("isSelection", false)
            val isCode = json.optBoolean("isCode", false)

            if (content.isBlank()) return null

            val noteType = when {
                isCode -> NoteType.CODE_SNIPPET
                isSelection -> NoteType.TEXT_CLIP
                else -> NoteType.FULL_ARTICLE
            }

            val defaultTag = when (noteType) {
                NoteType.CODE_SNIPPET -> "Código"
                NoteType.FULL_ARTICLE -> "Artigo"
                NoteType.TEXT_CLIP -> "Pesquisa"
                else -> "Geral"
            }

            NoteItem(
                title = if (isSelection && content.length <= 40) content else title,
                content = content,
                sourceUrl = url,
                sourceTitle = title,
                type = noteType,
                tags = listOf(defaultTag),
                spaceId = currentSpaceId,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
