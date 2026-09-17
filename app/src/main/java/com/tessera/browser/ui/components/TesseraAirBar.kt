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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import com.tessera.browser.data.SpeedDialItem

@Composable
fun TesseraAirBar(
    progress: Float,
    displayUrl: String,
    canGoBack: Boolean,
    tabCount: Int,
    isBookmarked: Boolean,
    favorites: List<SpeedDialItem>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onReload: () -> Unit,
    onSearch: (String) -> Unit,
    onOpenAiAction: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorite: (String) -> Unit,
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
        modifier = modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. MINIMIZED STATE: Floating Lupa Button + Quick Favorites Dock (Ergonomic for one hand)
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(tween(200)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150))
        ) {
            val pillShape = RoundedCornerShape(28.dp)
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 20.dp,
                        shape = pillShape,
                        ambientColor = Color.Black,
                        spotColor = accentColor.copy(alpha = 0.4f)
                    )
                    .clip(pillShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xEE251E1A), Color(0xFB16110F))
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.35f),
                                accentColor.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ),
                        shape = pillShape
                    )
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Floating Lupa Icon Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xEE2A201C), Color(0xFB16110F))
                            )
                        )
                        .border(1.dp, accentColor.copy(alpha = 0.55f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onExpandedChange(true) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (progress > 0f && progress < 1f) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(40.dp),
                            color = accentColor,
                            trackColor = Color.Transparent,
                            strokeWidth = 2.dp
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Expandir barra de navegação",
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Horizontal Favorites in Footer
                if (favorites.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        favorites.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onOpenFavorite(item.url) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. EXPANDED STATE: Encorpada Navigation Bar with Tabs, Bookmarks and AI Action
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250)) + scaleIn(spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(180)) + scaleOut(tween(180))
        ) {
            val shape = RoundedCornerShape(30.dp)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Favorites bar in web browsing mode (thumb-friendly above navigation bar)
                if (favorites.isNotEmpty()) {
                    AirFavoritesRow(
                        items = favorites,
                        onItemClick = {
                            onExpandedChange(false)
                            onOpenFavorite(it)
                        }
                    )
                }

                // Main navigation bar
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
                    if (progress > 0f && progress < 1f) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (queryText.isEmpty()) {
                                Text(
                                    text = "Pesquisar ou endereço...",
                                    color = Color.White.copy(alpha = 0.42f),
                                    fontSize = 13.sp
                                )
                            }

                            BasicTextField(
                                value = queryText,
                                onValueChange = { queryText = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.5.sp,
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

                        // Bookmark Star button (⭐)
                        AirActionIcon(
                            icon = if (isBookmarked) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            description = if (isBookmarked) "Remover favorito" else "Favoritar página",
                            enabled = true,
                            tint = if (isBookmarked) Color(0xFFFFCA28) else Color.White.copy(alpha = 0.85f),
                            onClick = onToggleBookmark
                        )

                        // Multi-tabs button (e.g. [ 2 ])
                        TabCounterBadge(
                            count = tabCount,
                            accentColor = accentColor,
                            onClick = onOpenTabs
                        )

                        // Quick AI Action Button (sparkle)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF00E5FF).copy(alpha = 0.25f),
                                            Color(0xFF7C4DFF).copy(alpha = 0.35f)
                                        )
                                    )
                                )
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), CircleShape)
                                .clickable(onClick = onOpenAiAction),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Ações Rápidas de IA",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Settings / Menu button
                        AirActionIcon(
                            icon = Icons.Rounded.Tune,
                            description = "Configuração fácil",
                            enabled = true,
                            onClick = onOpenSettings
                        )

                        // Minimize button
                        AirActionIcon(
                            icon = Icons.Rounded.KeyboardArrowDown,
                            description = "Minimizar",
                            enabled = true,
                            onClick = { onExpandedChange(false) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AirFavoritesRow(
    items: List<SpeedDialItem>,
    onItemClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xEE1A1513))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .clickable { onItemClick(item.url) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TabCounterBadge(
    count: Int,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.2.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AirActionIcon(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    tint: Color = Color.White.copy(alpha = if (enabled) 0.9f else 0.25f),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.06f else 0.02f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}
