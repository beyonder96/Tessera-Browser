package com.tessera.browser.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val remoteVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val releaseUrl: String
)

object TesseraUpdateManager {

    private const val PREFS_NAME = "tessera_update_prefs"
    private const val KEY_LAST_CHECK = "last_check_timestamp"
    private const val KEY_REMOTE_VERSION = "cached_remote_version"
    private const val KEY_DOWNLOAD_URL = "cached_download_url"
    private const val KEY_RELEASE_URL = "cached_release_url"
    private const val KEY_RELEASE_NOTES = "cached_release_notes"
    private const val CHECK_INTERVAL_MS = 15 * 60 * 1000L // 15 minutos de intervalo de cache

    suspend fun checkForUpdates(
        context: Context,
        repoOwner: String = "beyonder96",
        repoName: String = "Tessera-Browser",
        forceCheck: Boolean = false
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
            val now = System.currentTimeMillis()

            val currentVersion = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: ""
            } catch (e: Exception) {
                ""
            }

            // Se dentro da janela de cache e não for forçado, retorna o último resultado se ainda houver update
            if (!forceCheck && (now - lastCheck < CHECK_INTERVAL_MS)) {
                val cachedRemote = prefs.getString(KEY_REMOTE_VERSION, null)
                val cachedDownload = prefs.getString(KEY_DOWNLOAD_URL, null)
                val cachedRelease = prefs.getString(KEY_RELEASE_URL, null)
                val cachedNotes = prefs.getString(KEY_RELEASE_NOTES, "") ?: ""
                if (!cachedRemote.isNullOrBlank() && !cachedDownload.isNullOrBlank()) {
                    val hasUpdate = isNewerVersion(cachedRemote, currentVersion)
                    if (hasUpdate) {
                        return@withContext AppUpdateInfo(
                            hasUpdate = true,
                            remoteVersion = cachedRemote,
                            releaseNotes = cachedNotes,
                            downloadUrl = cachedDownload,
                            releaseUrl = cachedRelease ?: cachedDownload
                        )
                    }
                }
                return@withContext null
            }

            val url = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Tessera-Android-App")
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode != 200) {
                return@withContext null
            }

            val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)
            val tagName = json.optString("tag_name", "").trim()
            val remoteVersion = tagName.removePrefix("v").removePrefix("V").trim()
            val releaseNotes = json.optString("body", "Nova versão disponível com melhorias e correções.")
            val releaseUrl = json.optString("html_url", "https://github.com/$repoOwner/$repoName/releases/latest")

            var downloadUrl = releaseUrl
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                var fallbackApk: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    val assetDownload = asset.optString("browser_download_url", "")
                    if (assetName.contains("release", ignoreCase = true) && assetName.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = assetDownload
                        break
                    } else if (assetName.endsWith(".apk", ignoreCase = true) && fallbackApk == null) {
                        fallbackApk = assetDownload
                    }
                }
                if (downloadUrl == releaseUrl && fallbackApk != null) {
                    downloadUrl = fallbackApk
                }
            }

            val hasUpdate = isNewerVersion(remoteVersion, currentVersion)

            prefs.edit()
                .putLong(KEY_LAST_CHECK, now)
                .putString(KEY_REMOTE_VERSION, remoteVersion)
                .putString(KEY_DOWNLOAD_URL, downloadUrl)
                .putString(KEY_RELEASE_URL, releaseUrl)
                .putString(KEY_RELEASE_NOTES, releaseNotes)
                .apply()

            AppUpdateInfo(
                hasUpdate = hasUpdate,
                remoteVersion = remoteVersion,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                releaseUrl = releaseUrl
            )
        } catch (e: Exception) {
            null
        }
    }

    fun isNewerVersion(remote: String, current: String): Boolean {
        if (remote.isBlank() || current.isBlank()) return false
        val cleanRemote = remote.removePrefix("v").removePrefix("V").trim()
        val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
        val pRemote = cleanRemote.split(".").map { it.toIntOrNull() ?: 0 }
        val pCurrent = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(pRemote.size, pCurrent.size)
        for (i in 0 until maxLen) {
            val r = pRemote.getOrElse(i) { 0 }
            val c = pCurrent.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    fun launchDownload(context: Context, downloadUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
