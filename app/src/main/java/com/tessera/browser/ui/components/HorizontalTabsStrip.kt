package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.viewmodel.BrowserTab

/**
 * Opera Air / One inspired horizontal pill tabs strip.
 * Sits directly above the Omnibar for optimal mobile one-handed ergonomics.
 */
@Composable
fun HorizontalTabsStrip(
    tabs: List<BrowserTab>,
    activeTabId: String,
    accentColor: Color,
    isDarkMode: Boolean,
    isIncognito: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (tabs.isEmpty()) return

    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    val activeIndex = remember(tabs, activeTabId) {
        tabs.indexOfFirst { it.id == activeTabId }.coerceAtLeast(0)
    }

    LaunchedEffect(activeIndex, tabs.size) {
        if (activeIndex in tabs.indices) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    val contentColor = if (isIncognito || isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E)
    val mutedColor = if (isIncognito) Color(0xFFC4B5FD).copy(alpha = 0.70f) else if (isDarkMode) Color.White.copy(alpha = 0.50f) else Color(0xFF6B7280)

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            items = tabs,
            key = { _, tab -> tab.id }
        ) { _, tab ->
            val isActive = tab.id == activeTabId
            val pillShape = RoundedCornerShape(18.dp)

            val pillBg = when {
                isActive && isIncognito -> Color(0xF0201833)
                isActive && isDarkMode -> Color(0xF02C2622)
                isActive -> Color(0xF8FFFFFF)
                isIncognito -> Color(0x331E1530)
                isDarkMode -> Color(0x33FFFFFF)
                else -> Color(0x33000000)
            }

            val pillBorder = when {
                isActive && isIncognito -> accentColor.copy(alpha = 0.80f)
                isActive -> accentColor.copy(alpha = 0.70f)
                isIncognito -> Color.White.copy(alpha = 0.12f)
                isDarkMode -> Color.White.copy(alpha = 0.10f)
                else -> Color.Black.copy(alpha = 0.08f)
            }

            val hostDomain = remember(tab.url, tab.isHomePage) {
                if (tab.isHomePage) {
                    "Speed Dial"
                } else {
                    tab.url.removePrefix("https://").removePrefix("http://")
                        .takeWhile { it != '/' && it != '?' }
                        .ifBlank { "Nova Guia" }
                }
            }

            val titleText = remember(tab.title, tab.isHomePage, hostDomain) {
                if (tab.isHomePage) {
                    "Speed Dial"
                } else if (tab.title.isNotBlank() && tab.title != "Nova Guia" && tab.title != "duckduckgo.com") {
                    tab.title
                } else {
                    hostDomain
                }
            }

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .widthIn(min = 105.dp, max = 160.dp)
                    .then(
                        if (isActive) {
                            Modifier.shadow(
                                elevation = 6.dp,
                                shape = pillShape,
                                ambientColor = accentColor.copy(alpha = 0.20f),
                                spotColor = Color.Black.copy(alpha = 0.15f)
                            )
                        } else Modifier
                    )
                    .clip(pillShape)
                    .background(pillBg)
                    .border(
                        width = if (isActive) 1.2.dp else 0.8.dp,
                        color = pillBorder,
                        shape = pillShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectTab(tab.id)
                        }
                    )
                    .padding(horizontal = 9.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Leading Icon: Opera Speed Dial 4-dots grid or Favicon/Monogram
                    if (tab.isHomePage) {
                        SpeedDialDotsIcon(
                            color = if (isActive) accentColor else mutedColor,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        TabFaviconCircle(
                            domain = hostDomain,
                            accentColor = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Title
                    Text(
                        text = titleText,
                        color = if (isActive) contentColor else mutedColor,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Close Button on Active Tab (or if multiple tabs exist)
                    if (isActive && tabs.size > 1) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDarkMode || isIncognito) Color.White.copy(alpha = 0.15f)
                                    else Color.Black.copy(alpha = 0.08f)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCloseTab(tab.id)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Fechar aba",
                                tint = contentColor.copy(alpha = 0.80f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }

        // Trailing Add Tab Button (+)
        item {
            val addBtnBg = when {
                isIncognito -> Color(0x331E1530)
                isDarkMode -> Color(0x33FFFFFF)
                else -> Color(0x33000000)
            }
            val addBorder = when {
                isIncognito -> Color.White.copy(alpha = 0.14f)
                isDarkMode -> Color.White.copy(alpha = 0.12f)
                else -> Color.Black.copy(alpha = 0.10f)
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(addBtnBg)
                    .border(0.8.dp, addBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNewTab()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Nova Guia",
                    tint = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * 2x2 Speed Dial dot grid icon with hollow stroke circles (matches Opera Air).
 */
@Composable
private fun SpeedDialDotsIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val strokeWidth = 1.3.dp.toPx()
            val radius = 2.1.dp.toPx()
            val spacing = 3.6.dp.toPx()
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            val left = centerX - spacing / 2f - radius
            val right = centerX + spacing / 2f + radius
            val top = centerY - spacing / 2f - radius
            val bottom = centerY + spacing / 2f + radius

            drawCircle(color, radius = radius, center = Offset(left, top), style = Stroke(strokeWidth))
            drawCircle(color, radius = radius, center = Offset(right, top), style = Stroke(strokeWidth))
            drawCircle(color, radius = radius, center = Offset(left, bottom), style = Stroke(strokeWidth))
            drawCircle(color, radius = radius, center = Offset(right, bottom), style = Stroke(strokeWidth))
        }
    }
}

/**
 * Minimalist circle favicon or site monogram for web tabs.
 */
@Composable
private fun TabFaviconCircle(
    domain: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val cleanDomain = domain.lowercase()
    val isGoogle = cleanDomain.contains("google")
    val isOpera = cleanDomain.contains("opera")
    val isX = cleanDomain.contains("x.com") || cleanDomain.contains("twitter")
    val isGithub = cleanDomain.contains("github")
    val isWiki = cleanDomain.contains("wikipedia")

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                when {
                    isOpera -> Color.Black
                    isGoogle -> Color(0xFFE8F0FE)
                    isX -> Color.Black
                    isGithub -> Color(0xFF24292E)
                    else -> accentColor.copy(alpha = 0.16f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isOpera -> {
                // Opera red/white 'O' ring
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .border(1.6.dp, Color(0xFFFF1B2D), CircleShape)
                )
            }
            isGoogle -> Text("G", color = Color(0xFF4285F4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            isX -> Text("𝕏", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            isGithub -> Text("gh", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            isWiki -> Text("W", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            else -> {
                val letter = cleanDomain.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "W"
                Text(
                    text = letter,
                    color = accentColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
