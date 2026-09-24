package com.tessera.browser.data

import androidx.annotation.DrawableRes
import org.json.JSONObject
import java.util.UUID

data class SpeedDialItem(
    val id: String,
    val title: String,
    val url: String,
    @DrawableRes val iconRes: Int? = null,
    val iconUrl: String? = null,
    val initial: String? = null,
    val badgeColor: Long = 0xFF2A2522,
    val spaceId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("url", url)
        put("iconRes", iconRes ?: -1)
        put("iconUrl", iconUrl ?: "")
        put("initial", initial ?: "")
        put("badgeColor", badgeColor)
        put("spaceId", spaceId ?: "")
    }

    companion object {
        fun fromJson(json: JSONObject): SpeedDialItem {
            val res = json.optInt("iconRes", -1)
            val iconUrl = json.optString("iconUrl", "")
            val initial = json.optString("initial", "")
            val sId = json.optString("spaceId", "")
            return SpeedDialItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", ""),
                url = json.optString("url", ""),
                iconRes = if (res > 0) res else null,
                iconUrl = if (iconUrl.isNotBlank()) iconUrl else null,
                initial = if (initial.isNotBlank()) initial else null,
                badgeColor = json.optLong("badgeColor", 0xFF2A2522),
                spaceId = if (sId.isNotBlank()) sId else null
            )
        }
    }
}
