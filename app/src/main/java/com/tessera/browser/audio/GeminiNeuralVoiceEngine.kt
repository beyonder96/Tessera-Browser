package com.tessera.browser.audio

import android.content.Context
import android.util.Base64
import android.util.Log
import com.tessera.browser.data.PodcastVoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

object GeminiNeuralVoiceEngine {
    private const val TAG = "GeminiNeuralVoice"

    /**
     * Gera áudio neural com Gemini 2.0 Flash / 2.5 Flash Preview TTS e salva em arquivo WAV temporário.
     * Retorna o File gerado ou null se falhar.
     */
    suspend fun generateSpeechAudioFile(
        context: Context,
        apiKey: String,
        text: String,
        voice: PodcastVoice = PodcastVoice.KORE
    ): File? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || text.isBlank()) return@withContext null

        val cleanText = AudioTextNormalizer.normalizeForSpeech(text).take(1200)
        if (cleanText.isBlank()) return@withContext null

        val voiceName = if (voice.isNeural) voice.id else "Kore"

        val modelsToTry = listOf("gemini-2.0-flash", "gemini-2.5-flash-preview-tts")

        for (model in modelsToTry) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val payload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", cleanText)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", voiceName)
                                })
                            })
                        })
                    })
                }

                val url = URL(endpoint)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 18000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("User-Agent", "TesseraBrowser/1.6.0")
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload.toString()) }

                if (conn.responseCode in 200..299) {
                    val respStr = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                    val root = JSONObject(respStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val inlineData = parts.getJSONObject(0).optJSONObject("inlineData")
                            val base64Data = inlineData?.optString("data", "")
                            if (!base64Data.isNullOrBlank()) {
                                val pcmBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                val wavBytes = pcmToWav(pcmBytes, sampleRate = 24000)
                                val tempFile = File.createTempFile("tessera_podcast_", ".wav", context.cacheDir)
                                FileOutputStream(tempFile).use { it.write(wavBytes) }
                                return@withContext tempFile
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Tentativa no modelo $model retornou HTTP ${conn.responseCode}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao gerar áudio com modelo $model", e)
            }
        }
        null
    }

    /**
     * Converte PCM bruto 16-bit 24kHz mono para container WAV padrão tocável pelo MediaPlayer do Android.
     */
    fun pcmToWav(
        pcmData: ByteArray,
        sampleRate: Int = 24000,
        numChannels: Short = 1,
        bitsPerSample: Short = 16
    ): ByteArray {
        val dataSize = pcmData.size
        val totalSize = 36 + dataSize
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = (numChannels * bitsPerSample / 8).toShort()

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        // "RIFF"
        header.put('R'.code.toByte()).put('I'.code.toByte()).put('F'.code.toByte()).put('F'.code.toByte())
        header.putInt(totalSize)
        // "WAVE"
        header.put('W'.code.toByte()).put('A'.code.toByte()).put('V'.code.toByte()).put('E'.code.toByte())
        // "fmt "
        header.put('f'.code.toByte()).put('m'.code.toByte()).put('t'.code.toByte()).put(' '.code.toByte())
        header.putInt(16) // Subchunk1Size
        header.putShort(1) // AudioFormat (1 = PCM)
        header.putShort(numChannels)
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign)
        header.putShort(bitsPerSample)
        // "data"
        header.put('d'.code.toByte()).put('a'.code.toByte()).put('t'.code.toByte()).put('a'.code.toByte())
        header.putInt(dataSize)

        val wavBytes = ByteArray(44 + dataSize)
        System.arraycopy(header.array(), 0, wavBytes, 0, 44)
        System.arraycopy(pcmData, 0, wavBytes, 44, dataSize)
        return wavBytes
    }
}
