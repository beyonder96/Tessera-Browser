package com.tessera.browser.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast

/**
 * Utilitário para detecção e redirecionamento de links externos (Deep Links e App Links).
 * Trata URLs específicas de aplicativos populares (Instagram, WhatsApp, X/Twitter, Telegram,
 * YouTube, Spotify, Google Maps, Play Store, etc.), além de esquemas intent:// e protocolos customizados.
 */
object DeepLinkHelper {

    // Navegadores conhecidos que não devem ser selecionados como apps nativos especializados
    private val KNOWN_BROWSERS = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android",
        "com.google.android.apps.chrome",
        "com.ucmobile.intl",
        "com.kiwibrowser.browser",
        "com.vivaldi.browser"
    )

    // Domínios de aplicativos populares que possuem apps nativos dedicados
    private val POPULAR_APP_HOSTS = setOf(
        "instagram.com",
        "www.instagram.com",
        "instagr.am",
        "wa.me",
        "api.whatsapp.com",
        "chat.whatsapp.com",
        "twitter.com",
        "www.twitter.com",
        "x.com",
        "www.x.com",
        "t.me",
        "telegram.me",
        "open.spotify.com",
        "spotify.link",
        "maps.google.com",
        "maps.app.goo.gl",
        "play.google.com",
        "tiktok.com",
        "www.tiktok.com",
        "vm.tiktok.com",
        "reddit.com",
        "www.reddit.com",
        "linkedin.com",
        "www.linkedin.com",
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be"
    )

    /**
     * Intercepta e processa requisições de navegação do WebViewClient.
     * Retorna `true` se a URL foi direcionada para um aplicativo externo ou tratada,
     * ou `false` para que o WebView continue carregando a página normalmente.
     */
    fun handleUrlOverride(
        context: Context,
        view: WebView?,
        urlString: String,
        openLinksInExternalApps: Boolean
    ): Boolean {
        if (urlString.isBlank()) return false

        // 1. Esquema Android Intent (intent:// ou intent:)
        if (urlString.startsWith("intent://") || urlString.startsWith("intent:")) {
            return handleIntentScheme(context, view, urlString)
        }

        // 2. Esquemas Customizados não-HTTP (instagram://, whatsapp://, tg://, spotify://, tel:, mailto:, sms:, geo:, etc.)
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            return handleCustomScheme(context, view, urlString)
        }

        // 3. URLs HTTP / HTTPS: se o recurso de abrir em apps externos estiver ativado
        if (openLinksInExternalApps) {
            val uri = try { Uri.parse(urlString) } catch (e: Exception) { null } ?: return false
            val host = uri.host?.lowercase() ?: ""

            val isKnownApp = POPULAR_APP_HOSTS.any { host == it || host.endsWith(".$it") }
            if (isKnownApp || isTargetingExternalApp(context, uri)) {
                if (openUrlInExternalApp(context, uri)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Processa URLs formatadas no padrão intent:// da plataforma Android.
     */
    private fun handleIntentScheme(context: Context, view: WebView?, urlString: String): Boolean {
        return try {
            val intent = Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME) ?: return false
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val packageManager = context.packageManager
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

            // Se o app de destino está instalado e não é o próprio Tessera
            if (resolveInfo != null && resolveInfo.activityInfo.packageName != context.packageName) {
                context.startActivity(intent)
                return true
            }

            // Fallback para URL web se fornecido no intent
            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
            if (!fallbackUrl.isNullOrBlank()) {
                view?.loadUrl(fallbackUrl)
                return true
            }

            // Tentar abrir na Google Play Store se um pacote específico foi requisitado
            val targetPkg = intent.`package`
            if (!targetPkg.isNullOrBlank()) {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$targetPkg")).apply {
                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (marketIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(marketIntent)
                    return true
                }
            }

            true
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Trata esquemas de protocolo customizado (ex: instagram://, whatsapp://, tg://, etc.).
     */
    private fun handleCustomScheme(context: Context, view: WebView?, urlString: String): Boolean {
        return try {
            val uri = Uri.parse(urlString)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val packageManager = context.packageManager
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

            if (resolveInfo != null && resolveInfo.activityInfo.packageName != context.packageName) {
                context.startActivity(intent)
                true
            } else {
                // Tenta fallback para versão web correspondente se o app não estiver instalado
                val webFallback = getWebFallbackForCustomScheme(urlString)
                if (webFallback != null) {
                    view?.loadUrl(webFallback)
                } else {
                    Toast.makeText(context, "Aplicativo correspondente não instalado", Toast.LENGTH_SHORT).show()
                }
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Verifica se existe um aplicativo externo instalado registrado para manipular esta URI
     * (excluindo navegadores genéricos e o próprio Tessera).
     */
    fun isTargetingExternalApp(context: Context, uri: Uri): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfos.any { info ->
                val pkg = info.activityInfo.packageName
                pkg != context.packageName && !KNOWN_BROWSERS.contains(pkg)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Direciona a URI para o aplicativo externo nativo correspondente.
     */
    fun openUrlInExternalApp(context: Context, uri: Uri): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val packageManager = context.packageManager
            val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            val targetApp = resolveInfos.firstOrNull { info ->
                val pkg = info.activityInfo.packageName
                pkg != context.packageName && !KNOWN_BROWSERS.contains(pkg)
            }

            if (targetApp != null) {
                intent.setPackage(targetApp.activityInfo.packageName)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Abre uma URL string diretamente no app externo correspondente.
     */
    fun openUrlInExternalApp(context: Context, urlString: String): Boolean {
        val uri = try { Uri.parse(urlString) } catch (e: Exception) { null } ?: return false
        return openUrlInExternalApp(context, uri)
    }

    /**
     * Mapeia esquemas customizados conhecidos para URLs web caso o app nativo não esteja disponível.
     */
    private fun getWebFallbackForCustomScheme(uriString: String): String? {
        val lower = uriString.lowercase()
        return when {
            lower.startsWith("instagram://user?username=") -> {
                val username = uriString.substringAfter("username=").substringBefore("&")
                "https://www.instagram.com/$username"
            }
            lower.startsWith("whatsapp://send?phone=") -> {
                val phone = uriString.substringAfter("phone=").substringBefore("&")
                "https://wa.me/$phone"
            }
            lower.startsWith("tg://resolve?domain=") -> {
                val domain = uriString.substringAfter("domain=").substringBefore("&")
                "https://t.me/$domain"
            }
            lower.startsWith("twitter://user?screen_name=") -> {
                val screenName = uriString.substringAfter("screen_name=").substringBefore("&")
                "https://x.com/$screenName"
            }
            else -> null
        }
    }
}
