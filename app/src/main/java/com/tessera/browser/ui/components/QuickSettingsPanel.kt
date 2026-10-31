package com.tessera.browser.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.ui.theme.ApplePalette

/**
 * Menu Rápido Editorial — Floating Bento Card (iOS 18 / Arc Edition).
 *
 * Cartão flutuante destacado das bordas (`28.dp`) com estética monocromática
 * calibrada para os temas Journal Claro (#FCFBF7) e Pure OLED Escuro (#141416),
 * sem rolagem horizontal e sem switches gigantes.
 */
@Composable
fun QuickSettingsPanel(
    isDarkMode: Boolean,
    isIncognito: Boolean = false,
    forceDarkPages: Boolean,
    adBlockEnabled: Boolean = true,
    isDesktopMode: Boolean = false,
    cookieBlockerEnabled: Boolean = true,
    isWebPageActive: Boolean = false,
    currentUrl: String = "",
    isSecureSite: Boolean = true,
    canGoBack: Boolean = false,
    canGoForward: Boolean = false,
    isBookmarked: Boolean = false,
    isReaderModeActive: Boolean = false,
    isListeningPage: Boolean = false,
    isVideoPlaying: Boolean = false,
    pageTextZoom: Int = 100,
    privacyBlockedCount: Int = 0,
    onGoBack: () -> Unit = {},
    onGoForward: () -> Unit = {},
    onReload: () -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onListenPage: () -> Unit = {},
    onOpenAi: () -> Unit = {},
    onToggleIncognito: () -> Unit = {},
    onIncreaseTextZoom: () -> Unit = {},
    onDecreaseTextZoom: () -> Unit = {},
    onResetTextZoom: () -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit,
    onForceDarkPagesChanged: (Boolean) -> Unit,
    onAdBlockChanged: (Boolean) -> Unit,
    onDesktopModeChanged: (Boolean) -> Unit = {},
    onCookieBlockerChanged: (Boolean) -> Unit = {},
    onFindInPage: () -> Unit = {},
    onSharePage: () -> Unit = {},
    onPrintPage: () -> Unit = {},
    onAddToHomeScreen: () -> Unit = {},
    onSavePageOffline: () -> Unit = {},
    onShowQrCode: () -> Unit = {},
    onOpenDesktopSync: () -> Unit = {},
    onTranslatePage: () -> Unit = {},
    onOpenSiteSettings: () -> Unit = {},
    onOpenPrivacyDashboard: () -> Unit = {},
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit = {},
    onOpenReaderMode: () -> Unit = {},
    onOpenFullSettings: () -> Unit = {},
    onEnterPip: () -> Unit = {},
    onOpenInExternalApp: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val useDarkTokens = isDarkMode || isIncognito

    val cardBg = when {
        isIncognito -> Color(0xFF151122)
        isDarkMode -> ApplePalette.DarkSurface
        else -> Color(0xFFFCFBF7)
    }
    val cardBorder = when {
        isIncognito -> ApplePalette.Purple.copy(alpha = 0.28f)
        isDarkMode -> ApplePalette.DarkHairline
        else -> ApplePalette.LightHairline
    }
    val tileBg = when {
        isIncognito -> Color(0xFF1F1932)
        isDarkMode -> ApplePalette.DarkElevated
        else -> Color(0xFFF2EEE3)
    }
    val tileBorder = when {
        isIncognito -> ApplePalette.Purple.copy(alpha = 0.18f)
        isDarkMode -> Color.White.copy(alpha = 0.06f)
        else -> Color(0xFFE2DDD0)
    }
    val textPrimary = if (useDarkTokens) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val textSecondary = if (useDarkTokens) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val accentColor = when {
        isIncognito -> ApplePalette.Purple
        isDarkMode -> ApplePalette.BlueDark
        else -> ApplePalette.Blue
    }

    val cleanDomain = remember(currentUrl, isWebPageActive) {
        if (!isWebPageActive || currentUrl.isBlank()) {
            "Tessera Zen"
        } else {
            try {
                val uri = android.net.Uri.parse(
                    if (currentUrl.startsWith("http")) currentUrl else "https://$currentUrl"
                )
                uri.host?.removePrefix("www.") ?: currentUrl.substringBefore('/')
            } catch (e: Exception) {
                currentUrl
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(28.dp),
            color = cardBg,
            border = BorderStroke(0.8.dp, cardBorder),
            shadowElevation = if (useDarkTokens) 24.dp else 14.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Drag Handle Minimalista
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 34.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(textSecondary.copy(alpha = 0.25f))
                )

                // =========================================================
                // 1. HEADER CONTEXTUAL: DOMÍNIO/PERMISSÕES + ZOOM A-/A+ + ⚙️
                // =========================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Domínio Ativo & Permissões do Site
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tileBg)
                            .border(0.6.dp, tileBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                onDismiss()
                                if (isWebPageActive) onOpenSiteSettings() else onOpenPrivacyDashboard()
                            }
                            .padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                !isWebPageActive -> Icons.Rounded.Shield
                                isSecureSite -> Icons.Rounded.Lock
                                else -> Icons.Rounded.LockOpen
                            },
                            contentDescription = "Segurança e permissões",
                            tint = when {
                                !isWebPageActive -> accentColor
                                isSecureSite -> ApplePalette.Green
                                else -> ApplePalette.Red
                            },
                            modifier = Modifier.size(15.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cleanDomain,
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isWebPageActive) {
                                    "Permissões do site"
                                } else if (privacyBlockedCount > 0) {
                                    "$privacyBlockedCount rastreadores barrados"
                                } else {
                                    "Proteção ativa"
                                },
                                color = textSecondary,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Pílula Inline de Zoom de Texto (A- | 100% | A+)
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tileBg)
                            .border(0.6.dp, tileBorder, RoundedCornerShape(14.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 32.dp, height = 40.dp)
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
                                .height(40.dp)
                                .clickable { onResetTextZoom() }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${pageTextZoom}%",
                                color = if (pageTextZoom != 100) accentColor else textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 32.dp, height = 40.dp)
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

                    // Botão Engrenagem -> Configurações Completas
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tileBg)
                            .border(0.6.dp, tileBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                onDismiss()
                                onOpenFullSettings()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Configurações",
                            tint = textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // =========================================================
                // 2. NAVEGAÇÃO TÁTIL: 5 BOTÕES CIRCULARES DE TOQUE RÁPIDO
                // =========================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TactileNavButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        label = "Voltar",
                        enabled = canGoBack,
                        isActive = false,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onGoBack()
                        }
                    )
                    TactileNavButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        label = "Avançar",
                        enabled = canGoForward,
                        isActive = false,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onGoForward()
                        }
                    )
                    TactileNavButton(
                        icon = Icons.Rounded.Refresh,
                        label = "Recarregar",
                        enabled = isWebPageActive,
                        isActive = false,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onReload()
                        }
                    )
                    TactileNavButton(
                        icon = if (isBookmarked) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        label = if (isBookmarked) "Salvo" else "Favoritar",
                        enabled = isWebPageActive,
                        isActive = isBookmarked && isWebPageActive,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onToggleBookmark()
                        }
                    )
                    TactileNavButton(
                        icon = Icons.Rounded.Headphones,
                        label = if (isListeningPage) "Ouvindo" else "Ouvir",
                        enabled = isWebPageActive,
                        isActive = isListeningPage,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onListenPage()
                        }
                    )
                }

                // =========================================================
                // 3. DOIS HERO CARDS LADO A LADO: TESSERA AI & ANÔNIMO
                // =========================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hero Card 1: Tessera AI
                    HeroFeatureCard(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Tessera AI",
                        subtitle = if (isWebPageActive) "Resumir & perguntar" else "Síntese inteligente",
                        isActive = false,
                        isHighlightAccent = true,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onOpenAi()
                        }
                    )

                    // Hero Card 2: Modo Anônimo
                    HeroFeatureCard(
                        icon = Icons.Rounded.VisibilityOff,
                        title = "Modo Anônimo",
                        subtitle = if (isIncognito) "Ativo • Sem rastros" else "Navegação privada",
                        isActive = isIncognito,
                        isHighlightAccent = false,
                        tileBg = tileBg,
                        tileBorder = tileBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = ApplePalette.Purple,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onToggleIncognito()
                        }
                    )
                }

                // =========================================================
                // 4. GRADE BENTO MONOCROMÁTICA 4x2 (SEM ROLAGEM HORIZONTAL)
                // =========================================================
                val gridRow1 = remember(isWebPageActive, isReaderModeActive, isDesktopMode) {
                    listOf(
                        BentoActionSpec(
                            icon = Icons.Rounded.Share,
                            label = "Compartilhar",
                            enabled = isWebPageActive,
                            isActive = false,
                            action = ActionKind.SHARE
                        ),
                        BentoActionSpec(
                            icon = Icons.AutoMirrored.Rounded.MenuBook,
                            label = "Leitura",
                            enabled = isWebPageActive,
                            isActive = isReaderModeActive,
                            action = ActionKind.READER
                        ),
                        BentoActionSpec(
                            icon = Icons.Rounded.Search,
                            label = "Buscar",
                            enabled = isWebPageActive,
                            isActive = false,
                            action = ActionKind.FIND
                        ),
                        BentoActionSpec(
                            icon = Icons.Rounded.Computer,
                            label = "Versão PC",
                            enabled = true,
                            isActive = isDesktopMode,
                            action = ActionKind.DESKTOP
                        )
                    )
                }

                val gridRow2 = remember(isWebPageActive, isVideoPlaying) {
                    listOf(
                        BentoActionSpec(
                            icon = Icons.Rounded.Translate,
                            label = "Traduzir",
                            enabled = isWebPageActive,
                            isActive = false,
                            action = ActionKind.TRANSLATE
                        ),
                        BentoActionSpec(
                            icon = Icons.Rounded.Print,
                            label = "Salvar PDF",
                            enabled = isWebPageActive,
                            isActive = false,
                            action = ActionKind.PRINT
                        ),
                        BentoActionSpec(
                            icon = Icons.Rounded.Sync,
                            label = "Sync PC",
                            enabled = true,
                            isActive = false,
                            action = ActionKind.DESKTOP_SYNC
                        ),
                        if (isVideoPlaying) {
                            BentoActionSpec(
                                icon = Icons.Rounded.PictureInPictureAlt,
                                label = "Vídeo PiP",
                                enabled = true,
                                isActive = true,
                                action = ActionKind.PIP
                            )
                        } else {
                            BentoActionSpec(
                                icon = Icons.Rounded.QrCode2,
                                label = "Código QR",
                                enabled = isWebPageActive,
                                isActive = false,
                                action = ActionKind.QR
                            )
                        }
                    )
                }

                val handleBentoAction: (ActionKind) -> Unit = { kind ->
                    when (kind) {
                        ActionKind.SHARE -> { onDismiss(); onSharePage() }
                        ActionKind.READER -> { onDismiss(); onOpenReaderMode() }
                        ActionKind.FIND -> { onDismiss(); onFindInPage() }
                        ActionKind.DESKTOP -> { onDesktopModeChanged(!isDesktopMode) }
                        ActionKind.TRANSLATE -> { onDismiss(); onTranslatePage() }
                        ActionKind.PRINT -> { onDismiss(); onPrintPage() }
                        ActionKind.OFFLINE -> { onDismiss(); onSavePageOffline() }
                        ActionKind.DESKTOP_SYNC -> { onDismiss(); onOpenDesktopSync() }
                        ActionKind.PIP -> { onDismiss(); onEnterPip() }
                        ActionKind.QR -> { onDismiss(); onShowQrCode() }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridRow1.forEach { spec ->
                            BentoGridTile(
                                spec = spec,
                                tileBg = tileBg,
                                tileBorder = tileBorder,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                accentColor = accentColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleBentoAction(spec.action) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridRow2.forEach { spec ->
                            BentoGridTile(
                                spec = spec,
                                tileBg = tileBg,
                                tileBorder = tileBorder,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                accentColor = accentColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleBentoAction(spec.action) }
                            )
                        }
                    }
                }

                // Pílulas Contextuais Discretas da Página Ativa (Atalho Home & Abrir em App)
                if (isWebPageActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlimContextPill(
                            icon = Icons.Rounded.AppShortcut,
                            label = "Adicionar à Tela Inicial",
                            tileBg = tileBg,
                            tileBorder = tileBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                onAddToHomeScreen()
                            }
                        )
                        SlimContextPill(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            label = "Abrir no App",
                            tileBg = tileBg,
                            tileBorder = tileBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                onOpenInExternalApp()
                            }
                        )
                    }
                }

                // =========================================================
                // 5. TOGGLES TÁTEIS BENTO 2x2 (SEM SWITCHES GIGANTES)
                // =========================================================
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BentoToggleCard(
                            icon = Icons.Rounded.Shield,
                            title = "AdBlock",
                            stateText = if (adBlockEnabled) "Ativo" else "Inativo",
                            checked = adBlockEnabled,
                            tileBg = tileBg,
                            tileBorder = tileBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f),
                            onToggle = { onAdBlockChanged(!adBlockEnabled) }
                        )
                        BentoToggleCard(
                            icon = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            title = "Tema",
                            stateText = if (isDarkMode) "Pure OLED" else "Journal",
                            checked = isDarkMode,
                            tileBg = tileBg,
                            tileBorder = tileBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f),
                            onToggle = { onDarkModeChanged(!isDarkMode) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BentoToggleCard(
                            icon = Icons.Rounded.DarkMode,
                            title = "Sites Escuros",
                            stateText = if (forceDarkPages) "Forçado" else "Padrão",
                            checked = forceDarkPages,
                            tileBg = tileBg,
                            tileBorder = tileBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f),
                            onToggle = { onForceDarkPagesChanged(!forceDarkPages) }
                        )
                        BentoToggleCard(
                            icon = Icons.Rounded.Cookie,
                            title = "Anti-Cookies",
                            stateText = if (cookieBlockerEnabled) "Ativo" else "Inativo",
                            checked = cookieBlockerEnabled,
                            tileBg = tileBg,
                            tileBorder = tileBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f),
                            onToggle = { onCookieBlockerChanged(!cookieBlockerEnabled) }
                        )
                    }
                }

                // =========================================================
                // 6. RODAPÉ UNIFICADO: HISTÓRICO & FAVORITOS • DOWNLOADS • ESCUDO
                // =========================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(tileBg)
                        .border(0.6.dp, tileBorder, RoundedCornerShape(15.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FooterSegmentButton(
                        icon = Icons.Rounded.History,
                        label = "Histórico",
                        textColor = textPrimary,
                        iconTint = textSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onOpenHistory()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .width(0.8.dp)
                            .height(20.dp)
                            .background(cardBorder)
                    )

                    FooterSegmentButton(
                        icon = Icons.Rounded.Download,
                        label = "Downloads",
                        textColor = textPrimary,
                        iconTint = textSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onOpenDownloads()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .width(0.8.dp)
                            .height(20.dp)
                            .background(cardBorder)
                    )

                    FooterSegmentButton(
                        icon = Icons.Rounded.Shield,
                        label = "Escudo",
                        textColor = textPrimary,
                        iconTint = textSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onOpenPrivacyDashboard()
                        }
                    )
                }
            }
        }
    }
}

