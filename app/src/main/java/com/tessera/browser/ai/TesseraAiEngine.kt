package com.tessera.browser.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Provedor de IA configurável pelo usuário no Tessera Browser.
 */
enum class AiProvider(val displayName: String, val modelName: String, val keyName: String) {
    GROQ("Groq Cloud (Llama 3.3)", "llama-3.3-70b-versatile", "groq"),
    GEMINI("Google Gemini (Flash 2.0)", "gemini-2.0-flash", "gemini");

    companion object {
        fun fromKey(key: String?): AiProvider {
            return entries.firstOrNull { it.keyName.equals(key, ignoreCase = true) } ?: GEMINI
        }
    }
}

/**
 * Mensagem do chat de perguntas e respostas contextualizado com a página.
 */
data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" ou "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Estado completo do assistente de IA.
 */
data class TesseraAiState(
    val isVisible: Boolean = false,
    val isSummarizing: Boolean = false,
    val isAnsweringQuestion: Boolean = false,
    val pageTitle: String = "",
    val pageDomain: String = "",
    val pageUrl: String = "",
    val pageContent: String = "",
    val summary: String? = null,
    val chatMessages: List<AiChatMessage> = emptyList(),
    val error: String? = null,
    val activeProvider: AiProvider = AiProvider.GEMINI
)

/**
 * Motor central de Inteligência Artificial do Tessera Browser.
 * Alimentado exclusivamente via chaves oficiais de API do Groq ou Gemini fornecidas pelo usuário.
 */
object TesseraAiEngine {

    private const val TAG = "TesseraAiEngine"

