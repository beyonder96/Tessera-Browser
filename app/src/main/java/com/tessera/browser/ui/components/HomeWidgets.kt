package com.tessera.browser.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.viewmodel.QuoteItem
import com.tessera.browser.viewmodel.QuotesData
import com.tessera.browser.viewmodel.WeatherData

/**
 * Minimalist container that arranges the Weather and Quotes widgets on the Start Page.
 */
@Composable
fun HomeWidgetsContainer(
    showWeather: Boolean,
    showQuotes: Boolean,
    weatherData: WeatherData?,
    quotesData: QuotesData?,
    isDarkMode: Boolean,
    accentColor: Color,
    onRefreshWeather: () -> Unit,
    onQuoteClick: (QuoteItem) -> Unit,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showWeather && !showQuotes) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Weather Widget
        if (showWeather && weatherData != null) {
            WeatherMinimalWidget(
                data = weatherData,
                isDarkMode = isDarkMode,
                accentColor = accentColor,
                onRefresh = onRefreshWeather,
                onClick = onWeatherClick
            )
        }

        // 2. Quotes Widget
        if (showQuotes && quotesData != null && quotesData.items.isNotEmpty()) {
            QuotesMinimalWidget(
                data = quotesData,
                isDarkMode = isDarkMode,
                accentColor = accentColor,
                onQuoteClick = onQuoteClick
            )
        }
    }
}

/**
 * Minimalist weather info displaying temperature, weather condition icon, city, and brief status floating cleanly.
 */
@Composable
fun WeatherMinimalWidget(
    data: WeatherData,
    isDarkMode: Boolean,
    accentColor: Color,
    onRefresh: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherIcon = getWeatherIcon(data.weatherCode, data.isDay)
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(0f, 2f),
        blurRadius = 8f
    )
    val textColor = Color.White
    val subTextColor = Color.White.copy(alpha = 0.88f)

    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Weather Icon with soft ambient color
        Icon(
            imageVector = weatherIcon,
            contentDescription = data.conditionText,
            tint = if (data.isDay) Color(0xFFFFD54F) else Color(0xFF90CAF9),
            modifier = Modifier.size(19.dp)
        )

        // Temperature & City
        Text(
            text = "${data.temperature}°C",
            color = textColor,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(shadow = textShadow)
        )

        Text(
            text = "•",
            color = subTextColor,
            fontSize = 11.sp,
            style = TextStyle(shadow = textShadow)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Color(0xFF81D4FA),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = data.cityName,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                style = TextStyle(shadow = textShadow)
            )
        }

        Text(
            text = "•",
            color = subTextColor,
            fontSize = 11.sp,
            style = TextStyle(shadow = textShadow)
        )

        // Condition summary
        Text(
            text = data.conditionText,
            color = subTextColor,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Normal,
            style = TextStyle(shadow = textShadow)
        )

        // Loading or refresh indicator
        if (data.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = Color.White,
                strokeWidth = 1.5.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Atualizar tempo",
                    tint = subTextColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Minimalist ticker of financial quotes (USD, EUR, BTC) floating cleanly without wrapping box.
 */
@Composable
fun QuotesMinimalWidget(
    data: QuotesData,
    isDarkMode: Boolean,
    accentColor: Color,
    onQuoteClick: (QuoteItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.items.forEach { quote ->
            QuoteItemChip(
                quote = quote,
                isDarkMode = isDarkMode,
                onClick = { onQuoteClick(quote) }
            )
        }
    }
}

@Composable
private fun QuoteItemChip(
    quote: QuoteItem,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.55f),
        offset = Offset(0f, 2f),
        blurRadius = 6f
    )
    val textColor = Color.White
    val variationColor = if (quote.isPositive) Color(0xFF69F0AE) else Color(0xFFFF5252)

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = quote.symbol,
            color = textColor.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(shadow = textShadow)
        )

        Text(
            text = quote.value,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(shadow = textShadow)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (quote.isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                contentDescription = null,
                tint = variationColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = quote.change,
                color = variationColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(shadow = textShadow)
            )
        }
    }
}

/**
 * Maps WMO code to Material Icons for weather conditions.
 */
