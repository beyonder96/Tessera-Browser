package com.tessera.browser.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
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
import kotlinx.coroutines.launch
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
    accentColor: Color? = null,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSpaceSelectorMenu by remember { mutableStateOf(false) }
    var showCreateSpaceDialog by remember { mutableStateOf(false) }
    var newSpaceName by remember { mutableStateOf("") }
    var newSpaceEmoji by remember { mutableStateOf("📁") }

    val currentSpace = spaces.find { it.id == activeSpaceId } ?: spaces.firstOrNull() ?: BrowserSpace.DEFAULT_SPACES.first()
    val currentSpaceTabs = tabs.filter { it.spaceId == activeSpaceId }
    val visibleTabs = if (currentSpaceTabs.isNotEmpty()) currentSpaceTabs else tabs

    val monoAccent = accentColor ?: if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val onMonoAccent = if (monoAccent.luminance() > 0.5f) ApplePalette.DarkBackground else Color.White

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
                        color = monoAccent,
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
                if (visibleTabs.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            tint = mutedColor,
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = "Nenhuma aba aberta",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                        Text(
                            text = "Toque em Nova Guia para começar a navegar",
                            fontSize = 13.5.sp,
                            color = mutedColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val initialIndex = remember(visibleTabs, activeTabId) {
                        val idx = visibleTabs.indexOfFirst { it.id == activeTabId }
                        if (idx >= 0) idx else (visibleTabs.size - 1).coerceAtLeast(0)
                    }
                    val pagerState = rememberPagerState(
                        initialPage = initialIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0)),
                        pageCount = { visibleTabs.size }
                    )

                    HorizontalPager(
                        state = pagerState,
                        key = { page -> visibleTabs.getOrNull(page)?.id ?: page },
                        contentPadding = PaddingValues(horizontal = 42.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val tab = visibleTabs.getOrNull(page) ?: return@HorizontalPager
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val scale = 1f - (pageOffset * 0.12f).coerceIn(0f, 0.22f)
                        val alpha = 1f - (pageOffset * 0.40f).coerceIn(0f, 0.50f)
                        val rotZ = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction) * -2f

                        ArcTabCard(
                            tab = tab,
                            isActive = tab.id == activeTabId,
                            thumbnail = tabThumbnails[tab.id],
                            isDarkMode = isDarkMode,
                            accentColor = monoAccent,
                            scale = scale,
                            alpha = alpha,
                            rotationZ = rotZ,
                            onClick = {
                                onSelectTab(tab.id)
                                onDismiss()
                            },
                            onClose = { onCloseTab(tab.id) },
                            onTogglePin = { onTogglePin(tab.id) }
                        )
                    }
                }
            }

            // 3. BARRA INFERIOR SÓLIDA DE AÇÕES
            val hasTabs = visibleTabs.isNotEmpty()
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
                    onClick = { if (hasTabs) onCloseAllTabs() },
                    enabled = hasTabs,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Fechar Todas",
                        color = if (hasTabs) Color(0xFFFF453A) else mutedColor.copy(alpha = 0.35f),
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
                        .background(monoAccent)
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
                            tint = onMonoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Nova Guia",
                            color = onMonoAccent,
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
                                    .background(if (isSel) monoAccent.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSel) monoAccent else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { newSpaceEmoji = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                SpaceVectorIcon(
                                    identifier = iconKey,
                                    tint = if (isSel) monoAccent else mutedColor,
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
                    Text("Criar Espaço", color = monoAccent, fontWeight = FontWeight.Bold)
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
 * Retorna o nome da página de forma otimizada e limpa para o layout da pill.
 * Nunca exibe a URL completa com caminhos longos ou parâmetros.
 */
private fun getOptimizedTabName(tab: BrowserTab): String {
    if (tab.isHomePage) return "Início"
    val rawTitle = tab.title.trim()
    val isTitleAUrl = rawTitle.startsWith("http://", ignoreCase = true) ||
            rawTitle.startsWith("https://", ignoreCase = true) ||
            rawTitle.startsWith("www.", ignoreCase = true) ||
            rawTitle.contains("/")

    if (rawTitle.isNotBlank() && !isTitleAUrl && rawTitle != "Nova Guia") {
        return rawTitle
    }

    // Fallback: extrai somente o domínio enxuto (ex: "tudocelular.com" ou "google.com")
    return try {
        val candidate = if (!tab.url.startsWith("http://") && !tab.url.startsWith("https://")) {
            "https://${tab.url}"
        } else tab.url
        val uri = android.net.Uri.parse(candidate)
        val host = uri.host?.removePrefix("www.") ?: tab.url.removePrefix("https://").substringBefore('/')
        if (host.isNotBlank()) host else "Página Web"
    } catch (e: Exception) {
        "Página Web"
    }
}

/**
 * Cartão de Aba Imersivo estilo Arc Search.
 * Layout com pill otimizada acima e card com live preview abaixo (sem header interno e sem 'X').
 * Gesto para CIMA: Exclui a guia.
 * Gesto para BAIXO: Fixa/desafixa o card.
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
    onClose: () -> Unit,
    onTogglePin: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val animOffsetY = remember { Animatable(0f) }

    LaunchedEffect(tab.id) {
        animOffsetY.snapTo(0f)
    }

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            animOffsetY.snapTo((animOffsetY.value + delta).coerceIn(-500f, 250f))
        }
    }

    val currentY = animOffsetY.value
    val isClosing = currentY < -120f
    val isPinning = currentY > 90f
    val onAccentColor = if (accentColor.luminance() > 0.5f) ApplePalette.DarkBackground else Color.White

    val cardShape = RoundedCornerShape(24.dp)
    val cardBg = if (isDarkMode) ApplePalette.DarkSurface else ApplePalette.LightCard
    val cardBorder = if (isActive) {
        accentColor
    } else if (tab.isPinned) {
        accentColor.copy(alpha = 0.55f)
    } else if (isDarkMode) {
        ApplePalette.DarkHairline
    } else {
        ApplePalette.LightHairline
    }

    val contentColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val mutedColor = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val optimizedName = remember(tab.title, tab.url, tab.isHomePage) {
        getOptimizedTabName(tab)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha * (1f - (-currentY.coerceAtMost(0f) / 600f)).coerceIn(0.25f, 1f)
                this.rotationZ = rotationZ
            }
            .offset { IntOffset(0, currentY.roundToInt()) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (animOffsetY.value < -120f || velocity < -400f) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            animOffsetY.animateTo(-1000f, tween(180))
                            onClose()
                        }
                    } else if (animOffsetY.value > 90f || velocity > 350f) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTogglePin()
                        coroutineScope.launch {
                            animOffsetY.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            animOffsetY.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (animOffsetY.value.absoluteValue < 12f) {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // FEEDBACK VISUAL FLUTUANTE DE GESTO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ao puxar pra cima (excluir)
                if (currentY < -25f) {
                    Surface(
                        shape = CircleShape,
                        color = if (isClosing) Color(0xFFFF453A) else if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated,
                        border = BorderStroke(1.dp, if (isClosing) Color(0xFFFF453A) else if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = if (isClosing) Color.White else Color(0xFFFF453A),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isClosing) "Solte para fechar" else "Deslize para fechar",
                                color = if (isClosing) Color.White else contentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Ao puxar pra baixo (fixar / desafixar)
                if (currentY > 25f) {
                    val pinLabel = if (tab.isPinned) {
                        if (isPinning) "Solte para desafixar" else "Puxe para desafixar"
                    } else {
                        if (isPinning) "Solte para fixar" else "Puxe para fixar"
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isPinning) accentColor else if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated,
                        border = BorderStroke(1.dp, if (isPinning) accentColor else if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = if (isPinning) onAccentColor else accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = pinLabel,
                                color = if (isPinning) onAccentColor else contentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 1. A PILL COM O NOME DA PÁGINA (OTIMIZADO, NUNCA A URL COMPLETA)
            val pillBg = if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated
            val pillBorder = if (tab.isPinned) accentColor.copy(alpha = 0.55f) else if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline

            Surface(
                shape = CircleShape,
                color = pillBg,
                border = BorderStroke(1.dp, pillBorder),
                shadowElevation = 3.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = if (tab.isPinned) accentColor else mutedColor,
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = optimizedName,
                        color = contentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (tab.isPinned) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Guia Fixada",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // 2. CARD COM LIVE PREVIEW ABAIXO DA PILL (SEM X, SEM HEADER INTERNO)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = if (isActive) 12.dp else 4.dp,
                        shape = cardShape,
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.18f)
                    )
                    .clip(cardShape)
                    .background(cardBg)
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = cardBorder,
                        shape = cardShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = tab.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = optimizedName,
                            color = contentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
