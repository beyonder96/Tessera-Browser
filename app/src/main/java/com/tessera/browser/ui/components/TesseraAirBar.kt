package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TesseraAirBar(
    progress: Float,
    displayUrl: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onReload: () -> Unit,
    onSearch: (String) -> Unit,
    onOpenAi: (String) -> Unit,
    onOpenSettings: () -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    accentColor: Color = Color(0xFF64B5F6),
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf(displayUrl) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(displayUrl) {
        queryText = displayUrl
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "airbar_progress"
    )

    Box(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. MINIMIZED STATE: Floating Magnifying Glass ("Lupa")
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(tween(200)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black,
                        spotColor = accentColor.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xEE2A201C), Color(0xFB16110F))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.4f),
                                accentColor.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onExpandedChange(true) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Loading spinner if page is loading
                if (progress > 0f && progress < 1f) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(52.dp),
                        color = accentColor,
                        trackColor = Color.Transparent,
                        strokeWidth = 2.5.dp
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Expandir barra de navegação",
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // 2. EXPANDED STATE: Encorpada Navigation Bar
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250)) + scaleIn(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            val shape = RoundedCornerShape(32.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 28.dp,
                        shape = shape,
                        ambientColor = Color.Black,
                        spotColor = accentColor.copy(alpha = 0.35f)
                    )
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xF8241D19), Color(0xFA15100E))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ),
                        shape = shape
                    )
            ) {
                // Linear progress indicator on top edge
                AnimatedVisibility(
                    visible = progress > 0f && progress < 1f,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(250)),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = accentColor,
                        trackColor = Color.Transparent
                    )
                }

                // Main Controls Row (Height ~60dp, Encorpada)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Back button
                    AirActionIcon(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        description = "Voltar",
                        enabled = canGoBack,
                        onClick = onBack
                    )

                    // Home button
                    AirActionIcon(
                        icon = Icons.Rounded.Home,
                        description = "Início",
                        enabled = true,
                        onClick = {
                            onExpandedChange(false)
                            onHome()
                        }
                    )

                    // Search / URL Input Field (Encorpado)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (queryText.isEmpty()) {
                            Text(
                                text = "Pesquisar ou digitar endereço...",
                                color = Color.White.copy(alpha = 0.42f),
                                fontSize = 13.5.sp
                            )
                        }

                        BasicTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(accentColor),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (queryText.isNotBlank()) {
                                        onSearch(queryText.trim())
                                        focusManager.clearFocus()
                                        onExpandedChange(false)
                                    }
                                }
                            )
                        )
                    }

                    // Free AI Button inside the expanded bar
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.2f),
                                        Color(0xFF7C4DFF).copy(alpha = 0.32f)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(19.dp))
                            .clickable {
                                onOpenAi(queryText)
                                onExpandedChange(false)
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Consultar IA Gratuita",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "IA",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Reload button
                    AirActionIcon(
                        icon = Icons.Rounded.Refresh,
                        description = "Recarregar",
                        enabled = true,
                        onClick = onReload
                    )

                    // Settings button
                    AirActionIcon(
                        icon = Icons.Rounded.Tune,
                        description = "Configuração fácil",
                        enabled = true,
                        onClick = onOpenSettings
                    )

                    // Minimize / Collapse button
                    AirActionIcon(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        description = "Minimizar para lupa",
                        enabled = true,
                        onClick = { onExpandedChange(false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AirActionIcon(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.06f else 0.02f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White.copy(alpha = if (enabled) 0.9f else 0.25f),
            modifier = Modifier.size(19.dp)
        )
    }
}
