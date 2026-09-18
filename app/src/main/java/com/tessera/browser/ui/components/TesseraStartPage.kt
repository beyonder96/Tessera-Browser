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
import androidx.compose.foundation.rememberScrollState
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

        // Minimalist Home Widgets (Weather & Quotes) - Floating cleanly without wrapping box
        if (showWeatherWidget || showQuotesWidget) {
            HomeWidgetsContainer(
                showWeather = showWeatherWidget,
                showQuotes = showQuotesWidget,
                weatherData = weatherData,
                quotesData = quotesData,
                isDarkMode = isDarkMode,
                accentColor = activeWallpaper.accentColor,
                onRefreshWeather = onRefreshWeather,
                onQuoteClick = { quote ->
                    onSearch("cotação ${quote.name.lowercase()} hoje")
                },
                onWeatherClick = {
                    val city = weatherData?.cityName ?: "São Paulo"
                    onSearch("previsão do tempo $city")
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 16.dp)
            )
        }

        // Center Hero: Modern Uppercase Typography "TESSERA"
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CentralBrandHero(
                accentColor = activeWallpaper.accentColor,
                onClick = onSearchClick
            )
        }

        // BARRA DE FAVORITOS NO RODAPÉ (Uso com uma mão, flutuando acima da barra inferior)
        if (favorites.isNotEmpty()) {
            BottomFavoritesBar(
                items = favorites,
                isDarkMode = isDarkMode,
                onItemClick = onOpenUrl,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 122.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun CentralBrandHero(
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TESSERA",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                    blurRadius = 14f
                )
            )
        )
    }
}

@Composable
private fun BottomFavoritesBar(
    items: List<SpeedDialItem>,
    isDarkMode: Boolean,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    val scrollState = rememberScrollState()

    val containerBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xEE221B17), Color(0xF815100E))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF7F7FA))
        )
    }

    val containerBorder = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.04f))
        )
    }

    val itemBg = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0x0C000000)
    val itemTextColor = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1C1C1E)
    val bookmarkTint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF0078D4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 20.dp else 12.dp,
                shape = shape,
                spotColor = if (isDarkMode) Color.Black else Color(0x30000000)
            )
            .clip(shape)
            .background(containerBg)
            .border(
                width = 1.dp,
                brush = containerBorder,
                shape = shape
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bookmark,
                contentDescription = "Favoritos",
                tint = bookmarkTint,
                modifier = Modifier.size(18.dp)
            )

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(itemBg)
                        .clickable { onItemClick(item.url) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    ShortcutIcon(item = item, size = 18.dp)
                    Text(
                        text = item.title,
                        color = itemTextColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
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
