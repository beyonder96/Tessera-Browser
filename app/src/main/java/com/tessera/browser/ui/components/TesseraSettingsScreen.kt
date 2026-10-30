package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tessera.browser.R
import com.tessera.browser.ai.AiProvider
import com.tessera.browser.data.SearchEngine
import com.tessera.browser.ui.screens.DeveloperScreen
import com.tessera.browser.ui.theme.ApplePalette
import com.tessera.browser.ui.theme.AppleShapes

/**
 * Tela Completa de Configurações — Editorial Showcase (iOS 18 / Journal & Pure OLED).
 *
 * - Contraste de camadas refinado no modo claro (#FCFBF7 sobre #F7F4EB) e escuro (#141416 sobre #000000).
 * - Ícones monocromáticos padronizados em squircles contínuos de 32dp.
 * - Vitrine visual clicável de temas (Journal Claro vs Pure OLED Escuro) + Escala de Texto Web.
 * - Colapso suave de cabeçalho com histerese (zero pulo de layout).
 * - Modais Editoriais customizados para Limpeza de Dados e Chaves de API.
 */
@Composable
fun TesseraSettingsScreen(
    isDarkMode: Boolean,
    forceDarkPages: Boolean,
    pageTextZoom: Int = 100,
    tesseraAiEnabled: Boolean,
    adBlockEnabled: Boolean,
    cookieBlockerEnabled: Boolean,
    isAutoPipEnabled: Boolean = true,
    selectedSearchEngine: SearchEngine,
    geminiApiKey: String?,
    groqApiKey: String? = null,
    aiProvider: AiProvider = AiProvider.GROQ,
    isDefaultBrowser: Boolean = false,
    onRequestDefaultBrowser: () -> Unit = {},
    incognitoBiometricLock: Boolean = false,
    onIncognitoBiometricLockChanged: (Boolean) -> Unit = {},
    openLinksInExternalApps: Boolean = true,
    onOpenLinksInExternalAppsChanged: (Boolean) -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit,
    onForceDarkPagesChanged: (Boolean) -> Unit,
    onIncreaseTextZoom: () -> Unit = {},
    onDecreaseTextZoom: () -> Unit = {},
    onResetTextZoom: () -> Unit = {},
    onAutoPipChanged: (Boolean) -> Unit = {},
    onTesseraAiChanged: (Boolean) -> Unit,
    onAdBlockChanged: (Boolean) -> Unit,
    onCookieBlockerChanged: (Boolean) -> Unit,
    onSearchEngineSelected: (SearchEngine) -> Unit,
    onGeminiApiKeyChanged: (String) -> Unit,
    onGroqApiKeyChanged: (String) -> Unit = {},
    onAiProviderChanged: (AiProvider) -> Unit = {},
    onClearBrowsingData: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSiteSettings: () -> Unit,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showDeveloperScreen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = remember {
        listOf("Todos", "Navegação", "Aparência", "Privacidade", "IA", "Dados", "Sobre")
    }

    if (showDeveloperScreen) {
        DeveloperScreen(
            isDarkMode = isDarkMode,
            onBack = { showDeveloperScreen = false }
        )
        return
    }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var clearHistoryChecked by remember { mutableStateOf(true) }
    var clearCacheChecked by remember { mutableStateOf(true) }
    var clearCookiesChecked by remember { mutableStateOf(true) }

    var showGeminiKeyDialog by remember { mutableStateOf(false) }
    var tempGeminiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey.orEmpty()) }

    var showGroqKeyDialog by remember { mutableStateOf(false) }
    var tempGroqKey by remember(groqApiKey) { mutableStateOf(groqApiKey.orEmpty()) }

    // Histerese suave para o colapso do cabeçalho (elimina flicker/pulo na transição)
    var isHeaderCollapsed by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 46 && !isHeaderCollapsed) {
            isHeaderCollapsed = true
        } else if (scrollState.value < 12 && isHeaderCollapsed) {
            isHeaderCollapsed = false
        }
    }

    // PALETA EDITORIAL: JOURNAL NO CLARO (#FCFBF7 sobre #F7F4EB) & PURE OLED NO ESCURO (#141416 sobre #000000)
    val screenBg = if (isDarkMode) ApplePalette.DarkBackground else ApplePalette.LightBackground
    val topBarBg = if (isDarkMode) Color(0xFF0B0B0D) else ApplePalette.LightBackground
    val cardBg = if (isDarkMode) ApplePalette.DarkSurface else Color(0xFFFCFBF7)
    val squircleBg = if (isDarkMode) ApplePalette.DarkElevated else Color(0xFFF1EDE2)
    val pillBg = if (isDarkMode) ApplePalette.DarkSurface else Color(0xFFF2EEE3)
    val cardBorder = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline
    val dividerColor = if (isDarkMode) Color.White.copy(alpha = 0.06f) else ApplePalette.LightHairline
    val textPrimary = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val textSecondary = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val accentColor = if (isDarkMode) ApplePalette.BlueDark else ApplePalette.Blue

    fun matchesQuery(vararg keywords: String): Boolean {
        if (searchQuery.isBlank()) return true
        val q = searchQuery.trim().lowercase()
        return keywords.any { it.lowercase().contains(q) }
    }

    val showNavigation = (selectedCategoryIndex == 0 || selectedCategoryIndex == 1) &&
            matchesQuery("navegador", "padrão", "busca", "google", "duckduckgo", "bing", "brave", "buscador", "mecanismo", "pesquisa", "pip", "vídeo", "apps externos")

    val showAppearance = (selectedCategoryIndex == 0 || selectedCategoryIndex == 2) &&
            matchesQuery("aparência", "tema", "escuro", "claro", "oled", "journal", "zoom", "texto", "fonte", "tamanho", "páginas escuras")

    val showPrivacy = (selectedCategoryIndex == 0 || selectedCategoryIndex == 3) &&
            matchesQuery("privacidade", "adblock", "anúncios", "cookies", "lgpd", "biometria", "anônimo", "limpar dados", "segurança", "safe browsing", "permissões", "câmera", "microfone", "localização")

    val showAi = (selectedCategoryIndex == 0 || selectedCategoryIndex == 4) &&
            matchesQuery("ia", "inteligência", "groq", "gemini", "llama", "resumo", "assistente", "chave", "api")

    val showDownloads = (selectedCategoryIndex == 0 || selectedCategoryIndex == 5) &&
            matchesQuery("downloads", "armazenamento", "offline", "backup", "restaurar", "exportar", "histórico", "favoritos")

    val showAbout = (selectedCategoryIndex == 0 || selectedCategoryIndex == 6) &&
            matchesQuery("sobre", "versão", "desenvolvedor", "tessera")

    val hasAnyResult = showNavigation || showAppearance || showPrivacy || showAi || showDownloads || showAbout

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // =========================================================
        // TOP BAR EDITORIAL COM COLAPSO FLUIDO & CHIPS DE CATEGORIA
        // =========================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarBg)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Voltar",
                            tint = textPrimary
                        )
                    }
                    Text(
                        text = if (isHeaderCollapsed) "Configurações" else "Tessera Browser",
                        color = if (isHeaderCollapsed) textPrimary else textSecondary,
                        fontSize = if (isHeaderCollapsed) 17.sp else 13.sp,
                        fontWeight = if (isHeaderCollapsed) FontWeight.SemiBold else FontWeight.Medium
                    )
                }

                if (searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { searchQuery = "" }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Limpar",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !isHeaderCollapsed,
                enter = expandVertically(tween(220)) + fadeIn(tween(180)),
                exit = shrinkVertically(tween(220)) + fadeOut(tween(140))
            ) {
                Text(
                    text = "Configurações",
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp)
                )
            }

            // Campo de Pesquisa Editorial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(pillBg)
                    .border(0.7.dp, cardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Buscar ajustes...",
                            color = textSecondary,
                            fontSize = 13.5.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpar texto",
                        tint = textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }

            // Pílulas de Categoria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEachIndexed { index, categoryName ->
                    val isSelected = selectedCategoryIndex == index
                    val selectedBg = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
                    val selectedFg = if (isDarkMode) Color.Black else Color.White

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) selectedBg else pillBg)
                            .border(
                                width = 0.7.dp,
                                color = if (isSelected) selectedBg else cardBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedCategoryIndex = index }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) selectedFg else textPrimary
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.7.dp, color = cardBorder)
        }

        // =========================================================
        // CONTEÚDO PRINCIPAL EM SEÇÕES AGRUPADAS
        // =========================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // ---------------------------------------------------------
            // SEÇÃO 1: APARÊNCIA & LEITURA WEB (VITRINE VISUAL)
            // ---------------------------------------------------------
            if (showAppearance) {
                EditorialSection(title = "Aparência & Leitura", kickerColor = textSecondary) {
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tema do Ambiente",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Contraste editorial calibrado para dia e noite",
                                fontSize = 12.sp,
                                color = textSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Cartões Visuais Clicáveis Lado a Lado: Journal Claro vs Pure OLED Escuro
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ThemePreviewCard(
                                    title = "Journal",
                                    subtitle = "Papel creme",
                                    previewSurface = Color(0xFFF7F4EB),
                                    previewCard = Color(0xFFFCFBF7),
                                    previewInk = Color(0xFF1F1D1A),
                                    previewHairline = Color(0xFFDDD8CB),
                                    icon = Icons.Rounded.LightMode,
                                    isSelected = !isDarkMode,
                                    accentColor = accentColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    cardBorder = cardBorder,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onDarkModeChanged(false) }
                                )

                                ThemePreviewCard(
                                    title = "Pure OLED",
                                    subtitle = "Preto absoluto",
                                    previewSurface = Color(0xFF000000),
                                    previewCard = Color(0xFF18181C),
                                    previewInk = Color(0xFFF5F5F7),
                                    previewHairline = Color(0xFF2C2C32),
                                    icon = Icons.Rounded.DarkMode,
                                    isSelected = isDarkMode,
                                    accentColor = accentColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    cardBorder = cardBorder,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onDarkModeChanged(true) }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            // Escala Padrão de Texto das Páginas Web (A- / 100% / A+)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MonochromeSquircleIcon(
                                        icon = Icons.Rounded.FormatSize,
                                        squircleBg = squircleBg,
                                        iconTint = textPrimary
                                    )
                                    Column {
                                        Text(
                                            text = "Tamanho do texto web",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "Escala global das fontes nos sites",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(squircleBg)
                                        .border(0.6.dp, cardBorder, RoundedCornerShape(11.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 32.dp, height = 36.dp)
                                            .clickable(enabled = pageTextZoom > 60) { onDecreaseTextZoom() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "A-",
                                            color = if (pageTextZoom > 60) textPrimary else textSecondary.copy(alpha = 0.4f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clickable { onResetTextZoom() }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${pageTextZoom}%",
                                            color = if (pageTextZoom != 100) accentColor else textPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(width = 32.dp, height = 36.dp)
                                            .clickable(enabled = pageTextZoom < 200) { onIncreaseTextZoom() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "A+",
                                            color = if (pageTextZoom < 200) textPrimary else textSecondary.copy(alpha = 0.4f),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            // Forçar Páginas Escuras
                            EditorialSettingToggleRow(
                                icon = Icons.Rounded.DarkMode,
                                title = "Forçar páginas escuras",
                                subtitle = "Adapta sites claros automaticamente para leitura noturna",
                                checked = forceDarkPages,
                                onCheckedChange = onForceDarkPagesChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // SEÇÃO 2: NAVEGAÇÃO & BUSCA
            // ---------------------------------------------------------
            if (showNavigation) {
                EditorialSection(title = "Navegação & Busca", kickerColor = textSecondary) {
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            EditorialSettingClickRow(
                                icon = if (isDefaultBrowser) Icons.Rounded.CheckCircle else Icons.Rounded.Language,
                                iconTint = if (isDefaultBrowser) ApplePalette.Green else textPrimary,
                                squircleBg = if (isDefaultBrowser) ApplePalette.Green.copy(alpha = 0.14f) else squircleBg,
                                title = "Navegador padrão do sistema",
                                subtitle = if (isDefaultBrowser) "Tessera é seu navegador padrão" else "Toque para definir como padrão no Android",
                                subtitleColor = if (isDefaultBrowser) ApplePalette.Green else textSecondary,
                                showChevron = !isDefaultBrowser,
                                textPrimary = textPrimary,
                                onClick = onRequestDefaultBrowser
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingToggleRow(
                                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                                title = "Abrir links em apps instalados",
                                subtitle = "Direciona YouTube, Instagram, WhatsApp e X para seus apps nativos",
                                checked = openLinksInExternalApps,
                                onCheckedChange = onOpenLinksInExternalAppsChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingToggleRow(
                                icon = Icons.Rounded.PictureInPictureAlt,
                                title = "Picture-in-Picture automático",
                                subtitle = "Continua vídeos em janela flutuante ao minimizar o navegador",
                                checked = isAutoPipEnabled,
                                onCheckedChange = onAutoPipChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )
                        }
                    }

                    // Mecanismo de Busca
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Mecanismo de Busca",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Motor utilizado na barra de endereços e sugestões",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            SearchEngine.entries.forEachIndexed { index, engine ->
                                val isSelected = engine == selectedSearchEngine
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.10f) else Color.Transparent)
                                        .clickable { onSearchEngineSelected(engine) }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .background(squircleBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = engine.iconRes),
                                                contentDescription = engine.displayName,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = engine.displayName,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = engine.homeUrl.removePrefix("https://"),
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
                                                .background(accentColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selecionado",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                if (index < SearchEngine.entries.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = dividerColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // SEÇÃO 3: PRIVACIDADE & SEGURANÇA
            // ---------------------------------------------------------
            if (showPrivacy) {
                EditorialSection(title = "Privacidade & Segurança", kickerColor = textSecondary) {
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            EditorialSettingToggleRow(
                                icon = Icons.Rounded.Shield,
                                title = "Bloqueador de anúncios e rastreadores",
                                subtitle = "Filtra scripts invasivos e telemetria em tempo real",
                                checked = adBlockEnabled,
                                onCheckedChange = onAdBlockChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingToggleRow(
                                icon = Icons.Rounded.Cookie,
                                title = "Suprimir avisos de cookies",
                                subtitle = "Oculta banners repetitivos de consentimento LGPD/GDPR",
                                checked = cookieBlockerEnabled,
                                onCheckedChange = onCookieBlockerChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingToggleRow(
                                icon = Icons.Rounded.Fingerprint,
                                title = "Trancar modo anônimo com biometria",
                                subtitle = "Solicita impressão digital ou PIN ao retornar ao app",
                                checked = incognitoBiometricLock,
                                onCheckedChange = onIncognitoBiometricLockChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.Lock,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Permissões e dados de sites",
                                subtitle = "Câmera, microfone, localização, pop-ups e JavaScript por domínio",
                                subtitleColor = textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = onOpenSiteSettings
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MonochromeSquircleIcon(
                                    icon = Icons.Rounded.Security,
                                    squircleBg = ApplePalette.Green.copy(alpha = 0.14f),
                                    iconTint = ApplePalette.Green
                                )
                                Column {
                                    Text(
                                        text = "Google Safe Browsing",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Proteção ativa contra phishing e páginas maliciosas",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.DeleteOutline,
                                iconTint = ApplePalette.Red,
                                squircleBg = ApplePalette.Red.copy(alpha = 0.12f),
                                title = "Limpar dados de navegação",
                                subtitle = "Remover histórico, cache de imagens e cookies salvos",
                                subtitleColor = textSecondary,
                                showChevron = true,
                                textPrimary = ApplePalette.Red,
                                onClick = { showClearDataDialog = true }
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // SEÇÃO 4: INTELIGÊNCIA ARTIFICIAL
            // ---------------------------------------------------------
            if (showAi) {
                EditorialSection(title = "Inteligência Artificial", kickerColor = textSecondary) {
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            EditorialSettingToggleRow(
                                icon = Icons.Rounded.AutoAwesome,
                                title = "Assistente Tessera AI",
                                subtitle = "Síntese instantânea de artigos e respostas contextuais",
                                checked = tesseraAiEnabled,
                                onCheckedChange = onTesseraAiChanged,
                                squircleBg = squircleBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                isDarkMode = isDarkMode
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            Text(
                                text = "Motor de Inferência",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AiProviderCard(
                                    title = "Groq Cloud",
                                    subtitle = "Llama 3.3 70B",
                                    isSelected = aiProvider == AiProvider.GROQ,
                                    accentColor = accentColor,
                                    squircleBg = squircleBg,
                                    cardBorder = cardBorder,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onAiProviderChanged(AiProvider.GROQ) }
                                )

                                AiProviderCard(
                                    title = "Google Gemini",
                                    subtitle = "Gemini Flash",
                                    isSelected = aiProvider == AiProvider.GEMINI,
                                    accentColor = accentColor,
                                    squircleBg = squircleBg,
                                    cardBorder = cardBorder,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onAiProviderChanged(AiProvider.GEMINI) }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.Key,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Chave Groq Cloud",
                                subtitle = if (!groqApiKey.isNullOrBlank()) "Configurada • console.groq.com" else "Toque para adicionar chave gratuita",
                                subtitleColor = if (!groqApiKey.isNullOrBlank()) ApplePalette.Green else textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = { showGroqKeyDialog = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.Key,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Chave Google Gemini",
                                subtitle = if (!geminiApiKey.isNullOrBlank()) "Configurada • Google AI Studio" else "Toque para adicionar chave gratuita",
                                subtitleColor = if (!geminiApiKey.isNullOrBlank()) ApplePalette.Green else textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = { showGeminiKeyDialog = true }
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // SEÇÃO 5: DADOS, DOWNLOADS & BACKUP
            // ---------------------------------------------------------
            if (showDownloads) {
                EditorialSection(title = "Dados & Armazenamento", kickerColor = textSecondary) {
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            EditorialSettingClickRow(
                                icon = Icons.Rounded.History,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Histórico, Favoritos e Páginas Offline",
                                subtitle = "Gerenciar registros de navegação e leituras salvas",
                                subtitleColor = textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = onOpenHistory
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.Download,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Gerenciador de Downloads",
                                subtitle = "Arquivos baixados no dispositivo",
                                subtitleColor = textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = onOpenDownloads
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.Backup,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Exportar backup completo",
                                subtitle = "Salvar favoritos, histórico, espaços e ajustes em JSON",
                                subtitleColor = textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = onExportBackup
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), color = dividerColor)

                            EditorialSettingClickRow(
                                icon = Icons.Rounded.Restore,
                                iconTint = textPrimary,
                                squircleBg = squircleBg,
                                title = "Restaurar backup",
                                subtitle = "Importar dados a partir de um arquivo .json do Tessera",
                                subtitleColor = textSecondary,
                                showChevron = true,
                                textPrimary = textPrimary,
                                onClick = onImportBackup
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // SEÇÃO 6: SOBRE O TESSERA BROWSER
            // ---------------------------------------------------------
            if (showAbout) {
                EditorialSection(title = "Sobre", kickerColor = textSecondary) {
                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDeveloperScreen = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_developer_avatar),
                                    contentDescription = "Desenvolvedor",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, cardBorder, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = "Sobre o Desenvolvedor",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Filosofia de design, arquitetura e ecossistema Tessera",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    EditorialCard(backgroundColor = cardBg, borderColor = cardBorder) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_tessera_browser_logo),
                                contentDescription = "Tessera Browser Logo",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(AppleShapes.SquircleMedium)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_tessera_header_logo),
                                contentDescription = "Tessera",
                                colorFilter = ColorFilter.tint(textPrimary),
                                modifier = Modifier.height(34.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "BROWSER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 4.sp,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isDarkMode) "Versão 3.3.0 • Pure OLED" else "Versão 3.3.0 • Edição Journal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Projetado para foco, privacidade e leitura calma.",
                                fontSize = 12.5.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Estado Vazio na Busca
            if (!hasAnyResult) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MonochromeSquircleIcon(
                        icon = Icons.Rounded.Search,
                        squircleBg = squircleBg,
                        iconTint = textSecondary
                    )
                    Text(
                        text = "Nenhum ajuste encontrado",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Text(
                        text = "Tente buscar por tema, texto, adblock ou chave",
                        fontSize = 12.5.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // =========================================================
    // MODAL EDITORIAL 1: LIMPAR DADOS DE NAVEGAÇÃO
    // =========================================================
    if (showClearDataDialog) {
        Dialog(onDismissRequest = { showClearDataDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(26.dp),
                color = cardBg,
                border = BorderStroke(0.8.dp, cardBorder),
                shadowElevation = 22.dp
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MonochromeSquircleIcon(
                            icon = Icons.Rounded.DeleteOutline,
                            squircleBg = ApplePalette.Red.copy(alpha = 0.14f),
                            iconTint = ApplePalette.Red
                        )
                        Column {
                            Text(
                                text = "Limpar dados",
                                color = textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Escolha o que deseja remover agora",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditorialCheckOption(
                            label = "Histórico de navegação",
                            checked = clearHistoryChecked,
                            squircleBg = squircleBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            accentColor = ApplePalette.Red,
                            onToggle = { clearHistoryChecked = !clearHistoryChecked }
                        )
                        EditorialCheckOption(
                            label = "Cache de páginas e imagens",
                            checked = clearCacheChecked,
                            squircleBg = squircleBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            accentColor = ApplePalette.Red,
                            onToggle = { clearCacheChecked = !clearCacheChecked }
                        )
                        EditorialCheckOption(
                            label = "Cookies e sessões de sites",
                            checked = clearCookiesChecked,
                            squircleBg = squircleBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            accentColor = ApplePalette.Red,
                            onToggle = { clearCookiesChecked = !clearCookiesChecked }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(squircleBg)
                                .clickable { showClearDataDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancelar",
                                color = textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        val canConfirm = clearHistoryChecked || clearCacheChecked || clearCookiesChecked
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (canConfirm) ApplePalette.Red else ApplePalette.Red.copy(alpha = 0.35f))
                                .clickable(enabled = canConfirm) {
                                    showClearDataDialog = false
                                    onClearBrowsingData(clearHistoryChecked, clearCookiesChecked, clearCacheChecked)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Limpar Agora",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // MODAL EDITORIAL 2: CHAVE GEMINI API
    // =========================================================
    if (showGeminiKeyDialog) {
        EditorialApiKeyDialog(
            title = "Chave Google Gemini",
            description = "Insira sua chave gratuita gerada no Google AI Studio (aistudio.google.com). A chave permanece armazenada apenas no seu dispositivo.",
            placeholder = "AIzaSy...",
            value = tempGeminiKey,
            hasSavedKey = !geminiApiKey.isNullOrBlank(),
            cardBg = cardBg,
            cardBorder = cardBorder,
            squircleBg = squircleBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onValueChange = { tempGeminiKey = it },
            onSave = {
                onGeminiApiKeyChanged(tempGeminiKey.trim())
                showGeminiKeyDialog = false
            },
            onRemove = {
                tempGeminiKey = ""
                onGeminiApiKeyChanged("")
                showGeminiKeyDialog = false
            },
            onDismiss = { showGeminiKeyDialog = false }
        )
    }

    // =========================================================
    // MODAL EDITORIAL 3: CHAVE GROQ CLOUD API
    // =========================================================
    if (showGroqKeyDialog) {
        EditorialApiKeyDialog(
            title = "Chave Groq Cloud",
            description = "Insira sua chave gratuita de console.groq.com/keys para resumos instantâneos com Llama 3.3 70B. Armazenada apenas localmente.",
            placeholder = "gsk_...",
            value = tempGroqKey,
            hasSavedKey = !groqApiKey.isNullOrBlank(),
            cardBg = cardBg,
            cardBorder = cardBorder,
            squircleBg = squircleBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onValueChange = { tempGroqKey = it },
            onSave = {
                onGroqApiKeyChanged(tempGroqKey.trim())
                showGroqKeyDialog = false
            },
            onRemove = {
                tempGroqKey = ""
                onGroqApiKeyChanged("")
                showGroqKeyDialog = false
            },
            onDismiss = { showGroqKeyDialog = false }
        )
    }
}

@Composable
private fun EditorialSection(
    title: String,
    kickerColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = kickerColor,
            letterSpacing = 1.1.sp,
            modifier = Modifier.padding(start = 6.dp)
        )
        content()
    }
}

@Composable
private fun EditorialCard(
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
            .border(width = 0.8.dp, color = borderColor, shape = cardShape)
    ) {
        content()
    }
}

@Composable
private fun MonochromeSquircleIcon(
    icon: ImageVector,
    squircleBg: Color,
    iconTint: Color
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(squircleBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun EditorialSettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    squircleBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonochromeSquircleIcon(
                icon = icon,
                squircleBg = squircleBg,
                iconTint = textPrimary
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textSecondary,
                    lineHeight = 16.sp
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
private fun EditorialSettingClickRow(
    icon: ImageVector,
    iconTint: Color,
    squircleBg: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    showChevron: Boolean,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonochromeSquircleIcon(
                icon = icon,
                squircleBg = squircleBg,
                iconTint = iconTint
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor,
                    lineHeight = 16.sp
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = subtitleColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    title: String,
    subtitle: String,
    previewSurface: Color,
    previewCard: Color,
    previewInk: Color,
    previewHairline: Color,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBorder: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val outerShape = RoundedCornerShape(16.dp)
    val borderColor = if (isSelected) accentColor else cardBorder
    val borderWidth = if (isSelected) 1.6.dp else 0.8.dp

    Column(
        modifier = modifier
            .clip(outerShape)
            .border(borderWidth, borderColor, outerShape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mini Mockup Visual do Ambiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(previewSurface)
                .border(0.7.dp, previewHairline, RoundedCornerShape(11.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 26.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(previewInk.copy(alpha = 0.80f))
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(previewInk.copy(alpha = 0.25f))
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(previewCard)
                        .border(0.6.dp, previewHairline, RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(previewInk.copy(alpha = 0.45f))
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else textSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Column {
                    Text(
                        text = title,
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        color = textSecondary,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Ativo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiProviderCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    squircleBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(13.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) accentColor.copy(alpha = 0.11f) else squircleBg)
            .border(
                width = if (isSelected) 1.3.dp else 0.7.dp,
                color = if (isSelected) accentColor else cardBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) accentColor else textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textSecondary
            )
        }
    }
}

@Composable
private fun EditorialCheckOption(
    label: String,
    checked: Boolean,
    squircleBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    accentColor: Color,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(squircleBg)
            .border(
                width = 0.7.dp,
                color = if (checked) accentColor.copy(alpha = 0.45f) else cardBorder,
                shape = RoundedCornerShape(13.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) accentColor else Color.Transparent)
                .border(
                    width = 1.2.dp,
                    color = if (checked) accentColor else cardBorder,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorialApiKeyDialog(
    title: String,
    description: String,
    placeholder: String,
    value: String,
    hasSavedKey: Boolean,
    cardBg: Color,
    cardBorder: Color,
    squircleBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(26.dp),
            color = cardBg,
            border = BorderStroke(0.8.dp, cardBorder),
            shadowElevation = 22.dp
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MonochromeSquircleIcon(
                        icon = Icons.Rounded.Key,
                        squircleBg = squircleBg,
                        iconTint = textPrimary
                    )
                    Text(
                        text = title,
                        color = textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = description,
                    color = textSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(squircleBg)
                        .border(0.8.dp, cardBorder, RoundedCornerShape(13.dp))
                        .padding(horizontal = 13.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = textSecondary,
                            fontSize = 13.5.sp
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasSavedKey) {
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(ApplePalette.Red.copy(alpha = 0.12f))
                                .clickable(onClick = onRemove)
                                .padding(horizontal = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Remover",
                                color = ApplePalette.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(squircleBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancelar",
                            color = textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(accentColor)
                            .clickable(onClick = onSave),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Salvar",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
