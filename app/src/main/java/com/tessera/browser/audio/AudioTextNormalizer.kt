package com.tessera.browser.audio

object AudioTextNormalizer {

    fun normalizeForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        var text = rawText
            // 1. Remove tags HTML e URLs
            .replace(Regex("<[^>]*>"), " ")
            .replace(Regex("https?://\\S+"), " ")
            // 2. Remove marcações markdown
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            .replace(Regex("[*_~`#>]"), " ")
            .replace(Regex("^[-+*]\\s+", RegexOption.MULTILINE), " ")
            // 3. Converte moedas e unidades (padrão Tessera Bot)
            .replace(Regex("(?i)R\\$\\s*(\\d+(?:[.,]\\d+)?)")) { match ->
                val valor = match.groupValues[1].replace(".", "").replace(",", ".")
                val num = valor.toDoubleOrNull()
                if (num != null) {
                    val inteira = num.toInt()
                    val centavos = ((num - inteira) * 100).toInt()
                    if (centavos > 0) "$inteira reais e $centavos centavos" else "$inteira reais"
                } else "${match.groupValues[1]} reais"
            }
            .replace(Regex("(?i)\\$\\s*(\\d+(?:[.,]\\d+)?)"), "$1 dólares")
            .replace(Regex("(?i)€\\s*(\\d+(?:[.,]\\d+)?)"), "$1 euros")
            .replace(Regex("(?i)\\b(\\d+)\\s*km/h\\b"), "$1 quilômetros por hora")
            .replace(Regex("(?i)\\b(\\d+)\\s*kg\\b"), "$1 quilos")
            .replace(Regex("(?i)\\b(\\d+)\\s*ml\\b"), "$1 mililitros")
            .replace(Regex("(?i)\\b(\\d+)\\s*h\\s*(\\d+)\\s*min\\b"), "$1 horas e $2 minutos")
            .replace(Regex("(?i)\\b(\\d+)\\s*h\\b"), "$1 horas")
            .replace(Regex("(?i)\\b(\\d+)\\s*min\\b"), "$1 minutos")
            .replace(Regex("(?i)\\b(\\d+)\\s*seg\\b"), "$1 segundos")
            .replace(Regex("(?i)\\b(\\d+)%\\b"), "$1 por cento")
            // 4. Abreviações comuns
            .replace(Regex("(?i)\\bvs\\.?\\b"), "contra")
            .replace(Regex("(?i)\\bapê\\b"), "apartamento")
            .replace(Regex("(?i)\\bvc\\b"), "você")
            .replace(Regex("(?i)\\btbm\\b"), "também")
            .replace(Regex("(?i)\\bex\\.:?"), "por exemplo:")
            // 5. Remove caracteres isolados e pontuação excessiva
            .replace(Regex("[•●▪■◆★☆✦✧]"), " ")
            .replace(Regex("[\\|\\\\/_{}\\[\\]]"), " ")
            .replace(Regex("\\.{2,}"), ".")
            .replace(Regex("\\s+"), " ")
            .trim()

        return text
    }

    /**
     * Divide texto em sentenças ou blocos menores respeitando pontuação final,
     * ideal para geração de áudio neural ou TTS contínuo sem travamentos.
     */
    fun splitIntoSentences(text: String, maxCharsPerChunk: Int = 500): List<String> {
        val normalized = normalizeForSpeech(text)
        if (normalized.length <= maxCharsPerChunk) return listOf(normalized)

        val chunks = mutableListOf<String>()
        val regex = Regex("(?<=[.!?])\\s+")
        val sentences = normalized.split(regex)

        val current = StringBuilder()
        for (sentence in sentences) {
            if (current.length + sentence.length + 1 > maxCharsPerChunk && current.isNotEmpty()) {
                chunks.add(current.toString().trim())
                current.clear()
            }
            if (current.isNotEmpty()) current.append(" ")
            current.append(sentence)
        }
        if (current.isNotEmpty()) {
            chunks.add(current.toString().trim())
        }
        return chunks.filter { it.isNotBlank() }
    }
}
