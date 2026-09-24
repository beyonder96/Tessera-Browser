package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.tessera.browser.data.PodcastAudioState
import com.tessera.browser.viewmodel.ReaderArticle
import com.tessera.browser.viewmodel.ReaderBlock
import com.tessera.browser.viewmodel.ReaderBlockType
import com.tessera.browser.viewmodel.ReaderFontFamily
import com.tessera.browser.viewmodel.ReaderTheme

private data class ReaderColors(
    val canvasBackground: Color,
    val sheetBackground: Color,
    val text: Color,
    val textSecondary: Color,
    val surface: Color,
    val border: Color,
    val quoteBorder: Color,
    val accent: Color
)

@Composable
private fun rememberReaderColors(theme: ReaderTheme): ReaderColors {
    return remember(theme) {
        when (theme) {
            ReaderTheme.LIGHT -> ReaderColors(
                canvasBackground = Color(0xFFE8ECEF),
                sheetBackground = Color(0xFFFFFFFF),
                text = Color(0xFF1E1E22),
                textSecondary = Color(0xFF6B7280),
                surface = Color(0xFFF3F4F6),
                border = Color(0xFFE5E7EB),
                quoteBorder = Color(0xFF3B82F6),
                accent = Color(0xFF2563EB)
            )
            ReaderTheme.SEPIA -> ReaderColors(
                canvasBackground = Color(0xFFEFE6CF),
                sheetBackground = Color(0xFFFBF6E9),
                text = Color(0xFF3C2814),
                textSecondary = Color(0xFF7E6B52),
                surface = Color(0xFFF4ECDA),
                border = Color(0xFFE3D6BC),
                quoteBorder = Color(0xFFB45309),
                accent = Color(0xFFB45309)
            )
            ReaderTheme.DARK -> ReaderColors(
                canvasBackground = Color(0xFF131316),
                sheetBackground = Color(0xFF1E1E24),
                text = Color(0xFFE5E5EB),
                textSecondary = Color(0xFFA1A1AC),
                surface = Color(0xFF2A2A32),
                border = Color(0xFF34343E),
                quoteBorder = Color(0xFF60A5FA),
                accent = Color(0xFF60A5FA)
            )
            ReaderTheme.AMOLED -> ReaderColors(
                canvasBackground = Color(0xFF000000),
                sheetBackground = Color(0xFF0A0A0D),
                text = Color(0xFFD4D4DE),
                textSecondary = Color(0xFF767684),
                surface = Color(0xFF16161B),
                border = Color(0xFF24242C),
                quoteBorder = Color(0xFF38BDF8),
                accent = Color(0xFF38BDF8)
            )
        }
    }
}

private fun getComposeFontFamily(fontFamily: ReaderFontFamily): FontFamily {
    return when (fontFamily) {
        ReaderFontFamily.SERIF -> FontFamily.Serif
        ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
        ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
    }
}

private val HIGHLIGHT_COLORS = listOf(
    "#FEF08A" to "Amarelo",
    "#A7F3D0" to "Verde",
    "#FBCFE8" to "Rosa",
    "#BAE6FD" to "Azul",
    "#FED7AA" to "Laranja"
)