internal fun getWeatherIcon(weatherCode: Int, isDay: Boolean): ImageVector {
    return when (weatherCode) {
        0 -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
        1, 2 -> if (isDay) Icons.Rounded.WbCloudy else Icons.Rounded.NightsStay
        3 -> Icons.Rounded.Cloud
        45, 48 -> Icons.Rounded.Cloud
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Rounded.WaterDrop
        71, 73, 75, 77, 85, 86 -> Icons.Rounded.AcUnit
        95, 96, 99 -> Icons.Rounded.Thunderstorm
        else -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
    }
}

/**
 * Bento Box tile for Weather (1x1).
 */
@Composable
fun WeatherBentoCard(
    data: WeatherData,
    isDarkMode: Boolean,
    accentColor: Color,
    onRefresh: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bentoShape = RoundedCornerShape(22.dp)
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
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6E6E73)
    val weatherIcon = getWeatherIcon(data.weatherCode, data.isDay)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDarkMode) 10.dp else 4.dp,
                shape = bentoShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(bentoShape)
            .background(cardBg)
            .border(1.dp, cardBorder, bentoShape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: City & Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = data.cityName,
                        color = mutedColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                if (data.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = accentColor,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRefresh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Atualizar",
                            tint = mutedColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Center: Big Temperature + Weather Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = weatherIcon,
                    contentDescription = data.conditionText,
                    tint = if (data.isDay) Color(0xFFFFD54F) else Color(0xFF90CAF9),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "${data.temperature}°",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom: Condition summary
            Text(
                text = data.conditionText,
                color = mutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

/**
 * Bento Box tile for Financial Quotes (1x1).
 */
@Composable
fun QuotesBentoCard(
    data: QuotesData,
    isDarkMode: Boolean,
    accentColor: Color,
    onQuoteClick: (QuoteItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val bentoShape = RoundedCornerShape(22.dp)
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
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6E6E73)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDarkMode) 10.dp else 4.dp,
                shape = bentoShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(bentoShape)
            .background(cardBg)
            .border(1.dp, cardBorder, bentoShape)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Mercado & Câmbio",
                    color = mutedColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rows of quotes (USD, EUR, BTC)
            data.items.take(3).forEach { item ->
                val variationColor = if (item.isPositive) Color(0xFF69F0AE) else Color(0xFFFF5252)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onQuoteClick(item) }
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.symbol,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.value,
                            color = textColor.copy(alpha = 0.9f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = item.change,
                            color = variationColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bento Box tile for Privacy & Tracking Shield status (2x1 slender pill).
 */
@Composable
fun PrivacyShieldBentoCard(
    isDarkMode: Boolean,
    accentColor: Color,
    totalBlockedCount: Int = 0,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bentoShape = RoundedCornerShape(20.dp)
    val cardBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xBB1E1A18), Color(0xDD14100E))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xEEFFFFFF), Color(0xF2F7F8FA))
        )
    }
    val cardBorder = if (isDarkMode) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1E1E1E)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 6.dp else 2.dp,
                shape = bentoShape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(bentoShape)
            .background(cardBg)
            .border(1.dp, cardBorder, bentoShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF69F0AE))
            )
            Text(
                text = if (totalBlockedCount > 0) {
                    "Escudo Ativo • $totalBlockedCount itens bloqueados"
                } else {
                    "Navegação Segura & AdBlock Ativo"
                },
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            imageVector = Icons.Rounded.Security,
            contentDescription = "Protegido",
            tint = Color(0xFF69F0AE),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Bento Box tile for Notebook & Web Clipper status (2x1 slender pill).
 */
@Composable
fun NotebookBentoCard(
    isDarkMode: Boolean,
    accentColor: Color,
    notesCount: Int = 0,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bentoShape = RoundedCornerShape(20.dp)
    val cardBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xBB1E1A18), Color(0xDD14100E))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xEEFFFFFF), Color(0xF2F7F8FA))
        )
    }
    val cardBorder = if (isDarkMode) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1E1E1E)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 6.dp else 2.dp,
                shape = bentoShape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(bentoShape)
            .background(cardBg)
            .border(1.dp, cardBorder, bentoShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB300))
            )
            Text(
                text = if (notesCount > 0) {
                    "Caderno & Web Clipper • $notesCount anotações salvas"
                } else {
                    "Caderno de Notas & Web Clipper"
                },
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            imageVector = Icons.Rounded.EditNote,
            contentDescription = "Caderno",
            tint = Color(0xFFFFB300),
            modifier = Modifier.size(18.dp)
        )
    }
}


