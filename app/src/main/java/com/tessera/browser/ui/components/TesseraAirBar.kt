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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.graphics.graphicsLayer
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TesseraAirBar(
    progress: Float,
    displayUrl: String,
    canGoBack: Boolean,
    canGoForward: Boolean = false,
    tabCount: Int,
    isBookmarked: Boolean,
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
    onToggleBookmark: () -> Unit,
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

    // Dynamic Tinted Glass - Smooth transition to site brand color
    val animatedTintColor by animateColorAsState(
        targetValue = siteThemeColor ?: Color.Transparent,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "airbar_tint"
    )

    // Infinite rotating shimmer for Aurora AI Orb
    val infiniteTransition = rememberInfiniteTransition(label = "airbar_ai_infinite")
    val aiGlowAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_glow_angle"
    )

    val baseDarkOmni = Color(0xE0282422)
    val baseLightOmni = Color(0xEEFFFFFF)

    val omniBg = if (isDarkMode) {
        if (animatedTintColor != Color.Transparent) {
            lerp(baseDarkOmni, animatedTintColor.copy(alpha = 0.94f), 0.22f)
        } else {
            baseDarkOmni
        }
    } else {
        if (animatedTintColor != Color.Transparent) {
            lerp(baseLightOmni, animatedTintColor.copy(alpha = 0.96f), 0.14f)
        } else {
            baseLightOmni
        }
    }

    val dockBg = if (isDarkMode) {
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

    val effectiveAccent = if (animatedTintColor != Color.Transparent) {
        animatedTintColor
    } else {
        accentColor
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
        SolidColor(
            if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.85f)
        )
    }

    val contentColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.35f) else Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(dockBg)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    onBrowseForMe = {
                        isEditing = false
                        onBrowseForMe(it)
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
            if (progress > 0f && progress < 1f) {
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

            // 1. TOP ELEMENT: CAPSULE SEARCH BAR (Omnibar)
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
                        enabled = !isEditing,
                        onDragStopped = { velocity ->
                            val threshold = 70f
                            if (dragAccumulator < -threshold || velocity < -350f) {
                                onNextTab()
                            } else if (dragAccumulator > threshold || velocity > 350f) {
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
                                .size(34.dp)
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
                                cursorBrush = SolidColor(accentColor),
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

                        // Right: Actions (Clear, AI, Go)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
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
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
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
                    } else {
                        // Left: Back < and Forward > Chevrons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canGoBack, onClick = onBack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                    contentDescription = "Voltar",
                                    tint = if (canGoBack) contentColor else mutedColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = canGoForward, onClick = onForward),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Avançar",
                                    tint = if (canGoForward) contentColor else mutedColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Center: Display host or placeholder with Security / Lock icon
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
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .clickable { onOpenSiteSettings() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSecure) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                        contentDescription = "Configurações do site",
                                        tint = if (isSecure) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(3.dp))

                                // Privacy Shield Badge / Live Counter
                                if (privacyBlockedCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                            .border(0.5.dp, Color(0xFF00E676).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                            .clickable { onOpenPrivacyDashboard() }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Shield,
                                                contentDescription = "Escudo de Privacidade",
                                                tint = Color(0xFF00E676),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "$privacyBlockedCount",
                                                color = Color(0xFF00E676),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .clickable { onOpenPrivacyDashboard() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shield,
                                            contentDescription = "Painel de Privacidade",
                                            tint = mutedColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(3.dp))
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
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Right: Reader mode button whenever viewing a web page
                        if (!isHomePage || isReaderModeAvailable || isReaderModeActive) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isReaderModeActive -> accentColor
                                            isReaderModeAvailable -> accentColor.copy(alpha = 0.25f)
                                            else -> Color(0xFF222222)
                                        }
                                    )
                                    .clickable {
                                        onToggleReaderMode()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = if (isReaderModeActive) "Sair do Modo Leitura" else "Ativar Modo Leitura",
                                    tint = when {
                                        isReaderModeActive -> Color.White
                                        isReaderModeAvailable -> accentColor
                                        else -> Color(0xFFCCCCCC)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(34.dp))
                        }
                    }
                }
            }

            // 2. BOTTOM ELEMENT: EXACT 5 BUTTONS IN ORDER (Smoothly hides during editing)
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

                    // 3. Botão IA (Gorgeous 3D Iridescent Glowing Pearl Orb with Aurora Glow)
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
                            .graphicsLayer {
                                rotationZ = aiGlowAngle
                            }
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF80D8FF), // Vivid Soft Cyan
                                        Color(0xFF82B1FF), // Soft Sky Blue
                                        Color(0xFFB388FF), // Soft Lilac
                                        Color(0xFFEA80FC), // Soft Rose Violet
                                        Color(0xFFFF80AB), // Soft Coral Pink
                                        Color(0xFF80D8FF)  // Back to Cyan
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
                        // Inner container to keep icon & specular highlight oriented upright
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationZ = -aiGlowAngle },
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
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Tessera AI",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
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

@Composable
private fun SearchSuggestionsFloatingCard(
    queryText: String,
    suggestions: List<String>,
    onSelect: (String) -> Unit,
    onBrowseForMe: (String) -> Unit,
    onInsert: (String) -> Unit,
    accentColor: Color,
    isDarkMode: Boolean
) {
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
        // TOP BROWSE FOR ME ACTION ITEM (Arc Search Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBrowseForMe(queryText) }
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = if (isDarkMode) 0.18f else 0.10f),
                            Color(0xFF7C4DFF).copy(alpha = if (isDarkMode) 0.14f else 0.08f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Navegue por Mim",
                        color = if (isDarkMode) Color.White else Color(0xFF1E1E24),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF7C4DFF).copy(alpha = 0.22f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "IA ✨",
                            color = Color(0xFF7C4DFF),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Sintetizar: \"$queryText\"",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B7280),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Navegar com IA",
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        if (suggestions.isNotEmpty()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
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

