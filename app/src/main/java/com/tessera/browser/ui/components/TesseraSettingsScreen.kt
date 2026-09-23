package com.tessera.browser.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Tela Completa de Configurações do Tessera Browser.
 * Estruturada profissionalmente em seções com cards agrupados:
 * 1. Mecanismo de Busca & Inicialização
 * 2. Aparência & Personalização (Temas, Papéis de Parede, Widgets, Barra Lateral)
 * 3. Privacidade & Segurança (AdBlock, Cookies/LGPD, Limpar Dados, Permissões, Safe Browsing)
 * 4. Inteligência Artificial (Chave Gemini API, Tessera AI, Botão de Barra, Pop-ups de Texto)
 * 5. Downloads & Armazenamento (Hub de Downloads, Páginas Offline)
 * 6. Sobre o Tessera Browser
 */
@Composable
fun TesseraSettingsScreen(
    isDarkMode: Boolean,
    forceDarkPages: Boolean,
    showWallpaper: Boolean,
    selectedWallpaperId: String,
    customWallpaperUri: String? = null,
    showFavoritesBar: Boolean,
    showWeatherWidget: Boolean,
    showQuotesWidget: Boolean,
    tesseraAiEnabled: Boolean,
    aiToolbarButton: Boolean,
    aiTextHighlightPrompts: Boolean,
    showSidebar: Boolean,
    autoHideSidebar: Boolean,
    adBlockEnabled: Boolean,
    cookieBlockerEnabled: Boolean,
    selectedSearchEngine: SearchEngine,
    geminiApiKey: String?,
    onDarkModeChanged: (Boolean) -> Unit,
    onForceDarkPagesChanged: (Boolean) -> Unit,
    onShowWallpaperChanged: (Boolean) -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onUploadWallpaper: () -> Unit,
    onShowFavoritesBarChanged: (Boolean) -> Unit,
    onShowWeatherWidgetChanged: (Boolean) -> Unit,
    onShowQuotesWidgetChanged: (Boolean) -> Unit,
    onTesseraAiChanged: (Boolean) -> Unit,
    onAiToolbarButtonChanged: (Boolean) -> Unit,
    onAiTextHighlightPromptsChanged: (Boolean) -> Unit,
    onShowSidebarChanged: (Boolean) -> Unit,
    onAutoHideSidebarChanged: (Boolean) -> Unit,
    onAdBlockChanged: (Boolean) -> Unit,
    onCookieBlockerChanged: (Boolean) -> Unit,
    onSearchEngineSelected: (SearchEngine) -> Unit,
    onGeminiApiKeyChanged: (String) -> Unit,
    onClearBrowsingData: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSiteSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var clearHistoryChecked by remember { mutableStateOf(true) }
    var clearCacheChecked by remember { mutableStateOf(true) }
    var clearCookiesChecked by remember { mutableStateOf(true) }

    var showGeminiKeyDialog by remember { mutableStateOf(false) }
    var tempGeminiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey.orEmpty()) }

    val screenBg = if (isDarkMode) Color(0xFF14100E) else Color(0xFFF7F8FA)
    val topBarBg = if (isDarkMode) Color(0xFF1A1513) else Color.White
    val titleColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF19191C)
    val textPrimary = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1C1C1E)
    val textSecondary = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF636366)
    val sectionHeaderColor = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0078D4)
    val cardBg = if (isDarkMode) Color(0xFF201B18) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val dividerColor = if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // TOP APP BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarBg)
                .statusBarsPadding()
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Configurações",
                        color = titleColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tessera Browser",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // SCROLLABLE SETTINGS CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // ==========================================
            // SEÇÃO 1: MECANISMO DE BUSCA
            // ==========================================
            SettingsSection(
                title = "Mecanismo de busca",
                headerColor = sectionHeaderColor
            ) {
                SettingsCard(backgroundColor = cardBg, borderColor = cardBorder) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Buscador padrão",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Escolha o mecanismo utilizado para pesquisas na barra de endereços",
                            fontSize = 12.5.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        SearchEngine.entries.forEachIndexed { index, engine ->
                            val isSelected = engine == selectedSearchEngine
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isDarkMode) Color(0xFF00E5FF).copy(alpha = 0.12f) else Color(0xFF0288D1).copy(alpha = 0.10f)
                                        } else Color.Transparent
                                    )
                                    .clickable { onSearchEngineSelected(engine) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = engine.iconRes),
                                        contentDescription = engine.displayName,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = engine.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = engine.homeUrl,
                                            fontSize = 11.5.sp,
                                            color = textSecondary
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0288D1)),
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
                            if (index < SearchEngine.entries.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = dividerColor
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SEÇÃO 2: APARÊNCIA & PERSONALIZAÇÃO
            // ==========================================
            SettingsSection(
                title = "Aparência e Personalização",
                headerColor = sectionHeaderColor
            ) {
                SettingsCard(backgroundColor = cardBg, borderColor = cardBorder) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Tema do Aplicativo (Ambiente)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ambiente (Tema do app)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = if (isDarkMode) "Modo escuro ativado" else "Modo claro ativado",
                                    fontSize = 12.5.sp,
                                    color = textSecondary
                                )
                            }
                            ThemeTogglePill(
                                isDarkMode = isDarkMode,
                                onToggle = { onDarkModeChanged(!isDarkMode) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Forçar Modo Escuro nas Páginas Web
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Forçar páginas escuras",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Inverte as cores de fundo em sites claros para leitura confortável à noite",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                            TesseraSwitch(
                                checked = forceDarkPages,
                                onCheckedChange = onForceDarkPagesChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Papel de Parede da Nova Guia
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Papel de parede da tela inicial",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Exibe imagens ou gradientes de alta qualidade na tela de início",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                            TesseraSwitch(
                                checked = showWallpaper,
                                onCheckedChange = onShowWallpaperChanged,
                                isDarkMode = isDarkMode
                            )
                        }

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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Barra de Favoritos na Tela Inicial
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Barra de favoritos no rodapé",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Exibe atalhos rápidos flutuantes na tela inicial para acesso com uma mão",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                            TesseraSwitch(
                                checked = showFavoritesBar,
                                onCheckedChange = onShowFavoritesBarChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Widgets da Tela Inicial: Clima
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WbSunny,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA726),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Widget de previsão do tempo",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Temperatura e clima local na tela inicial",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            TesseraSwitch(
                                checked = showWeatherWidget,
                                onCheckedChange = onShowWeatherWidgetChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Widgets da Tela Inicial: Cotações
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Widget de cotações financeiras",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Valores atualizados de Dólar, Euro e Bitcoin",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            TesseraSwitch(
                                checked = showQuotesWidget,
                                onCheckedChange = onShowQuotesWidgetChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Barra Lateral
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ViewSidebar,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0078D4),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Barra lateral flutuante",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Acesso rápido a abas e ferramentas na lateral da tela",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
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
                                    .padding(start = 30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ocultar automaticamente a barra lateral",
                                    color = textSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TesseraSwitch(
                                    checked = autoHideSidebar,
                                    onCheckedChange = onAutoHideSidebarChanged,
                                    isDarkMode = isDarkMode
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SEÇÃO 3: PRIVACIDADE & SEGURANÇA
            // ==========================================
            SettingsSection(
                title = "Privacidade e Segurança",
                headerColor = sectionHeaderColor
            ) {
                SettingsCard(backgroundColor = cardBg, borderColor = cardBorder) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Bloqueador de Anúncios (AdBlock)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Bloqueador de anúncios (AdBlock)",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Bloqueia anúncios intrusivos e rastreadores em tempo real",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            TesseraSwitch(
                                checked = adBlockEnabled,
                                onCheckedChange = onAdBlockChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Bloqueador de Avisos de Cookies (LGPD)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cookie,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Bloquear avisos de cookies (LGPD)",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Suprime pop-ups irritantes de aceitação de cookies",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            TesseraSwitch(
                                checked = cookieBlockerEnabled,
                                onCheckedChange = onCookieBlockerChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Limpar Dados de Navegação
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showClearDataDialog = true }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Limpar dados de navegação",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Histórico, cache de imagens e cookies salvos",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Permissões e Configurações de Sites
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenSiteSettings)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Permissões e dados de sites",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Gerencie permissões de câmera, microfone, localização e armazenamento",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Google Safe Browsing Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Navegação Segura (Google Safe Browsing)",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Proteção em tempo real contra phishing, malwares e sites maliciosos",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SEÇÃO 4: INTELIGÊNCIA ARTIFICIAL
            // ==========================================
            SettingsSection(
                title = "Inteligência Artificial (Tessera AI)",
                headerColor = sectionHeaderColor
            ) {
                SettingsCard(backgroundColor = cardBg, borderColor = cardBorder) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Chave Gemini API Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showGeminiKeyDialog = true }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0078D4),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Chave Gemini API (Google AI Studio)",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (!geminiApiKey.isNullOrBlank()) "Chave ativa (Gemini 2.0 Flash ativado)" else "Toque para configurar sua chave gratuita",
                                        fontSize = 12.sp,
                                        color = if (!geminiApiKey.isNullOrBlank()) Color(0xFF81C784) else textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Tessera AI Master Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Assistente Tessera AI",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Habilita resumos estilo Arc Search e ações contextuais",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                            TesseraSwitch(
                                checked = tesseraAiEnabled,
                                onCheckedChange = onTesseraAiChanged,
                                isDarkMode = isDarkMode
                            )
                        }

                        if (tesseraAiEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = dividerColor)

                            // Botão de IA na barra de ferramentas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Botão de IA na barra inferior",
                                        fontSize = 13.5.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Exibe o ícone circular iridescente na AirBar",
                                        fontSize = 11.5.sp,
                                        color = textSecondary
                                    )
                                }
                                TesseraSwitch(
                                    checked = aiToolbarButton,
                                    onCheckedChange = onAiToolbarButtonChanged,
                                    isDarkMode = isDarkMode
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = dividerColor)

                            // Pop-up de IA ao selecionar texto
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Avisos de IA no destaque de texto",
                                        fontSize = 13.5.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Oferece traduzir, resumir ou explicar trechos selecionados",
                                        fontSize = 11.5.sp,
                                        color = textSecondary
                                    )
                                }
                                TesseraSwitch(
                                    checked = aiTextHighlightPrompts,
                                    onCheckedChange = onAiTextHighlightPromptsChanged,
                                    isDarkMode = isDarkMode
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SEÇÃO 5: DOWNLOADS & HISTÓRICO
            // ==========================================
            SettingsSection(
                title = "Downloads e Dados Salvos",
                headerColor = sectionHeaderColor
            ) {
                SettingsCard(backgroundColor = cardBg, borderColor = cardBorder) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Hub de Downloads
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenDownloads)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Gerenciador de Downloads",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Visualizar arquivos baixados e progresso",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = dividerColor)

                        // Histórico e Favoritos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenHistory)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Histórico e Favoritos",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Acessar histórico de navegação, favoritos e páginas salvas",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SEÇÃO 6: SOBRE O TESSERA BROWSER
            // ==========================================
            SettingsSection(
                title = "Sobre",
                headerColor = sectionHeaderColor
            ) {
                SettingsCard(backgroundColor = cardBg, borderColor = cardBorder) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TESSERA BROWSER",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Versão 1.6.0",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Navegador ultrarrápido, seguro e focado em produtividade desenvolvido com Jetpack Compose, Kotlin e Material You no Android 15.",
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // DIÁLOGO DE LIMPEZA DE DADOS
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
                        color = textSecondary
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

    // DIÁLOGO DA CHAVE GEMINI API
    if (showGeminiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showGeminiKeyDialog = false },
            title = {
                Text(
                    text = "Chave da API Gemini",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Configure sua chave gratuita do Google AI Studio (aistudio.google.com) para resumos Arc ultrarrápidos e sem limites.\n\nSe deixar em branco, o Tessera usará automaticamente o motor inteligente integrado.",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tempGeminiKey,
                        onValueChange = { tempGeminiKey = it },
                        placeholder = { Text("Cole sua chave AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onGeminiApiKeyChanged(tempGeminiKey.trim())
                        showGeminiKeyDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                Row {
                    if (!geminiApiKey.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                tempGeminiKey = ""
                                onGeminiApiKeyChanged("")
                                showGeminiKeyDialog = false
                            }
                        ) {
                            Text("Remover", color = Color(0xFFE53935))
                        }
                    }
                    TextButton(onClick = { showGeminiKeyDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    headerColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = headerColor,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 6.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = cardShape)
    ) {
        content()
    }
}
