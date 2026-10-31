package com.tessera.browser.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.tessera.browser.R
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.ui.theme.ApplePalette

/**
 * Tessera Start Page — Pure Zen Editorial Edition.
 *
 * Experiência purista e minimalista:
 * - Fundo sólido (OLED Puro no escuro / Papel Journal no claro).
 * - Pílula dispensável de navegador padrão no topo.
 * - Wordmark centralizado "TESSERA" com gatilho direto para busca rápida.
 */
@Composable
fun TesseraStartPage(
    isDarkMode: Boolean,
    onSearchClick: () -> Unit = {},
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
        // 1. FUNDO SÓLIDO (OLED PURO NO ESCURO / PAPEL JOURNAL NO CLARO)
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSearchClick
                    )
                    .padding(horizontal = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tessera_symbol),
                    contentDescription = "Tessera Logo",
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = Modifier.size(64.dp)
                )
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
