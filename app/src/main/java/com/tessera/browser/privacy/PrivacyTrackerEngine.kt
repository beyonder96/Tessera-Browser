package com.tessera.browser.privacy

import com.tessera.browser.data.BlockedTrackerItem
import com.tessera.browser.data.TrackerCategory

object PrivacyTrackerEngine {

    const val BYTES_PER_BLOCKED_REQUEST: Long = 46000L // Média ponderada ~45KB por script/banner bloqueado
    const val TIME_SAVED_PER_BLOCKED_MS: Long = 85L    // Média de latência de rede/parse economizada

    val KnownAdBlockHosts = setOf(
        "doubleclick.net", "googleadservices.com", "googlesyndication.com",
        "pagead2.googlesyndication.com", "adservice.google.com", "admob.com",
        "taboola.com", "outbrain.com", "popads.net", "adnxs.com", "criteo.com",
        "amazon-adsystem.com", "scorecardresearch.com", "quantserve.com",
        "zedo.com", "advertising.com", "rubiconproject.com", "pubmatic.com",
        "googletagservices.com", "adcolony.com", "appsflyer.com", "branch.io",
        "chartbeat.com", "smartadserver.com", "casalemedia.com", "criteo.net",
        "yieldmo.com", "adroll.com", "inmobi.com", "unityads.unity3d.com",
        "google-analytics.com", "googletagmanager.com", "facebook.net",
        "connect.facebook.net", "pixel.facebook.com", "hotjar.com", "clarity.ms",
        "optimizely.com", "mixpanel.com", "segment.io", "amplitude.com",
        "moatads.com", "iasds.com", "openx.net", "adform.net"
    )

    fun isAdOrTracker(host: String, url: String): Boolean {
        val h = host.lowercase()
        val u = url.lowercase()

        return KnownAdBlockHosts.any { h.endsWith(it) || h == it } ||
                u.contains("/pagead/") ||
                u.contains("/adservice/") ||
                u.contains("/ads/") ||
                u.contains("/pixel?") ||
                u.contains("/collect?") ||
                u.contains("/analytics.js") ||
                u.contains("/gtm.js") ||
                u.contains("/fbevents.js")
    }

    fun identifyTracker(host: String, url: String): Pair<String, TrackerCategory> {
        val h = host.lowercase()
        val u = url.lowercase()

        return when {
            h.contains("doubleclick") || h.contains("googlead") || h.contains("googlesyndication") || h.contains("admob") ->
                Pair("Google Ads & DoubleClick", TrackerCategory.ADVERTISING)

            h.contains("google-analytics") || h.contains("googletagmanager") || h.contains("googletagservices") ->
                Pair("Google Analytics & GTM", TrackerCategory.ANALYTICS)

            h.contains("facebook") || u.contains("fbevents.js") ->
                Pair("Meta Pixel & Facebook Tracker", TrackerCategory.SOCIAL)

            h.contains("criteo") ->
                Pair("Criteo Retargeting", TrackerCategory.ADVERTISING)

            h.contains("taboola") || h.contains("outbrain") ->
                Pair("Taboola / Outbrain Native Ads", TrackerCategory.ADVERTISING)

            h.contains("amazon-adsystem") ->
                Pair("Amazon Advertising", TrackerCategory.ADVERTISING)

            h.contains("appsflyer") || h.contains("branch.io") ->
                Pair("AppsFlyer & Branch Telemetria", TrackerCategory.FINGERPRINTING)

            h.contains("chartbeat") || h.contains("scorecardresearch") || h.contains("quantserve") ->
                Pair("Audience Analytics", TrackerCategory.ANALYTICS)

            h.contains("hotjar") || h.contains("clarity.ms") ->
                Pair("Heatmap & Session Recording", TrackerCategory.FINGERPRINTING)

            h.contains("mixpanel") || h.contains("segment") || h.contains("amplitude") ->
                Pair("Product Analytics Tracker", TrackerCategory.ANALYTICS)

            h.contains("rubicon") || h.contains("pubmatic") || h.contains("adnxs") || h.contains("openx") ->
                Pair("Ad Exchange RTB Network", TrackerCategory.ADVERTISING)

            h.contains("popads") || h.contains("zedo") || h.contains("adcolony") || h.contains("inmobi") ->
                Pair("Mobile Ad Network", TrackerCategory.ADVERTISING)

            else -> {
                val cleanHost = h.replace("www.", "").substringBefore(":")
                Pair(cleanHost.ifBlank { "Rastreador Web" }, TrackerCategory.ADVERTISING)
            }
        }
    }
}
