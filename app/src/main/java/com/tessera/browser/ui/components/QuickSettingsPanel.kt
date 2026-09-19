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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import com.tessera.browser.data.SearchEngine

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.WallpaperTheme
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import coil.compose.SubcomposeAsyncImage

@Composable
fun QuickSettingsPanel(
    isDarkMode: Boolean,
    forceDarkPages: Boolean,
    showWallpaper: Boolean,
    selectedWallpaperId: String,
    customWallpaperUri: String? = null,
    showFavoritesBar: Boolean,
    tesseraAiEnabled: Boolean,
    aiToolbarButton: Boolean,
    aiTextHighlightPrompts: Boolean,
    showSidebar: Boolean,
    autoHideSidebar: Boolean,
    adBlockEnabled: Boolean = true,
    isDesktopMode: Boolean = false,
    cookieBlockerEnabled: Boolean = true,
    isWebPageActive: Boolean = false,
    showWeatherWidget: Boolean = true,
    showQuotesWidget: Boolean = true,
    onDarkModeChanged: (Boolean) -> Unit,
    onForceDarkPagesChanged: (Boolean) -> Unit,
    onShowWallpaperChanged: (Boolean) -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onUploadWallpaper: () -> Unit = {},
    onShowFavoritesBarChanged: (Boolean) -> Unit,
    onShowWeatherWidgetChanged: (Boolean) -> Unit = {},
    onShowQuotesWidgetChanged: (Boolean) -> Unit = {},
    onTesseraAiChanged: (Boolean) -> Unit,
    onAiToolbarButtonChanged: (Boolean) -> Unit,
    onAiTextHighlightPromptsChanged: (Boolean) -> Unit,
    onShowSidebarChanged: (Boolean) -> Unit,
    onAutoHideSidebarChanged: (Boolean) -> Unit,
    onAdBlockChanged: (Boolean) -> Unit,
    onDesktopModeChanged: (Boolean) -> Unit = {},
    onCookieBlockerChanged: (Boolean) -> Unit = {},
    onFindInPage: () -> Unit = {},
    onSharePage: () -> Unit = {},
    onPrintPage: () -> Unit = {},
    onAddToHomeScreen: () -> Unit = {},
    onSavePageOffline: () -> Unit = {},
    onShowQrCode: () -> Unit = {},
    selectedSearchEngine: SearchEngine = SearchEngine.GOOGLE,
    onSearchEngineSelected: (SearchEngine) -> Unit = {},
    onClearBrowsingData: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit = { _, _, _ -> },
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit = {},
    onOpenReaderMode: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val verticalScrollState = rememberScrollState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var clearHistoryChecked by remember { mutableStateOf(true) }
    var clearCacheChecked by remember { mutableStateOf(true) }
    var clearCookiesChecked by remember { mutableStateOf(true) }


    val panelBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xF51E1916), Color(0xF815110E))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF8F8FA))
        )
    }

    val panelBorder = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.Black.copy(alpha = 0.08f),
                Color.Black.copy(alpha = 0.03f)
            )
        )
    }

    val titleColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF19191C)
    val closeIconTint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF48484A)
    val primaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.88f) else Color(0xFF1C1C1E)
    val secondaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF2C2C2E)
    val tertiaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF636366)
    val sectionHeaderColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0x0A000000)
    val cardArrowTint = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
    val cardHistoryIconTint = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF3A3A3C)
    val infoIconTint = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93)
    val secondaryIconTint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF8E8E93)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(panelShape)
            .background(panelBg)
            .border(
                width = 1.dp,
                brush = panelBorder,
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
                color = titleColor,
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
                    tint = closeIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)

        // 1. SEÇÃO: AMBIENTE
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ambiente",
                color = primaryTextColor,
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
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = infoIconTint,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.NorthEast,
                    contentDescription = null,
                    tint = infoIconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            TesseraSwitch(
                checked = forceDarkPages,
                onCheckedChange = onForceDarkPagesChanged,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)

        // 2. SEÇÃO: PAPEL DE PAREDE
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exibir papel de parede",
                color = primaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            TesseraSwitch(
                checked = showWallpaper,
                onCheckedChange = onShowWallpaperChanged,
                isDarkMode = isDarkMode
            )
        }

        // Galeria de miniaturas de papéis de parede (Reference Screenshot 3)
        if (showWallpaper) {
            Spacer(modifier = Modifier.height(14.dp))
            WallpaperCarousel(
                wallpapers = AvailableWallpapers,
                selectedId = selectedWallpaperId,
                customWallpaperUri = customWallpaperUri,
                onUpload = onUploadWallpaper,
                onSelect = onSelectWallpaper,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SettingsDivider(isDarkMode = isDarkMode)

        // 3. SEÇÃO: SEGURANÇA E NAVEGAÇÃO
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Mecanismo de busca padrão",
            color = sectionHeaderColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        SearchEngineSelector(
            selectedEngine = selectedSearchEngine,
            onSelectEngine = onSearchEngineSelected,
            isDarkMode = isDarkMode
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Bloqueador de Anúncios
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
                    tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF1976D2),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Bloqueador de anúncios (AdBlock)",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = adBlockEnabled,
                onCheckedChange = onAdBlockChanged,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Histórico e Favoritos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
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
                    tint = cardHistoryIconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Histórico e Favoritos",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = cardArrowTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Downloads
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
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
                    tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Downloads",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = cardArrowTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Limpar Dados de Navegação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .clickable(onClick = { showClearDataDialog = true })
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Limpar dados de navegação",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Histórico, cache e cookies",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93),
                        fontSize = 11.5.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = cardArrowTint,
                modifier = Modifier.size(16.dp)
            )
        }


        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)

        // SEÇÃO: FERRAMENTAS DA PÁGINA (CHROME & OPERA)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Ferramentas da página",
            color = sectionHeaderColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isWebPageActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onOpenReaderMode()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Modo de Leitura (Somente Texto)",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Versão para Computador (Desktop)
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
                    imageVector = Icons.Rounded.Computer,
                    contentDescription = null,
                    tint = if (isDesktopMode) Color(0xFF42A5F5) else secondaryIconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Versão para computador",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = isDesktopMode,
                onCheckedChange = onDesktopModeChanged,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bloqueador de Avisos de Cookies
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
                    imageVector = Icons.Rounded.Cookie,
                    contentDescription = null,
                    tint = if (cookieBlockerEnabled) Color(0xFFFFB74D) else secondaryIconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Bloquear avisos de cookies (LGPD)",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = cookieBlockerEnabled,
                onCheckedChange = onCookieBlockerChanged,
                isDarkMode = isDarkMode
            )
        }

        if (isWebPageActive) {
            Spacer(modifier = Modifier.height(10.dp))

            // Localizar na Página
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onFindInPage()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = cardHistoryIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Localizar na página",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compartilhar Página
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onSharePage()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = cardHistoryIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Compartilhar página",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Imprimir / Salvar em PDF
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onPrintPage()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Print,
                        contentDescription = null,
                        tint = cardHistoryIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Salvar em PDF / Imprimir",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Adicionar à Tela Inicial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onAddToHomeScreen()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AppShortcut,
                        contentDescription = null,
                        tint = cardHistoryIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Adicionar à Tela Inicial",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Salvar para ler offline (.mht)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onSavePageOffline()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OfflinePin,
                        contentDescription = null,
                        tint = cardHistoryIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Salvar para ler offline (.mht)",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Código QR da Página
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onShowQrCode()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode2,
                        contentDescription = null,
                        tint = cardHistoryIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Código QR da página",
                        color = secondaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = cardArrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }
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
                    tint = secondaryIconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Exibir a barra de favoritos",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showFavoritesBar,
                onCheckedChange = onShowFavoritesBarChanged,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)

        // 4. SEÇÃO: WIDGETS DA TELA INICIAL
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Widgets da tela inicial",
            color = sectionHeaderColor,
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
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showWeatherWidget,
                onCheckedChange = onShowWeatherWidgetChanged,
                isDarkMode = isDarkMode
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
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showQuotesWidget,
                onCheckedChange = onShowQuotesWidgetChanged,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)

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
                    tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF0078D4),
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "Tessera AI",
                    color = primaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            TesseraSwitch(
                checked = tesseraAiEnabled,
                onCheckedChange = onTesseraAiChanged,
                isDarkMode = isDarkMode
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
                    color = tertiaryTextColor,
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TesseraSwitch(
                    checked = aiToolbarButton,
                    onCheckedChange = onAiToolbarButtonChanged,
                    isDarkMode = isDarkMode
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
                    color = tertiaryTextColor,
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TesseraSwitch(
                    checked = aiTextHighlightPrompts,
                    onCheckedChange = onAiTextHighlightPromptsChanged,
                    isDarkMode = isDarkMode
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)

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
                    tint = secondaryIconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Exibir a barra lateral",
                    color = secondaryTextColor,
                    fontSize = 14.5.sp
                )
            }
            TesseraSwitch(
                checked = showSidebar,
                onCheckedChange = onShowSidebarChanged,
                isDarkMode = isDarkMode
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
                    color = tertiaryTextColor,
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TesseraSwitch(
                    checked = autoHideSidebar,
                    onCheckedChange = onAutoHideSidebarChanged,
                    isDarkMode = isDarkMode
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Limpar dados de navegação",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecione quais dados você deseja remover:",
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF636366)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clearHistoryChecked = !clearHistoryChecked }
                    ) {
                        Checkbox(
                            checked = clearHistoryChecked,
                            onCheckedChange = { clearHistoryChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Histórico de navegação", fontSize = 14.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clearCacheChecked = !clearCacheChecked }
                    ) {
                        Checkbox(
                            checked = clearCacheChecked,
                            onCheckedChange = { clearCacheChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cache de páginas e imagens", fontSize = 14.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clearCookiesChecked = !clearCookiesChecked }
                    ) {
                        Checkbox(
                            checked = clearCookiesChecked,
                            onCheckedChange = { clearCookiesChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cookies e dados de sites", fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        onClearBrowsingData(clearHistoryChecked, clearCookiesChecked, clearCacheChecked)
                    }
                ) {
                    Text("Limpar agora", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
private fun WallpaperCarousel(
    wallpapers: List<WallpaperTheme>,
    selectedId: String,
    customWallpaperUri: String?,
    onUpload: () -> Unit,
    onSelect: (String) -> Unit,
    isDarkMode: Boolean = true
) {
    val scrollState = rememberScrollState()
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Botão de Upload de Foto da Galeria
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onUpload)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 86.dp, height = 58.dp)
                    .clip(shape)
                    .background(
                        if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f)
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.7f),
                                Color(0xFF7C4DFF).copy(alpha = 0.7f)
                            )
                        ),
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = "Upload de Foto",
                        tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0078D4),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Upload",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1E1E1E),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Nova Foto",
                color = if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF636366),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // 2. Foto Personalizada Carregada pelo Usuário
        if (!customWallpaperUri.isNullOrBlank()) {
            val isCustomSelected = selectedId == "custom"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect("custom") }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 58.dp)
                        .clip(shape)
                        .border(
                            width = if (isCustomSelected) 2.dp else 1.dp,
                            color = if (isCustomSelected) Color(0xFF00E5FF) else (if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f)),
                            shape = shape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = customWallpaperUri,
                        contentDescription = "Minha Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isCustomSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF)),
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
                    text = "Minha Foto",
                    color = if (isCustomSelected) Color(0xFF00E5FF) else (if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF636366)),
                    fontSize = 11.5.sp,
                    fontWeight = if (isCustomSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        // 3. Papéis de Parede Oficiais (Villa Mediterrânea principal mantida + novos temas)
        wallpapers.forEach { theme ->
            val isSelected = theme.id == selectedId
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
                            color = if (isSelected) theme.accentColor else (if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f)),
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
                    color = if (isSelected) theme.accentColor else (if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF636366)),
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
            .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0x0E000000))
            .border(
                1.dp,
                if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color(0x18000000),
                pillShape
            )
            .clickable(onClick = onToggle)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ThemeIconChip(
            icon = Icons.Rounded.LightMode,
            isSelected = !isDarkMode,
            isDarkMode = isDarkMode,
            description = "Modo claro"
        )
        Spacer(modifier = Modifier.width(4.dp))
        ThemeIconChip(
            icon = Icons.Rounded.DarkMode,
            isSelected = isDarkMode,
            isDarkMode = isDarkMode,
            description = "Modo escuro"
        )
    }
}

