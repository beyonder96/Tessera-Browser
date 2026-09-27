package com.tessera.browser.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object DefaultBrowserHelper {

    fun isDefaultBrowser(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                roleManager?.isRoleHeld(RoleManager.ROLE_BROWSER) ?: false
            } else {
                val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                val resolveInfo = context.packageManager.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY)
                resolveInfo?.activityInfo?.packageName == context.packageName
            }
        } catch (e: Exception) {
            false
        }
    }

    fun createRequestDefaultBrowserIntent(context: Context): Intent? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                } else {
                    Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                }
            } else {
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            }
        } catch (e: Exception) {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        }
    }

    fun requestDefaultBrowser(context: Context) {
        try {
            val intent = createRequestDefaultBrowserIntent(context) ?: Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                if (context !is Activity) {
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Não foi possível abrir as configurações de aplicativos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
