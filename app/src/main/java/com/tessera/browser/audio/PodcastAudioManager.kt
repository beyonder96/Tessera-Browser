package com.tessera.browser.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.content.ContextCompat
import com.tessera.browser.data.PodcastAudioState
import com.tessera.browser.data.PodcastVoice
import com.tessera.browser.service.TesseraAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

object PodcastAudioManager {
    private const val TAG = "PodcastAudioManager"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _audioState = MutableStateFlow(PodcastAudioState())
    val audioState: StateFlow<PodcastAudioState> = _audioState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    private var progressJob: Job? = null
    private var synthesisJob: Job? = null

    private var currentTextToPlay: String = ""
    private var currentTitle: String = ""
    private var currentSubtitle: String = ""
    private var currentAudioFile: File? = null
    private var geminiApiKeyProvider: (() -> String?)? = null

    fun initialize(context: Context, apiKeyProvider: () -> String?) {
        geminiApiKeyProvider = apiKeyProvider
        initTts(context)
    }

    private fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    try {
                        tts?.language = Locale("pt", "BR")
                    } catch (e: Exception) {
                        try { tts?.language = Locale.getDefault() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    fun playArticleOrContent(
        context: Context,
        title: String,
        subtitle: String,
        content: String,
        voice: PodcastVoice = _audioState.value.selectedVoice
    ) {
        val cleanTitle = title.ifBlank { "Artigo do Navegador" }
        currentTitle = cleanTitle
        currentSubtitle = subtitle
        currentTextToPlay = content

        stopCurrentPlayback(context, keepMiniPlayer = true)

        _audioState.update {
            it.copy(
                isPlaying = false,
                isBuffering = true,
                title = cleanTitle,
                subtitle = subtitle,
                currentPositionMs = 0L,
                durationMs = 0L,
                selectedVoice = voice,
                isMiniPlayerVisible = true,
                error = null
            )
        }

        startForegroundAudioService(context)

        synthesisJob?.cancel()
        synthesisJob = scope.launch(Dispatchers.IO) {
            val apiKey = geminiApiKeyProvider?.invoke()?.trim().orEmpty()

            // Tenta síntese neural com Gemini se voz neural selecionada e chave disponível
            var audioFile: File? = null
            if (voice.isNeural && apiKey.isNotBlank()) {
                audioFile = GeminiNeuralVoiceEngine.generateSpeechAudioFile(
                    context = context,
                    apiKey = apiKey,
                    text = content,
                    voice = voice
                )
            }

            scope.launch(Dispatchers.Main) {
                if (audioFile != null && audioFile.exists()) {
                    currentAudioFile = audioFile
                    playWavFile(context, audioFile)
                } else {
                    // Fallback para TTS Nativo (Garante 100% de funcionamento offline / sem chave)
                    playViaTts(context, content)
                }
            }
        }
    }

    private fun playWavFile(context: Context, file: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    val duration = mp.duration.toLong()
                    applySpeedToMediaPlayer(mp, _audioState.value.playbackSpeed)
                    mp.start()
                    _audioState.update {
                        it.copy(
                            isPlaying = true,
                            isBuffering = false,
                            durationMs = duration,
                            isNeuralVoiceActive = true,
                            error = null
                        )
                    }
                    startProgressTracker()
                    updateNotification(context)
                }
                setOnCompletionListener {
                    _audioState.update { it.copy(isPlaying = false, currentPositionMs = it.durationMs) }
                    progressJob?.cancel()
                    updateNotification(context)
                }
                setOnErrorListener { _, _, _ ->
                    Log.e(TAG, "Erro no MediaPlayer ao reproduzir WAV neural")
                    _audioState.update { it.copy(isPlaying = false, isBuffering = false) }
                    progressJob?.cancel()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao iniciar MediaPlayer com arquivo WAV", e)
            playViaTts(context, currentTextToPlay)
        }
    }

    private fun playViaTts(context: Context, text: String) {
        initTts(context)
        val normalized = AudioTextNormalizer.normalizeForSpeech(text)
        val wordCount = normalized.split("\\s+".toRegex()).size
        // Estima duração: média de 140 palavras por minuto
        val estimatedDurationMs = maxOf(5000L, ((wordCount / 140.0) * 60000L).toLong())

        _audioState.update {
            it.copy(
                isPlaying = true,
                isBuffering = false,
                durationMs = estimatedDurationMs,
                isNeuralVoiceActive = false,
                error = null
            )
        }

        try {
            tts?.setSpeechRate(_audioState.value.playbackSpeed)
            tts?.stop()

            val chunks = AudioTextNormalizer.splitIntoSentences(normalized, maxCharsPerChunk = 2500)
            chunks.forEachIndexed { index, chunk ->
                val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(chunk, mode, null, "podcast_chunk_$index")
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    val lastId = "podcast_chunk_${chunks.size - 1}"
                    if (utteranceId == lastId) {
                        scope.launch(Dispatchers.Main) {
                            _audioState.update { it.copy(isPlaying = false, currentPositionMs = it.durationMs) }
                            progressJob?.cancel()
                            updateNotification(context)
                        }
                    }
                }
                override fun onError(utteranceId: String?) {
                    scope.launch(Dispatchers.Main) {
                        _audioState.update { it.copy(isPlaying = false, isBuffering = false) }
                        progressJob?.cancel()
                        updateNotification(context)
                    }
                }
            })

            startProgressTrackerForTts(estimatedDurationMs)
            updateNotification(context)
        } catch (e: Exception) {
            Log.e(TAG, "Erro no TTS local", e)
            _audioState.update { it.copy(isPlaying = false, isBuffering = false, error = "Falha na reprodução de áudio") }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _audioState.update { it.copy(currentPositionMs = current) }
                delay(250)
            }
        }
    }

    private fun startProgressTrackerForTts(totalDurationMs: Long) {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            val startTime = System.currentTimeMillis()
            while (isActive && _audioState.value.isPlaying) {
                val elapsed = ((System.currentTimeMillis() - startTime) * _audioState.value.playbackSpeed).toLong()
                _audioState.update { it.copy(currentPositionMs = minOf(elapsed, totalDurationMs)) }
                if (elapsed >= totalDurationMs) break
                delay(300)
            }
        }
    }

    fun togglePlayPause(context: Context) {
        if (_audioState.value.isPlaying) {
            pause(context)
        } else {
            resume(context)
        }
    }

    fun pause(context: Context) {
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            } else if (tts != null) {
                tts?.stop()
            }
            progressJob?.cancel()
            _audioState.update { it.copy(isPlaying = false) }
            updateNotification(context)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao pausar", e)
        }
    }

    fun resume(context: Context) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                _audioState.update { it.copy(isPlaying = true) }
                startProgressTracker()
                updateNotification(context)
            } else if (currentTextToPlay.isNotBlank()) {
                playViaTts(context, currentTextToPlay)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao retomar reprodução", e)
        }
    }

    fun seekBy(context: Context, deltaMs: Long) {
        if (mediaPlayer != null) {
            try {
                val current = mediaPlayer?.currentPosition ?: 0
                val target = (current + deltaMs).coerceIn(0, mediaPlayer?.duration?.toLong() ?: 0L).toInt()
                mediaPlayer?.seekTo(target)
                _audioState.update { it.copy(currentPositionMs = target.toLong()) }
                updateNotification(context)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar posição no MediaPlayer", e)
            }
        } else {
            // Em TTS faz avanço proporcional de tempo
            _audioState.update {
                val next = (it.currentPositionMs + deltaMs).coerceIn(0L, it.durationMs)
                it.copy(currentPositionMs = next)
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        if (mediaPlayer != null) {
            try {
                val target = positionMs.coerceIn(0L, mediaPlayer?.duration?.toLong() ?: 0L).toInt()
                mediaPlayer?.seekTo(target)
                _audioState.update { it.copy(currentPositionMs = target.toLong()) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro no seekToPosition", e)
            }
        }
    }

    fun setSpeed(speed: Float) {
        _audioState.update { it.copy(playbackSpeed = speed) }
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                applySpeedToMediaPlayer(mediaPlayer!!, speed)
            }
            tts?.setSpeechRate(speed)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar velocidade", e)
        }
    }

    private fun applySpeedToMediaPlayer(mp: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = mp.playbackParams ?: PlaybackParams()
                params.speed = speed
                mp.playbackParams = params
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível definir PlaybackParams no MediaPlayer", e)
            }
        }
    }

    fun selectVoice(context: Context, voice: PodcastVoice) {
        _audioState.update { it.copy(selectedVoice = voice) }
        if (currentTextToPlay.isNotBlank()) {
            playArticleOrContent(context, currentTitle, currentSubtitle, currentTextToPlay, voice)
        }
    }

    fun dismissPlayer(context: Context) {
        stopCurrentPlayback(context, keepMiniPlayer = false)
        stopForegroundAudioService(context)
    }

    fun setFullPlayerOpen(isOpen: Boolean) {
        _audioState.update { it.copy(isFullPlayerOpen = isOpen) }
    }

    private fun stopCurrentPlayback(context: Context, keepMiniPlayer: Boolean) {
        progressJob?.cancel()
        synthesisJob?.cancel()
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar reprodução", e)
        }

        try {
            currentAudioFile?.delete()
            currentAudioFile = null
        } catch (_: Exception) {}

        _audioState.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                isMiniPlayerVisible = keepMiniPlayer,
                isFullPlayerOpen = if (!keepMiniPlayer) false else it.isFullPlayerOpen
            )
        }
    }

    private fun startForegroundAudioService(context: Context) {
        try {
            val intent = Intent(context, TesseraAudioService::class.java).apply {
                action = TesseraAudioService.ACTION_START
                putExtra(TesseraAudioService.EXTRA_TITLE, currentTitle)
                putExtra(TesseraAudioService.EXTRA_SUBTITLE, currentSubtitle)
            }
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar TesseraAudioService em primeiro plano", e)
        }
    }

    private fun updateNotification(context: Context) {
        try {
            val intent = Intent(context, TesseraAudioService::class.java).apply {
                action = TesseraAudioService.ACTION_UPDATE_STATUS
                putExtra(TesseraAudioService.EXTRA_IS_PLAYING, _audioState.value.isPlaying)
                putExtra(TesseraAudioService.EXTRA_TITLE, _audioState.value.title)
                putExtra(TesseraAudioService.EXTRA_SUBTITLE, _audioState.value.subtitle)
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar notificação", e)
        }
    }

    private fun stopForegroundAudioService(context: Context) {
        try {
            val intent = Intent(context, TesseraAudioService::class.java).apply {
                action = TesseraAudioService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar serviço de áudio", e)
        }
    }
}
