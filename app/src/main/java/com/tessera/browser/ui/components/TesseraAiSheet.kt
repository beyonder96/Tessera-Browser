package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.ai.AiChatMessage
import com.tessera.browser.ai.AiProvider
import com.tessera.browser.ai.TesseraAiState
import kotlinx.coroutines.delay

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
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val context = LocalContext.current
    BackHandler(enabled = state.isVisible) {
        onDismiss()
    }

    var copiedSummary by remember { mutableStateOf(false) }
    LaunchedEffect(copiedSummary) {
        if (copiedSummary) {
            delay(2000)
            copiedSummary = false
        }
    }

    var questionInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(state.chatMessages.size, state.isAnsweringQuestion) {
        if (state.chatMessages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val sheetBg = if (isDarkMode) Color(0xF2161210) else Color(0xFAFBFBFD)
    val surfaceCardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val textColor = if (isDarkMode) Color(0xFFF3F3F5) else Color(0xFF1B1B1E)
    val mutedColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF6E6E73)
    val accentColor = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0288D1)

    val activeKey = when (state.activeProvider) {
        AiProvider.GEMINI -> geminiApiKey?.trim().orEmpty()
        AiProvider.GROQ -> groqApiKey?.trim().orEmpty()
    }
    val hasActiveKey = activeKey.isNotBlank()

    // Scrim overlay
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
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
                    onClick = {} // Intercept clicks inside sheet
                )
                .shadow(24.dp, sheetShape)
                .clip(sheetShape)
                .background(sheetBg)
                .border(1.dp, cardBorder, sheetShape)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Grab Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(mutedColor.copy(alpha = 0.3f))
                    )
                }

                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Tessera AI",
                                    color = textColor,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.pageDomain.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(accentColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = state.pageDomain,
                                            color = accentColor,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Resumo e Dúvidas com ${state.activeProvider.displayName}",
                                color = mutedColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Provider switcher pill + close
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Switch Provider Pill
                        val otherProvider = if (state.activeProvider == AiProvider.GEMINI) AiProvider.GROQ else AiProvider.GEMINI
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(surfaceCardBg)
                                .border(0.8.dp, cardBorder, RoundedCornerShape(14.dp))
                                .clickable { onSelectProvider(otherProvider) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (state.activeProvider == AiProvider.GEMINI) "⚡ Usar Groq" else "✨ Usar Gemini",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(surfaceCardBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Fechar",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Check if key is configured
                    if (!hasActiveKey) {
                        AiKeySetupCard(
                            provider = state.activeProvider,
                            isDarkMode = isDarkMode,
                            onSave = { onSaveKey(state.activeProvider, it) }
                        )
                    } else {
                        // Main AI Content
                        if (state.pageTitle.isNotBlank()) {
                            Text(
                                text = state.pageTitle,
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Section 1: Page Summary
                        when {
                            state.isSummarizing -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(surfaceCardBg)
                                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            color = accentColor,
                                            strokeWidth = 2.5.dp
                                        )
                                        Text(
                                            text = "Sintetizando resumo da página...",
                                            color = textColor,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Analisando conceitos essenciais via ${state.activeProvider.displayName}",
                                            color = mutedColor,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }

                            state.error != null && state.summary == null -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFE53935).copy(alpha = 0.08f))
                                        .border(1.dp, Color(0xFFE53935).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Não foi possível gerar o resumo",
                                            color = Color(0xFFEF5350),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = state.error,
                                            color = textColor.copy(alpha = 0.8f),
                                            fontSize = 12.5.sp
                                        )
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(accentColor.copy(alpha = 0.15f))
                                                .clickable(onClick = onRegenerateSummary)
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "Tentar novamente",
                                                color = accentColor,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            !state.summary.isNullOrBlank() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(surfaceCardBg)
                                        .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "RESUMO DA PÁGINA",
                                                color = accentColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )

                                            // Action controls
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                // Speak button
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSpeaking) Color(0xFFE53935) else surfaceCardBg)
                                                        .clickable {
                                                            if (isSpeaking) onStopSpeaking()
                                                            else onSpeakText(state.summary)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                                        contentDescription = "Ouvir",
                                                        tint = if (isSpeaking) Color.White else textColor,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }

                                                // Copy button
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (copiedSummary) Color(0xFF43A047) else surfaceCardBg)
                                                        .clickable {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                            val clip = ClipData.newPlainText("Resumo Tessera", state.summary)
                                                            clipboard?.setPrimaryClip(clip)
                                                            copiedSummary = true
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (copiedSummary) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                                        contentDescription = "Copiar",
                                                        tint = if (copiedSummary) Color.White else textColor,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }

                                                // Refresh button
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(surfaceCardBg)
                                                        .clickable(onClick = onRegenerateSummary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Refresh,
                                                        contentDescription = "Regerar",
                                                        tint = textColor,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.summary,
                                            color = textColor,
                                            fontSize = 13.5.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Section 2: Questions & Answers (Tirar Dúvidas)
                        Text(
                            text = "TIRAR DÚVIDAS SOBRE A PÁGINA",
                            color = mutedColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset suggestion chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val promptSuggestions = listOf(
                                "💡 Explique de forma simples",
                                "📌 Principais conclusões",
                                "⚖️ Prós e contras",
                                "❓ O que isso significa na prática?"
                            )
                            promptSuggestions.forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(surfaceCardBg)
                                        .border(0.8.dp, cardBorder, RoundedCornerShape(12.dp))
                                        .clickable { onAskQuestion(suggestion.replace(Regex("^[💡📌⚖️❓]\\s*"), "")) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        color = textColor.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chat Messages List
                        if (state.chatMessages.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.chatMessages.forEach { msg ->
                                    val isUser = msg.role == "user"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                                    )
                                                )
                                                .background(
                                                    if (isUser) accentColor.copy(alpha = 0.18f) else surfaceCardBg
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isUser) accentColor.copy(alpha = 0.35f) else cardBorder,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Column {
                                                if (!isUser) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Tessera AI",
                                                            color = accentColor,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                                val clip = ClipData.newPlainText("Resposta Tessera AI", msg.content)
                                                                clipboard?.setPrimaryClip(clip)
                                                            },
                                                            modifier = Modifier.size(20.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.ContentCopy,
                                                                contentDescription = "Copiar resposta",
                                                                tint = mutedColor,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                                Text(
                                                    text = msg.content,
                                                    color = textColor,
                                                    fontSize = 13.5.sp,
                                                    lineHeight = 19.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Answering indicator
                        if (state.isAnsweringQuestion) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = accentColor,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Consultando a página com ${state.activeProvider.displayName}...",
                                    color = mutedColor,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Bottom Input Bar for Questions (Only if key is set)
                if (hasActiveKey) {
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
                                        fontSize = 13.5.sp
                                    )
                                }
                                BasicTextField(
                                    value = questionInput,
                                    onValueChange = { questionInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = textColor,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(accentColor),
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
                                        .background(accentColor)
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

/**
 * Card minimalista para configuração imediata da chave de IA no próprio diálogo.
 */
@Composable
private fun AiKeySetupCard(
    provider: AiProvider,
    isDarkMode: Boolean,
    onSave: (String) -> Unit
) {
    var keyText by remember { mutableStateOf("") }
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isDarkMode) Color(0xFFEEEEF2) else Color(0xFF1C1C1E)
    val mutedColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF6E6E73)
    val accentColor = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0288D1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Configurar chave do ${provider.displayName}",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (provider == AiProvider.GROQ) {
                    "O Groq oferece inferência ultra-rápida (Llama 3.3 70B) com plano gratuito. Obtenha sua chave em console.groq.com."
                } else {
                    "O Google Gemini 2.0 Flash possui cota gratuita para desenvolvedores. Obtenha sua chave em aistudio.google.com."
                },
                color = mutedColor,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )

            // Input field
            val inputShape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(inputShape)
                    .background(if (isDarkMode) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.04f))
                    .border(1.dp, cardBorder, inputShape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (keyText.isNotBlank()) accentColor else accentColor.copy(alpha = 0.3f))
                    .clickable(enabled = keyText.isNotBlank()) {
                        onSave(keyText.trim())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Salvar e Ativar IA",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
