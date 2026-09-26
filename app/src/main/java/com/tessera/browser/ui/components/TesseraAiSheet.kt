package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.ai.AiChatMessage
import com.tessera.browser.ai.AiProvider
import com.tessera.browser.ai.TesseraAiState
import kotlinx.coroutines.delay

/**
 * Interface profissional e elegante do Tessera Intelligence (Assistente IA).
 * Apresenta síntese estruturada da página com formatação rica, leitura por voz (TTS),
 * chat interativo com a página, salvamento direto no caderno e suporte a Gemini e Groq.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TesseraAiSheet(
    state: TesseraAiState,
    geminiApiKey: String?,
    groqApiKey: String?,
    isDarkMode: Boolean,
    isSpeaking: Boolean = false,
    onSpeakText: (String) -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    onSelectProvider: (AiProvider) -> Unit = {},
    onSaveKey: (AiProvider, String) -> Unit = { _, _ -> },
    onRegenerateSummary: () -> Unit = {},
    onAskQuestion: (String) -> Unit = {},
    onSaveToNotebook: ((title: String, content: String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val context = LocalContext.current
    BackHandler(enabled = state.isVisible) {
        onDismiss()
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Síntese & Insights, 1 = Chat da Página
    var showKeyConfigModal by remember { mutableStateOf(false) }
    var copiedSummaryFeedback by remember { mutableStateOf(false) }
    var savedNoteFeedback by remember { mutableStateOf(false) }
    var questionInput by remember { mutableStateOf("") }
    val summaryScrollState = rememberScrollState()
    val chatScrollState = rememberScrollState()

    LaunchedEffect(copiedSummaryFeedback) {
        if (copiedSummaryFeedback) {
            delay(2200)
            copiedSummaryFeedback = false
        }
    }

    LaunchedEffect(savedNoteFeedback) {
        if (savedNoteFeedback) {
            delay(2400)
            savedNoteFeedback = false
        }
    }

    // Auto-scroll chat when questions are submitted or answered
    LaunchedEffect(state.chatMessages.size, state.isAnsweringQuestion) {
        if (state.chatMessages.isNotEmpty() && selectedTab == 1) {
            chatScrollState.animateScrollTo(chatScrollState.maxValue)
        }
    }

    // Luminous animation for the AI aura
    val infiniteTransition = rememberInfiniteTransition(label = "ai_aura_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_pulse"
    )

    val activeKey = when (state.activeProvider) {
        AiProvider.GEMINI -> geminiApiKey?.trim().orEmpty()
        AiProvider.GROQ -> groqApiKey?.trim().orEmpty()
    }
    val hasActiveKey = activeKey.isNotBlank()

    // Paleta de Cores e Gradientes de Alta Fidelidade
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val sheetBg = if (isDarkMode) Color(0xF7151210) else Color(0xFAFBFBFD)
    val surfaceCardBg = if (isDarkMode) Color(0xFF221D1A) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val textColor = if (isDarkMode) Color(0xFFF3F3F5) else Color(0xFF1A1A1E)
    val mutedColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF71717A)

    val aiAuraBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFF8B5CF6),
                Color(0xFF3B82F6),
                Color(0xFF06B6D4),
                Color(0xFF8B5CF6)
            )
        )
    }

    val providerAccentColor = if (state.activeProvider == AiProvider.GEMINI) {
        Color(0xFF4285F4)
    } else {
        Color(0xFFFF9800)
    }

    // Fundo escuro com backdrop suave
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.60f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intercepta cliques no interior
                )
                .shadow(24.dp, sheetShape)
                .clip(sheetShape)
                .background(sheetBg)
                .border(1.dp, cardBorder, sheetShape)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. DRAG HANDLE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(mutedColor.copy(alpha = 0.35f))
                    )
                }

                // 2. HEADER DA IA: Identidade Visual Luminous + Seletor de Modelo + Ações
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar e Título
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avatar Luminous Glow
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(aiAuraBrush)
                                .border(1.5.dp, Color.White.copy(alpha = pulseAlpha), CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(if (isDarkMode) Color(0xFF1D1714) else Color(0xFF2E2438)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFE0C3FC),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Tessera AI",
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.pageDomain.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(providerAccentColor.copy(alpha = 0.14f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = state.pageDomain,
                                            color = providerAccentColor,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Inteligência contextual de página",
                                color = mutedColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Seletor de Modelo & Botões de Ação
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pill do Modelo Ativo (Alternador Rápido Gemini <-> Groq)
                        var showProviderMenu by remember { mutableStateOf(false) }
                        Box {
                            val providerShape = RoundedCornerShape(14.dp)
                            Row(
                                modifier = Modifier
                                    .clip(providerShape)
                                    .background(surfaceCardBg)
                                    .border(1.dp, cardBorder, providerShape)
                                    .clickable { showProviderMenu = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.activeProvider == AiProvider.GEMINI) Icons.Rounded.AutoAwesome else Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = providerAccentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (state.activeProvider == AiProvider.GEMINI) "Gemini Flash" else "Groq Llama",
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            DropdownMenu(
                                expanded = showProviderMenu,
                                onDismissRequest = { showProviderMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF4285F4), modifier = Modifier.size(16.dp))
                                                Text("Google Gemini (Flash 2.0)", fontWeight = FontWeight.Bold)
                                            }
                                            Text("Cota gratuita do Google AI Studio • Alta precisão", fontSize = 11.sp, color = mutedColor)
                                        }
                                    },
                                    onClick = {
                                        showProviderMenu = false
                                        onSelectProvider(AiProvider.GEMINI)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Rounded.Bolt, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                                Text("Groq Cloud (Llama 3.3 70B)", fontWeight = FontWeight.Bold)
                                            }
                                            Text("Inferência ultra-rápida por hardware LPUs", fontSize = 11.sp, color = mutedColor)
                                        }
                                    },
                                    onClick = {
                                        showProviderMenu = false
                                        onSelectProvider(AiProvider.GROQ)
                                    }
                                )
                            }
                        }

                        // Botão de Chaves de API
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(surfaceCardBg)
                                .border(1.dp, cardBorder, CircleShape)
                                .clickable { showKeyConfigModal = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = "Configurar Chaves",
                                tint = if (hasActiveKey) mutedColor else providerAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Fechar
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(surfaceCardBg)
                                .border(1.dp, cardBorder, CircleShape)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Fechar",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // 3. SEGMENTED TABS: [ 📄 Síntese & Insights ] | [ 💬 Chat da Página ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceCardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabModifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                    val activeTabShape = RoundedCornerShape(13.dp)

                    // Aba 1: Síntese & Insights
                    Box(
                        modifier = tabModifier
                            .clip(activeTabShape)
                            .background(if (selectedTab == 0) providerAccentColor.copy(alpha = 0.16f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (selectedTab == 0) providerAccentColor.copy(alpha = 0.4f) else Color.Transparent,
                                activeTabShape
                            )
                            .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = if (selectedTab == 0) providerAccentColor else mutedColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Síntese & Insights",
                                color = if (selectedTab == 0) textColor else mutedColor,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Aba 2: Chat da Página
                    Box(
                        modifier = tabModifier
                            .clip(activeTabShape)
                            .background(if (selectedTab == 1) providerAccentColor.copy(alpha = 0.16f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (selectedTab == 1) providerAccentColor.copy(alpha = 0.4f) else Color.Transparent,
                                activeTabShape
                            )
                            .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Forum,
                                contentDescription = null,
                                tint = if (selectedTab == 1) providerAccentColor else mutedColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Chat com a Página",
                                color = if (selectedTab == 1) textColor else mutedColor,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                            if (state.chatMessages.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(providerAccentColor)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${state.chatMessages.size}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

                // 4. CONTEÚDO PRINCIPAL (ALTERNADO ENTRE SÍNTESE E CHAT)
                if (!hasActiveKey) {
                    // Caso não haja chave configurada para o provedor ativo
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AiKeyOnboardingCard(
                            provider = state.activeProvider,
                            isDarkMode = isDarkMode,
                            onSave = { onSaveKey(state.activeProvider, it) }
                        )
                    }
                } else if (selectedTab == 0) {
                    // ABA 1: SÍNTESE & INSIGHTS DA PÁGINA
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(summaryScrollState)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Título do Artigo / Página
                        if (state.pageTitle.isNotBlank()) {
                            Text(
                                text = state.pageTitle,
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // ESTADO DE CARREGAMENTO / SUMARIZAÇÃO
                        if (state.isSummarizing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(surfaceCardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                                    .padding(26.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(providerAccentColor.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(26.dp),
                                            color = providerAccentColor,
                                            strokeWidth = 2.5.dp
                                        )
                                    }
                                    Text(
                                        text = "Sintetizando inteligência da página...",
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Processando visão geral, pontos essenciais e conclusões com ${state.activeProvider.displayName}",
                                        color = mutedColor,
                                        fontSize = 11.5.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (state.error != null && state.summary == null) {
                            // ESTADO DE ERRO
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFFE53935).copy(alpha = 0.08f))
                                    .border(1.dp, Color(0xFFE53935).copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Não foi possível gerar a síntese",
                                        color = Color(0xFFEF5350),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = state.error,
                                        color = textColor.copy(alpha = 0.85f),
                                        fontSize = 12.5.sp
                                    )
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(providerAccentColor.copy(alpha = 0.15f))
                                            .clickable(onClick = onRegenerateSummary)
                                            .padding(horizontal = 14.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            tint = providerAccentColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Tentar novamente",
                                            color = providerAccentColor,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else if (!state.summary.isNullOrBlank()) {
                            // BARRA DE FERRAMENTAS DA SÍNTESE (Ouvir, Copiar, Salvar no Caderno, Regerar)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(surfaceCardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Metadata rápida
                                val readingTime = (state.pageContent.split(" ").size / 180).coerceAtLeast(1)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TipsAndUpdates,
                                        contentDescription = null,
                                        tint = providerAccentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "⏱️ ~$readingTime min economizados",
                                        color = mutedColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Botões de ação em cápsula
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Ouvir Resumo (TTS)
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isSpeaking) Color(0xFFE53935) else (if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)))
                                            .clickable {
                                                if (isSpeaking) onStopSpeaking()
                                                else onSpeakText(state.summary)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = "Ouvir síntese",
                                            tint = if (isSpeaking) Color.White else textColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // 2. Copiar Síntese
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (copiedSummaryFeedback) Color(0xFF43A047) else (if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)))
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                val clip = ClipData.newPlainText("Síntese Tessera AI", state.summary)
                                                clipboard?.setPrimaryClip(clip)
                                                copiedSummaryFeedback = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (copiedSummaryFeedback) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                            contentDescription = "Copiar síntese",
                                            tint = if (copiedSummaryFeedback) Color.White else textColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // 3. Salvar no Caderno de Notas
                                    if (onSaveToNotebook != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(if (savedNoteFeedback) Color(0xFF0288D1) else (if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)))
                                            .clickable {
                                                val title = state.pageTitle.ifBlank { "Síntese: ${state.pageDomain}" }
                                                onSaveToNotebook(title, state.summary)
                                                savedNoteFeedback = true
                                            },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (savedNoteFeedback) Icons.Rounded.Check else Icons.Rounded.BookmarkAdd,
                                                contentDescription = "Salvar no Caderno",
                                                tint = if (savedNoteFeedback) Color.White else textColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    // 4. Regerar
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                                            .clickable(onClick = onRegenerateSummary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = "Regerar síntese",
                                            tint = textColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            // RENDERIZADOR ESTRUTURADO DA SÍNTESE (Formatação Rica sem asteriscos crus)
                            RichStructuredSummaryView(
                                summaryText = state.summary,
                                isDarkMode = isDarkMode,
                                accentColor = providerAccentColor,
                                textColor = textColor,
                                mutedColor = mutedColor,
                                cardBg = surfaceCardBg,
                                cardBorder = cardBorder
                            )

                            // SUGESTÕES DE PERGUNTAS RÁPIDAS
                            Text(
                                text = "APROFUNDAR COM IA",
                                color = mutedColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val quickSuggestions = listOf(
                                    "💡 Explique para leigos",
                                    "⚖️ Quais os prós e contras?",
                                    "📌 Principais dados e números",
                                    "❓ Qual o impacto prático?"
                                )
                                quickSuggestions.forEach { prompt ->
                                    val cleanPrompt = prompt.replace(Regex("^[💡⚖️📌❓]\\s*"), "")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(surfaceCardBg)
                                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedTab = 1
                                            onAskQuestion(cleanPrompt)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = prompt,
                                            color = textColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ABA 2: CHAT INTERATIVO COM A PÁGINA
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Lista de Mensagens
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp)
                        ) {
                            if (state.chatMessages.isEmpty()) {
                                // Welcome State do Chat
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(providerAccentColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Psychology,
                                                contentDescription = null,
                                                tint = providerAccentColor,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Text(
                                            text = "Converse com esta página",
                                            color = textColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "A IA leu o conteúdo completo e está pronta para esclarecer dúvidas, extrair detalhes ou comparar pontos.",
                                            color = mutedColor,
                                            fontSize = 12.5.sp,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Chips de sugestão inicial
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                "Quais as conclusões principais deste texto?",
                                                "Explique de forma simplificada em tópicos",
                                                "Há contradições ou pontos negativos mencionados?"
                                            ).forEach { q ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(surfaceCardBg)
                                                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                                        .clickable { onAskQuestion(q) }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.TipsAndUpdates,
                                                            contentDescription = null,
                                                            tint = providerAccentColor,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = q,
                                                            color = textColor,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(chatScrollState)
                                        .padding(vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    state.chatMessages.forEach { msg ->
                                        val isUser = msg.role == "user"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                        ) {
                                            if (isUser) {
                                                // Bolha do Usuário
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.85f)
                                                        .clip(
                                                            RoundedCornerShape(
                                                                topStart = 18.dp,
                                                                topEnd = 18.dp,
                                                                bottomStart = 18.dp,
                                                                bottomEnd = 4.dp
                                                            )
                                                        )
                                                        .background(providerAccentColor)
                                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                                ) {
                                                    Text(
                                                        text = msg.content,
                                                        color = Color.White,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        lineHeight = 19.sp
                                                    )
                                                }
                                            } else {
                                                // Resposta da IA (Card estruturado)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.95f)
                                                        .clip(
                                                            RoundedCornerShape(
                                                                topStart = 18.dp,
                                                                topEnd = 18.dp,
                                                                bottomStart = 4.dp,
                                                                bottomEnd = 18.dp
                                                            )
                                                        )
                                                        .background(surfaceCardBg)
                                                        .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                                        .padding(14.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.AutoAwesome,
                                                                    contentDescription = null,
                                                                    tint = providerAccentColor,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Text(
                                                                    text = "Tessera AI",
                                                                    color = providerAccentColor,
                                                                    fontSize = 11.5.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }

                                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                IconButton(
                                                                    onClick = { onSpeakText(msg.content) },
                                                                    modifier = Modifier.size(22.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                                                        contentDescription = "Ouvir",
                                                                        tint = mutedColor,
                                                                        modifier = Modifier.size(13.dp)
                                                                    )
                                                                }
                                                                IconButton(
                                                                    onClick = {
                                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                                        val clip = ClipData.newPlainText("Resposta Tessera AI", msg.content)
                                                                        clipboard?.setPrimaryClip(clip)
                                                                    },
                                                                    modifier = Modifier.size(22.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.ContentCopy,
                                                                        contentDescription = "Copiar resposta",
                                                                        tint = mutedColor,
                                                                        modifier = Modifier.size(13.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        // Texto com suporte a formatação markdown simples
                                                        FormattedAiMessageText(
                                                            content = msg.content,
                                                            textColor = textColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Indicador de resposta em geração
                                    if (state.isAnsweringQuestion) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(start = 6.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = providerAccentColor,
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = "Tessera AI está analisando o conteúdo da página...",
                                                color = mutedColor,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // BARRA INFERIOR DE ENVIO DE PERGUNTAS DO CHAT
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(sheetBg)
                                .border(width = 0.8.dp, color = cardBorder)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            val inputShape = RoundedCornerShape(22.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(inputShape)
                                    .background(surfaceCardBg)
                                    .border(1.dp, cardBorder, inputShape)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (questionInput.isEmpty()) {
                                        Text(
                                            text = "Pergunte algo sobre esta página...",
                                            color = mutedColor,
                                            fontSize = 13.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = questionInput,
                                        onValueChange = { questionInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        cursorBrush = SolidColor(providerAccentColor),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (questionInput.isNotBlank() && !state.isAnsweringQuestion) {
                                                    val q = questionInput.trim()
                                                    questionInput = ""
                                                    onAskQuestion(q)
                                                }
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (questionInput.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(providerAccentColor)
                                            .clickable {
                                                if (!state.isAnsweringQuestion) {
                                                    val q = questionInput.trim()
                                                    questionInput = ""
                                                    onAskQuestion(q)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = "Enviar",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MODAL DE CONFIGURAÇÃO DE CHAVES DE API (DISPONÍVEL SEMPRE VIA BOTÃO DE CHAVE)
    if (showKeyConfigModal) {
        AiKeyManagementDialog(
            currentProvider = state.activeProvider,
            currentGeminiKey = geminiApiKey.orEmpty(),
            currentGroqKey = groqApiKey.orEmpty(),
            isDarkMode = isDarkMode,
            onSave = { prov, key ->
                onSaveKey(prov, key)
                showKeyConfigModal = false
            },
            onDismiss = { showKeyConfigModal = false }
        )
    }
}

/**
 * Renderizador com tipografia rica e estruturação por seções da síntese de IA.
 * Converte títulos, marcadores e conclusões em cartões visuais polidos.
 */