    /**
     * Gera um resumo estruturado, conciso e elegante da página acessada.
     */
    suspend fun generateSummary(
        provider: AiProvider,
        apiKey: String,
        pageTitle: String,
        pageDomain: String,
        pageContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Nenhuma chave de API configurada para ${provider.displayName}.")
            )
        }

        val trimmedContent = if (pageContent.length > 12000) {
            pageContent.take(12000) + "\n...[conteúdo resumido para análise]"
        } else {
            pageContent
        }

        val systemPrompt = """
            Você é o assistente inteligente minimalista do navegador Tessera.
            Sua missão é resumir com máxima clareza, precisão e elegância o conteúdo da página que o usuário está visualizando.
            
            Diretrizes:
            - Escreva em Português do Brasil de forma direta e sem jargões desnecessários.
            - Estruture com:
              1. **Visão Geral**: 1 a 2 parágrafos sintetizando a essência e o propósito do texto.
              2. **Pontos-Chave**: 3 a 5 tópicos objetivos com os principais fatos ou aprendizados (use '- ' para cada ponto).
              3. **Conclusão**: 1 parágrafo curto sobre o impacto, conclusão ou recomendação.
            - Seja conciso e evite prolixidade.
        """.trimIndent()

        val userPrompt = """
            Página: $pageTitle
            Origem: $pageDomain
            
            Conteúdo da página:
            $trimmedContent
        """.trimIndent()

        try {
            val responseText = when (provider) {
                AiProvider.GROQ -> executeGroqRequest(
                    apiKey = apiKey,
                    systemPrompt = systemPrompt,
                    messages = listOf(AiChatMessage(role = "user", content = userPrompt))
                )
                AiProvider.GEMINI -> executeGeminiRequest(
                    apiKey = apiKey,
                    systemPrompt = systemPrompt,
                    messages = listOf(AiChatMessage(role = "user", content = userPrompt))
                )
            }
            Result.success(responseText.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resumo com $provider", e)
            Result.failure(e)
        }
    }

    /**
     * Responde dúvidas e perguntas do usuário tendo como base o conteúdo da página acessada.
     */
    suspend fun askQuestion(
        provider: AiProvider,
        apiKey: String,
        pageTitle: String,
        pageDomain: String,
        pageContent: String,
        conversationHistory: List<AiChatMessage>,
        userQuestion: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Nenhuma chave de API configurada para ${provider.displayName}.")
            )
        }

        val trimmedContent = if (pageContent.length > 12000) {
            pageContent.take(12000) + "\n...[conteúdo contextualizado]"
        } else {
            pageContent
        }

        val systemPrompt = """
            Você é o assistente inteligente minimalista do navegador Tessera.
            O usuário está navegando na página "$pageTitle" ($pageDomain) e tem dúvidas específicas sobre ela.
            
            Conteúdo de referência da página:
            $trimmedContent
            
            Diretrizes:
            - Responda à dúvida do usuário de maneira clara, objetiva e útil em Português do Brasil.
            - Priorize as informações contidas no texto da página. Se a resposta não estiver no texto, esclareça isso e responda com base no seu conhecimento geral com sinceridade.
            - Seja amigável, direto ao ponto e não utilize introduções repetitivas.
        """.trimIndent()

        val fullHistory = conversationHistory + AiChatMessage(role = "user", content = userQuestion)

        try {
            val responseText = when (provider) {
                AiProvider.GROQ -> executeGroqRequest(
                    apiKey = apiKey,
                    systemPrompt = systemPrompt,
                    messages = fullHistory
                )
                AiProvider.GEMINI -> executeGeminiRequest(
                    apiKey = apiKey,
                    systemPrompt = systemPrompt,
                    messages = fullHistory
                )
            }
            Result.success(responseText.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao responder pergunta com $provider", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // GROQ API CLIENT (OpenAI Compatible)
    // ==========================================
    private fun executeGroqRequest(
        apiKey: String,
        systemPrompt: String,
        messages: List<AiChatMessage>
    ): String {
        val messagesJsonArray = JSONArray()

        // System prompt
        messagesJsonArray.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })

        // Conversation messages
        for (msg in messages) {
            messagesJsonArray.put(JSONObject().apply {
                put("role", if (msg.role == "assistant" || msg.role == "model") "assistant" else "user")
                put("content", msg.content)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", AiProvider.GROQ.modelName)
            put("messages", messagesJsonArray)
            put("temperature", 0.4)
            put("max_tokens", 1024)
        }

        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12000
            readTimeout = 25000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            setRequestProperty("User-Agent", "TesseraBrowser/2.0")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(requestBody.toString()) }

        val responseCode = conn.responseCode
        if (responseCode in 200..299) {
            val responseString = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val root = JSONObject(responseString)
            val choices = root.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val messageObj = firstChoice.optJSONObject("message")
                val text = messageObj?.optString("content")
                if (!text.isNullOrBlank()) {
                    return text
                }
            }
            throw IllegalStateException("Resposta vazia da API do Groq.")
        } else {
            val errorText = try {
                BufferedReader(InputStreamReader(conn.errorStream, "UTF-8")).use { it.readText() }
            } catch (e: Exception) {
                "Código HTTP $responseCode"
            }
            when (responseCode) {
                401, 403 -> throw IllegalArgumentException("Chave Groq inválida ou expirada. Verifique no console.groq.com.")
                429 -> throw IllegalStateException("Limite de requisições do Groq atingido. Aguarde alguns segundos.")
                else -> throw IllegalStateException("Erro no Groq ($responseCode): $errorText")
            }
        }
    }

    // ==========================================
    // GOOGLE GEMINI API CLIENT
    // ==========================================
    private fun executeGeminiRequest(
        apiKey: String,
        systemPrompt: String,
        messages: List<AiChatMessage>
    ): String {
        val contentsArray = JSONArray()

        // First message includes system prompt context
        var isFirst = true
        for (msg in messages) {
            val textContent = if (isFirst && msg.role == "user") {
                isFirst = false
                "$systemPrompt\n\n${msg.content}"
            } else {
                msg.content
            }

            contentsArray.put(JSONObject().apply {
                put("role", if (msg.role == "assistant" || msg.role == "model") "model" else "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", textContent)
                    })
                })
            })
        }

        // If only system prompt exists or no user messages
        if (contentsArray.length() == 0) {
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
        }

        val requestBody = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 1024)
            })
        }

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${AiProvider.GEMINI.modelName}:generateContent?key=${apiKey.trim()}"
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12000
            readTimeout = 25000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("User-Agent", "TesseraBrowser/2.0")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(requestBody.toString()) }

        val responseCode = conn.responseCode
        if (responseCode in 200..299) {
            val responseString = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    if (!text.isNullOrBlank()) {
                        return text
                    }
                }
            }
            throw IllegalStateException("Resposta vazia da API do Gemini.")
        } else {
            val errorText = try {
                BufferedReader(InputStreamReader(conn.errorStream, "UTF-8")).use { it.readText() }
            } catch (e: Exception) {
                "Código HTTP $responseCode"
            }
            when (responseCode) {
                400, 401, 403 -> throw IllegalArgumentException("Chave Gemini inválida ou sem permissão. Verifique em aistudio.google.com.")
                429 -> throw IllegalStateException("Cota de requisições do Gemini atingida. Tente novamente em instantes.")
                else -> throw IllegalStateException("Erro no Gemini ($responseCode): $errorText")
            }
        }
    }
}
