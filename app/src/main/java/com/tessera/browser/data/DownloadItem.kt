package com.tessera.browser.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DownloadStatus {
    PENDING,
    RUNNING,
    SUCCESSFUL,
    FAILED,
    CANCELLED;

    companion object {
        fun fromString(value: String): DownloadStatus = try {
            valueOf(value)
        } catch (e: Exception) {
            SUCCESSFUL
        }
    }
}

enum class DownloadFileType {
    PDF,
    APK,
    IMAGE,
    VIDEO,
    AUDIO,
    ARCHIVE,
    DOCUMENT,
    GENERIC
}

data class DownloadItem(
    val id: Long,
    val fileName: String,
    val url: String,
    val mimeType: String,
    val filePath: String? = null,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.RUNNING,
    val timestamp: Long = System.currentTimeMillis()
) {
    val fileType: DownloadFileType
        get() {
            val lowerName = fileName.lowercase()
            val lowerMime = mimeType.lowercase()
            return when {
                lowerName.endsWith(".apk") || lowerMime.contains("android.package-archive") -> DownloadFileType.APK
                lowerName.endsWith(".pdf") || lowerMime.contains("pdf") -> DownloadFileType.PDF
                lowerMime.startsWith("image/") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg") ||
                        lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif") || lowerName.endsWith(".svg") -> DownloadFileType.IMAGE
                lowerMime.startsWith("video/") || lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv") ||
                        lowerName.endsWith(".webm") || lowerName.endsWith(".mov") || lowerName.endsWith(".avi") -> DownloadFileType.VIDEO
                lowerMime.startsWith("audio/") || lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") ||
                        lowerName.endsWith(".m4a") || lowerName.endsWith(".flac") || lowerName.endsWith(".ogg") -> DownloadFileType.AUDIO
                lowerName.endsWith(".zip") || lowerName.endsWith(".rar") || lowerName.endsWith(".7z") ||
                        lowerName.endsWith(".tar") || lowerName.endsWith(".gz") || lowerMime.contains("zip") || lowerMime.contains("compressed") -> DownloadFileType.ARCHIVE
                lowerName.endsWith(".doc") || lowerName.endsWith(".docx") || lowerName.endsWith(".xls") ||
                        lowerName.endsWith(".xlsx") || lowerName.endsWith(".ppt") || lowerName.endsWith(".pptx") ||
                        lowerName.endsWith(".txt") || lowerName.endsWith(".csv") -> DownloadFileType.DOCUMENT
                else -> DownloadFileType.GENERIC
            }
        }

    val formattedSize: String
        get() = formatDownloadSize(totalBytes)

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("fileName", fileName)
        put("url", url)
        put("mimeType", mimeType)
        put("filePath", filePath ?: "")
        put("totalBytes", totalBytes)
        put("downloadedBytes", downloadedBytes)
        put("status", status.name)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(json: JSONObject): DownloadItem {
            val filePathRaw = json.optString("filePath", "")
            return DownloadItem(
                id = json.optLong("id", System.currentTimeMillis()),
                fileName = json.optString("fileName", "arquivo"),
                url = json.optString("url", ""),
                mimeType = json.optString("mimeType", "*/*"),
                filePath = if (filePathRaw.isNotBlank()) filePathRaw else null,
                totalBytes = json.optLong("totalBytes", -1L),
                downloadedBytes = json.optLong("downloadedBytes", 0L),
                status = DownloadStatus.fromString(json.optString("status", "SUCCESSFUL")),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

fun formatDownloadSize(bytes: Long): String {
    if (bytes <= 0) return "--"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
        else -> "$bytes B"
    }
}

data class DownloadNotice(
    val id: Long,
    val fileName: String,
    val status: DownloadStatus,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

