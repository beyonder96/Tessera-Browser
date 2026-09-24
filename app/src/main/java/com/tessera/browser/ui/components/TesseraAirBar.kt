package com.tessera.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.SpeedDialItem

/**
 * Tessera Fluid Omnibar — Unified, ergonomic, compact navigation capsule.
 *
 * Mode 1: Home Mode — Minimalist floating dock (Search Pill, Tabs, AI) matching Opera & Dia aesthetics.
 * Mode 2: Web Mode — Compact Single-Line Capsule [Back] [Lock | Domain] [AI] [Tabs] [Menu].
 * Mode 3: Editing Mode — Expands smoothly into focused search bar with live suggestions.
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
    accentColor: Color = Color(0xFF0288D1),
    siteThemeColor: Color? = null,
    currentSpaceEmoji: String = "🌐",
    currentSpaceName: String = "Geral",
    currentSpaceColor: Color = Color(0xFF0288D1),
    onOpenSpaces: () -> Unit = {},
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

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "airbar_progress"
    )

    val animatedTintColor by animateColorAsState(
        targetValue = siteThemeColor ?: Color.Transparent,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "airbar_tint"
    )

    val effectiveAccent = if (animatedTintColor != Color.Transparent) animatedTintColor else accentColor

    // Clean tokens for Glass & Bento
    val capsuleBg = if (isDarkMode) {
        if (animatedTintColor != Color.Transparent) {
            lerp(Color(0xE61E1A18), animatedTintColor.copy(alpha = 0.92f), 0.20f)
        } else {
            Color(0xE61C1917)
        }
    } else {
        if (animatedTintColor != Color.Transparent) {
            lerp(Color(0xF5FFFFFF), animatedTintColor.copy(alpha = 0.94f), 0.12f)
        } else {
            Color(0xF5FFFFFF)
        }
    }

    val capsuleBorder = if (isDarkMode) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val contentColor = if (isDarkMode) Color.White.copy(alpha = 0.94f) else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = if (isHomePage) 8.dp else 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Floating search suggestions card (appears above the search bar while typing)
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

            // 1. STATE A: EDITING / SEARCH EXPANDED MODE
            if (isEditing) {
                val omniShape = RoundedCornerShape(26.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = omniShape,
                            ambientColor = Color.Black.copy(alpha = 0.18f),
                            spotColor = Color.Black.copy(alpha = 0.12f)
                        )
                        .clip(omniShape)
                        .background(capsuleBg)
                        .border(1.dp, capsuleBorder, omniShape)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Back/Dismiss button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { isEditing = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Voltar",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center: Search input
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (queryText.isEmpty()) {
                                Text(
                                    text = "Pesquisar ou digitar endereço",
                                    color = mutedColor,
                                    fontSize = 14.5.sp,
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
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

                        // Right: Actions (Clear, Go)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (queryText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable { isEditing = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Cancelar",
                                        tint = mutedColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isHomePage) {
                // 2. STATE B: HOME MODE (Opera / Dia Browser Inspired Minimalist Dock)
                TesseraHomeBottomDock(
                    tabCount = tabCount,
                    isDarkMode = isDarkMode,
                    effectiveAccent = effectiveAccent,
                    capsuleBg = capsuleBg,
                    capsuleBorder = capsuleBorder,
                    contentColor = contentColor,
                    mutedColor = mutedColor,
                    onSearchClick = { isEditing = true },
                    onOpenTabs = onOpenTabs,
                    onOpenAiAction = onOpenAiAction
                )
            } else {
                // 3. STATE C: WEB BROWSING — CÁPSULA ÚNICA DE ALTURA COMPACTA (50-52dp)
                // Layout: [ ⮜ Voltar ] [ 🔒 domínio.com ] [ ✦ IA ] [ 🗂️ Abas ] [ ⋮ Menu ]
                TesseraSingleWebCapsule(
                    progress = animatedProgress,
                    displayUrl = displayUrl,
                    canGoBack = canGoBack,
                    tabCount = tabCount,
                    isDarkMode = isDarkMode,
                    effectiveAccent = effectiveAccent,
                    capsuleBg = capsuleBg,
                    capsuleBorder = capsuleBorder,
                    contentColor = contentColor,
                    mutedColor = mutedColor,
                    currentSpaceColor = currentSpaceColor,
                    privacyBlockedCount = privacyBlockedCount,
                    isReaderModeActive = isReaderModeActive,
                    isReaderModeAvailable = isReaderModeAvailable,
                    onBack = onBack,
                    onSearchClick = { isEditing = true },
                    onOpenAiAction = onOpenAiAction,
                    onOpenTabs = onOpenTabs,
                    onOpenSettings = onOpenSettings,
                    onOpenSiteSettings = onOpenSiteSettings,
                    onOpenPrivacyDashboard = onOpenPrivacyDashboard,
                    onToggleReaderMode = onToggleReaderMode,
                    onNextTab = onNextTab,
                    onPreviousTab = onPreviousTab
                )
            }
        }
    }
}

/**
 * Single-line compact navigation capsule for web browsing.
 * Exactly matches the user's requested layout:
 * [ ⮜ Voltar ] [ 🔒 domínio.com ] [ ✦ IA ] [ 🗂️ Abas ] [ ⋮ Menu ]
 */
