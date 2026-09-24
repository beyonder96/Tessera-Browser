package com.tessera.browser.data

import org.json.JSONObject
import java.util.UUID

/**
 * Representa um Espaço isolado de navegação (Arc-style Spaces).
 * Cada espaço possui sua própria paleta de cores temática, emoji identificador,
 * abas ativas, grupos e contexto de favoritos/histórico.
 */
data class BrowserSpace(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconEmoji: String = "🌐",
    val colorArgb: Long = 0xFF0288D1,
    val isDefault: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("iconEmoji", iconEmoji)
        put("colorArgb", colorArgb)
        put("isDefault", isDefault)
    }

    companion object {
        val DEFAULT_SPACES = listOf(
            BrowserSpace(
                id = "space_general",
                name = "Geral",
                iconEmoji = "🌐",
                colorArgb = 0xFF0288D1, // Ocean Blue
                isDefault = true
            ),
            BrowserSpace(
                id = "space_work",
                name = "Trabalho",
                iconEmoji = "💼",
                colorArgb = 0xFFFF7043, // Amber / Warm Orange
                isDefault = false
            ),
            BrowserSpace(
                id = "space_personal",
                name = "Pessoal",
                iconEmoji = "🏠",
                colorArgb = 0xFF00E676, // Neon Emerald
                isDefault = false
            )
        )

        val PRESET_COLORS = listOf(
            0xFF0288D1, // Ocean Blue
            0xFFFF7043, // Amber / Warm Orange
            0xFF00E676, // Neon Emerald
            0xFFAB47BC, // Violet / Purple
            0xFFFF4081, // Rose Pink
            0xFFFFD600, // Solar Gold
            0xFF00B0FF, // Sky Blue
            0xFF26A69A, // Seafoam Teal
            0xFF78909C  // Slate Gray
        )

        val PRESET_EMOJIS = listOf(
            "🌐", "💼", "🏠", "📚", "🚀", "🎨", "🎮", "⚡", "🔬", "💡", "🎧", "☕", "📊", "🎯"
        )

        fun fromJson(json: JSONObject): BrowserSpace {
            return BrowserSpace(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Espaço"),
                iconEmoji = json.optString("iconEmoji", "🌐"),
                colorArgb = json.optLong("colorArgb", 0xFF0288D1),
                isDefault = json.optBoolean("isDefault", false)
            )
        }
    }
}
