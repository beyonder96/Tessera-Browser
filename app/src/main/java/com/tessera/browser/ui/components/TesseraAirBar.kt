package com.tessera.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.R
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.ui.theme.ApplePalette
import com.tessera.browser.ui.theme.AppleShapes
import com.tessera.browser.viewmodel.BrowserTab

/**
 * Tessera Unified Safari Omnibar — Apple iOS 18 "Liquid Glass" Edition.
 *
 * Uma única cápsula flutuante inferior ergonômica com acabamento em vidro translúcido:
 * - Ações essenciais integradas (Voltar/Avançar contextual, Indicador de Segurança/Leitura, Domínio).
 * - Gesto de deslizar (swipe) na barra para alternar abas instantaneamente.
 * - Expansão fluida para busca rápida com sugestões contextuais.
 * - Botão de IA / Apple Intelligence, badge de abas em squircle e menu de configurações.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TesseraAirBar(
    progress: Float,
    displayUrl: String,
    canGoBack: Boolean,
    canGoForward: Boolean = false,
    tabCount: Int,
    isBookmarked: Boolean = false,
    isIncognito: Boolean = false,
    isDarkMode: Boolean = false,
    isHomePage: Boolean = false,
    isReaderModeActive: Boolean = false,
    isReaderModeAvailable: Boolean = false,
    favorites: List<SpeedDialItem> = emptyList(),
    searchSuggestions: List<String> = emptyList(),
    onBack: () -> Unit,
    onForward: () -> Unit = {},
    onHome: () -> Unit = {},
    onReload: () -> Unit = {},
    onSearch: (String) -> Unit,
    onVoiceSearch: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onOpenAi: (String) -> Unit = {},
    onBrowseForMe: (String) -> Unit = {},
    onOpenAiAction: () -> Unit,
    onToggleBookmark: () -> Unit = {},
    onToggleIncognito: () -> Unit = {},
    onToggleReaderMode: () -> Unit = {},
    onOpenTabs: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenSiteSettings: () -> Unit = {},
    privacyBlockedCount: Int = 0,
    onOpenPrivacyDashboard: () -> Unit = {},
    onOpenFavorite: (String) -> Unit = {},
    onFastAction: () -> Unit = {},
    onNextTab: () -> Unit = {},
    onPreviousTab: () -> Unit = {},
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    isEditingExternal: Boolean = false,
    onEditingChange: (Boolean) -> Unit = {},
    accentColor: Color = ApplePalette.BlueDark,
    siteThemeColor: Color? = null,
    currentSpaceEmoji: String = "🌐",
    currentSpaceName: String = "Geral",
    currentSpaceColor: Color = ApplePalette.BlueDark,
    onOpenSpaces: () -> Unit = {},
    tabs: List<BrowserTab> = emptyList(),
    activeTabId: String = "",
    showTabStrip: Boolean = true,
    onSelectTab: (String) -> Unit = {},
    onCloseTab: (String) -> Unit = {},
    onNewTab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf(displayUrl) }
    var isEditing by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        dragAccumulator += delta
    }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(displayUrl) {
        if (!isEditing) {
            queryText = displayUrl
        }
    }

    LaunchedEffect(isEditingExternal) {
        if (isEditingExternal && !isEditing) {
            isEditing = true
        } else if (!isEditingExternal && isEditing) {
            isEditing = false
        }
    }

    LaunchedEffect(isEditing) {
        onEditingChange(isEditing)
        if (isEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    BackHandler(enabled = isEditing) {
        isEditing = false
    }

    // Gerenciamento fluido do progresso e fade-out gracioso ao concluir
    var showProgress by remember { mutableStateOf(false) }
    var targetProgressFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progress, isHomePage) {
        if (isHomePage) {
            showProgress = false
            targetProgressFraction = 0f
        } else if (progress > 0f && progress < 1f) {
            showProgress = true
            targetProgressFraction = progress
        } else if (progress >= 1f) {
            targetProgressFraction = 1f
            delay(200)
            showProgress = false
        } else {
            // progress == 0f (ex: ao finalizar a navegação ou reset)
            if (showProgress) {
                targetProgressFraction = 1f
                delay(200)
                showProgress = false
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgressFraction,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "safari_progress"
    )

    val progressAlpha by animateFloatAsState(
        targetValue = if (showProgress && !isEditing && !isHomePage) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = LinearOutSlowInEasing),
        label = "safari_progress_alpha"
    )

    val animatedTintColor by animateColorAsState(
        targetValue = siteThemeColor ?: Color.Transparent,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "safari_tint"
    )

    val effectiveAccent = when {
        isIncognito -> ApplePalette.Purple
        animatedTintColor != Color.Transparent -> animatedTintColor
        isDarkMode -> ApplePalette.BlueDark
        else -> ApplePalette.Blue
    }

    // Cores e Superfícies 100% Sólidas (Fim do vidro translúcido)
    val solidBarBg = when {
        isIncognito -> Color(0xFF161224)
        isDarkMode -> ApplePalette.DarkSurface
        else -> ApplePalette.LightSurface
    }

    val solidPillBg = when {
        isIncognito -> Color(0xFF221A38)
        isDarkMode -> ApplePalette.DarkElevated
        else -> ApplePalette.LightElevated
    }

    val solidBorder = when {
        isIncognito -> Color(0x66BF5AF2)
        isDarkMode -> ApplePalette.DarkHairline
        else -> ApplePalette.LightHairline
    }

    val contentColor = if (isIncognito || isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val mutedColor = if (isIncognito) Color(0xFFD8B4FE).copy(alpha = 0.70f) else if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary

    // BARRA INFERIOR FLUTUANTE ELEGANTE
    val floatingBarShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sugestões Flutuantes de Busca
            if (isEditing && queryText.isNotBlank()) {
                SearchSuggestionsFloatingCard(
                    queryText = queryText,
                    suggestions = searchSuggestions,
                    onSelect = {
                        isEditing = false
                        onSearch(it)
                    },
                    onInsert = {
                        queryText = it
                        onQueryChange(it)
                    },
                    accentColor = effectiveAccent,
                    isDarkMode = isDarkMode
                )
            }

            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val isHomeCollapsed = isHomePage && !isEditing
            val targetWidth = if (isHomeCollapsed) 56.dp else (screenWidth - 28.dp)
            val animatedBarWidth by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dock_morph_width"
            )
            val cornerRadius by animateDpAsState(
                targetValue = if (isHomeCollapsed) 28.dp else 26.dp,
                label = "dock_corner_radius"
            )
            val currentDockShape = RoundedCornerShape(cornerRadius)

            // Cápsula Flutuante Principal Morphing (Recolhe para 56dp só com a Lupa na Home)
            Box(
                modifier = Modifier
                    .width(animatedBarWidth)
                    .height(if (isHomeCollapsed) 54.dp else 58.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = currentDockShape,
                        ambientColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.14f),
                        spotColor = Color.Black.copy(alpha = if (isDarkMode) 0.25f else 0.10f)
                    )
                    .clip(currentDockShape)
                    .background(solidBarBg)
                    .border(width = 1.dp, color = solidBorder, shape = currentDockShape)
            ) {
                if (isHomeCollapsed) {
                    // ESTADO RECOLHIDO: APENAS A LUPA CENTRALIZADA (ESTILO TESSERA LAUNCHER)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isEditing = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Pesquisar",
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    // Linha Principal de Controles Cirúrgicos (Altura 48dp, centralizada no Dock de 58dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp)
                            .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        enabled = !isEditing && !isHomePage,
                        onDragStopped = { velocity ->
                            val threshold = 50f
                            if (dragAccumulator < -threshold || velocity < -280f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNextTab()
                            } else if (dragAccumulator > threshold || velocity > 280f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPreviousTab()
                            }
                            dragAccumulator = 0f
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditing) {
                    // ==========================================
                    // MODO DE EDIÇÃO / DIGITAÇÃO DE BUSCA
                    // ==========================================
                    // 1. Botão Cancelar / Fechar à esquerda
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { isEditing = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Cancelar busca",
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 2. Campo de Entrada com Ícone de Lupa
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(solidPillBg)
                            .border(0.8.dp, solidBorder, RoundedCornerShape(22.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = mutedColor,
                            modifier = Modifier.size(16.dp)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            if (queryText.isEmpty()) {
                                Text(
                                    text = if (isIncognito) "Buscar em modo anônimo" else "Pesquisar ou digitar endereço",
                                    color = mutedColor,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                            BasicTextField(
                                value = queryText,
                                onValueChange = {
                                    queryText = it
                                    onQueryChange(it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = contentColor,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(effectiveAccent),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Go,
                                    autoCorrectEnabled = !isIncognito,
                                    platformImeOptions = if (isIncognito) PlatformImeOptions(
                                        privateImeOptions = "com.tessera.browser.INCOGNITO"
                                    ) else null
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        if (queryText.isNotBlank()) {
                                            isEditing = false
                                            onSearch(queryText.trim())
                                        }
                                    }
                                )
                            )
                        }

                        if (queryText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        queryText = ""
                                        onQueryChange("")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Limpar",
                                    tint = mutedColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            // Busca por Voz discreta e monocromática
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isEditing = false
                                        onVoiceSearch()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Pesquisa por voz",
                                    tint = mutedColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    // 3. Botão Ir (quando há texto) OU Atalhos de Abas & Menu (quando vazio — acesso direto a partir da Home!)
                    if (queryText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(effectiveAccent)
                                .clickable {
                                    isEditing = false
                                    onSearch(queryText.trim())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Ir",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        val tabBadgeShape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isEditing = false
                                        onOpenTabs()
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isEditing = false
                                        onOpenSpaces()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(tabBadgeShape)
                                    .border(
                                        width = 1.3.dp,
                                        color = contentColor.copy(alpha = 0.88f),
                                        shape = tabBadgeShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabCount.toString(),
                                    color = contentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isEditing = false
                                        onOpenSettings()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "Menu de opções",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // MODO NORMAL: OS 4 CONTROLES ESSENCIAIS
                    // ==========================================

                    // 1. [VOLTAR] Contextual (se houver histórico e não for home)
                    if (canGoBack && !isHomePage) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onBack()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Voltar",
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // 2. [CAMPO DE URL/BUSCA CENTRAL E LIMPO]
                    val isSecure = displayUrl.startsWith("https://", ignoreCase = true)
                    val isWeb = displayUrl.startsWith("http://") || displayUrl.startsWith("https://")
                    val hostText = if (displayUrl.isNotBlank() && !isHomePage) {
                        try {
                            val uri = android.net.Uri.parse(
                                if (!displayUrl.startsWith("http://") && !displayUrl.startsWith("https://")) {
                                    "https://$displayUrl"
                                } else displayUrl
                            )
                            val host = uri.host
                            if (!host.isNullOrBlank()) {
                                host.removePrefix("www.")
                            } else {
                                displayUrl.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore('/')
                            }
                        } catch (e: Exception) {
                            displayUrl
                        }
                    } else {
                        "Pesquisar ou digitar endereço"
                    }

                    val pillShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(pillShape)
                            .background(solidPillBg)
                            .border(0.8.dp, solidBorder, pillShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isEditing = true }
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isWeb && !isHomePage) {
                                    Icon(
                                        imageVector = if (isSecure) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                        contentDescription = "Segurança",
                                        tint = if (isSecure) ApplePalette.Green else ApplePalette.Red,
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else if (isHomePage) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = mutedColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                Text(
                                    text = hostText,
                                    color = if (isHomePage) mutedColor else contentColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isHomePage) FontWeight.Normal else FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // SMARTDOCK: Sugestão Contextual de Modo Leitor
                            AnimatedVisibility(
                                visible = (isReaderModeAvailable || isReaderModeActive) && !isHomePage,
                                enter = fadeIn(tween(180)) + expandHorizontally(),
                                exit = fadeOut(tween(180)) + shrinkHorizontally()
                            ) {
                                val readerBg = if (isReaderModeActive) {
                                    if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
                                } else {
                                    if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated
                                }
                                val readerContentColor = if (isReaderModeActive) {
                                    if (isDarkMode) Color.Black else Color.White
                                } else {
                                    contentColor
                                }

                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(CircleShape)
                                        .background(readerBg)
                                        .border(
                                            width = 0.8.dp,
                                            color = if (isReaderModeActive) Color.Transparent else solidBorder,
                                            shape = CircleShape
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onToggleReaderMode()
                                        }
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                            contentDescription = "Modo Leitura",
                                            tint = readerContentColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Leitor",
                                            color = readerContentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Indicador de Progresso Luminoso Integrado na Base da Pílula (Estilo Safari / Arc)
                        if (progressAlpha > 0f) {
                            val highlightColor = when {
                                isIncognito -> Color(0xFFE9D5FF)
                                isDarkMode -> Color(0xFF90CAF9)
                                else -> Color(0xFFBAE6FD)
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(animatedProgress.coerceIn(0.005f, 1f))
                                    .height(2.5.dp)
                                    .graphicsLayer { alpha = progressAlpha }
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                effectiveAccent.copy(alpha = 0.70f),
                                                effectiveAccent,
                                                highlightColor
                                            )
                                        ),
                                        shape = RoundedCornerShape(topEnd = 1.5.dp, bottomEnd = 1.5.dp)
                                    )
                            )
                        }
                    }

                    // 3. [CONTADOR DE ABAS SÓLIDO]
                    val tabBadgeShape = RoundedCornerShape(8.dp)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenTabs()
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenSpaces()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(23.dp)
                                .clip(tabBadgeShape)
                                .border(
                                    width = 1.3.dp,
                                    color = contentColor.copy(alpha = 0.90f),
                                    shape = tabBadgeShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabCount.toString(),
                                color = contentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 4. [MENU (•••)]
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenSettings()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "Menu de opções",
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    }
}
}

@Composable
private fun SearchSuggestionsFloatingCard(
    queryText: String,
    suggestions: List<String>,
    onSelect: (String) -> Unit,
    onInsert: (String) -> Unit,
    accentColor: Color,
    isDarkMode: Boolean
) {
    if (suggestions.isEmpty()) return

    val cardShape = RoundedCornerShape(22.dp)
    val cardBg = if (isDarkMode) ApplePalette.DarkSurface else Color(0xFFFCFBF7)
    val textColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val iconTint = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val cardBorder = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(0.8.dp, cardBorder, cardShape)
            .padding(vertical = 4.dp)
    ) {
        suggestions.take(5).forEachIndexed { index, suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(suggestion) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = suggestion,
                        color = textColor,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.NorthWest,
                    contentDescription = "Inserir",
                    tint = accentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onInsert(suggestion) }
                )
            }

            if (index < suggestions.take(5).size - 1) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }
}
