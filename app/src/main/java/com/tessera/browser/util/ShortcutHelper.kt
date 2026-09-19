package com.tessera.browser.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.graphics.drawable.IconCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import com.tessera.browser.MainActivity
import com.tessera.browser.R

object ShortcutHelper {
    fun addPinShortcut(
        context: Context,
        url: String,
        title: String,
        favicon: Bitmap? = null
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(
                context,
                "Seu inicializador (Launcher) não permite fixar atalhos na tela inicial",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val cleanTitle = if (title.isNotBlank()) title.trim() else "Página Web"
        val shortcutIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val iconCompat = if (favicon != null && !favicon.isRecycled) {
            try {
                IconCompat.createWithBitmap(favicon)
            } catch (e: Exception) {
                IconCompat.createWithResource(context, R.mipmap.ic_launcher)
            }
        } else {
            IconCompat.createWithResource(context, R.mipmap.ic_launcher)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "tessera_shortcut_${url.hashCode()}")
            .setShortLabel(if (cleanTitle.length > 25) cleanTitle.take(22) + "..." else cleanTitle)
            .setLongLabel(cleanTitle)
            .setIcon(iconCompat)
            .setIntent(shortcutIntent)
            .build()

        val success = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        if (success) {
            Toast.makeText(
                context,
                "Atalho criado na Tela Inicial!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return success
    }
}
