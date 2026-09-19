package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.TranslationState

@Composable
fun TranslateBar(
    state: TranslationState,
    isDarkMode: Boolean,
    accentColor: Color = Color(0xFF0288D1),
    onTranslate: () -> Unit,
    onRevert: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isBannerVisible,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        val barShape = RoundedCornerShape(26.dp)
        val bgBrush = if (isDarkMode) {
            Brush.verticalGradient(
                listOf(Color(0xE62A2420), Color(0xF21F1B18))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0xF5FFFFFF), Color(0xFAF4F6F8))
            )
        }

        val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E4E8)
        val textColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
        val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF757575)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = barShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(barShape)
                .background(bgBrush)
                .border(1.dp, borderColor, barShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ícone do Google Tradutor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
                            contentDescription = "Tradutor",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (state.isTranslating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Traduzindo...",
                                color = mutedColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Pílulas de Alternância de Idioma: [ Original ] | [ Português ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Botão Idioma Original
                    val isOriginalSelected = !state.isTranslated && !state.isTranslating
                    val originalShape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .clip(originalShape)
                            .background(
                                if (isOriginalSelected) {
                                    if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E4E8)
                                } else Color.Transparent
                            )
                            .clickable(enabled = !isOriginalSelected && !state.isTranslating) {
                                onRevert()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.detectedLanguageName,
                            color = if (isOriginalSelected) textColor else mutedColor,
                            fontSize = 12.5.sp,
                            fontWeight = if (isOriginalSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Botão Idioma Alvo (Português)
                    val isTargetSelected = state.isTranslated
                    val targetShape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .clip(targetShape)
                            .background(
                                if (isTargetSelected) {
                                    accentColor
                                } else if (state.isTranslating) {
                                    accentColor.copy(alpha = 0.3f)
                                } else {
                                    if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFFECEFF1)
                                }
                            )
                            .clickable(enabled = !isTargetSelected && !state.isTranslating) {
                                onTranslate()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.targetLanguageName,
                            color = if (isTargetSelected) Color.White else if (state.isTranslating) accentColor else textColor,
                            fontSize = 12.5.sp,
                            fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }

                // Botão Fechar ✕
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar barra de tradução",
                        tint = mutedColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
