package com.tessera.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.PageErrorInfo

@Composable
fun TesseraOfflineErrorView(
    errorInfo: PageErrorInfo,
    isDarkMode: Boolean = true,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDarkMode) Color(0xF5141110) else Color(0xF8FBF9F5)
    val cardBg = if (isDarkMode) Color(0xFF1E1B19) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E)
    val subTextColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF636366)

    val host = try {
        val uri = java.net.URI(errorInfo.url)
        (uri.host ?: errorInfo.url).replace("www.", "")
    } catch (e: Exception) {
        errorInfo.url.replace("https://", "").replace("http://", "").split("/").firstOrNull() ?: errorInfo.url
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(28.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.3f))
                .clip(shape)
                .background(cardBg)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.04f))
                    ),
                    shape = shape
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Icon Aura
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFF5252).copy(alpha = 0.25f),
                                Color(0xFFFF7A00).copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) Color(0xFF2E1A1A) else Color(0xFFFFEBEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (errorInfo.isOffline) Icons.Rounded.WifiOff else Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = if (errorInfo.isOffline) "Sem conexão com a internet" else "Não foi possível carregar a página",
                color = textColor,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Host chip
            if (host.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = host,
                        color = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Description
            Text(
                text = if (errorInfo.isOffline) {
                    "Verifique sua conexão Wi-Fi ou dados móveis e tente recarregar a página."
                } else {
                    errorInfo.description.ifBlank { "O servidor demorou muito para responder ou o endereço digitado pode estar incorreto." }
                },
                color = subTextColor,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ir para Início
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                        )
                        .clickable(onClick = onGoHome),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Início",
                            color = textColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Tentar Novamente
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF0288D1))
                            )
                        )
                        .clickable(onClick = onRetry),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Tentar Novamente",
                            color = Color.Black,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