private enum class ActionKind {
    SHARE, READER, FIND, DESKTOP, TRANSLATE, PRINT, OFFLINE, PIP, QR, DESKTOP_SYNC
}

@Immutable
private data class BentoActionSpec(
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean,
    val isActive: Boolean,
    val action: ActionKind
)

@Composable
private fun TactileNavButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    isActive: Boolean,
    tileBg: Color,
    tileBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) accentColor.copy(alpha = 0.15f) else tileBg
    val borderColor = if (isActive) accentColor.copy(alpha = 0.50f) else tileBorder
    val iconColor = when {
        !enabled -> textSecondary.copy(alpha = 0.35f)
        isActive -> accentColor
        else -> textPrimary
    }

    Column(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.55f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(0.7.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(19.dp)
            )
        }
        Text(
            text = label,
            color = if (isActive) accentColor else textSecondary,
            fontSize = 10.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeroFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    isHighlightAccent: Boolean,
    tileBg: Color,
    tileBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isActive -> accentColor.copy(alpha = 0.16f)
        isHighlightAccent -> accentColor.copy(alpha = 0.08f)
        else -> tileBg
    }
    val border = when {
        isActive -> accentColor.copy(alpha = 0.55f)
        isHighlightAccent -> accentColor.copy(alpha = 0.28f)
        else -> tileBorder
    }
    val squircleBg = when {
        isActive -> accentColor
        isHighlightAccent -> accentColor.copy(alpha = 0.16f)
        else -> textPrimary.copy(alpha = 0.07f)
    }
    val iconTint = when {
        isActive -> Color.White
        isHighlightAccent -> accentColor
        else -> textPrimary
    }

    Row(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(0.8.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(squircleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isActive) accentColor else textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
}

@Composable
private fun BentoGridTile(
    spec: BentoActionSpec,
    tileBg: Color,
    tileBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (spec.isActive) accentColor.copy(alpha = 0.15f) else tileBg
    val borderColor = if (spec.isActive) accentColor.copy(alpha = 0.50f) else tileBorder
    val iconColor = when {
        !spec.enabled -> textSecondary.copy(alpha = 0.38f)
        spec.isActive -> accentColor
        else -> textPrimary
    }

    Column(
        modifier = modifier
            .height(64.dp)
            .alpha(if (spec.enabled) 1f else 0.52f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(0.7.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = spec.enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = spec.icon,
            contentDescription = spec.label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = spec.label,
            color = if (spec.isActive) accentColor else textPrimary,
            fontSize = 10.5.sp,
            fontWeight = if (spec.isActive) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SlimContextPill(
    icon: ImageVector,
    label: String,
    tileBg: Color,
    tileBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(tileBg)
            .border(0.6.dp, tileBorder, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BentoToggleCard(
    icon: ImageVector,
    title: String,
    stateText: String,
    checked: Boolean,
    tileBg: Color,
    tileBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) accentColor.copy(alpha = 0.12f) else tileBg,
        animationSpec = tween(180),
        label = "bento_toggle_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor.copy(alpha = 0.42f) else tileBorder,
        animationSpec = tween(180),
        label = "bento_toggle_border"
    )

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(0.7.dp, borderColor, RoundedCornerShape(15.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (checked) accentColor.copy(alpha = 0.18f)
                        else textPrimary.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) accentColor else textSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stateText,
                    color = if (checked) accentColor else textSecondary,
                    fontSize = 10.sp,
                    fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }

        // Indicador Luminoso Minimalista
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (checked) ApplePalette.Green else textSecondary.copy(alpha = 0.28f))
        )
    }
}

@Composable
private fun FooterSegmentButton(
    icon: ImageVector,
    label: String,
    textColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(15.dp)
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
        thickness = 0.7.dp,
        color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else ApplePalette.LightHairline
    )
}
