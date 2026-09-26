package com.tessera.browser.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.data.WallpaperTheme
import com.tessera.browser.viewmodel.QuotesData
import com.tessera.browser.viewmodel.WeatherData

/**
 * Tessera Start Page — Minimalist, elegant, breathable Home screen.
 * Inspired by Opera & Dia Browser aesthetics:
 * - Top header with active Arc Space indicator & Menu button.
 * - Clean central wordmark & compact Speed Dial shortcuts.
 * - Breathing whitespace, no cluttered vertical bento towers.
 */
@Composable
fun TesseraStartPage(
    activeWallpaper: WallpaperTheme,
    showWallpaper: Boolean,
    customWallpaperUri: String? = null,
    isDarkMode: Boolean,
    favorites: List<SpeedDialItem>,
    searchSuggestions: List<String> = emptyList(),
    trendingTopics: List<String> = emptyList(),
    digitalMinimalismMode: Boolean = true,
    showWeatherWidget: Boolean = false,
    showQuotesWidget: Boolean = false,
    weatherData: WeatherData? = null,
    quotesData: QuotesData? = null,
    onRefreshWeather: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onOpenAi: (String) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    totalBlockedCount: Int = 0,
    onOpenPrivacyDashboard: () -> Unit = {},
    currentSpaceEmoji: String = "🌐",
    currentSpaceName: String = "Geral",
    currentSpaceColor: Color = Color(0xFF0288D1),
    onOpenSpaces: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Dynamic Wallpaper Background or Solid Dark/Light Canvas
        if (showWallpaper) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                    }
                    .blur(radius = 14.dp)
            ) {
                if (activeWallpaper.id == "custom" && !customWallpaperUri.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = customWallpaperUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (activeWallpaper.drawableRes != null) {
                    Image(
                        painter = painterResource(activeWallpaper.drawableRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = activeWallpaper.gradientColors,
                                    radius = 1800f
                                )
                            )
                    )
                }
            }

            // Ambient lighting vignette layer for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x35000000),
                                Color(0x15000000),
                                Color(0x25000000),
                                Color(0x65000000)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) Color(0xFF120E0D) else Color(0xFFF7F8FA))
            )
        }

        // 2. Top Header: Space Switcher Pill (Left) & Menu/Settings Button (Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Space Switcher Pill (Arc Spaces)
            val spacePillShape = RoundedCornerShape(20.dp)
            val pillBg = if (isDarkMode) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.75f)
            val pillBorder = if (isDarkMode) currentSpaceColor.copy(alpha = 0.50f) else currentSpaceColor.copy(alpha = 0.35f)

            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = spacePillShape,
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(spacePillShape)
                    .background(pillBg)
                    .border(1.dp, pillBorder, spacePillShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenSpaces
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = currentSpaceEmoji, fontSize = 13.sp)
                Text(
                    text = currentSpaceName,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.size(15.dp)
                )
            }

            // Menu / QuickSettings Button (Right)
            val menuBtnShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = menuBtnShape,
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(menuBtnShape)
                    .background(pillBg)
                    .border(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f), menuBtnShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenSettings
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Configurações e Menu",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF1E1E1E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3. Central Clean Container (Wordmark & Speed Dial Shortcuts)
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(top = 90.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // Elegant Minimal Wordmark "TESSERA"
                Text(
                    text = "TESSERA",
                    color = if (showWallpaper || isDarkMode) Color.White else Color(0xFF1E1E1E),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.35f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                            blurRadius = 8f
                        )
                    )
                )

                // Opera / Dia Browser Style Speed Dial (Clean icon tiles, no bloated borders)
                if (favorites.isNotEmpty()) {
                    OperaSpeedDialGrid(
                        items = favorites,
                        isDarkMode = isDarkMode,
                        onItemClick = onOpenUrl,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Breathing room to clear bottom floating dock
                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
}

/**
 * Opera / Dia Browser style Speed Dial grid.
 * Displays shortcuts in a clean, floating, squircle grid with crisp favicons and short labels.
 */
@Composable
fun OperaSpeedDialGrid(
    items: List<SpeedDialItem>,
    isDarkMode: Boolean,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chunkedItems = items.take(8).chunked(4)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                rowItems.forEach { item ->
                    OperaShortcutTile(
                        item = item,
                        isDarkMode = isDarkMode,
                        onClick = { onItemClick(item.url) }
                    )
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.width(68.dp))
                }
            }
        }
    }
}

@Composable
fun OperaShortcutTile(
    item: SpeedDialItem,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val squircleShape = RoundedCornerShape(18.dp)
    val tileBg = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.85f)
    val tileBorder = if (isDarkMode) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.90f) else Color(0xFF1E1E1E)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = squircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(squircleShape)
                .background(tileBg)
                .border(1.dp, tileBorder, squircleShape),
            contentAlignment = Alignment.Center
        ) {
            ShortcutIcon(item = item, size = 26.dp)
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = item.title,
            color = textColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShortcutIcon(
    item: SpeedDialItem,
    size: Dp = 18.dp
) {
    if (item.iconRes != null) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.title,
            modifier = Modifier.size(size)
        )
    } else if (!item.iconUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = item.iconUrl,
            contentDescription = item.title,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            loading = {
                InitialBadge(
                    initial = item.initial,
                    badgeColor = Color(item.badgeColor),
                    size = size
                )
            },
            error = {
                InitialBadge(
                    initial = item.initial,
                    badgeColor = Color(item.badgeColor),
                    size = size
                )
            }
        )
    } else {
        InitialBadge(
            initial = item.initial,
            badgeColor = Color(item.badgeColor),
            size = size
        )
    }
}

@Composable
private fun InitialBadge(
    initial: String?,
    badgeColor: Color,
    size: Dp = 18.dp
) {
    val letter = initial?.firstOrNull()?.uppercase() ?: "•"
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
