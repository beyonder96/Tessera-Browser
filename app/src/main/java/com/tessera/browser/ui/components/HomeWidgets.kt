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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
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
private fun getWeatherIcon(weatherCode: Int, isDay: Boolean): ImageVector {
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
