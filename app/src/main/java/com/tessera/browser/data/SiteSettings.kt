package com.tessera.browser.data

import org.json.JSONObject

data class SiteSettings(
    val origin: String, // e.g. "https://example.com" or "example.com"
    val locationGranted: Boolean = false,
    val locationConfigured: Boolean = false,
    val cameraGranted: Boolean = false,
    val cameraConfigured: Boolean = false,
    val micGranted: Boolean = false,
    val micConfigured: Boolean = false,
    val cookiesAllowed: Boolean = true,
    val javascriptAllowed: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("origin", origin)
        put("locationGranted", locationGranted)
        put("locationConfigured", locationConfigured)
        put("cameraGranted", cameraGranted)
        put("cameraConfigured", cameraConfigured)
        put("micGranted", micGranted)
        put("micConfigured", micConfigured)
        put("cookiesAllowed", cookiesAllowed)
        put("javascriptAllowed", javascriptAllowed)
        put("lastModified", lastModified)
    }

    companion object {
        fun fromJson(json: JSONObject): SiteSettings {
            return SiteSettings(
                origin = json.optString("origin", ""),
                locationGranted = json.optBoolean("locationGranted", false),
                locationConfigured = json.optBoolean("locationConfigured", false),
                cameraGranted = json.optBoolean("cameraGranted", false),
                cameraConfigured = json.optBoolean("cameraConfigured", false),
                micGranted = json.optBoolean("micGranted", false),
                micConfigured = json.optBoolean("micConfigured", false),
                cookiesAllowed = json.optBoolean("cookiesAllowed", true),
                javascriptAllowed = json.optBoolean("javascriptAllowed", true),
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }
    }
}
