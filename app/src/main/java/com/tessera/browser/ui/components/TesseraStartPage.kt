package com.tessera.browser.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.tessera.browser.R
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
import com.tessera.browser.ui.theme.ApplePalette
import com.tessera.browser.ui.theme.AppleShapes
import com.tessera.browser.viewmodel.QuotesData
import com.tessera.browser.viewmodel.WeatherData
import java.util.Calendar

/**
 * Tessera Start Page — Apple Safari iOS 18 Edition.
 *
 * Uma experiência arejada, sofisticada e minimalista no padrão Apple Human Interface Guidelines:
 * - Cabeçalho translúcido com seletor de Espaço em cápsula de vidro e menu de ações.
 * - Saudação e wordmark "TESSERA" com tipografia refinada Cupertino.
 * - Grade tátil de Favoritos Safari em squircles contínuos com microinteração de compressão física.
 * - Cartões compactos estilo iOS WidgetKit (Clima, Relatório de Privacidade e Citação inspiradora).
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
    currentSpaceColor: Color = ApplePalette.BlueDark,
    onOpenSpaces: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDefaultDismissed by rememberSaveable { mutableStateOf(false) }
    var isDefaultBrowser by remember(context) {
        mutableStateOf(com.tessera.browser.util.DefaultBrowserHelper.isDefaultBrowser(context))
    }

    val defaultBrowserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultBrowser = com.tessera.browser.util.DefaultBrowserHelper.isDefaultBrowser(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. FUNDO SÓLIDO (OLED PURO NO ESCURO / BRANCO MATTE NO CLARO)
        val backgroundColor = if (isDarkMode) ApplePalette.DarkBackground else ApplePalette.LightBackground
        val contentColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
        val mutedColor = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
        val surfaceColor = if (isDarkMode) ApplePalette.DarkSurface else ApplePalette.LightSurface
        val borderColor = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                )
        )

        // BANNER DE NAVEGADOR PADRÃO (DISMISSIBLE PILL)
        if (!isDefaultBrowser && !isDefaultDismissed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.12f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .clickable {
                            try {
                                val intent = com.tessera.browser.util.DefaultBrowserHelper.createRequestDefaultBrowserIntent(context)
                                defaultBrowserLauncher.launch(intent)
                            } catch (e: Exception) {
                                com.tessera.browser.util.DefaultBrowserHelper.openDefaultBrowserSettings(context)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = ApplePalette.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Definir Tessera como navegador padrão",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = mutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { isDefaultDismissed = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 11.sp,
                            color = mutedColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. CENTRO ZEN: WORDMARK LIMPO E MINIMALISTA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSearchClick
                    )
                    .padding(horizontal = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tessera_header_logo),
                    contentDescription = "Tessera",
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}

/**
 * Grade de Favoritos Safari iOS 18 com tiles em Squircle contínuo e microinteração de compressão física.
 */
@Composable
fun SafariFavoritesGrid(
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
                    SafariFavoriteTile(
                        item = item,
                        isDarkMode = isDarkMode,
                        onClick = { onItemClick(item.url) }
                    )
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.width(72.dp))
                }
            }
        }
    }
}

/**
 * Tile individual de favorito Safari com acabamento tátil Apple.
 */
@Composable
fun SafariFavoriteTile(
    item: SpeedDialItem,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "safari_tile_press"
    )

    val squircleShape = AppleShapes.SquircleMedium
    val tileBg = if (isDarkMode) Color(0x33FFFFFF) else ApplePalette.LightCard
    val tileBorder = if (isDarkMode) Color.White.copy(alpha = 0.16f) else ApplePalette.LightHairline
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.92f) else ApplePalette.LightLabelPrimary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = squircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(squircleShape)
                .background(tileBg)
                .border(0.8.dp, tileBorder, squircleShape),
            contentAlignment = Alignment.Center
        ) {
            ShortcutIcon(item = item, size = 28.dp)
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

/**
 * Card compacto de Clima estilo iOS WidgetKit.
 */
@Composable
private fun AppleWeatherWidgetCard(
    weather: WeatherData,
    isDarkMode: Boolean,
    onRefresh: () -> Unit
) {
    val cardShape = AppleShapes.SquircleLarge
    val cardBg = if (isDarkMode) Color(0xD91C1C1E) else Color(0xF2FFFFFF)
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape, ambientColor = Color.Black.copy(alpha = 0.1f))
            .clip(cardShape)
            .background(cardBg)
            .border(0.8.dp, cardBorder, cardShape)
            .clickable(onClick = onRefresh)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ApplePalette.Orange.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = ApplePalette.Orange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = weather.cityName.ifBlank { "Clima Atual" },
                    color = if (isDarkMode) Color.White else Color(0xFF1C1C1E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = weather.conditionText.ifBlank { "Céu limpo" },
                    color = if (isDarkMode) Color.White.copy(alpha = 0.60f) else Color(0xFF8E8E93),
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "${weather.temperature.toInt()}°",
            color = if (isDarkMode) Color.White else Color(0xFF1C1C1E),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Card compacto de Relatório de Privacidade Safari.
 */
@Composable
private fun ApplePrivacyReportCard(
    totalBlocked: Int,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val cardShape = AppleShapes.SquircleLarge
    val cardBg = if (isDarkMode) Color(0xD91C1C1E) else Color(0xF2FFFFFF)
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape, ambientColor = Color.Black.copy(alpha = 0.1f))
            .clip(cardShape)
            .background(cardBg)
            .border(0.8.dp, cardBorder, cardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ApplePalette.Mint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = ApplePalette.Mint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = "Relatório de Privacidade",
                    color = if (isDarkMode) Color.White else Color(0xFF1C1C1E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$totalBlocked rastreadores bloqueados",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.60f) else Color(0xFF8E8E93),
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isDarkMode) Color.White.copy(alpha = 0.40f) else Color(0xFF8E8E93),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Card compacto de Cotações & Mercado estilo Apple Stocks WidgetKit.
 */
@Composable
private fun AppleQuotesWidgetCard(
    quotes: QuotesData,
    isDarkMode: Boolean
) {
    if (quotes.items.isEmpty()) return

    val cardShape = AppleShapes.SquircleLarge
    val cardBg = if (isDarkMode) Color(0xD91C1C1E) else Color(0xF2FFFFFF)
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape, ambientColor = Color.Black.copy(alpha = 0.1f))
            .clip(cardShape)
            .background(cardBg)
            .border(0.8.dp, cardBorder, cardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "COTAÇÕES & MERCADO",
                color = if (isDarkMode) Color.White.copy(alpha = 0.50f) else Color(0xFF8E8E93),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tempo Real",
                color = ApplePalette.Green,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            quotes.items.take(3).forEach { item ->
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.symbol,
                        color = if (isDarkMode) Color.White else Color(0xFF1C1C1E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.value,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.70f) else Color(0xFF48484A),
                        fontSize = 11.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (item.isPositive) ApplePalette.Green.copy(alpha = 0.18f)
                                else ApplePalette.Red.copy(alpha = 0.18f)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.change,
                            color = if (item.isPositive) ApplePalette.Green else ApplePalette.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
