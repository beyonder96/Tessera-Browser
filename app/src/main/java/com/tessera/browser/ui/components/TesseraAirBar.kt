package com.tessera.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.R
import com.tessera.browser.data.SpeedDialItem

/**
 * Tessera AirBar — Iconic Two-Tier Ergonomic Floating Navigation Bar.
 *
 * Tier 1: Floating Omnibar Capsule:
 *   [ < ] [ > ]     website.com     [ ⚡ ]
 *   - Swipeable horizontally for quick tab switching.
 *   - Tap to expand into full search input.
 *   - Right action button: Arc/Fast Action / Reader Mode toggle / Reload.
 *
 * Tier 2: 5-Button Floating Dock:
 *   [ 🕵 Incognito ] [ ⭐ Star ] ( 🔮 3D Glowing Pearl Orb ) [ 🔲 Tab Count ] [ ☰ Menu ]
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

    val baseDarkOmni = Color(0xE024201E)
    val baseLightOmni = Color(0xEEFFFFFF)

    val omniBg = if (isDarkMode) {
        if (animatedTintColor != Color.Transparent) {
            lerp(baseDarkOmni, animatedTintColor.copy(alpha = 0.94f), 0.20f)
        } else {
            baseDarkOmni
        }
    } else {
        if (animatedTintColor != Color.Transparent) {
            lerp(baseLightOmni, animatedTintColor.copy(alpha = 0.96f), 0.12f)
        } else {
            baseLightOmni
        }
    }

    val dockBg = if (isHomePage) {
        SolidColor(Color.Transparent)
    } else if (isDarkMode) {
        if (animatedTintColor != Color.Transparent) {
            Brush.verticalGradient(
                listOf(
                    Color(0x00120E0D),
                    lerp(Color(0x88120E0D), animatedTintColor.copy(alpha = 0.35f), 0.28f),
                    lerp(Color(0xEE120E0D), animatedTintColor.copy(alpha = 0.55f), 0.28f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0x00120E0D), Color(0xAA120E0D), Color(0xEE120E0D))
            )
        }
    } else {
        if (animatedTintColor != Color.Transparent) {
            Brush.verticalGradient(
                listOf(
                    Color(0x00FFFFFF),
                    lerp(Color(0xCCFFFFFF), animatedTintColor.copy(alpha = 0.20f), 0.25f),
                    lerp(Color(0xFAFFFFFF), animatedTintColor.copy(alpha = 0.35f), 0.25f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0x00FFFFFF), Color(0xCCFFFFFF), Color(0xFAFFFFFF))
            )
        }
    }

    val omniBorderBrush = if (animatedTintColor != Color.Transparent) {
        Brush.horizontalGradient(
            colors = if (isDarkMode) {
                listOf(
                    Color.White.copy(alpha = 0.25f),
                    animatedTintColor.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.12f),
                    animatedTintColor.copy(alpha = 0.40f)
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    animatedTintColor.copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.70f),
                    animatedTintColor.copy(alpha = 0.35f)
                )
            }
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDarkMode) 0.30f else 0.85f),
                Color.White.copy(alpha = if (isDarkMode) 0.08f else 0.30f)
            )
        )
    }

    val contentColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(dockBg)
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

            // Linear progress indicator when loading
            if (progress > 0f && progress < 1f && !isHomePage) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = effectiveAccent,
                    trackColor = Color.Transparent
                )
            }

            // =========================================================================
            // 1. TOP ELEMENT: CAPSULE SEARCH BAR (Omnibar)
            // =========================================================================
            val omniShape = RoundedCornerShape(26.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = omniShape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(omniShape)
                    .background(omniBg)
                    .border(
                        width = 1.dp,
                        brush = omniBorderBrush,
                        shape = omniShape
                    )
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        enabled = !isEditing && !isHomePage,
                        onDragStopped = { velocity ->
                            val threshold = 60f
                            if (dragAccumulator < -threshold || velocity < -300f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNextTab()
                            } else if (dragAccumulator > threshold || velocity > 300f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPreviousTab()
                            }
                            dragAccumulator = 0f
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isEditing) {
                            isEditing = true
                        }
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isEditing) {
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
                    } else if (isHomePage) {
                        // Home Page Minimalist Search Capsule (Search Pill)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkMode) Color.White.copy(0.08f) else Color.Black.copy(0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Pesquisar",
                                    tint = effectiveAccent,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Text(
                                text = "Pesquisar ou digitar endereço",
                                color = mutedColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Left: Back < and Forward > Chevrons (Faithfully matching user screenshot)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
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
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                    contentDescription = "Voltar",
                                    tint = if (canGoBack) contentColor else mutedColor.copy(alpha = 0.35f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        enabled = canGoForward,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onForward()
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Avançar",
                                    tint = if (canGoForward) contentColor else mutedColor.copy(alpha = 0.35f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Center: Display Host centered (e.g. website.com) with Security Icon & Privacy Counter
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val isSecure = displayUrl.startsWith("https://", ignoreCase = true)
                            val isWeb = displayUrl.startsWith("http://") || displayUrl.startsWith("https://")

                            if (isWeb) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onOpenSiteSettings
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSecure) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                        contentDescription = "Configurações do site",
                                        tint = if (isSecure) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(3.dp))

                                if (privacyBlockedCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF06B6D4).copy(alpha = 0.14f))
                                            .border(0.5.dp, Color(0xFF06B6D4).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
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
                                                contentDescription = "Escudo",
                                                tint = Color(0xFF06B6D4),
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Text(
                                                text = "$privacyBlockedCount",
                                                color = Color(0xFF06B6D4),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }

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
                                color = if (displayUrl.isNotBlank()) contentColor else mutedColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Right: Black Squircle Action Button with White Circle & Bolt Icon ⚡ (Matches Screenshot)
                        val isReaderMode = isReaderModeAvailable || isReaderModeActive
                        val actionSquircleShape = RoundedCornerShape(10.dp)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = actionSquircleShape,
                                    ambientColor = Color.Black.copy(alpha = 0.25f),
                                    spotColor = Color.Black.copy(alpha = 0.20f)
                                )
                                .clip(actionSquircleShape)
                                .background(
                                    if (isReaderModeActive) effectiveAccent
                                    else Color(0xFF141414)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (isReaderMode) {
                                            onToggleReaderMode()
                                        } else {
                                            onFastAction()
                                            onReload()
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isReaderModeActive) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = "Sair do Modo Leitura",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            } else {
                                // Circular white emblem with Lightning Bolt
                                Box(
                                    modifier = Modifier
                                        .size(23.dp)
                                        .border(1.6.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = if (isReaderModeAvailable) "Modo Leitura" else "Ação Rápida / Recarregar",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. BOTTOM ELEMENT: EXACT 5 BUTTONS IN ORDER (Smoothly hides during editing)
            // [ 🕵 Incógnito | ⭐ Favoritos | 🔮 Esfera IA 3D | 🔲 Abas | ☰ Menu ]
            // =========================================================================
            AnimatedVisibility(
                visible = !isEditing,
                enter = fadeIn(tween(160)) + expandVertically(),
                exit = fadeOut(tween(140)) + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // 1. Incógnito Button (Spy hat & glasses line icon)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIncognito) effectiveAccent.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleIncognito()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_incognito),
                            contentDescription = "Navegação Anônima",
                            tint = if (isIncognito) effectiveAccent else contentColor,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // 2. Favoritos Button (Star icon)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleBookmark()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Favoritos",
                            tint = if (isBookmarked) Color(0xFFFFB300) else contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 3. Botão IA (Gorgeous 3D Iridescent Glowing Pearl Orb)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x6680D8FF),
                                spotColor = Color(0x99B388FF)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF80D8FF), // Vivid Soft Cyan
                                        Color(0xFF82B1FF), // Soft Sky Blue
                                        Color(0xFFB388FF), // Soft Lilac
                                        Color(0xFFEA80FC)  // Soft Rose Violet
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color.White.copy(alpha = 0.35f)
                                    )
                                ),
                                shape = CircleShape
                            )
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
                        // Soft specular glossy highlight on top of the orb
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                    }

                    // 4. Abas Button (Rounded square with border, number badge, and Space indicator)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                        val tabBadgeShape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(tabBadgeShape)
                                .border(
                                    width = 1.8.dp,
                                    color = if (currentSpaceColor != Color.Unspecified) {
                                        lerp(contentColor, currentSpaceColor, 0.5f)
                                    } else contentColor,
                                    shape = tabBadgeShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabCount.toString(),
                                color = contentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Space indicator dot at top-right
                        if (currentSpaceColor != Color.Unspecified) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(currentSpaceColor)
                                    .border(1.dp, omniBg, CircleShape)
                            )
                        }
                    }

                    // 5. Configurações Button (Hamburger menu icon ≡)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Configurações",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimalist floating dock for Tessera Browser Home screen.
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

        // 3. AI Action Button (3D Pearl Orb)
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x6680D8FF),
                    spotColor = Color(0x99B388FF)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF80D8FF),
                            Color(0xFF82B1FF),
                            Color(0xFFB388FF),
                            Color(0xFFEA80FC)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.35f)
                        )
                    ),
                    shape = CircleShape
                )
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
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.50f))
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
