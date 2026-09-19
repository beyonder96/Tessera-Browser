package com.tessera.browser.data

import org.json.JSONObject
import java.util.UUID

data class SavedPageItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("url", url)
        put("filePath", filePath)
        put("timestamp", timestamp)
        put("fileSize", fileSize)
    }

    companion object {
        fun fromJson(json: JSONObject): SavedPageItem = SavedPageItem(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", "Página Salva"),
            url = json.optString("url", ""),
            filePath = json.optString("filePath", ""),
            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
            fileSize = json.optLong("fileSize", 0L)
        )
    }
}
