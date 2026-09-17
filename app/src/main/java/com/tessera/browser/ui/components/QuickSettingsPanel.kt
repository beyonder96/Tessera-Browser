package com.tessera.browser.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.WallpaperTheme

@Composable
fun QuickSettingsPanel(
    isDarkMode: Boolean,
    forceDarkPages: Boolean,
    showWallpaper: Boolean,
    selectedWallpaperId: String,
    showFavoritesBar: Boolean,
    showCatInara: Boolean,
    tesseraAiEnabled: Boolean,
    aiToolbarButton: Boolean,
    aiTextHighlightPrompts: Boolean,
    showSidebar: Boolean,
    autoHideSidebar: Boolean,
    adBlockEnabled: Boolean = true,
    showWeatherWidget: Boolean = true,
    showQuotesWidget: Boolean = true,
    onDarkModeChanged: (Boolean) -> Unit,
    onForceDarkPagesChanged: (Boolean) -> Unit,
    onShowWallpaperChanged: (Boolean) -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onShowFavoritesBarChanged: (Boolean) -> Unit,
    onShowCatInaraChanged: (Boolean) -> Unit,
    onShowWeatherWidgetChanged: (Boolean) -> Unit = {},
    onShowQuotesWidgetChanged: (Boolean) -> Unit = {},
    onTesseraAiChanged: (Boolean) -> Unit,
    onAiToolbarButtonChanged: (Boolean) -> Unit,
    onAiTextHighlightPromptsChanged: (Boolean) -> Unit,
    onShowSidebarChanged: (Boolean) -> Unit,
    onAutoHideSidebarChanged: (Boolean) -> Unit,
    onAdBlockChanged: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val verticalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(panelShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF51E1916), Color(0xF815110E))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = panelShape
            )
            .padding(horizontal = 24.dp)
            .verticalScroll(verticalScrollState)
    ) {
        // Sticky Header with Close button
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configuração fácil",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider()

        // 1. SEÇÃO: AMBIENTE
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ambiente",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium
            )

            ThemeTogglePill(
                isDarkMode = isDarkMode,
                onToggle = { onDarkModeChanged(!isDarkMode) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Forçar páginas escuras
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Forçar páginas escuras",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.NorthEast,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(14.dp)
                )
            }
            TesseraSwitch(
                checked = forceDarkPages,
                onCheckedChange = onForceDarkPagesChanged
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider()

        // 2. SEÇÃO: PAPEL DE PAREDE
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exibir papel de parede",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            TesseraSwitch(
                checked = showWallpaper,
                onCheckedChange = onShowWallpaperChanged
            )
        }

        // Galeria de miniaturas de papéis de parede (Reference Screenshot 3)
        if (showWallpaper) {
            Spacer(modifier = Modifier.height(14.dp))
            WallpaperCarousel(
                wallpapers = AvailableWallpapers,
                selectedId = selectedWallpaperId,
                onSelect = onSelectWallpaper
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SettingsDivider()

        // 3. SEÇÃO: SEGURANÇA E NAVEGAÇÃO
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Bloqueador de anúncios (AdBlock)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = adBlockEnabled,
                onCheckedChange = onAdBlockChanged
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Histórico e Favoritos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .clickable(onClick = onOpenHistory)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Histórico e Favoritos",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Downloads
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .clickable(onClick = onOpenDownloads)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Downloads",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. SEÇÃO: BARRA DE FAVORITOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.StarOutline,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Exibir a barra de favoritos",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showFavoritesBar,
                onCheckedChange = onShowFavoritesBarChanged
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider()

        // 4. SEÇÃO: WIDGETS DA TELA INICIAL
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Widgets da tela inicial",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Widget de Tempo / Clima
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFA726),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Widget de tempo (clima)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showWeatherWidget,
                onCheckedChange = onShowWeatherWidgetChanged
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Widget de Cotações
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Widget de cotações (USD, EUR, BTC)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showQuotesWidget,
                onCheckedChange = onShowQuotesWidgetChanged
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider()

        // 5. SEÇÃO: CONFIGURAÇÕES DO GATO (EASTER EGG INARA)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Configurações do mascote",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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
                    text = "Mostrar Inara 🐾",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showCatInara,
                onCheckedChange = onShowCatInaraChanged
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider()

        // 5. SEÇÃO: TESSERA AI (OPERA AI)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "Tessera AI",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            TesseraSwitch(
                checked = tesseraAiEnabled,
                onCheckedChange = onTesseraAiChanged
            )
        }

        if (tesseraAiEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            // Sub-item: Botão de IA na barra de ferramentas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Botão de IA na barra de ferramentas",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TesseraSwitch(
                    checked = aiToolbarButton,
                    onCheckedChange = onAiToolbarButtonChanged
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Sub-item: Avisos da IA no pop-up de destaque
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avisos da IA no pop-up de destaque do texto",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TesseraSwitch(
                    checked = aiTextHighlightPrompts,
                    onCheckedChange = onAiTextHighlightPromptsChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider()

        // 6. SEÇÃO: BARRA LATERAL
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ViewSidebar,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Exibir a barra lateral",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showSidebar,
                onCheckedChange = onShowSidebarChanged
            )
        }

        if (showSidebar) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ocultar automaticamente a barra lateral",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TesseraSwitch(
                    checked = autoHideSidebar,
                    onCheckedChange = onAutoHideSidebarChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun WallpaperCarousel(
    wallpapers: List<WallpaperTheme>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        wallpapers.forEach { theme ->
            val isSelected = theme.id == selectedId
            val shape = RoundedCornerShape(16.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(theme.id) }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 58.dp)
                        .clip(shape)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) theme.accentColor else Color.White.copy(alpha = 0.15f),
                            shape = shape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (theme.drawableRes != null) {
                        Image(
                            painter = painterResource(theme.drawableRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(theme.gradientColors))
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selecionado",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = theme.name,
                    color = if (isSelected) theme.accentColor else Color.White.copy(alpha = 0.75f),
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ThemeTogglePill(
    isDarkMode: Boolean,
    onToggle: () -> Unit
) {
    val pillShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), pillShape)
            .clickable(onClick = onToggle)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ThemeIconChip(
            icon = Icons.Rounded.LightMode,
            isSelected = !isDarkMode,
            description = "Modo claro"
        )
        Spacer(modifier = Modifier.width(4.dp))
        ThemeIconChip(
            icon = Icons.Rounded.DarkMode,
            isSelected = isDarkMode,
            description = "Modo escuro"
        )
    }
}

@Composable
private fun ThemeIconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    description: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(200),
        label = "chip_bg"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White.copy(alpha = if (isSelected) 0.95f else 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TesseraSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF64B5F6),
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
            uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
            uncheckedBorderColor = Color.White.copy(alpha = 0.15f)
        )
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.07f)
    )
}
