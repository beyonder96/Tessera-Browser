package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.BrowseForMeResult
import com.tessera.browser.data.BrowseForMeState
import com.tessera.browser.data.BrowseSource

@Composable
fun TesseraBrowseForMeScreen(
    state: BrowseForMeState,
    isDarkMode: Boolean,
    accentColor: Color,
    onOpenUrl: (String) -> Unit,
    onOpenWebSearch: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val context = LocalContext.current
    BackHandler(enabled = state.isVisible) {
        onDismiss()
    }

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(2000)
            copied = false
        }
    }

    val screenBg = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xFF140F0D), Color(0xFF0D0A09), Color(0xFF060505))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6), Color(0xFFFFFFFF))
        )
    }

    val contentColor = if (isDarkMode) Color.White else Color(0xFF111827)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF4B5563)

    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(tween(240)) + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { it / 3 })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(screenBg)
                .statusBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP APP BAR
                BrowseForMeTopBar(
                    query = state.currentQuery,
                    isDarkMode = isDarkMode,
                    accentColor = accentColor,
                    copied = copied,
                    onBack = onDismiss,
                    onCopy = {
                        val textToCopy = buildString {
                            appendLine("✦ ${state.result?.headline ?: state.currentQuery}")
                            appendLine()
                            state.result?.quickAnswer?.let { appendLine(it); appendLine() }
                            state.result?.keyTakeaways?.forEach { appendLine("• $it") }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Tessera Browse for Me", textToCopy))
                        copied = true
                        Toast.makeText(context, "Resumo copiado!", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "✦ ${state.result?.headline ?: state.currentQuery}\n\n${state.result?.quickAnswer ?: ""}\n\n— Navegado com Tessera Browser"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar síntese"))
                    },
                    onSearchExternal = {
                        onOpenWebSearch(state.currentQuery)
                    }
                )

                // BODY CONTENT
                if (state.isLoading) {
                    BrowseForMeLoadingView(
                        query = state.currentQuery,
                        loadingStep = state.loadingStep,
                        isDarkMode = isDarkMode,
                        accentColor = accentColor
                    )
                } else if (state.error != null) {
                    BrowseForMeErrorView(
                        error = state.error,
                        isDarkMode = isDarkMode,
                        accentColor = accentColor,
                        onRetry = onRetry,
                        onWebSearch = { onOpenWebSearch(state.currentQuery) }
                    )
                } else if (state.result != null) {
                    BrowseForMeContentView(
                        result = state.result,
                        isDarkMode = isDarkMode,
                        accentColor = accentColor,
                        onOpenSource = onOpenUrl,
                        onWebSearch = { onOpenWebSearch(state.currentQuery) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowseForMeTopBar(
    query: String,
    isDarkMode: Boolean,
    accentColor: Color,
    copied: Boolean,
    onBack: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSearchExternal: () -> Unit
) {
    val iconColor = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF1F2937)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Voltar",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Iridescent Feature Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.16f),
                                Color(0xFF7C4DFF).copy(alpha = 0.20f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.7f),
                                Color(0xFFB388FF).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Browse for Me",
                        color = if (isDarkMode) Color.White else Color(0xFF0D47A1),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                    contentDescription = "Copiar síntese",
                    tint = if (copied) Color(0xFF69F0AE) else iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Compartilhar",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onSearchExternal) {
                Icon(
                    imageVector = Icons.Rounded.OpenInBrowser,
                    contentDescription = "Busca completa na Web",
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun BrowseForMeLoadingView(
    query: String,
    loadingStep: String,
    isDarkMode: Boolean,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "browse_loading_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Orb with Pulsing Animation
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x6600E5FF),
                    spotColor = Color(0x887C4DFF)
                )
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF7C4DFF),
                            Color(0xFFFF4081),
                            Color(0xFF00E5FF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Navegando por você...",
            color = if (isDarkMode) Color.White else Color(0xFF111827),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Query capsule
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "“$query”",
                color = accentColor,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live status message with spinner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = accentColor,
                strokeWidth = 2.dp
            )
            Text(
                text = loadingStep,
                color = if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF4B5563),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun BrowseForMeErrorView(
    error: String,
    isDarkMode: Boolean,
    accentColor: Color,
    onRetry: () -> Unit,
    onWebSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Não foi possível sintetizar a pesquisa",
            color = if (isDarkMode) Color.White else Color(0xFF111827),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            color = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B7280),
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tentar novamente",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f))
                    .clickable(onClick = onWebSearch)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Buscar no Google",
                    color = if (isDarkMode) Color.White else Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BrowseForMeContentView(
    result: BrowseForMeResult,
    isDarkMode: Boolean,
    accentColor: Color,
    onOpenSource: (String) -> Unit,
    onWebSearch: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. EDITORIAL HEADLINE
        Text(
            text = result.headline,
            color = if (isDarkMode) Color.White else Color(0xFF111827),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 30.sp
        )

        // 2. QUICK ANSWER HIGHLIGHT CARD
        if (result.quickAnswer.isNotBlank()) {
            val quickShape = RoundedCornerShape(22.dp)
            val quickBg = if (isDarkMode) {
                Brush.verticalGradient(
                    listOf(Color(0xEE1E1917), Color(0xDD14100E))
                )
            } else {
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFF9FAFB))
                )
            }
            val quickBorder = if (isDarkMode) {
                Brush.linearGradient(
                    listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color.White.copy(alpha = 0.15f))
                )
            } else {
                Brush.linearGradient(
                    listOf(Color(0xFF0288D1).copy(alpha = 0.4f), Color.Black.copy(alpha = 0.08f))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkMode) 12.dp else 4.dp,
                        shape = quickShape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(quickShape)
                    .background(quickBg)
                    .border(1.2.dp, quickBorder, quickShape)
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "RESPOSTA DIRETA",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = result.quickAnswer,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.94f) else Color(0xFF1F2937),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 3. KEY TAKEAWAYS (Pontos Cruciais)
        if (result.keyTakeaways.isNotEmpty()) {
            val cardShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                    .border(
                        1.dp,
                        if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f),
                        cardShape
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DESTAQUES IMPORTANTES",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B7280),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    result.keyTakeaways.forEach { takeaway ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(
                                text = takeaway,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1F2937),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. DETAILED SECTIONS
        result.sections.forEach { section ->
            val sectionShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(sectionShape)
                    .background(if (isDarkMode) Color(0x99201A18) else Color(0xFFFFFFFF))
                    .border(
                        1.dp,
                        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                        sectionShape
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = section.title,
                        color = if (isDarkMode) Color.White else Color(0xFF111827),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (section.content.isNotBlank()) {
                        Text(
                            text = section.content,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF374151),
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp
                        )
                    }

                    if (section.bulletPoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        section.bulletPoints.forEach { bullet ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "—",
                                    color = accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = bullet,
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF374151),
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. SOURCES CAROUSEL
        if (result.sources.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "FONTES CONSULTADAS NA WEB",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val sourcesScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(sourcesScrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    result.sources.forEach { source ->
                        BrowseSourceCard(
                            source = source,
                            isDarkMode = isDarkMode,
                            accentColor = accentColor,
                            onClick = { onOpenSource(source.url) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 6. ACTION FOOTER: OPEN COMPLETE WEB SEARCH
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                .clickable(onClick = onWebSearch)
                .padding(vertical = 14.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Ver resultados convencionais no Google",
                    color = accentColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BrowseSourceCard(
    source: BrowseSource,
    isDarkMode: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val cardBg = if (isDarkMode) Color(0xCC201A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .width(180.dp)
            .shadow(
                elevation = if (isDarkMode) 6.dp else 2.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(1.dp, cardBorder, cardShape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Domain badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = source.domain,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = source.title,
                color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF111827),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Abrir fonte",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
