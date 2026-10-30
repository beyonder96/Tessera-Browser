package com.tessera.browser.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.ui.theme.ApplePalette

@Immutable
enum class WebPermissionPromptType {
    CAMERA,
    MICROPHONE,
    CAMERA_AND_MICROPHONE,
    LOCATION,
    SSL_WARNING
}

@Immutable
data class WebPermissionPromptState(
    val type: WebPermissionPromptType,
    val origin: String,
    val title: String,
    val message: String,
    val confirmLabel: String = "Permitir",
    val denyLabel: String = "Bloquear",
    val isDanger: Boolean = false,
    val onAllow: () -> Unit,
    val onDeny: () -> Unit
)

@Composable
fun WebPermissionFloatingCard(
    prompt: WebPermissionPromptState,
    isDarkMode: Boolean,
    isIncognito: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardBg = when {
        isIncognito -> Color(0xFF171324)
        isDarkMode -> ApplePalette.DarkSurface
        else -> Color(0xFFFCFBF7)
    }
    val cardBorder = when {
        prompt.isDanger -> ApplePalette.Red.copy(alpha = 0.38f)
        isIncognito -> ApplePalette.Purple.copy(alpha = 0.30f)
        isDarkMode -> ApplePalette.DarkHairline
        else -> ApplePalette.LightHairline
    }
    val textPrimary = if (isDarkMode || isIncognito) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val textSecondary = if (isDarkMode || isIncognito) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val squircleBg = when {
        prompt.isDanger -> ApplePalette.Red.copy(alpha = 0.14f)
        isDarkMode || isIncognito -> ApplePalette.DarkElevated
        else -> Color(0xFFF1EDE2)
    }
    val accentColor = when {
        isIncognito -> ApplePalette.Purple
        isDarkMode -> ApplePalette.BlueDark
        else -> ApplePalette.Blue
    }

    val icon = when (prompt.type) {
        WebPermissionPromptType.CAMERA -> Icons.Rounded.Videocam
        WebPermissionPromptType.MICROPHONE -> Icons.Rounded.Mic
        WebPermissionPromptType.CAMERA_AND_MICROPHONE -> Icons.Rounded.Videocam
        WebPermissionPromptType.LOCATION -> Icons.Rounded.LocationOn
        WebPermissionPromptType.SSL_WARNING -> Icons.Rounded.WarningAmber
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp)
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(0.8.dp, cardBorder),
        shadowElevation = if (isDarkMode || isIncognito) 20.dp else 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(squircleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (prompt.isDanger) ApplePalette.Red else textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (prompt.origin.isNotBlank()) {
                        Text(
                            text = prompt.origin,
                            color = if (prompt.isDanger) ApplePalette.Red else accentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = prompt.title,
                        color = textPrimary,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = prompt.message,
                color = textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val secondaryBtnBg = if (isDarkMode || isIncognito) ApplePalette.DarkElevated else Color(0xFFF1EDE2)

                if (prompt.isDanger) {
                    // Em alertas SSL, a ação segura (Voltar / Cancelar) ganha destaque primário
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(secondaryBtnBg)
                            .border(0.6.dp, ApplePalette.Red.copy(alpha = 0.35f), RoundedCornerShape(13.dp))
                            .clickable { prompt.onAllow() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prompt.confirmLabel,
                            color = ApplePalette.Red,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(accentColor)
                            .clickable { prompt.onDeny() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prompt.denyLabel,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(secondaryBtnBg)
                            .clickable { prompt.onDeny() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prompt.denyLabel,
                            color = textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(accentColor)
                            .clickable { prompt.onAllow() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prompt.confirmLabel,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UndoCloseTabPill(
    closedTabTitle: String,
    isDarkMode: Boolean,
    isIncognito: Boolean = false,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBg = when {
        isIncognito -> Color(0xFF191426)
        isDarkMode -> ApplePalette.DarkSurface
        else -> Color(0xFFFCFBF7)
    }
    val pillBorder = when {
        isIncognito -> ApplePalette.Purple.copy(alpha = 0.30f)
        isDarkMode -> ApplePalette.DarkHairline
        else -> ApplePalette.LightHairline
    }
    val textPrimary = if (isDarkMode || isIncognito) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val textSecondary = if (isDarkMode || isIncognito) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val accentColor = when {
        isIncognito -> ApplePalette.Purple
        isDarkMode -> ApplePalette.BlueDark
        else -> ApplePalette.Blue
    }

    Surface(
        modifier = modifier
            .widthIn(max = 380.dp)
            .padding(horizontal = 16.dp),
        shape = CircleShape,
        color = pillBg,
        border = BorderStroke(0.8.dp, pillBorder),
        shadowElevation = if (isDarkMode || isIncognito) 16.dp else 9.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Replay,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(16.dp)
            )

            val label = if (closedTabTitle.isNotBlank() && closedTabTitle != "Nova Guia" && closedTabTitle != "Início") {
                "Guia fechada · $closedTabTitle"
            } else {
                "Guia fechada"
            }

            Text(
                text = label,
                color = textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f))
                    .clickable { onUndo() }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Desfazer",
                    color = accentColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dispensar",
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