private fun parseColorHex(hex: String, defaultColor: Color = Color(0xFFFEF08A)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun TesseraReaderScreen(
    article: ReaderArticle,
    isLoading: Boolean = false,
    fontSizeSp: Int,
    theme: ReaderTheme,
    fontFamily: ReaderFontFamily,
    showImages: Boolean,
    isTtsPlaying: Boolean,
    isSettingsOpen: Boolean,
    readerHighlights: Map<Int, String> = emptyMap(),
    isHighlighterActive: Boolean = false,
    activeHighlightColor: String = "#FEF08A",
    isAudioBarVisible: Boolean = false,
    podcastAudioState: PodcastAudioState = PodcastAudioState(),
    onClose: () -> Unit,
    onToggleSettings: () -> Unit,
    onUpdateFontSize: (delta: Int) -> Unit,
    onSelectTheme: (ReaderTheme) -> Unit,
    onSelectFontFamily: (ReaderFontFamily) -> Unit,
    onToggleShowImages: () -> Unit,
    onToggleTts: () -> Unit,
    onToggleHighlighterActive: () -> Unit = {},
    onSelectHighlightColor: (String) -> Unit = {},
    onToggleHighlightBlock: (Int, String?) -> Unit = { _, _ -> },
    onClearHighlights: () -> Unit = {},
    onToggleAudioBar: () -> Unit = {},
    onSeekAudio: (Long) -> Unit = {},
    onCycleSpeed: () -> Unit = {},
    onOpenArcSummary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = rememberReaderColors(theme)
    val composeFontFamily = remember(fontFamily) { getComposeFontFamily(fontFamily) }
    val listState = rememberLazyListState()

    // Smooth Reading Progress based on list state
    val readingProgress by remember(article.blocks.size) {
        derivedStateOf {
            val total = article.blocks.size + 2
            if (total <= 1) 0f
            else (listState.firstVisibleItemIndex.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
    }

    // Intercept back button to return to browser
    BackHandler(enabled = true) {
        onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP PDF TOOLBAR
            ReaderTopBar(
                domain = article.domain,
                readTimeMinutes = article.readingTimeMinutes,
                theme = theme,
                colors = colors,
                isTtsPlaying = isTtsPlaying,
                isAudioBarVisible = isAudioBarVisible,
                isHighlighterActive = isHighlighterActive,
                highlightCount = readerHighlights.size,
                isSettingsOpen = isSettingsOpen,
                onClose = onClose,
                onToggleTheme = {
                    val nextTheme = when (theme) {
                        ReaderTheme.SEPIA -> ReaderTheme.DARK
                        ReaderTheme.DARK -> ReaderTheme.AMOLED
                        ReaderTheme.AMOLED -> ReaderTheme.LIGHT
                        ReaderTheme.LIGHT -> ReaderTheme.SEPIA
                    }
                    onSelectTheme(nextTheme)
                },
                onToggleHighlighter = onToggleHighlighterActive,
                onToggleAudioBar = {
                    onToggleAudioBar()
                    if (!isTtsPlaying && !isAudioBarVisible) {
                        onToggleTts()
                    }
                },
                onOpenArcSummary = onOpenArcSummary,
                onCopyText = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    if (clipboard != null && (article.plainText.isNotBlank() || article.blocks.isNotEmpty())) {
                        val text = if (article.plainText.isNotBlank()) {
                            "${article.title}\n\n${article.plainText}"
                        } else {
                            "${article.title}\n\n" + article.blocks.joinToString("\n\n") { it.text }
                        }
                        val clip = ClipData.newPlainText(article.title, text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Texto do artigo copiado!", Toast.LENGTH_SHORT).show()
                    }
                },
                onToggleSettings = onToggleSettings
            )

            // READING PROGRESS BAR (THIN LINE)
            LinearProgressIndicator(
                progress = { readingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = colors.accent,
                trackColor = colors.border
            )

            // EXPANDABLE HIGHLIGHTER TOOLBAR
            AnimatedVisibility(
                visible = isHighlighterActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                HighlighterPaletteBar(
                    activeColor = activeHighlightColor,
                    highlightCount = readerHighlights.size,
                    colors = colors,
                    onSelectColor = onSelectHighlightColor,
                    onClearAll = onClearHighlights
                )
            }

            // EXPANDABLE TYPOGRAPHY & THEME SETTINGS PANEL
            AnimatedVisibility(
                visible = isSettingsOpen,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                ReaderSettingsPanel(
                    fontSizeSp = fontSizeSp,
                    currentTheme = theme,
                    currentFont = fontFamily,
                    showImages = showImages,
                    colors = colors,
                    onUpdateFontSize = onUpdateFontSize,
                    onSelectTheme = onSelectTheme,
                    onSelectFontFamily = onSelectFontFamily,
                    onToggleShowImages = onToggleShowImages
                )
            }

            // MAIN DOCUMENT BODY (PDF SHEET CONTAINER)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isLoading && article.blocks.isEmpty()) {
                    // SKELETON / LOADING SPREAD (INSTANT FEEDBACK)
                    ReaderLoadingSkeleton(
                        title = article.title,
                        domain = article.domain,
                        colors = colors,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp)
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp)
                    ) {
                        // DOCUMENT SHEET CONTAINER (PDF CARD FEEL)
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.sheetBackground,
                                shadowElevation = 3.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 22.dp, vertical = 26.dp)
                                ) {
                                    // PDF / DOCUMENT BADGE
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(colors.surface)
                                                .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PictureAsPdf,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (article.domain.isNotBlank()) "DOCUMENTO • ${article.domain.uppercase()}" else "MODO LEITURA PDF",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = colors.textSecondary
                                            )
                                        }

                                        // Theme Badge Indicator
                                        Text(
                                            text = when (theme) {
                                                ReaderTheme.SEPIA -> "👁️ Conforto Ocular"
                                                ReaderTheme.DARK -> "🌙 Noturno"
                                                ReaderTheme.AMOLED -> "🖤 AMOLED"
                                                ReaderTheme.LIGHT -> "☀️ Claro"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Article Title
                                    Text(
                                        text = article.title,
                                        fontFamily = composeFontFamily,
                                        fontSize = (fontSizeSp + 8).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.text,
                                        lineHeight = (fontSizeSp + 14).sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Metadata Bar (Author, Date, Reading Time, Highlights count)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!article.author.isNullOrBlank()) {
                                            Text(
                                                text = "Por ${article.author}",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (!article.publishDate.isNullOrBlank()) {
                                            Text(
                                                text = "•",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                            Text(
                                                text = article.publishDate,
                                                fontSize = 12.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = "•",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )

                                        Text(
                                            text = "⏱️ ${article.readingTimeMinutes} min",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )

                                        if (readerHighlights.isNotEmpty()) {
                                            Text(
                                                text = "•",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                            Text(
                                                text = "🖍️ ${readerHighlights.size} marcas",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))
                                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Highlighting Instructions banner (if tool active)
                                    if (isHighlighterActive) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.accent.copy(alpha = 0.12f))
                                                .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.BorderColor,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Modo Marca-texto ativado: toque em qualquer parágrafo para destacar ou remover a marcação.",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.text
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    // RENDER ALL ARTICLE BLOCKS
                                    article.blocks.forEachIndexed { index, block ->
                                        val highlightHex = readerHighlights[index]
                                        val isHighlighted = highlightHex != null

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    enabled = isHighlighterActive,
                                                    onClick = {
                                                        onToggleHighlightBlock(index, activeHighlightColor)
                                                    }
                                                )
                                        ) {
                                            ReaderBlockItem(
                                                block = block,
                                                fontSizeSp = fontSizeSp,
                                                fontFamily = composeFontFamily,
                                                showImages = showImages,
                                                colors = colors,
                                                isHighlighted = isHighlighted,
                                                highlightColorHex = highlightHex,
                                                theme = theme,
                                                onLongClick = {
                                                    onToggleHighlightBlock(index, activeHighlightColor)
                                                    Toast.makeText(
                                                        context,
                                                        if (isHighlighted) "Marcação removida!" else "Texto destacado!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        }
                                    }

                                    // DOCUMENT END BANNER
                                    Spacer(modifier = Modifier.height(30.dp))
                                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(18.dp))

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "— Fim do Documento —",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textSecondary,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tessera Reader • Experiência Imersiva Sem Distrações",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING AUDIO CONTROLLER DOCK
        AnimatedVisibility(
            visible = isAudioBarVisible || isTtsPlaying,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderAudioPlayerBar(
                title = article.title,
                domain = article.domain,
                isPlaying = isTtsPlaying,
                audioState = podcastAudioState,
                colors = colors,
                onPlayPause = onToggleTts,
                onSeek = onSeekAudio,
                onCycleSpeed = onCycleSpeed,
                onClose = onToggleAudioBar
            )
        }
    }
}

@Composable
private fun ReaderBlockItem(
    block: ReaderBlock,
    fontSizeSp: Int,
    fontFamily: FontFamily,
    showImages: Boolean,
    colors: ReaderColors,
    isHighlighted: Boolean,
    highlightColorHex: String?,
    theme: ReaderTheme,
    onLongClick: () -> Unit
) {
    val highlightColor = if (isHighlighted && highlightColorHex != null) {
        parseColorHex(highlightColorHex)
    } else Color.Transparent

    val isDarkTheme = theme == ReaderTheme.DARK || theme == ReaderTheme.AMOLED

    val highlightModifier = if (isHighlighted) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isDarkTheme) highlightColor.copy(alpha = 0.28f)
                else highlightColor.copy(alpha = 0.65f)
            )
            .border(
                width = 1.dp,
                color = if (isDarkTheme) highlightColor.copy(alpha = 0.6f) else highlightColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Box(modifier = highlightModifier) {
        when (block.type) {
            ReaderBlockType.H1 -> {
                Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontSize = (fontSizeSp + 6).sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    lineHeight = (fontSizeSp + 12).sp,
                    modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
                )
            }
            ReaderBlockType.H2 -> {
                Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontSize = (fontSizeSp + 4).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    lineHeight = (fontSizeSp + 10).sp,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
            }
            ReaderBlockType.H3 -> {
                Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontSize = (fontSizeSp + 2).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    lineHeight = (fontSizeSp + 8).sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                )
            }
            ReaderBlockType.PARAGRAPH -> {
                Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontSize = fontSizeSp.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.text,
                    lineHeight = (fontSizeSp * 1.68f).sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
            ReaderBlockType.BLOCKQUOTE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(colors.surface)
                        .border(
                            width = 1.dp,
                            color = colors.border,
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(56.dp)
                                .background(colors.quoteBorder)
                        )
                        Text(
                            text = block.text,
                            fontFamily = fontFamily,
                            fontSize = (fontSizeSp + 1).sp,
                            fontStyle = FontStyle.Italic,
                            color = colors.text,
                            lineHeight = (fontSizeSp * 1.6f).sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            ReaderBlockType.IMAGE -> {
                if (showImages && !block.imageUrl.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SubcomposeAsyncImage(
                            model = block.imageUrl,
                            contentDescription = block.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                        )
                        if (!block.caption.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = block.caption,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderTopBar(
    domain: String,
    readTimeMinutes: Int,
    theme: ReaderTheme,
    colors: ReaderColors,
    isTtsPlaying: Boolean,
    isAudioBarVisible: Boolean,
    isHighlighterActive: Boolean,
    highlightCount: Int,
    isSettingsOpen: Boolean,
    onClose: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleHighlighter: () -> Unit,
    onToggleAudioBar: () -> Unit,
    onOpenArcSummary: () -> Unit,
    onCopyText: () -> Unit,
    onToggleSettings: () -> Unit
) {
    Surface(
        color = colors.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close / Back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Sair do modo de leitura",
                        tint = colors.text
                    )
                }

                // Domain & Read Time
                Column {
                    Text(
                        text = domain.ifBlank { "Modo Leitura PDF" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "⏱️ $readTimeMinutes min • PDF",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Quick Toolbar Actions: Theme, Highlighter, Audio, Copy, Aa Settings, AI Summary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Quick Theme Switcher (Conforto Ocular / Escuro / AMOLED / Claro)
                IconButton(onClick = onToggleTheme) {
                    val themeIcon = when (theme) {
                        ReaderTheme.SEPIA -> Icons.Rounded.Visibility
                        ReaderTheme.DARK -> Icons.Rounded.DarkMode
                        ReaderTheme.AMOLED -> Icons.Rounded.DarkMode
                        ReaderTheme.LIGHT -> Icons.Rounded.LightMode
                    }
                    Icon(
                        imageVector = themeIcon,
                        contentDescription = "Alternar Modo Conforto Ocular / Escuro",
                        tint = if (theme == ReaderTheme.SEPIA) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Highlighter Tool Toggle
                IconButton(onClick = onToggleHighlighter) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isHighlighterActive) colors.accent.copy(alpha = 0.2f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BorderColor,
                            contentDescription = "Marca-texto",
                            tint = if (isHighlighterActive) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Audio / TTS Player Toggle
                IconButton(onClick = onToggleAudioBar) {
                    Icon(
                        imageVector = if (isTtsPlaying || isAudioBarVisible) Icons.Rounded.Headphones else Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Ouvir artigo em áudio",
                        tint = if (isTtsPlaying) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Arc AI Summary
                IconButton(onClick = onOpenArcSummary) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Resumir artigo",
                        tint = colors.accent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Typography & Appearance Settings (Aa)
                IconButton(onClick = onToggleSettings) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSettingsOpen) colors.border else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatSize,
                            contentDescription = "Configurações de leitura",
                            tint = if (isSettingsOpen) colors.accent else colors.text
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlighterPaletteBar(
    activeColor: String,
    highlightCount: Int,
    colors: ReaderColors,
    onSelectColor: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Surface(
        color = colors.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Marcador:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )

                HIGHLIGHT_COLORS.forEach { (hex, name) ->
                    val color = parseColorHex(hex)
                    val isSelected = activeColor.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) colors.accent else Color(0x33888888),
                                shape = CircleShape
                            )
                            .clickable { onSelectColor(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = name,
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            if (highlightCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onClearAll() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Limpar marcações",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Limpar ($highlightCount)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderAudioPlayerBar(
    title: String,
    domain: String,
    isPlaying: Boolean,
    audioState: PodcastAudioState,
    colors: ReaderColors,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onCycleSpeed: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = colors.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Headphones,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = if (isPlaying) "Ouvindo Artigo..." else "Áudio do Artigo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar áudio",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Controls (Rewind 10s, Play/Pause, Forward 10s, Speed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Speed Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.border)
                        .clickable { onCycleSpeed() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${audioState.playbackSpeed}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }

                // Rewind 10s
                IconButton(onClick = { onSeek(-10000L) }) {
                    Icon(
                        imageVector = Icons.Rounded.Replay10,
                        contentDescription = "Voltar 10 segundos",
                        tint = colors.text,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Main Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Forward 10s
                IconButton(onClick = { onSeek(10000L) }) {
                    Icon(
                        imageVector = Icons.Rounded.Forward10,
                        contentDescription = "Avançar 10 segundos",
                        tint = colors.text,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Voice / Engine Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Voz Neural",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsPanel(
    fontSizeSp: Int,
    currentTheme: ReaderTheme,
    currentFont: ReaderFontFamily,
    showImages: Boolean,
    colors: ReaderColors,
    onUpdateFontSize: (delta: Int) -> Unit,
    onSelectTheme: (ReaderTheme) -> Unit,
    onSelectFontFamily: (ReaderFontFamily) -> Unit,
    onToggleShowImages: () -> Unit
) {
    Surface(
        color = colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Row 1: Font Size Controls (A- / A+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tamanho do texto",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.border)
                            .clickable { onUpdateFontSize(-2) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                    }

                    Text(
                        text = "${fontSizeSp}sp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                        modifier = Modifier.widthIn(min = 36.dp),
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.border)
                            .clickable { onUpdateFontSize(+2) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A+",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                    }
                }
            }

            // Row 2: Font Family Picker (Serif, Sans, Mono)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fonte do documento",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontPill(
                        label = "Livro (Serif)",
                        fontFamily = FontFamily.Serif,
                        isSelected = currentFont == ReaderFontFamily.SERIF,
                        colors = colors,
                        onClick = { onSelectFontFamily(ReaderFontFamily.SERIF) }
                    )
                    FontPill(
                        label = "Moderno",
                        fontFamily = FontFamily.SansSerif,
                        isSelected = currentFont == ReaderFontFamily.SANS_SERIF,
                        colors = colors,
                        onClick = { onSelectFontFamily(ReaderFontFamily.SANS_SERIF) }
                    )
                    FontPill(
                        label = "Mono",
                        fontFamily = FontFamily.Monospace,
                        isSelected = currentFont == ReaderFontFamily.MONOSPACE,
                        colors = colors,
                        onClick = { onSelectFontFamily(ReaderFontFamily.MONOSPACE) }
                    )
                }
            }

            // Row 3: Reading Themes (Sepia = Conforto Ocular, Escuro, AMOLED, Claro)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Modo de visualização",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = when (currentTheme) {
                            ReaderTheme.SEPIA -> "Conforto Ocular (Luz quente)"
                            ReaderTheme.DARK -> "Modo Escuro suave"
                            ReaderTheme.AMOLED -> "Preto AMOLED puro"
                            ReaderTheme.LIGHT -> "Documento Claro padrão"
                        },
                        fontSize = 11.sp,
                        color = colors.accent
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Sepia / Conforto Ocular
                    ThemeCircle(
                        color = Color(0xFFF7EED8),
                        label = "Conforto Ocular",
                        isSelected = currentTheme == ReaderTheme.SEPIA,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.SEPIA) }
                    )
                    // Dark
                    ThemeCircle(
                        color = Color(0xFF1E1E24),
                        label = "Escuro",
                        isSelected = currentTheme == ReaderTheme.DARK,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.DARK) }
                    )
                    // AMOLED
                    ThemeCircle(
                        color = Color(0xFF000000),
                        label = "AMOLED",
                        isSelected = currentTheme == ReaderTheme.AMOLED,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.AMOLED) }
                    )
                    // Light
                    ThemeCircle(
                        color = Color(0xFFFFFFFF),
                        label = "Claro",
                        isSelected = currentTheme == ReaderTheme.LIGHT,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.LIGHT) }
                    )
                }
            }

            // Row 4: Toggle Article Images
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Exibir imagens do artigo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.text
                    )
                }

                Switch(
                    checked = showImages,
                    onCheckedChange = { onToggleShowImages() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.border,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surface
                    )
                )
            }
        }
    }
}

@Composable
private fun FontPill(
    label: String,
    fontFamily: FontFamily,
    isSelected: Boolean,
    colors: ReaderColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.accent else colors.border)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = fontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else colors.text
        )
    }
}

@Composable
private fun ThemeCircle(
    color: Color,
    label: String,
    isSelected: Boolean,
    colors: ReaderColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) colors.accent else Color(0x33888888),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = label,
                tint = if (color == Color(0xFF000000) || color == Color(0xFF1E1E24)) Color.White else Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ReaderLoadingSkeleton(
    title: String,
    domain: String,
    colors: ReaderColors,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.sheetBackground,
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = alpha))
                )
                Text(
                    text = "FORMATANDO DOCUMENTO PARA LEITURA...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title placeholder
            Text(
                text = title.ifBlank { "Formatando página como documento..." },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text.copy(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            Spacer(modifier = Modifier.height(18.dp))

            // Skeleton Paragraphs
            repeat(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it % 2 == 0) 0.95f else 0.85f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.border.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
