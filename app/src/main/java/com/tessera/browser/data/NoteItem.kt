package com.tessera.browser.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Tipo de anotação ou clipe capturado no Caderno do Tessera.
 */
enum class NoteType(val displayName: String, val iconEmoji: String) {
    TEXT_CLIP("Trecho", "📝"),
    CODE_SNIPPET("Código", "💻"),
    FULL_ARTICLE("Artigo", "📰"),
    AI_SUMMARY("Síntese IA", "✨"),
    QUICK_NOTE("Anotação", "💡");

    companion object {
        fun fromString(value: String): NoteType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TEXT_CLIP
        }
    }
}

/**
 * Representa um item ou trecho salvo no Caderno de Notas & Web Clipper.
 */
data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val sourceUrl: String = "",
    val sourceTitle: String = "",
    val type: NoteType = NoteType.TEXT_CLIP,
    val tags: List<String> = emptyList(),
    val spaceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val aiSummary: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("sourceUrl", sourceUrl)
        put("sourceTitle", sourceTitle)
        put("type", type.name)
        val tagsArray = JSONArray()
        tags.forEach { tagsArray.put(it) }
        put("tags", tagsArray)
        if (spaceId != null) put("spaceId", spaceId)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("isPinned", isPinned)
        if (aiSummary != null) put("aiSummary", aiSummary)
    }

    companion object {
        fun fromJson(json: JSONObject): NoteItem {
            val tagsList = mutableListOf<String>()
            val tagsArray = json.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.optString(i))
                }
            }

            return NoteItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Anotação sem título"),
                content = json.optString("content", ""),
                sourceUrl = json.optString("sourceUrl", ""),
                sourceTitle = json.optString("sourceTitle", ""),
                type = NoteType.fromString(json.optString("type", NoteType.TEXT_CLIP.name)),
                tags = tagsList,
                spaceId = if (json.has("spaceId") && !json.isNull("spaceId")) json.optString("spaceId") else null,
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
                isPinned = json.optBoolean("isPinned", false),
                aiSummary = if (json.has("aiSummary") && !json.isNull("aiSummary")) json.optString("aiSummary") else null
            )
        }

        val PRESET_TAGS = listOf("Pesquisa", "Artigo", "Código", "Resumos", "Geral", "Trabalho", "Estudo")
    }
}
