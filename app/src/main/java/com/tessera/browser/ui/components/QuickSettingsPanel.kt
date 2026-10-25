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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.tessera.browser.data.AvailableWallpapers
import com.tessera.browser.data.SearchEngine
import com.tessera.browser.data.WallpaperTheme
import com.tessera.browser.ui.theme.ApplePalette
import com.tessera.browser.ui.theme.AppleShapes

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
    onOpenInExternalApp: () -> Unit = {},
    geminiApiKey: String? = null,
    onGeminiApiKeyChanged: (String) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val verticalScrollState = rememberScrollState()

    val panelBg = if (isDarkMode) ApplePalette.OledSurface else ApplePalette.MatteSurface
    val panelBorder = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline

    val titleColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val iconButtonTint = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val primaryTextColor = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val secondaryTextColor = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val sectionHeaderColor = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val cardBg = if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightElevated
    val accentColor = if (isDarkMode) ApplePalette.BlueDark else ApplePalette.Blue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(panelBg)
            .border(width = 1.dp, color = panelBorder, shape = panelShape)
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
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ações & Ferramentas",
                    color = titleColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isWebPageActive) "Página ativa & navegador" else "Navegador & ferramentas rápidas",
                    color = sectionHeaderColor,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Atalho para Configurações Completas
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
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Fechar
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

        Spacer(modifier = Modifier.height(16.dp))

        // CARD DE NAVEGADOR PADRÃO (SE AINDA NÃO FOR O PADRÃO DO ANDROID)
        val context = LocalContext.current
        val isDefaultBrowser = remember(context) { com.tessera.browser.util.DefaultBrowserHelper.isDefaultBrowser(context) }
        if (!isDefaultBrowser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
                    .border(1.dp, panelBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        onDismiss()
                        com.tessera.browser.util.DefaultBrowserHelper.requestDefaultBrowser(context)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Definir como Navegador Padrão",
                            color = primaryTextColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Abra links externos diretamente no Tessera",
                            color = secondaryTextColor,
                            fontSize = 11.5.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 1. LINHA DE AÇÕES RÁPIDAS (ESTILO SHARE SHEET)
        Text(
            text = "AÇÕES RÁPIDAS",
            color = sectionHeaderColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        val actionItems = buildList {
            add(
                QuickActionItem(
                    icon = Icons.Rounded.Share,
                    label = "Compartilhar",
                    tint = Color(0xFF64B5F6),
                    onClick = {
                        onDismiss()
                        onSharePage()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    label = "Leitura",
                    tint = Color(0xFFFFB74D),
                    onClick = {
                        onDismiss()
                        onOpenReaderMode()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.Search,
                    label = "Buscar",
                    tint = Color(0xFF81C784),
                    onClick = {
                        onDismiss()
                        onFindInPage()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.Computer,
                    label = if (isDesktopMode) "Versão PC (Ativo)" else "Versão PC",
                    tint = if (isDesktopMode) accentColor else Color(0xFFBA68C8),
                    isActive = isDesktopMode,
                    onClick = {
                        onDesktopModeChanged(!isDesktopMode)
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.Translate,
                    label = "Traduzir",
                    tint = Color(0xFF26A69A),
                    onClick = {
                        onDismiss()
                        onTranslatePage()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.OfflinePin,
                    label = "Salvar Offline",
                    tint = primaryTextColor,
                    onClick = {
                        onDismiss()
                        onSavePageOffline()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.Print,
                    label = "Salvar PDF",
                    tint = Color(0xFFFF8A65),
                    onClick = {
                        onDismiss()
                        onPrintPage()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.AppShortcut,
                    label = "Atalho Home",
                    tint = Color(0xFF4DD0E1),
                    onClick = {
                        onDismiss()
                        onAddToHomeScreen()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.Rounded.QrCode2,
                    label = "Código QR",
                    tint = Color(0xFF90A4AE),
                    onClick = {
                        onDismiss()
                        onShowQrCode()
                    }
                )
            )
            add(
                QuickActionItem(
                    icon = Icons.AutoMirrored.Rounded.OpenInNew,
                    label = "Abrir no App",
                    tint = Color(0xFFFFD54F),
                    onClick = {
                        onDismiss()
                        onOpenInExternalApp()
                    }
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            actionItems.forEach { action ->
                QuickActionTile(
                    item = action,
                    cardBg = cardBg,
                    textColor = primaryTextColor,
                    borderColor = panelBorder
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        SettingsDivider(isDarkMode = isDarkMode)
        Spacer(modifier = Modifier.height(16.dp))

        // 2. FERRAMENTAS E PROTEÇÃO (ESTILO TABELA AGRUPADA NATIVA)
        Text(
            text = "FERRAMENTAS & PROTEÇÃO",
            color = sectionHeaderColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, panelBorder, RoundedCornerShape(16.dp))
        ) {
            // AdBlock
            QuickSettingToggleRow(
                icon = Icons.Rounded.Shield,
                iconTint = if (adBlockEnabled) Color(0xFF64B5F6) else sectionHeaderColor,
                title = "Bloqueador de Anúncios",
                subtitle = if (adBlockEnabled) "Ativo • Rastreadores bloqueados" else "Desativado",
                checked = adBlockEnabled,
                onCheckedChange = onAdBlockChanged,
                isDarkMode = isDarkMode
            )

            HorizontalDivider(thickness = 0.8.dp, color = panelBorder)

            // Modo Escuro
            QuickSettingToggleRow(
                icon = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                iconTint = if (isDarkMode) Color(0xFFFFA726) else Color(0xFFF57C00),
                title = "Tema Escuro OLED",
                subtitle = if (isDarkMode) "Ativo • Preto absoluto" else "Modo claro ativo",
                checked = isDarkMode,
                onCheckedChange = onDarkModeChanged,
                isDarkMode = isDarkMode
            )

            HorizontalDivider(thickness = 0.8.dp, color = panelBorder)

            // Forçar Dark Mode em Sites
            QuickSettingToggleRow(
                icon = Icons.Rounded.DarkMode,
                iconTint = if (forceDarkPages) Color(0xFFBA68C8) else sectionHeaderColor,
                title = "Forçar Modo Escuro em Sites",
                subtitle = if (forceDarkPages) "Invertendo páginas web claras" else "Padrão do site",
                checked = forceDarkPages,
                onCheckedChange = onForceDarkPagesChanged,
                isDarkMode = isDarkMode
            )

            HorizontalDivider(thickness = 0.8.dp, color = panelBorder)

            // Bloquear Avisos de Cookies
            QuickSettingToggleRow(
                icon = Icons.Rounded.Cookie,
                iconTint = if (cookieBlockerEnabled) Color(0xFFFFB74D) else sectionHeaderColor,
                title = "Bloquear Avisos de Cookies",
                subtitle = if (cookieBlockerEnabled) "Ocultando popups LGPD/GDPR" else "Padrão",
                checked = cookieBlockerEnabled,
                onCheckedChange = onCookieBlockerChanged,
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. ATALHOS RÁPIDOS (Histórico, Downloads, Privacidade)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickNavButton(
                icon = Icons.Rounded.History,
                label = "Histórico",
                cardBg = cardBg,
                borderColor = panelBorder,
                tint = Color(0xFF64B5F6),
                textColor = primaryTextColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    onDismiss()
                    onOpenHistory()
                }
            )

            QuickNavButton(
                icon = Icons.Rounded.Download,
                label = "Downloads",
                cardBg = cardBg,
                borderColor = panelBorder,
                tint = Color(0xFF81C784),
                textColor = primaryTextColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    onDismiss()
                    onOpenDownloads()
                }
            )

            QuickNavButton(
                icon = Icons.Rounded.Shield,
                label = "Privacidade",
                cardBg = cardBg,
                borderColor = panelBorder,
                tint = Color(0xFF00E676),
                textColor = primaryTextColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    onDismiss()
                    onOpenPrivacyDashboard()
                }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. BOTÃO ELEGANTE: TODAS AS CONFIGURAÇÕES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, panelBorder, RoundedCornerShape(16.dp))
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Todas as Configurações",
                        color = primaryTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Buscador, papéis de parede, IA e personalização",
                        color = sectionHeaderColor,
                        fontSize = 11.5.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = secondaryTextColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

private data class QuickActionItem(
    val icon: ImageVector,
    val label: String,
    val tint: Color,
    val isActive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionTile(
    item: QuickActionItem,
    cardBg: Color,
    textColor: Color,
    borderColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = item.onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (item.isActive) item.tint.copy(alpha = 0.2f) else cardBg)
                .border(
                    width = if (item.isActive) 1.5.dp else 1.dp,
                    color = if (item.isActive) item.tint else borderColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = item.tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.label,
            color = textColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickSettingToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary,
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
private fun QuickNavButton(
    icon: ImageVector,
    label: String,
    cardBg: Color,
    borderColor: Color,
    tint: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
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
            checkedTrackColor = ApplePalette.Green,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = if (isDarkMode) Color(0x3D787880) else Color(0x28787880),
            uncheckedBorderColor = Color.Transparent
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
