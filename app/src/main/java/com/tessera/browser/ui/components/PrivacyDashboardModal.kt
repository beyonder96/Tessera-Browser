package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.BlockedTrackerItem
import com.tessera.browser.data.PrivacyDashboardState

@Composable
fun PrivacyDashboardModal(
    state: PrivacyDashboardState,
    adBlockEnabled: Boolean,
    cookieBlockerEnabled: Boolean,
    isDarkMode: Boolean,
    accentColor: Color,
    onToggleAdBlock: (Boolean) -> Unit,
    onToggleCookieBlocker: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val haptic = LocalHapticFeedback.current
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    val bgColor = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xF81C1614), Color(0xFC120E0D), Color(0xFF090706))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF9FAFB), Color(0xFFF3F4F6))
        )
    }

    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E24)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.60f) else Color(0xFF6B7280)
    val shieldGreen = Color(0xFF00E676)

    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(320)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(260))
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(panelShape)
                .background(bgColor)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                    ),
                    shape = panelShape
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Pull handle
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.20f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Row (Domain & Security status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isCurrentSiteSecure) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = null,
                            tint = if (state.isCurrentSiteSecure) shieldGreen else Color(0xFFFF5252),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (state.currentDomain.isNotBlank()) state.currentDomain else "Navegação Geral",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (state.isCurrentSiteSecure) "Conexão Segura & Criptografada HTTPS" else "Conexão Não Criptografada",
                        color = if (state.isCurrentSiteSecure) shieldGreen.copy(alpha = 0.85f) else Color(0xFFFF5252),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = mutedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // HERO CARD: ESCUDO VISUAL & MÉTRICAS BENTO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                shieldGreen.copy(alpha = if (isDarkMode) 0.16f else 0.10f),
                                Color(0xFF00B0FF).copy(alpha = if (isDarkMode) 0.12f else 0.06f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                shieldGreen.copy(alpha = 0.40f),
                                Color(0xFF00B0FF).copy(alpha = 0.20f)
                            )
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Shield Icon with Aura Glow
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(shieldGreen, Color(0xFF00B0FF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.85f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.pageBlockedCount}",
                            color = textColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Rastreadores e Anúncios Bloqueados",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Privacidade blindada nesta página",
                            color = mutedColor,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Bento Row: 3 Savings Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrivacyMetricBentoTile(
                            icon = Icons.Rounded.DataUsage,
                            value = state.formattedDataSaved,
                            label = "Dados Salvos",
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )

                        PrivacyMetricBentoTile(
                            icon = Icons.Rounded.Speed,
                            value = state.formattedTimeSaved,
                            label = "Mais Rápido",
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )

                        PrivacyMetricBentoTile(
                            icon = Icons.Rounded.BatteryChargingFull,
                            value = "Poupada",
                            label = "CPU / Bateria",
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BREAKDOWN DOS RASTREADORES BLOQUEADOS
            Text(
                text = "Empresas & Rastreadores Bloqueados (${state.pageBlockedItems.size})",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.pageBlockedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ Nenhum rastreador invasivo ativo nesta página.\nSeus dados estão protegidos.",
                        color = mutedColor,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.pageBlockedItems.forEach { item ->
                        BlockedTrackerItemCard(item = item, isDarkMode = isDarkMode)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CONTROLES DE PROTEÇÃO IMEDIATA
            Text(
                text = "Controles de Proteção",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                    .border(
                        1.dp,
                        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Toggle AdBlock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloqueador de Anúncios",
                            color = textColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Remove propagandas, pop-ups e banners intrusivos",
                            color = mutedColor,
                            fontSize = 11.5.sp
                        )
                    }

                    Switch(
                        checked = adBlockEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleAdBlock(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = shieldGreen
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
                )

                // Toggle Cookie Blocker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloquear Rastreamento Cross-Site",
                            color = textColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Impede que redes sigam seus passos entre sites",
                            color = mutedColor,
                            fontSize = 11.5.sp
                        )
                    }

                    Switch(
                        checked = cookieBlockerEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleCookieBlocker(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = shieldGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TOTAL ACUMULADO DESDE A INSTALAÇÃO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total bloqueado no navegador:",
                    color = mutedColor,
                    fontSize = 12.sp
                )
                Text(
                    text = "${state.totalBlockedCount} itens",
                    color = shieldGreen,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacyMetricBentoTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDarkMode) Color.Black.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.85f))
            .border(
                1.dp,
                if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                color = if (isDarkMode) Color.White else Color(0xFF1E1E24),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = if (isDarkMode) Color.White.copy(alpha = 0.60f) else Color(0xFF6B7280),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun BlockedTrackerItemCard(
    item: BlockedTrackerItem,
    isDarkMode: Boolean
) {
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.category.iconEmoji,
                fontSize = 18.sp
            )

            Column {
                Text(
                    text = item.entityName,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E24),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.category.displayName} • ${item.domain}",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.55f) else Color(0xFF6B7280),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF00E676).copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${item.count}x",
                    color = Color(0xFF00E676),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Bloqueado",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
