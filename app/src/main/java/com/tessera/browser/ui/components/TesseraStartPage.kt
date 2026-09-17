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
import androidx.compose.material.icons.rounded.Pets
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    showCatInara: Boolean,
    isDarkMode: Boolean,
    favorites: List<SpeedDialItem>,
    searchSuggestions: List<String>,
    trendingTopics: List<String>,
    showWeatherWidget: Boolean = true,
    showQuotesWidget: Boolean = true,
    weatherData: WeatherData? = null,
    quotesData: QuotesData? = null,
    onRefreshWeather: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onOpenAi: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Request focus and show keyboard when expanded
    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
            searchQuery = ""
            onSearchQueryChange("")
        }
    }

    // Collapse search bar on back press
    BackHandler(enabled = isSearchExpanded) {
        isSearchExpanded = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic Wallpaper Background (Mediterranean Summer Villa or Custom/Gradient)
        if (showWallpaper) {
            if (!customWallpaperUri.isNullOrBlank()) {
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
        if (!isSearchExpanded && (showWeatherWidget || showQuotesWidget)) {
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
        if (!isSearchExpanded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CentralBrandHero(
                    accentColor = activeWallpaper.accentColor,
                    onClick = { isSearchExpanded = true }
                )
            }
        }

        // Easter Egg: Inara the Cat
        if (showCatInara && !isSearchExpanded) {
            InaraCatBadge(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 190.dp)
            )
        }

        // BARRA DE FAVORITOS NO RODAPÉ (Uso com uma mão, flutuando acima da barra inferior)
        if (!isSearchExpanded && favorites.isNotEmpty()) {
            BottomFavoritesBar(
                items = favorites,
                onItemClick = onOpenUrl,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 122.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        // Scrim when search is expanded to dismiss on tap outside
        if (isSearchExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isSearchExpanded = false
                    }
            )
        }

        // SEARCH BAR ANCHORED AT THE BOTTOM (ABOVE KEYBOARD, ONE-HANDED REACH)
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = fadeIn(tween(200)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Predictions (when typing) OR Trending Topics (when empty)
                if (searchQuery.isNotBlank() && searchSuggestions.isNotEmpty()) {
                    SearchSuggestionsCard(
                        suggestions = searchSuggestions,
                        onSelect = {
                            isSearchExpanded = false
                            onSearch(it)
                        },
                        onInsert = {
                            searchQuery = it
                            onSearchQueryChange(it)
                        },
                        accentColor = activeWallpaper.accentColor
                    )
                } else if (searchQuery.isBlank()) {
                    TrendingTopicsRow(
                        topics = trendingTopics,
                        onSelect = {
                            isSearchExpanded = false
                            onSearch(it)
                        },
                        accentColor = activeWallpaper.accentColor
                    )
                }

                // Encorpada Search Bar positioned comfortably at thumb height above keyboard
                EncorpadaSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        onSearchQueryChange(it)
                    },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            isSearchExpanded = false
                            onSearch(searchQuery.trim())
                        }
                    },
                    onAiClick = {
                        isSearchExpanded = false
                        onOpenAi(searchQuery.trim())
                    },
                    onCollapse = { isSearchExpanded = false },
                    accentColor = activeWallpaper.accentColor,
                    focusRequester = focusRequester
                )
            }
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
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(20.dp, shape = shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xEE221B17), Color(0xF815100E))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
                ),
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
                tint = Color(0xFF64B5F6),
                modifier = Modifier.size(18.dp)
            )

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .clickable { onItemClick(item.url) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    ShortcutIcon(item = item, size = 18.dp)
                    Text(
                        text = item.title,
                        color = Color.White.copy(alpha = 0.9f),
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
private fun TrendingTopicsRow(
    topics: List<String>,
    onSelect: (String) -> Unit,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xF51E1815))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Tendências de Pesquisa",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topics.forEach { topic ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .clickable { onSelect(topic.replace("🔥 ", "")) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = topic,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionsCard(
    suggestions: List<String>,
    onSelect: (String) -> Unit,
    onInsert: (String) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFA1E1815))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
            .padding(vertical = 6.dp)
    ) {
        suggestions.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(suggestion) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = suggestion,
                        color = Color.White.copy(alpha = 0.92f),
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
        }
    }
}

@Composable
private fun EncorpadaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onAiClick: () -> Unit,
    onCollapse: () -> Unit,
    accentColor: Color,
    focusRequester: FocusRequester
) {
    val barShape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 32.dp,
                shape = barShape,
                ambientColor = Color.Black,
                spotColor = accentColor.copy(alpha = 0.4f)
            )
            .clip(barShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF8241D19), Color(0xFA15100E))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = barShape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Pesquisar",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Input field (Encorpado, 15.5sp)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Pesquisar ou digitar endereço...",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(accentColor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }

            // Embedded Free AI Button (DuckDuckGo AI)
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.22f),
                                Color(0xFF7C4DFF).copy(alpha = 0.35f)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Color(0xFF00E5FF).copy(alpha = 0.55f),
                        RoundedCornerShape(19.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAiClick
                    )
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "IA Gratuita",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "IA",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Clear or Submit Actions
            if (query.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpar",
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable(onClick = onSearch),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Ir",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Collapse button when empty
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onCollapse),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
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

@Composable
private fun InaraCatBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33CCA882))
            .border(1.dp, Color(0x66CCA882), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
