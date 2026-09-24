package com.tessera.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

@Composable
fun TesseraStartPage(
    activeWallpaper: WallpaperTheme,
    showWallpaper: Boolean,
    customWallpaperUri: String? = null,
    isDarkMode: Boolean,
    favorites: List<SpeedDialItem>,
    searchSuggestions: List<String> = emptyList(),
    trendingTopics: List<String> = emptyList(),
    showWeatherWidget: Boolean = true,
    showQuotesWidget: Boolean = true,
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic Wallpaper Background (Mediterranean Summer Villa or Custom/Gradient) with gentle blur
        if (showWallpaper) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                    }
                    .blur(radius = 10.dp)
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

            // Ambient lighting vignette layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x44000000),
                                Color(0x15000000),
                                Color(0x25000000),
                                Color(0x77000000)
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

        // 1. Top Header Row: Brand wordmark on left + Theme & Colors Pill on right
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Wordmark
            Text(
                text = "TESSERA",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.65f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSearchClick
                    )
            )

            // Theme & Colors Pill
            val pillShape = RoundedCornerShape(20.dp)
            val pillBg = if (isDarkMode) Color.Black.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.85f)
            val pillBorder = if (isDarkMode) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)
            val textColor = if (isDarkMode) Color.White.copy(alpha = 0.92f) else Color(0xFF1E1E1E)

            Row(
                modifier = Modifier
                    .clip(pillShape)
                    .background(pillBg)
                    .border(1.dp, pillBorder, pillShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenSettings
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Tema & Configurações",
                    tint = activeWallpaper.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Tema & Cores",
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Central Bento Grid Container (Smoothly scrollable)
        val bentoScrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(top = 56.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(bentoScrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Bento Row 1: Dual Cards (Weather & Quotes)
                if (showWeatherWidget && weatherData != null && showQuotesWidget && quotesData != null && quotesData.items.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeatherBentoCard(
                            data = weatherData,
                            isDarkMode = isDarkMode,
                            accentColor = activeWallpaper.accentColor,
                            onRefresh = onRefreshWeather,
                            onClick = {
                                val city = weatherData.cityName
                                onSearch("previsão do tempo $city")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuotesBentoCard(
                            data = quotesData,
                            isDarkMode = isDarkMode,
                            accentColor = activeWallpaper.accentColor,
                            onQuoteClick = { quote ->
                                onSearch("cotação ${quote.name.lowercase()} hoje")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (showWeatherWidget && weatherData != null) {
                    WeatherBentoCard(
                        data = weatherData,
                        isDarkMode = isDarkMode,
                        accentColor = activeWallpaper.accentColor,
                        onRefresh = onRefreshWeather,
                        onClick = {
                            val city = weatherData.cityName
                            onSearch("previsão do tempo $city")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (showQuotesWidget && quotesData != null && quotesData.items.isNotEmpty()) {
                    QuotesBentoCard(
                        data = quotesData,
                        isDarkMode = isDarkMode,
                        accentColor = activeWallpaper.accentColor,
                        onQuoteClick = { quote ->
                            onSearch("cotação ${quote.name.lowercase()} hoje")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bento Row 2: Favorites & Quick Shortcuts
                if (favorites.isNotEmpty()) {
                    FavoritesBentoCard(
                        items = favorites,
                        isDarkMode = isDarkMode,
                        accentColor = activeWallpaper.accentColor,
                        onItemClick = onOpenUrl,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bento Row 3: Privacy & Security Shield Pill
                PrivacyShieldBentoCard(
                    isDarkMode = isDarkMode,
                    accentColor = activeWallpaper.accentColor,
                    totalBlockedCount = totalBlockedCount,
                    onClick = onOpenPrivacyDashboard,
                    modifier = Modifier.fillMaxWidth()
                )

                // Bottom padding to clear floating AirBar
                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

/**
 * Bento Box tile displaying Favorites / Speed Dial in a clean modern grid.
 */
@Composable
fun FavoritesBentoCard(
    items: List<SpeedDialItem>,
    isDarkMode: Boolean,
    accentColor: Color,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bentoShape = RoundedCornerShape(24.dp)
    val cardBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xCC201A18), Color(0xDD161210))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xEEFFFFFF), Color(0xF2F5F7FA))
        )
    }
    val cardBorder = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.05f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.04f))
        )
    }
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6E6E73)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 12.dp else 4.dp,
                shape = bentoShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(bentoShape)
            .background(cardBg)
            .border(1.dp, cardBorder, bentoShape)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Favoritos Rápidos",
                    color = mutedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val chunkedItems = items.chunked(4)
            chunkedItems.forEachIndexed { rowIndex, rowItems ->
                if (rowIndex > 0) Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Top
                ) {
                    rowItems.forEach { item ->
                        BentoShortcutItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = { onItemClick(item.url) }
                        )
                    }
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BentoShortcutItem(
    item: SpeedDialItem,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val squircleShape = RoundedCornerShape(16.dp)
    val tileBg = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0x0A000000)
    val tileBorder = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.90f) else Color(0xFF1E1E1E)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(squircleShape)
                .background(tileBg)
                .border(1.dp, tileBorder, squircleShape),
            contentAlignment = Alignment.Center
        ) {
            ShortcutIcon(item = item, size = 26.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.title,
            color = textColor,
            fontSize = 11.sp,
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