@Composable
private fun TesseraSingleWebCapsule(
    progress: Float,
    displayUrl: String,
    canGoBack: Boolean,
    tabCount: Int,
    isDarkMode: Boolean,
    effectiveAccent: Color,
    capsuleBg: Color,
    capsuleBorder: Color,
    contentColor: Color,
    mutedColor: Color,
    currentSpaceColor: Color,
    privacyBlockedCount: Int,
    isReaderModeActive: Boolean,
    isReaderModeAvailable: Boolean,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onOpenAiAction: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSiteSettings: () -> Unit,
    onOpenPrivacyDashboard: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onNextTab: () -> Unit,
    onPreviousTab: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val capsuleShape = RoundedCornerShape(26.dp)
    val buttonShape = RoundedCornerShape(14.dp)

    var dragAccumulator by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        dragAccumulator += delta
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Back Button (⮜)
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = buttonShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(buttonShape)
                .background(capsuleBg)
                .border(1.dp, capsuleBorder, buttonShape)
                .clickable(
                    enabled = canGoBack,
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
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Voltar",
                tint = if (canGoBack) contentColor else mutedColor.copy(alpha = 0.35f),
                modifier = Modifier.size(19.dp)
            )
        }

        // 2. Central Domain & Security Capsule (weight = 1f)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = capsuleShape,
                    ambientColor = Color.Black.copy(alpha = 0.14f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(capsuleShape)
                .background(capsuleBg)
                .border(1.dp, capsuleBorder, capsuleShape)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        val threshold = 60f
                        if (dragAccumulator < -threshold || velocity < -300f) {
                            onNextTab()
                        } else if (dragAccumulator > threshold || velocity > 300f) {
                            onPreviousTab()
                        }
                        dragAccumulator = 0f
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle loading bar inside the top of the capsule
            if (progress > 0f && progress < 1f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
                    color = effectiveAccent,
                    trackColor = Color.Transparent
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val isSecure = displayUrl.startsWith("https://", ignoreCase = true)
                val isWeb = displayUrl.startsWith("http://") || displayUrl.startsWith("https://")

                if (isWeb) {
                    // Security Lock Icon
                    Icon(
                        imageVector = if (isSecure) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = "Segurança",
                        tint = if (isSecure) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenSiteSettings
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))

                    // Privacy Shield indicator (if trackers blocked)
                    if (privacyBlockedCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                .border(0.5.dp, Color(0xFF00E676).copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onOpenPrivacyDashboard
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "$privacyBlockedCount",
                                    color = Color(0xFF00E676),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }

                // Clean Host Display
                val hostText = if (displayUrl.isNotBlank()) {
                    try {
                        val uri = java.net.URI(displayUrl)
                        val host = uri.host ?: displayUrl
                        if (host.startsWith("www.")) host.substring(4) else host
                    } catch (e: Exception) {
                        displayUrl
                    }
                } else {
                    "Pesquisar ou digitar endereço"
                }

                Text(
                    text = hostText,
                    color = contentColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Reader mode badge if active/available
                if (isReaderModeActive || isReaderModeAvailable) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = "Modo Leitor",
                        tint = if (isReaderModeActive) effectiveAccent else mutedColor,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleReaderMode
                            )
                    )
                }
            }
        }

        // 3. AI Button (✦)
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = buttonShape,
                    ambientColor = effectiveAccent.copy(alpha = 0.25f),
                    spotColor = effectiveAccent.copy(alpha = 0.20f)
                )
                .clip(buttonShape)
                .background(effectiveAccent.copy(alpha = 0.16f))
                .border(1.dp, effectiveAccent.copy(alpha = 0.35f), buttonShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenAiAction()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Tessera AI",
                tint = effectiveAccent,
                modifier = Modifier.size(19.dp)
            )
        }

        // 4. Tabs Button ([ 1 ])
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = buttonShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(buttonShape)
                .background(capsuleBg)
                .border(1.dp, capsuleBorder, buttonShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenTabs()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val badgeShape = RoundedCornerShape(5.dp)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.4.dp, contentColor.copy(alpha = 0.85f), badgeShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$tabCount",
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Dot indicating Space color
            if (currentSpaceColor != Color.Unspecified) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 5.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(currentSpaceColor)
                )
            }
        }

        // 5. Menu Button (⋮)
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = buttonShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(buttonShape)
                .background(capsuleBg)
                .border(1.dp, capsuleBorder, buttonShape)
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
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Menu de Opções",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Minimalist floating dock for Tessera Browser Home screen.
 * Displays: [ 🔍 Pesquisar ou digitar endereço ] [ 1 ] ( ✦ )
 * Inspired by Opera & Dia Browser aesthetics.
 */
