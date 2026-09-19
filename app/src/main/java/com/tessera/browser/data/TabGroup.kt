package com.tessera.browser.data

import org.json.JSONObject
import java.util.UUID

data class TabGroup(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val colorArgb: Long = 0xFF4285F4, // Google Blue default
    val isCollapsed: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("colorArgb", colorArgb)
        put("isCollapsed", isCollapsed)
    }

    companion object {
        val PRESET_COLORS = listOf(
            0xFF4285F4, // Blue
            0xFF34A853, // Green
            0xFFEA4335, // Red
            0xFFFBBC05, // Yellow
            0xFFA142F4, // Purple
            0xFFFF7043, // Coral
            0xFF00ACC1, // Cyan
            0xFFE91E63  // Pink
        )

        fun fromJson(json: JSONObject): TabGroup {
            return TabGroup(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Grupo"),
                colorArgb = json.optLong("colorArgb", 0xFF4285F4),
                isCollapsed = json.optBoolean("isCollapsed", false)
            )
        }
    }
}
