# Tessera Browser ProGuard Rules

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Compose
-dontwarn androidx.compose.**
