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
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Translate
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.SearchEngine
import com.tessera.browser.data.WallpaperTheme

/**
 * Painel de Configurações Rápidas (Quick Settings BottomSheet).
 * Limpo, focado e direto ao ponto — inspirado nos melhores navegadores modernos (Opera, Samsung Internet, Arc).
 * Contém alternâncias imediatas (Modo Escuro, Desktop, AdBlock, Cookies), ações contextuais da página ativa,
 * personalização rápida de papel de parede e atalho direto para a Tela Completa de Configurações.
 */
@Composable
fun QuickSettingsPanel(
    isDarkMode: Boolean,
    forceDarkPages: Boolean,
    showWallpaper: Boolean,
    selectedWallpaperId: String,
    customWallpaperUri: String? = null,
    showFavoritesBar: Boolean = true,
    tesseraAiEnabled: Boolean = true,
    aiToolbarButton: Boolean = true,
    aiTextHighlightPrompts: Boolean = true,
    showSidebar: Boolean = false,
    autoHideSidebar: Boolean = true,
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
    onShowFavoritesBarChanged: (Boolean) -> Unit = {},
    onShowWeatherWidgetChanged: (Boolean) -> Unit = {},
    onShowQuotesWidgetChanged: (Boolean) -> Unit = {},
    onTesseraAiChanged: (Boolean) -> Unit = {},
    onAiToolbarButtonChanged: (Boolean) -> Unit = {},
    onAiTextHighlightPromptsChanged: (Boolean) -> Unit = {},
    onShowSidebarChanged: (Boolean) -> Unit = {},
    onAutoHideSidebarChanged: (Boolean) -> Unit = {},
    onAdBlockChanged: (Boolean) -> Unit,
    onDesktopModeChanged: (Boolean) -> Unit = {},
    onCookieBlockerChanged: (Boolean) -> Unit = {},
    onFindInPage: () -> Unit = {},
    onSharePage: () -> Unit = {},
    onPrintPage: () -> Unit = {},
    onAddToHomeScreen: () -> Unit = {},
    onSavePageOffline: () -> Unit = {},
    onShowQrCode: () -> Unit = {},
    onTranslatePage: () -> Unit = {},
    onOpenSiteSettings: () -> Unit = {},
    onOpenPrivacyDashboard: () -> Unit = {},
    selectedSearchEngine: SearchEngine = SearchEngine.GOOGLE,
    onSearchEngineSelected: (SearchEngine) -> Unit = {},
    onClearBrowsingData: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit = { _, _, _ -> },
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit = {},
    onOpenReaderMode: () -> Unit = {},
    onOpenFullSettings: () -> Unit = {},
    isAutoPipEnabled: Boolean = true,
    onAutoPipChanged: (Boolean) -> Unit = {},
    onEnterPip: () -> Unit = {},
    onOpenNotebook: () -> Unit = {},
    onClipPage: () -> Unit = {},
    geminiApiKey: String? = null,
    onGeminiApiKeyChanged: (String) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val verticalScrollState = rememberScrollState()

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
    val iconButtonTint = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF48484A)
    val primaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.88f) else Color(0xFF1C1C1E)
    val secondaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF48484A)
    val sectionHeaderColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0x0A000000)
    val cardArrowTint = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
    val accentColor = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0078D4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(panelShape)
            .background(panelBg)
            .border(width = 1.dp, brush = panelBorder, shape = panelShape)
            .padding(horizontal = 20.dp)
            .verticalScroll(verticalScrollState)
    ) {
        // DRAG HANDLE & HEADER
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f))
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Configurações rápidas",
                    color = titleColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ajustes rápidos de navegação e página",
                    color = sectionHeaderColor,
                    fontSize = 11.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botão de atalho para todas as configurações
                IconButton(
                    onClick = {
                        onDismiss()
                        onOpenFullSettings()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Todas as configurações",
                        tint = accentColor,
                        modifier = Modifier.size(21.dp)
                    )
                }

                // Botão de fechar
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = iconButtonTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SettingsDivider(isDarkMode = isDarkMode)
        Spacer(modifier = Modifier.height(14.dp))

        // 1. GRADE DE ALTERNÂNCIAS RÁPIDAS (Quick Toggles)
        Text(
            text = "ALTERNÂNCIAS RÁPIDAS",
            color = sectionHeaderColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Toggle 1: Ambiente (Tema Claro / Escuro)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = null,
                    tint = if (isDarkMode) Color(0xFFFFA726) else Color(0xFFF57C00),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Ambiente",
                        color = primaryTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isDarkMode) "Modo escuro" else "Modo claro",
                        color = sectionHeaderColor,
                        fontSize = 11.5.sp
                    )
                }
            }
            ThemeTogglePill(
                isDarkMode = isDarkMode,
                onToggle = { onDarkModeChanged(!isDarkMode) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grade 2 colunas com toggles compactos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card: Modo Desktop / Computador
            QuickToggleMiniCard(
                icon = Icons.Rounded.Computer,
                iconTint = if (isDesktopMode) Color(0xFF42A5F5) else sectionHeaderColor,
                title = "Versão PC",
                subtitle = if (isDesktopMode) "Ativa" else "Celular",
                checked = isDesktopMode,
                onCheckedChange = onDesktopModeChanged,
                isDarkMode = isDarkMode,
                modifier = Modifier.weight(1f)
            )

            // Card: Bloqueador de Anúncios (AdBlock)
            QuickToggleMiniCard(
                icon = Icons.Rounded.Shield,
                iconTint = if (adBlockEnabled) Color(0xFF64B5F6) else sectionHeaderColor,
                title = "AdBlock",
                subtitle = if (adBlockEnabled) "Ativo" else "Inativo",
                checked = adBlockEnabled,
                onCheckedChange = onAdBlockChanged,
                isDarkMode = isDarkMode,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card: Forçar Páginas Escuras
            QuickToggleMiniCard(
                icon = Icons.Rounded.DarkMode,
                iconTint = if (forceDarkPages) Color(0xFFBA68C8) else sectionHeaderColor,
                title = "Sites escuros",
                subtitle = if (forceDarkPages) "Forçado" else "Padrão",
                checked = forceDarkPages,
                onCheckedChange = onForceDarkPagesChanged,
                isDarkMode = isDarkMode,
                modifier = Modifier.weight(1f)
            )

            // Card: Bloquear Avisos de Cookies LGPD
            QuickToggleMiniCard(
                icon = Icons.Rounded.Cookie,
                iconTint = if (cookieBlockerEnabled) Color(0xFFFFB74D) else sectionHeaderColor,
                title = "Sem cookies",
                subtitle = if (cookieBlockerEnabled) "Bloqueando" else "Padrão",
                checked = cookieBlockerEnabled,
                onCheckedChange = onCookieBlockerChanged,
                isDarkMode = isDarkMode,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card: PiP Automático ao Sair
        QuickToggleMiniCard(
            icon = Icons.Rounded.SmartDisplay,
            iconTint = if (isAutoPipEnabled) Color(0xFF26A69A) else sectionHeaderColor,
            title = "PiP Automático",
            subtitle = if (isAutoPipEnabled) "Janela flutuante ao sair" else "Desativado",
            checked = isAutoPipEnabled,
            onCheckedChange = onAutoPipChanged,
            isDarkMode = isDarkMode,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. SEÇÃO: FERRAMENTAS DA PÁGINA (Somente quando navegando)
        if (isWebPageActive) {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsDivider(isDarkMode = isDarkMode)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AÇÕES DA PÁGINA ATIVA",
                color = sectionHeaderColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Grade 2 colunas com ações rápidas
            val actionItems = listOf(
                PageActionItem(Icons.AutoMirrored.Rounded.MenuBook, "Modo de Leitura", Color(0xFF42A5F5)) {
                    onDismiss()
                    onOpenReaderMode()
                },
                PageActionItem(Icons.Rounded.Search, "Localizar na Página", Color(0xFF66BB6A)) {
                    onDismiss()
                    onFindInPage()
                },
                PageActionItem(Icons.Rounded.Translate, "Traduzir Página", Color(0xFF26A69A)) {
                    onDismiss()
                    onTranslatePage()
                },
                PageActionItem(Icons.Rounded.Share, "Compartilhar", Color(0xFFAB47BC)) {
                    onDismiss()
                    onSharePage()
                },
                PageActionItem(Icons.Rounded.Print, "Salvar em PDF", Color(0xFFFFA726)) {
                    onDismiss()
                    onPrintPage()
                },
                PageActionItem(Icons.Rounded.AppShortcut, "Adicionar à Home", Color(0xFF29B6F6)) {
                    onDismiss()
                    onAddToHomeScreen()
                },
                PageActionItem(Icons.Rounded.OfflinePin, "Salvar Offline", Color(0xFF8D6E63)) {
                    onDismiss()
                    onSavePageOffline()
                },
                PageActionItem(Icons.Rounded.QrCode2, "Código QR", Color(0xFF78909C)) {
                    onDismiss()
                    onShowQrCode()
                },
                PageActionItem(Icons.Rounded.Lock, "Permissões do Site", Color(0xFFEF5350)) {
                    onDismiss()
                    onOpenSiteSettings()
                },
                PageActionItem(Icons.Rounded.Shield, "Escudo de Privacidade", Color(0xFF00E676)) {
                    onDismiss()
                    onOpenPrivacyDashboard()
                },
                PageActionItem(Icons.Rounded.SmartDisplay, "Janela Flutuante", Color(0xFF42A5F5)) {
                    onDismiss()
                    onEnterPip()
                },
                PageActionItem(Icons.Rounded.EditNote, "Clipar no Caderno", Color(0xFFFFB300)) {
                    onDismiss()
                    onClipPage()
                }
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                actionItems.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { action ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .clickable(onClick = action.onClick)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.tint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = action.title,
                                    color = secondaryTextColor,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 3. PERSONALIZAÇÃO RÁPIDA: PAPEL DE PAREDE
        Spacer(modifier = Modifier.height(16.dp))
        SettingsDivider(isDarkMode = isDarkMode)
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Papel de parede da tela inicial",
                color = primaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            TesseraSwitch(
                checked = showWallpaper,
                onCheckedChange = onShowWallpaperChanged,
                isDarkMode = isDarkMode
            )
        }

        if (showWallpaper) {
            Spacer(modifier = Modifier.height(10.dp))
            WallpaperCarousel(
                wallpapers = AvailableWallpapers,
                selectedId = selectedWallpaperId,
                customWallpaperUri = customWallpaperUri,
                onUpload = onUploadWallpaper,
                onSelect = onSelectWallpaper,
                isDarkMode = isDarkMode
            )
        }

        // 4. ATALHOS RÁPIDOS
        Spacer(modifier = Modifier.height(16.dp))
        SettingsDivider(isDarkMode = isDarkMode)
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Histórico e Favoritos
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onOpenHistory()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF1976D2),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Histórico & Hub",
                    color = primaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Downloads
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable {
                        onDismiss()
                        onOpenDownloads()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
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
                    color = primaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Caderno de Notas & Web Clipper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .clickable {
                    onDismiss()
                    onOpenNotebook()
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.EditNote,
                contentDescription = null,
                tint = if (isDarkMode) Color(0xFFFFB300) else Color(0xFFF57F17),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Caderno & Web Clipper",
                    color = primaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Notas salvas, trechos e sínteses com IA",
                    color = sectionHeaderColor,
                    fontSize = 11.sp
                )
            }
        }

        // 5. CARD DESTACADO: TODAS AS CONFIGURAÇÕES
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isDarkMode) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), Color(0xFF7C4DFF).copy(alpha = 0.15f))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFFE3F2FD), Color(0xFFEDE7F6))
                        )
                    }
                )
                .border(
                    width = 1.2.dp,
                    color = if (isDarkMode) Color(0xFF00E5FF).copy(alpha = 0.4f) else Color(0xFF0288D1).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable {
                    onDismiss()
                    onOpenFullSettings()
                }
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF0288D1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Todas as configurações",
                        color = primaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mecanismo de busca, IA, privacidade e mais",
                        color = sectionHeaderColor,
                        fontSize = 11.5.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

private data class PageActionItem(
    val icon: ImageVector,
    val title: String,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickToggleMiniCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0x0A000000)
    val primaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.88f) else Color(0xFF1C1C1E)
    val sectionHeaderColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = title,
                    color = primaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = sectionHeaderColor,
                    fontSize = 11.sp
                )
            }
        }
        TesseraSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            isDarkMode = isDarkMode
        )
    }
}

@Composable
fun WallpaperCarousel(
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

        // 3. Papéis de Parede Oficiais
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
fun ThemeTogglePill(
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
fun ThemeIconChip(
    icon: ImageVector,
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
fun TesseraSwitch(
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
fun SettingsDivider(
    isDarkMode: Boolean = true
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color(0x0E000000)
    )
}

@Composable
fun SearchEngineSelector(
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