@Composable
private fun ThemeIconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isDarkMode: Boolean = true,
    description: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDarkMode) Color.White.copy(alpha = 0.18f) else Color.White
        } else Color.Transparent,
        animationSpec = tween(200),
        label = "chip_bg"
    )

    val iconTint = if (isSelected) {
        if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1C1C1E)
    } else {
        if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF8E8E93)
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .then(
                if (isSelected && !isDarkMode) {
                    Modifier.shadow(2.dp, CircleShape)
                } else Modifier
            )
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TesseraSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDarkMode: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF0078D4),
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.White,
            uncheckedTrackColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color(0x24000000),
            uncheckedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color(0x18000000)
        )
    )
}

@Composable
private fun SettingsDivider(
    isDarkMode: Boolean = true
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color(0x0E000000)
    )
}

@Composable
private fun SearchEngineSelector(
    selectedEngine: SearchEngine,
    onSelectEngine: (SearchEngine) -> Unit,
    isDarkMode: Boolean
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchEngine.entries.forEach { engine ->
            val isSelected = engine == selectedEngine
            val shape = RoundedCornerShape(14.dp)
            val chipBg = if (isSelected) {
                if (isDarkMode) Color(0xFF00E5FF).copy(alpha = 0.18f) else Color(0xFF0288D1).copy(alpha = 0.12f)
            } else {
                if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
            }
            val borderModifier = if (isSelected) {
                Modifier.border(
                    width = 1.3.dp,
                    color = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1),
                    shape = shape
                )
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                    shape = shape
                )
            }

            Row(
                modifier = Modifier
                    .clip(shape)
                    .then(borderModifier)
                    .background(chipBg)
                    .clickable { onSelectEngine(engine) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(id = engine.iconRes),
                    contentDescription = engine.displayName,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = engine.displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1)
                    } else {
                        if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF1E1E1E)
                    }
                )
            }
        }
    }
}

