package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ArcSummarySheet(
    isVisible: Boolean,
    isGenerating: Boolean,
    summaryText: String?,
    pageTitle: String,
    pageDomain: String,
    readingTimeSavedMinutes: Int,
    error: String?,
    isDarkMode: Boolean,
    isSpeaking: Boolean = false,
    onSpeakSummary: (String) -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    onRetry: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val context = LocalContext.current
    BackHandler(enabled = isVisible) {
        onDismiss()
    }

    var copiedToClipboard by remember { mutableStateOf(false) }
    LaunchedEffect(copiedToClipboard) {
        if (copiedToClipboard) {
            delay(2200)
            copiedToClipboard = false
        }
    }

    // Arc Iridescent animated transition
    val infiniteTransition = rememberInfiniteTransition(label = "arc_aurora")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_rotation"
    )
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_shimmer"
    )

    // Dynamic step messages during generation
    var stepIndex by remember { mutableIntStateOf(0) }
    val stepMessages = remember {
        listOf(
            "✨ Lendo o conteúdo da página...",
            "🔍 Analisando tópicos e conceitos...",
            "⚡ Sintetizando resumo no estilo Arc...",
            "🎨 Finalizando destaques..."
        )
    }
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            stepIndex = 0
            while (true) {
                delay(1600)
                stepIndex = (stepIndex + 1) % stepMessages.size
            }
        }
    }

    // Colors matching Arc's signature iridescent gradient
    val arcGradientColors = remember {
        listOf(
            Color(0xFF00E5FF), // Electric Cyan
            Color(0xFF2979FF), // Deep Sky Blue
            Color(0xFFB388FF), // Vivid Lilac
            Color(0xFFFF4081), // Neon Rose
            Color(0xFFFFD54F), // Amber Gold
            Color(0xFF00E5FF)  // Loop Cyan
        )
    }

    val sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val sheetBg = if (isDarkMode) Color(0xF5141010) else Color(0xFBF8F9FA)
    val textColor = if (isDarkMode) Color(0xFFF0F0F2) else Color(0xFF1C1C1E)
    val mutedTextColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF636366)

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
        // Arc glowing border container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intercept clicks inside the sheet
                )
                .shadow(
                    elevation = 28.dp,
                    shape = sheetShape,
                    ambientColor = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    spotColor = Color(0xFFB388FF).copy(alpha = 0.5f)
                )
                // Outer animated Arc gradient border
                .background(
                    brush = Brush.sweepGradient(
                        colors = arcGradientColors
                    ),
                    shape = sheetShape
                )
                .padding(2.dp) // Thickness of the glowing border
        ) {
            // Main sheet body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(sheetShape)
                    .background(sheetBg)
                    .navigationBarsPadding()
            ) {
                // Top handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(mutedTextColor.copy(alpha = 0.35f))
                    )
                }

                // Header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rotating glowing Arc Sparkle Orb
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFFB388FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (isGenerating) rotationAnim else 0f)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Resumo Arc IA",
                                    color = textColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (pageDomain.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = pageDomain,
                                            color = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0078D4),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            if (readingTimeSavedMinutes > 0) {
                                Text(
                                    text = "⚡ Economiza ~${readingTimeSavedMinutes} min de leitura",
                                    color = Color(0xFFFFB300),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                            )
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar Resumo",
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Shimmer scan indicator when generating
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00E5FF),
                                        Color(0xFFB388FF),
                                        Color(0xFFFF4081),
                                        Color.Transparent
                                    ),
                                    startX = shimmerTranslate - 300f,
                                    endX = shimmerTranslate
                                )
                            )
                    )
                }

                // Main Content Area (Scrollable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        isGenerating -> {
                            // Arc Shimmer Loading State
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .shadow(16.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(arcGradientColors)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .rotate(rotationAnim)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = stepMessages[stepIndex],
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Extraindo os pontos cruciais do artigo sem distrações...",
                                    color = mutedTextColor,
                                    fontSize = 12.5.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Shimmering skeleton cards
                                SkeletonCard(isDarkMode = isDarkMode, widthFactor = 0.95f)
                                Spacer(modifier = Modifier.height(10.dp))
                                SkeletonCard(isDarkMode = isDarkMode, widthFactor = 0.85f)
                                Spacer(modifier = Modifier.height(10.dp))
                                SkeletonCard(isDarkMode = isDarkMode, widthFactor = 0.90f)
                            }
                        }

                        error != null -> {
                            // Error State with Retry
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "😕 Não foi possível resumir a página",
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    color = mutedTextColor,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF00E5FF), Color(0xFF2979FF))
                                            )
                                        )
                                        .clickable(onClick = onRetry)
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Tentar Novamente",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        !summaryText.isNullOrBlank() -> {
                            // Completed Arc Summary Display
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                // Original Article Title Card
                                if (pageTitle.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = pageTitle,
                                            color = textColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 21.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // Formatted Arc Summary View
                                FormattedArcSummaryContent(
                                    summaryRaw = summaryText,
                                    isDarkMode = isDarkMode
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }

                // Bottom Action Bar (Only visible when summary is ready)
                if (!isGenerating && !summaryText.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Copy Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    if (copiedToClipboard) Color(0xFF2E7D32)
                                    else if (isDarkMode) Color.White.copy(alpha = 0.08f)
                                    else Color.Black.copy(alpha = 0.06f)
                                )
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("Resumo Arc", summaryText)
                                    clipboard?.setPrimaryClip(clip)
                                    copiedToClipboard = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (copiedToClipboard) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    tint = if (copiedToClipboard) Color.White else textColor,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = if (copiedToClipboard) "Copiado!" else "Copiar Resumo",
                                    color = if (copiedToClipboard) Color.White else textColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Text-To-Speech Listen Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    if (isSpeaking) {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFE53935), Color(0xFFFF5722))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF00E5FF), Color(0xFF2979FF))
                                        )
                                    }
                                )
                                .clickable {
                                    if (isSpeaking) {
                                        onStopSpeaking()
                                    } else {
                                        onSpeakSummary(summaryText)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isSpeaking) "Parar Áudio" else "Ouvir Resumo",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonCard(
    isDarkMode: Boolean,
    widthFactor: Float
) {
    val barColor = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(barColor)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFactor)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barColor.copy(alpha = 0.18f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFactor * 0.75f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barColor.copy(alpha = 0.12f))
        )
    }
}

@Composable
private fun FormattedArcSummaryContent(
    summaryRaw: String,
    isDarkMode: Boolean
) {
    val textColor = if (isDarkMode) Color(0xFFEEEEF2) else Color(0xFF1C1C1E)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f)

    val lines = summaryRaw.split("\n")
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            when {
                // Section Header (e.g. ## Título or **Destaques**)
                trimmed.startsWith("#") -> {
                    val cleanHeader = trimmed.replace(Regex("^#+\\s*"), "").replace("**", "")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cleanHeader,
                        color = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF0078D4),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bullet point or Key Takeaway with emoji/bullet
                trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•") -> {
                    val bulletText = trimmed.replace(Regex("^[-*•]\\s*"), "")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = bulletText,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Standard paragraph
                else -> {
                    Text(
                        text = trimmed.replace("**", ""),
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}
