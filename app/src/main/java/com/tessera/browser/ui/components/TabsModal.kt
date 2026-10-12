package com.tessera.browser.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.BrowserSpace
import com.tessera.browser.data.TabGroup
import com.tessera.browser.ui.theme.ApplePalette
import com.tessera.browser.ui.theme.AppleShapes
import com.tessera.browser.viewmodel.BrowserTab
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Gerenciador de Abas Arc Search Style — Tessera Browser.
 * Carrossel horizontal imersivo com cartões grandes, física suave,
 * preview REAL em Bitmap da WebView e gesto de swipe up para fechar.
 */
@Composable
fun TabsModal(
    tabs: List<BrowserTab>,
    activeTabId: String,
    tabThumbnails: Map<String, Bitmap> = emptyMap(),
    tabGroups: List<TabGroup> = emptyList(),
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit,
    onCreateGroup: (title: String, colorArgb: Long) -> Unit = { _, _ -> },
    onDeleteGroup: (groupId: String, closeTabs: Boolean) -> Unit = { _, _ -> },
    onAddTabToGroup: (tabId: String, groupId: String) -> Unit = { _, _ -> },
    onRemoveTabFromGroup: (tabId: String) -> Unit = {},
    onTogglePin: (String) -> Unit = {},
    onCloseAllTabs: () -> Unit = {},
    onArchiveInactiveTabs: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    spaces: List<BrowserSpace> = BrowserSpace.DEFAULT_SPACES,
    activeSpaceId: String = "space_general",
    onSelectSpace: (String) -> Unit = {},
    onCreateSpace: (name: String, emoji: String, colorArgb: Long) -> Unit = { _, _, _ -> },
    onUpdateSpace: (spaceId: String, name: String, emoji: String, colorArgb: Long) -> Unit = { _, _, _, _ -> },
    onDeleteSpace: (spaceId: String) -> Unit = {},
    onMoveTabToSpace: (tabId: String, targetSpaceId: String) -> Unit = { _, _ -> },
    accentColor: Color = ApplePalette.BlueDark,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSpaceSelectorMenu by remember { mutableStateOf(false) }
    var showCreateSpaceDialog by remember { mutableStateOf(false) }
    var newSpaceName by remember { mutableStateOf("") }
    var newSpaceEmoji by remember { mutableStateOf("📁") }

    val currentSpace = spaces.find { it.id == activeSpaceId } ?: spaces.firstOrNull() ?: BrowserSpace.DEFAULT_SPACES.first()
    val currentSpaceTabs = tabs.filter { it.spaceId == activeSpaceId }

    val sheetBg = if (isDarkMode) ApplePalette.DarkBackground else ApplePalette.LightBackground
    val cardBg = if (isDarkMode) ApplePalette.DarkSurface else ApplePalette.LightCard
    val cardBorder = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline
    val contentColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val mutedColor = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sheetBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. CABEÇALHO SUPERIOR SÓLIDO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Botão Concluído / Fechar Modal
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Concluído",
                        color = accentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. CARROSSEL CENTRAL DE CARTÕES GRANDES (ARC SEARCH STYLE)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (currentSpaceTabs.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.dp, cardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = null,
                                tint = mutedColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = "Nenhuma guia aberta",
                            color = contentColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toque em Nova Guia para começar a navegar",
                            color = mutedColor,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val initialIndex = remember(currentSpaceTabs, activeTabId) {
                        currentSpaceTabs.indexOfFirst { it.id == activeTabId }.coerceAtLeast(0)
                    }
                    val pagerState = rememberPagerState(
                        initialPage = initialIndex,
                        pageCount = { currentSpaceTabs.size }
                    )

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 42.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val tab = currentSpaceTabs[page]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val scale = 1f - (pageOffset * 0.12f).coerceIn(0f, 0.22f)
                        val alpha = 1f - (pageOffset * 0.40f).coerceIn(0f, 0.50f)
                        val rotZ = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction) * -2f

                        ArcTabCard(
                            tab = tab,
                            isActive = tab.id == activeTabId,
                            thumbnail = tabThumbnails[tab.id],
                            isDarkMode = isDarkMode,
                            accentColor = accentColor,
                            scale = scale,
                            alpha = alpha,
                            rotationZ = rotZ,
                            onClick = {
                                onSelectTab(tab.id)
                                onDismiss()
                            },
                            onClose = { onCloseTab(tab.id) }
                        )
                    }
                }
            }

            // 3. BARRA INFERIOR SÓLIDA DE AÇÕES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .border(width = 0.8.dp, color = cardBorder)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fechar Todas
                TextButton(
                    onClick = onCloseAllTabs,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Fechar Todas",
                        color = Color(0xFFFF453A),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botão Central: [+ Nova Guia]
                val newTabShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(newTabShape)
                        .background(accentColor)
                        .clickable(onClick = {
                            onNewTab()
                            onDismiss()
                        })
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Nova Guia",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Nova Guia",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Histórico Rápido
                TextButton(
                    onClick = onOpenHistory,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Histórico",
                        color = contentColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // DIÁLOGO: CRIAR NOVO ESPAÇO
    if (showCreateSpaceDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSpaceDialog = false },
            title = { Text("Novo Espaço", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSpaceName,
                        onValueChange = { newSpaceName = it },
                        label = { Text("Nome do Espaço") },
                        placeholder = { Text("Ex: Trabalho, Estudos, Pessoal") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Ícone:", fontSize = 13.sp)
                        val iconPresets = listOf("🌐", "💼", "🏠", "📚", "🚀", "🎨", "🎧")
                        iconPresets.forEach { iconKey ->
                            val isSel = newSpaceEmoji == iconKey
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) accentColor.copy(alpha = 0.20f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSel) accentColor else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { newSpaceEmoji = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                SpaceVectorIcon(
                                    identifier = iconKey,
                                    tint = if (isSel) accentColor else mutedColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSpaceName.isNotBlank()) {
                            onCreateSpace(newSpaceName.trim(), newSpaceEmoji, TabGroup.PRESET_COLORS[1])
                            showCreateSpaceDialog = false
                        }
                    }
                ) {
                    Text("Criar Espaço", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSpaceDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Cartão de Aba Imersivo estilo Arc Search com preview real da página e gesto de swipe up.
 */
@Composable
private fun ArcTabCard(
    tab: BrowserTab,
    isActive: Boolean,
    thumbnail: Bitmap?,
    isDarkMode: Boolean,
    accentColor: Color,
    scale: Float,
    alpha: Float,
    rotationZ: Float,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var offsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = tween(durationMillis = 150),
        label = "tab_card_offset"
    )

    val draggableState = rememberDraggableState { delta ->
        offsetY = (offsetY + delta).coerceAtMost(0f)
    }

    val cardShape = RoundedCornerShape(22.dp)
    val cardBg = if (isDarkMode) ApplePalette.DarkSurface else ApplePalette.LightCard
    val cardBorder = if (isActive) accentColor else if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline
    val contentColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val mutedColor = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary

    val hostDomain = remember(tab.url, tab.isHomePage) {
        if (tab.isHomePage) {
            "tessera://inicio"
        } else {
            try {
                val uri = android.net.Uri.parse(
                    if (!tab.url.startsWith("http://") && !tab.url.startsWith("https://")) {
                        "https://${tab.url}"
                    } else tab.url
                )
                uri.host?.removePrefix("www.") ?: tab.url.removePrefix("https://").substringBefore('/')
            } catch (e: Exception) {
                tab.url
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
                this.rotationZ = rotationZ
            }
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (offsetY < -140f || velocity < -400f) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClose()
                    } else {
                        offsetY = 0f
                    }
                }
            )
            .shadow(
                elevation = if (isActive) 14.dp else 4.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(
                width = if (isActive) 1.8.dp else 1.dp,
                color = cardBorder,
                shape = cardShape
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. HEADER DO CARTÃO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (tab.isHomePage) "Página Inicial" else tab.title.ifBlank { hostDomain },
                            color = contentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = hostDomain,
                            color = mutedColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Botão Fechar no Cartão
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0x33FFFFFF) else Color(0x14000000))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar guia",
                        tint = contentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 2. PREVIEW REAL DA PÁGINA (WEBVIEW THUMBNAIL)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = tab.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (tab.isHomePage) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TESSERA",
                            color = contentColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "Página Inicial",
                            color = mutedColor,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(cardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = hostDomain,
                            color = contentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
