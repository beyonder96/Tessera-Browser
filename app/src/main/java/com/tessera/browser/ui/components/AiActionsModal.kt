package com.tessera.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ShortText
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AiActionsModal(
    pageUrl: String,
    onAction: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF8201816), Color(0xFC14100E))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
                ),
                shape = panelShape
            )
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Ações Rápidas de IA",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 1: Navegue por Mim (Arc Search Style)
        AiActionCard(
            icon = Icons.Rounded.AutoAwesome,
            title = "Navegue por Mim com IA",
            description = "Síntese visual estilo Arc Search com respostas diretas, tópicos e fontes.",
            badge = "Novo ✨",
            isFeatured = true,
            onClick = { onAction("browse_for_me") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Resumo da Página Estilo Arc
        AiActionCard(
            icon = Icons.Rounded.AutoAwesome,
            title = "Resumir Página Atual",
            description = "Síntese instantânea com IA gratuita, tópicos-chave e efeito visual do Arc.",
            badge = "Efeito Arc ✨",
            isFeatured = false,
            onClick = { onAction("summarize") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Explicar Conteúdo
        AiActionCard(
            icon = Icons.Rounded.Lightbulb,
            title = "Explicar Conteúdo",
            description = "Explica o tema do site em linguagem simples e didática.",
            onClick = { onAction("explain") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 3: Chat Livre
        AiActionCard(
            icon = Icons.AutoMirrored.Rounded.Chat,
            title = "Perguntas Livres com IA",
            description = "Faça qualquer pergunta diretamente ao assistente gratuito.",
            onClick = { onAction("chat") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 4: Clipar e Resumir no Caderno
        AiActionCard(
            icon = Icons.Rounded.EditNote,
            title = "Clipar & Resumir no Caderno",
            description = "Salva o artigo ou trecho no seu Caderno com síntese e tópicos automáticos.",
            badge = "Novo 📝",
            onClick = { onAction("clip_with_summary") }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AiActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    isFeatured: Boolean = false,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)

    val borderBrush = if (isFeatured) {
        Brush.linearGradient(
            listOf(
                Color(0xFF00E5FF),
                Color(0xFF7C4DFF),
                Color(0xFFFF4081)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
        )
    }

    val bgModifier = if (isFeatured) {
        Modifier.background(
            Brush.linearGradient(
                listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.08f),
                    Color(0xFF7C4DFF).copy(alpha = 0.12f)
                )
            )
        )
    } else {
        Modifier.background(Color.White.copy(alpha = 0.06f))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .then(bgModifier)
            .border(if (isFeatured) 1.5.dp else 1.dp, borderBrush, cardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isFeatured) {
                        Brush.linearGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF).copy(alpha = 0.2f), Color(0xFF7C4DFF).copy(alpha = 0.3f))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFeatured) Color.White else Color(0xFF00E5FF),
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
