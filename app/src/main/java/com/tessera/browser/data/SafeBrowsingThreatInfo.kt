package com.tessera.browser.data

data class SafeBrowsingThreatInfo(
    val url: String,
    val threatType: Int,
    val threatTitle: String,
    val threatDescription: String
) {
    companion object {
        const val THREAT_MALWARE = 1
        const val THREAT_PHISHING = 2
        const val THREAT_UNWANTED_SOFTWARE = 3
        const val THREAT_BILLING = 4

        fun create(url: String, threatType: Int): SafeBrowsingThreatInfo {
            val (title, description) = when (threatType) {
                THREAT_PHISHING -> "Aviso de Phishing / Falsificação" to
                        "O site que você está tentando acessar pode ser uma cópia fraudulenta de outra página concebida para induzir você a compartilhar senhas, cartões de crédito ou dados sensíveis."
                THREAT_MALWARE -> "Aviso de Malware" to
                        "O site à frente foi identificado como distribuidor de softwares maliciosos que podem danificar seu aparelho ou roubar suas informações."
                THREAT_UNWANTED_SOFTWARE -> "Software Indesejado Detectado" to
                        "Este site pode tentar instalar programas maliciosos que modificam as configurações do seu navegador sem seu consentimento."
                THREAT_BILLING -> "Risco de Cobrança Indesejada" to
                        "Esta página pode tentar realizar cobranças financeiras não autorizadas ou induzir a assinaturas enganosas de serviços móveis."
                else -> "Ameaça de Segurança Detectada" to
                        "O Google Safe Browsing identificou comportamentos inseguros ou perigosos nesta página web."
            }
            return SafeBrowsingThreatInfo(
                url = url,
                threatType = threatType,
                threatTitle = title,
                threatDescription = description
            )
        }
    }
}
