package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import androidx.compose.ui.platform.LocalFocusManager
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
    isReaderModeActive: Boolean = false,
    isReaderModeAvailable: Boolean = false,
    favorites: List<SpeedDialItem> = emptyList(),
    onBack: () -> Unit,
    onForward: () -> Unit = {},
    onHome: () -> Unit = {},
    onReload: () -> Unit = {},
    onSearch: (String) -> Unit,
    onOpenAiAction: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleIncognito: () -> Unit = {},
    onToggleReaderMode: () -> Unit = {},
    onOpenTabs: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenFavorite: (String) -> Unit = {},
    onFastAction: () -> Unit = {},
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    accentColor: Color = Color(0xFF0288D1),
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf(displayUrl) }
    var isEditing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(displayUrl) {
        queryText = displayUrl
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "airbar_progress"
    )

    val omniBg = if (isDarkMode) Color(0xE0282422) else Color(0xEEFFFFFF)
    val dockBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0x00120E0D), Color(0xAA120E0D), Color(0xEE120E0D))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0x00FFFFFF), Color(0xCCFFFFFF), Color(0xFAFFFFFF))
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
            // Linear progress indicator when loading
            if (progress > 0f && progress < 1f) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = accentColor,
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
                        color = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.85f),
                        shape = omniShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isEditing = true
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                    // Center: Address / Search Text Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isEditing) {
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
                        } else {
                            BasicTextField(
                                value = queryText,
                                onValueChange = { queryText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = contentColor,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
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
                    }

                    // Right: Close (if editing) OR Black Circle with Electric Bolt Icon ⚡
                    if (isEditing && queryText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    queryText = ""
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
                    } else {
                        // Quick Action Button: Switches to Reader Mode if available/active, else Action / Reload (Matches Image 1 & 2)
                        val isReaderMode = isReaderModeAvailable || isReaderModeActive
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    if (isReaderModeActive) accentColor
                                    else if (isReaderModeAvailable) Color(0xFF222222)
                                    else Color(0xFF141414)
                                )
                                .clickable {
                                    if (isEditing) {
                                        isEditing = false
                                        if (queryText.isNotBlank()) onSearch(queryText.trim())
                                    } else if (isReaderMode) {
                                        onToggleReaderMode()
                                    } else {
                                        onFastAction()
                                        onReload()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isReaderMode) Icons.AutoMirrored.Rounded.MenuBook else Icons.Rounded.Bolt,
                                contentDescription = if (isReaderMode) "Modo Leitura" else "Ação Rápida / Recarregar",
                                tint = if (isReaderModeActive) Color.White else if (isReaderModeAvailable) accentColor else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. BOTTOM ELEMENT: EXACT 5 BUTTONS IN ORDER
            // [ Incógnito | Favoritos | Botão IA (Orb) | Abas (Badge) | Configurações ]
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
                            if (isIncognito) accentColor.copy(alpha = 0.15f) else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleIncognito
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_incognito),
                        contentDescription = "Navegação Anônima",
                        tint = if (isIncognito) accentColor else contentColor,
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
                            onClick = onToggleBookmark
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
                            onClick = onOpenAiAction
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

                // 4. Abas Button (Rounded square with border and number badge)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenTabs
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
                                color = contentColor,
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
                }

                // 5. Configurações Button (Hamburger menu icon ≡)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenSettings
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

