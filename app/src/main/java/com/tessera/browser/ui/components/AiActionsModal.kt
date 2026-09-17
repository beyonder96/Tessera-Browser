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

        // Action 1: Resumo da Página
        AiActionCard(
            icon = Icons.AutoMirrored.Rounded.ShortText,
            title = "Resumir Página Atual",
            description = "Gera um resumo conciso com os pontos mais importantes do conteúdo.",
            onClick = { onAction("summarize") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Explicar Conteúdo
        AiActionCard(
            icon = Icons.Rounded.Lightbulb,
            title = "Explicar Conteúdo",
            description = "Explica o tema do site em linguagem simples e fácil de entender.",
            onClick = { onAction("explain") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 3: Chat Livre
        AiActionCard(
            icon = Icons.AutoMirrored.Rounded.Chat,
            title = "Abrir Chat com IA",
            description = "Faça qualquer pergunta diretamente no assistente gratuito.",
            onClick = { onAction("chat") }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AiActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), cardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF00E5FF).copy(alpha = 0.2f), Color(0xFF7C4DFF).copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
