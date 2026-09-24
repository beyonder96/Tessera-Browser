package com.tessera.browser.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tessera.browser.MainActivity
import com.tessera.browser.audio.PodcastAudioManager

class TesseraAudioService : Service() {

    companion object {
        const val CHANNEL_ID = "tessera_audio_playback"
        const val NOTIFICATION_ID = 4001

        const val ACTION_START = "com.tessera.browser.audio.START"
        const val ACTION_UPDATE_STATUS = "com.tessera.browser.audio.UPDATE_STATUS"
        const val ACTION_PLAY_PAUSE = "com.tessera.browser.audio.PLAY_PAUSE"
        const val ACTION_SKIP_FORWARD = "com.tessera.browser.audio.SKIP_FORWARD"
        const val ACTION_SKIP_BACKWARD = "com.tessera.browser.audio.SKIP_BACKWARD"
        const val ACTION_STOP = "com.tessera.browser.audio.STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
    }

    private var currentTitle: String = "Podcastify — Tessera Browser"
    private var currentSubtitle: String = "Reproduzindo áudio..."
    private var isPlaying: Boolean = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: currentTitle
                currentSubtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: currentSubtitle
                isPlaying = true
                val notification = buildNotification(isPlaying)
                startForegroundWithServiceType(notification)
            }
            ACTION_UPDATE_STATUS -> {
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying)
                currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: currentTitle
                currentSubtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: currentSubtitle
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, buildNotification(isPlaying))
            }
            ACTION_PLAY_PAUSE -> {
                PodcastAudioManager.togglePlayPause(this)
            }
            ACTION_SKIP_FORWARD -> {
                PodcastAudioManager.seekBy(this, 15000L)
            }
            ACTION_SKIP_BACKWARD -> {
                PodcastAudioManager.seekBy(this, -15000L)
            }
            ACTION_STOP -> {
                PodcastAudioManager.dismissPlayer(this)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundWithServiceType(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(playing: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, TesseraAudioService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipForwardIntent = Intent(this, TesseraAudioService::class.java).apply {
            action = ACTION_SKIP_FORWARD
        }
        val skipForwardPendingIntent = PendingIntent.getService(
            this,
            2,
            skipForwardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipBackwardIntent = Intent(this, TesseraAudioService::class.java).apply {
            action = ACTION_SKIP_BACKWARD
        }
        val skipBackwardPendingIntent = PendingIntent.getService(
            this,
            3,
            skipBackwardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TesseraAudioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseText = if (playing) "Pausar" else "Reproduzir"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText(currentSubtitle)
            .setSubText("Podcastify • Tessera")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(playing)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_rew, "-15s", skipBackwardPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_ff, "+15s", skipForwardPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Parar", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Podcastify & Áudio em Segundo Plano",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reprodução de voz e podcast do Tessera Browser"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
