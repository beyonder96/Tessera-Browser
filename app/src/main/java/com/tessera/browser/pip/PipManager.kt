package com.tessera.browser.pip

import android.app.Activity
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.util.Rational

/**
 * Gerenciador central de Picture-in-Picture (PiP) e Sobreposição ("Aparecer sobre outros apps").
 */
object PipManager {

    private const val TAG = "PipManager"

    const val VIDEO_DETECTION_SCRIPT = """
        (function() {
            if (window.__tesseraVideoObserverInstalled) return;
            window.__tesseraVideoObserverInstalled = true;

            function checkVideoState() {
                try {
                    var videos = document.getElementsByTagName('video');
                    var isAnyPlaying = false;
                    var vWidth = 16;
                    var vHeight = 9;
                    for (var i = 0; i < videos.length; i++) {
                        var v = videos[i];
                        if (!v.paused && !v.ended && v.readyState > 1) {
                            isAnyPlaying = true;
                            if (v.videoWidth > 0 && v.videoHeight > 0) {
                                vWidth = v.videoWidth;
                                vHeight = v.videoHeight;
                            }
                            break;
                        }
                    }
                    var bridge = window.TesseraBridge || window.TesseraNativeBridge;
                    if (bridge && typeof bridge.onVideoPlaybackStateChanged === 'function') {
                        bridge.onVideoPlaybackStateChanged(isAnyPlaying, vWidth, vHeight);
                    }
                } catch (e) {}
            }

            document.addEventListener('play', checkVideoState, true);
            document.addEventListener('pause', checkVideoState, true);
            document.addEventListener('ended', checkVideoState, true);
            document.addEventListener('loadeddata', checkVideoState, true);
            setInterval(checkVideoState, 2500);
        })();
    """

    /**
     * Verifica se o dispositivo e a versão do Android suportam Picture-in-Picture.
     */
    fun isPipSupported(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    /**
     * Verifica se a permissão especial de Picture-in-Picture está habilitada no sistema.
     */
    fun isPipPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        return try {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao verificar permissão de PiP", e)
            false
        }
    }

    /**
     * Verifica se a permissão de "Aparecer sobre outros apps" (SYSTEM_ALERT_WINDOW / Overlay) está concedida.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Settings.canDrawOverlays(context)
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao verificar Settings.canDrawOverlays", e)
                false
            }
        } else {
            true
        }
    }

    /**
     * Verifica se o app possui todas as permissões necessárias para o modo janela flutuante / PiP.
     */
    fun hasOverlayOrPipPermission(context: Context): Boolean {
        if (!isPipSupported(context)) return false
        // Em muitos aparelhos (Samsung, Xiaomi, Motorola), tanto a permissão de PiP
        // quanto a de sobreposição ("Aparecer sobre outros apps") são consultadas.
        return isPipPermissionGranted(context) || isOverlayPermissionGranted(context)
    }

    /**
     * Abre a tela de configurações do sistema para o usuário ativar a permissão de PiP ou Sobreposição.
     */
    fun openPipOrOverlaySettings(context: Context) {
        // Tenta primeiro abrir a tela dedicada de PiP do app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.parse("package:${context.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível abrir PICTURE_IN_PICTURE_SETTINGS diretamente", e)
            }
        }

        // Fallback para tela de "Aparecer sobre outros aplicativos" (Overlay)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível abrir ACTION_MANAGE_OVERLAY_PERMISSION", e)
            }
        }

        // Fallback final: Detalhes gerais do aplicativo
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível abrir detalhes do app nas configurações", e)
        }
    }

    /**
     * Constrói os parâmetros de PictureInPictureParams seguros com proporção de tela válida.
     */
    fun buildPipParams(width: Int = 16, height: Int = 9, autoEnter: Boolean = true): PictureInPictureParams? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return try {
            val safeRational = calculateSafeRational(width, height)
            val builder = PictureInPictureParams.Builder()
                .setAspectRatio(safeRational)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(autoEnter)
                builder.setSeamlessResizeEnabled(true)
            }
            builder.build()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao construir PictureInPictureParams", e)
            null
        }
    }

    /**
     * Tenta entrar no modo Picture-in-Picture programaticamente.
     * Retorna true se a transição foi disparada com sucesso.
     */
    fun enterPip(activity: Activity, width: Int = 16, height: Int = 9): Boolean {
        if (!isPipSupported(activity)) return false
        if (!hasOverlayOrPipPermission(activity)) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = buildPipParams(width, height, autoEnter = false)
            return try {
                if (params != null) {
                    activity.enterPictureInPictureMode(params)
                } else {
                    @Suppress("DEPRECATION")
                    activity.enterPictureInPictureMode()
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao disparar enterPictureInPictureMode", e)
                false
            }
        }
        return false
    }

    /**
     * Garante que o Rational esteja dentro dos limites estritos do Android:
     * entre 1:2.39 (0.418) e 2.39:1 (2.39).
     */
    fun calculateSafeRational(width: Int, height: Int): Rational {
        if (width <= 0 || height <= 0) return Rational(16, 9)
        val ratio = width.toFloat() / height.toFloat()
        return when {
            ratio > 2.38f -> Rational(238, 100)
            ratio < 0.42f -> Rational(42, 100)
            else -> Rational(width, height)
        }
    }
}