@Composable
fun TesseraHomeBottomDock(
    tabCount: Int,
    isDarkMode: Boolean,
    effectiveAccent: Color = Color(0xFF0288D1),
    capsuleBg: Color = if (isDarkMode) Color(0xE61C1917) else Color(0xF5FFFFFF),
    capsuleBorder: Color = if (isDarkMode) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f),
    contentColor: Color = if (isDarkMode) Color.White.copy(alpha = 0.94f) else Color(0xFF1E1E1E),
    mutedColor: Color = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93),
    onSearchClick: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenAiAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val homePillShape = RoundedCornerShape(26.dp)
    val squircleShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Search Pill (weight = 1f)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = homePillShape,
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                )
                .clip(homePillShape)
                .background(capsuleBg)
                .border(1.dp, capsuleBorder, homePillShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Pesquisar",
                    tint = mutedColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Pesquisar ou digitar endereço",
                    color = mutedColor,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. Tab Count Button
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = squircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                )
                .clip(squircleShape)
                .background(capsuleBg)
                .border(1.dp, capsuleBorder, squircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenTabs()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.5.dp, contentColor.copy(alpha = 0.85f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$tabCount",
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 3. AI Action Button
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = squircleShape,
                    ambientColor = effectiveAccent.copy(alpha = 0.30f),
                    spotColor = effectiveAccent.copy(alpha = 0.25f)
                )
                .clip(squircleShape)
                .background(effectiveAccent.copy(alpha = 0.18f))
                .border(1.dp, effectiveAccent.copy(alpha = 0.40f), squircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenAiAction()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Tessera AI",
                tint = effectiveAccent,
                modifier = Modifier.size(22.dp)
            )
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

    val cardShape = RoundedCornerShape(20.dp)
    val cardBg = if (isDarkMode) Color(0xF0201A16) else Color(0xF8FFFFFF)
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.92f) else Color(0xFF1C1C1E)
    val iconTint = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93)
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(1.dp, cardBorder, cardShape)
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
                    color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }
}
