package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.DownloadNotice
import com.tessera.browser.data.DownloadStatus

@Composable
fun DownloadVisualBanner(
    notice: DownloadNotice?,
    isDarkMode: Boolean = true,
    onOpenFile: () -> Unit,
    onViewDownloads: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notice != null,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(280)) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(220)) + fadeOut(),
        modifier = modifier
    ) {
        if (notice == null) return@AnimatedVisibility

        val shape = RoundedCornerShape(22.dp)
        val bannerBg = if (isDarkMode) Color(0xF21C1A18) else Color(0xF8FFFFFF)
        val textColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E)
        val subTextColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF636366)

        val borderBrush = when (notice.status) {
            DownloadStatus.SUCCESSFUL -> Brush.linearGradient(
                listOf(Color(0xFF00E676).copy(alpha = 0.6f), Color(0xFF00B0FF).copy(alpha = 0.4f))
            )
            DownloadStatus.FAILED -> Brush.linearGradient(
                listOf(Color(0xFFFF5252).copy(alpha = 0.7f), Color(0xFFFF1744).copy(alpha = 0.4f))
            )
            else -> Brush.linearGradient(
                listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color(0xFF7C4DFF).copy(alpha = 0.4f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .shadow(elevation = 12.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(shape)
                .background(bannerBg)
                .border(width = 1.2.dp, brush = borderBrush, shape = shape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    when (notice.status) {
                        DownloadStatus.RUNNING, DownloadStatus.PENDING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = Color(0xFF00E5FF),
                                trackColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
                            )
                        }
                        DownloadStatus.SUCCESSFUL -> {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DownloadStatus.FAILED -> {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DownloadStatus.CANCELLED -> {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                tint = Color(0xFFFFA726),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = notice.fileName,
                            color = textColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = notice.message,
                            color = subTextColor,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notice.status == DownloadStatus.SUCCESSFUL) {
                        // "Abrir" action button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .clickable(onClick = onOpenFile)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FileOpen,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF69F0AE) else Color(0xFF00C853),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Abrir",
                                    color = if (isDarkMode) Color(0xFF69F0AE) else Color(0xFF00C853),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (notice.status == DownloadStatus.RUNNING) {
                        // "Ver" action button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)
                                )
                                .clickable(onClick = onViewDownloads)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Ver",
                                    color = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Dismiss Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar notificação",
                            tint = subTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
