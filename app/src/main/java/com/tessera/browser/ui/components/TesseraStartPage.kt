package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.data.WallpaperTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TesseraStartPage(
    speedDialItems: List<SpeedDialItem>,
    activeWallpaper: WallpaperTheme,
    showWallpaper: Boolean,
    showFavoritesBar: Boolean,
    showCatInara: Boolean,
    showAiButton: Boolean,
    onSearch: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onAddShortcut: (String, String) -> Unit,
    onRemoveShortcut: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    if (showAddDialog) {
        AddShortcutDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, url ->
                onAddShortcut(title, url)
                showAddDialog = false
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic Wallpaper Background
        if (showWallpaper) {
            if (activeWallpaper.drawableRes != null) {
                Image(
                    painter = painterResource(id = activeWallpaper.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(activeWallpaper.gradientColors))
                )
            }

            // Ambient dark chocolate vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99100D0B),
                                Color(0x33140F0D),
                                Color(0xDD0D0A08)
                            )
                        )
                    )
            )
        } else {
            // Pure dark background when wallpaper is toggled off
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF120E0D))
            )
        }

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            StartPageTopBar(
                showAiButton = showAiButton,
                onOpenSettings = onOpenSettings,
                onOpenAi = onOpenAi
            )

            // Optional Favorites Bar (configured via Quick Settings)
            AnimatedVisibility(
                visible = showFavoritesBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FavoritesBar(
                    items = speedDialItems.take(4),
                    onOpenUrl = onOpenUrl,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Central Search Pill (Opera style "Pesquisar na Web")
            CentralSearchPill(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        focusManager.clearFocus()
                        onSearch(searchQuery.trim())
                    }
                },
                onAddShortcut = { showAddDialog = true }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Speed Dial Grid ("Discagem Rápida")
            SpeedDialSection(
                items = speedDialItems,
                onItemClick = onOpenUrl,
                onAddClick = { showAddDialog = true }
            )

            // Easter Egg: Inara the Cat (Configurações do gato)
            if (showCatInara) {
                Spacer(modifier = Modifier.height(28.dp))
                InaraCatBadge()
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StartPageTopBar(
    showAiButton: Boolean,
    onOpenSettings: () -> Unit,
    onOpenAi: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Monogram Logo + "Discagem Rápida" tab pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tessera monogram logo ring
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color(0xFF26A69A), Color(0xFF64B5F6), Color(0xFF26A69A))
                        )
                    )
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14100E)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF26A69A))
                )
            }

            // Tab pill "Discagem Rápida"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Discagem Rápida",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right: Settings + AI triggers
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tessera AI Action Button (can be toggled in Quick Settings)
            if (showAiButton) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable(onClick = onOpenAi),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Tessera AI",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Tune / Settings Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Configuração fácil",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoritesBar(
    items: List<SpeedDialItem>,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Bookmark,
            contentDescription = null,
            tint = Color(0xFF64B5F6),
            modifier = Modifier.size(16.dp)
        )
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onOpenUrl(item.url) }
            ) {
                Text(
                    text = item.iconEmoji ?: item.initial ?: "•",
                    fontSize = 12.sp
                )
                Text(
                    text = item.title,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CentralSearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onAddShortcut: () -> Unit
) {
    val pillShape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, shape = pillShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(pillShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC231B17), Color(0xAA181310))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = pillShape
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search icon badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Pesquisar",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Search input field
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Pesquisar na Web",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(Color(0xFF64B5F6)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }

            // Add (+) button inside search pill
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAddShortcut),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Adicionar atalho",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeedDialSection(
    items: List<SpeedDialItem>,
    onItemClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        maxItemsInEachRow = 4
    ) {
        items.forEach { item ->
            SpeedDialTile(
                title = item.title,
                iconEmoji = item.iconEmoji,
                initial = item.initial,
                badgeColor = Color(item.badgeColor),
                onClick = { onItemClick(item.url) }
            )
        }

        // Add shortcut button tile
        AddShortcutTile(onClick = onAddClick)
    }
}

@Composable
private fun SpeedDialTile(
    title: String,
    iconEmoji: String?,
    initial: String?,
    badgeColor: Color,
    onClick: () -> Unit
) {
    val tileShape = RoundedCornerShape(18.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(12.dp, shape = tileShape)
                .clip(tileShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xD9251E1A), Color(0xB3181310))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = tileShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (iconEmoji != null) {
                Text(
                    text = iconEmoji,
                    fontSize = 24.sp
                )
            } else if (initial != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddShortcutTile(onClick: () -> Unit) {
    val tileShape = RoundedCornerShape(18.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(tileShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.04f))
                    ),
                    shape = tileShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Adicionar atalho",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Adicionar",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InaraCatBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33CCA882))
            .border(1.dp, Color(0x66CCA882), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Pets,
            contentDescription = null,
            tint = Color(0xFFCCA882),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Inara: Ronronando e cuidando do seu browser 🐾",
            color = Color(0xFFCCA882),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