@Composable
private fun RichStructuredSummaryView(
    summaryText: String,
    isDarkMode: Boolean,
    accentColor: Color,
    textColor: Color,
    mutedColor: Color,
    cardBg: Color,
    cardBorder: Color
) {
    // Quebra por linhas e identifica seções típicas da resposta estruturada
    val lines = summaryText.lines()
    val overviewLines = mutableListOf<String>()
    val bulletPoints = mutableListOf<String>()
    val conclusionLines = mutableListOf<String>()
    var currentSection = 0 // 0 = general/overview, 1 = bullets, 2 = conclusion

    lines.forEach { line ->
        val trimmed = line.trim()
        val lower = trimmed.lowercase()
        when {
            lower.contains("pontos-chave") || lower.contains("pontos chave") || lower.contains("principais destaques") -> {
                currentSection = 1
            }
            lower.contains("conclusão") || lower.contains("conclusao") || lower.contains("considerações finais") -> {
                currentSection = 2
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                bulletPoints.add(trimmed.removePrefix("- ").removePrefix("* ").removePrefix("• "))
            }
            trimmed.isNotBlank() -> {
                when (currentSection) {
                    1 -> bulletPoints.add(trimmed)
                    2 -> conclusionLines.add(trimmed)
                    else -> overviewLines.add(trimmed)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. VISÃO GERAL (CARD COM GRADIENTE SUAVE)
        if (overviewLines.isNotEmpty()) {
            val overviewText = overviewLines.joinToString("\n\n")
                .replace(Regex("^\\d+\\.\\s*\\*\\*[^\\*]+\\*\\*:?\\s*"), "")
                .replace("**Visão Geral**:", "")
                .replace("**Visão Geral**", "")
                .trim()

            if (overviewText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor.copy(alpha = if (isDarkMode) 0.12f else 0.08f),
                                    cardBg
                                )
                            )
                        )
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "VISÃO GERAL",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        FormattedAiMessageText(
                            content = overviewText,
                            textColor = textColor
                        )
                    }
                }
            }
        }

        // 2. PONTOS-CHAVE (LISTA DE CARDS DE DESTAQUE)
        if (bulletPoints.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "PONTOS ESSENCIAIS",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    bulletPoints.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            FormattedAiMessageText(
                                content = point,
                                textColor = textColor
                            )
                        }
                    }
                }
            }
        }

        // 3. CONCLUSÃO (CALLOUT CARD)
        if (conclusionLines.isNotEmpty()) {
            val conclusionText = conclusionLines.joinToString("\n\n")
                .replace(Regex("^\\d+\\.\\s*\\*\\*[^\\*]+\\*\\*:?\\s*"), "")
                .replace("**Conclusão**:", "")
                .replace("**Conclusão**", "")
                .trim()

            if (conclusionText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "CONCLUSÃO",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        FormattedAiMessageText(
                            content = conclusionText,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formata texto com marcação simples (**negrito**, *itálico*, `código`) sem exibir asteriscos crus.
 */
@Composable
private fun FormattedAiMessageText(
    content: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(content, textColor) {
        parseMarkdownToAnnotatedString(content, textColor)
    }

    Text(
        text = annotatedString,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

/**
 * Converte marcações simples de markdown em AnnotatedString com SpanStyles.
 */
private fun parseMarkdownToAnnotatedString(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        var lastIndex = 0

        boldRegex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val boldContent = match.groupValues[1]
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor))
            append(boldContent)
            pop()

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

/**
 * Card de Onboarding quando a chave do provedor ainda não foi configurada.
 */
@Composable
private fun AiKeyOnboardingCard(
    provider: AiProvider,
    isDarkMode: Boolean,
    onSave: (String) -> Unit
) {
    var keyText by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val accentColor = if (provider == AiProvider.GEMINI) Color(0xFF4285F4) else Color(0xFFFF9800)
    val cardBg = if (isDarkMode) Color(0xFF221D1A) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val textColor = if (isDarkMode) Color(0xFFF3F3F5) else Color(0xFF1A1A1E)
    val mutedColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF71717A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header do Onboarding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (provider == AiProvider.GEMINI) Icons.Rounded.AutoAwesome else Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Conectar ${provider.displayName}",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (provider == AiProvider.GEMINI) "Cota gratuita para desenvolvedores" else "Inferência ultra-rápida LPU",
                        color = mutedColor,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = if (provider == AiProvider.GEMINI) {
                    "O Google Gemini 2.0 Flash sintetiza páginas e responde dúvidas com inteligência profunda. Crie sua chave gratuita no Google AI Studio."
                } else {
                    "O Groq Cloud acelera a inferência do modelo Llama 3.3 70B com respostas quase instantâneas. Obtenha sua chave no console do Groq."
                },
                color = textColor.copy(alpha = 0.85f),
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )

            // Link para obter chave gratuita
            val portalUrl = if (provider == AiProvider.GEMINI) "https://aistudio.google.com" else "https://console.groq.com"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl))
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Obter chave gratuita em ${if (provider == AiProvider.GEMINI) "aistudio.google.com" else "console.groq.com"}",
                    color = accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Campo de chave mascarado com toggle de visualização
            val inputShape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(inputShape)
                    .background(if (isDarkMode) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.04f))
                    .border(1.dp, cardBorder, inputShape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = mutedColor,
                    modifier = Modifier.size(16.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (keyText.isEmpty()) {
                        Text(
                            text = if (provider == AiProvider.GROQ) "gsk_..." else "AIzaSy...",
                            color = mutedColor,
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(
                    onClick = { isKeyVisible = !isKeyVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Mostrar/ocultar chave",
                        tint = mutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Botão de Salvar e Ativar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (keyText.isNotBlank()) accentColor else accentColor.copy(alpha = 0.3f))
                    .clickable(enabled = keyText.isNotBlank()) {
                        onSave(keyText.trim())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Salvar e Iniciar IA",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Diálogo de Gerenciamento das Chaves de API (Gemini e Groq).
 */
@Composable
private fun AiKeyManagementDialog(
    currentProvider: AiProvider,
    currentGeminiKey: String,
    currentGroqKey: String,
    isDarkMode: Boolean,
    onSave: (AiProvider, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var geminiKeyInput by remember { mutableStateOf(currentGeminiKey) }
    var groqKeyInput by remember { mutableStateOf(currentGroqKey) }
    val accentColor = if (selectedProvider == AiProvider.GEMINI) Color(0xFF4285F4) else Color(0xFFFF9800)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Chaves de IA", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Seletor de Abas de Provedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProvider.entries.forEach { prov ->
                        val isSel = selectedProvider == prov
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSel) accentColor else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { selectedProvider = prov }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (prov == AiProvider.GEMINI) "Google Gemini" else "Groq Cloud",
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = if (isSel) accentColor else Color.Unspecified
                            )
                        }
                    }
                }

                if (selectedProvider == AiProvider.GEMINI) {
                    androidx.compose.material3.OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        label = { Text("Chave de API do Gemini") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    androidx.compose.material3.OutlinedTextField(
                        value = groqKeyInput,
                        onValueChange = { groqKeyInput = it },
                        label = { Text("Chave de API do Groq") },
                        placeholder = { Text("gsk_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedProvider == AiProvider.GEMINI) {
                        onSave(AiProvider.GEMINI, geminiKeyInput.trim())
                    } else {
                        onSave(AiProvider.GROQ, groqKeyInput.trim())
                    }
                }
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold, color = accentColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
