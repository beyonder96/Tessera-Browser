package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

import com.tessera.browser.ui.theme.ApplePalette

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
            ReaderTheme.LIGHT, ReaderTheme.SEPIA -> ReaderColors(
                canvasBackground = Color(0xFFECE7DA),
                sheetBackground = ApplePalette.NewspaperPaper,
                text = ApplePalette.NewspaperInk,
                textSecondary = Color(0xFF5A5852),
                surface = Color(0xFFEFECE2),
                border = Color(0xFFE2DDD0),
                quoteBorder = ApplePalette.NewspaperInk,
                accent = ApplePalette.NewspaperInk
            )
            ReaderTheme.DARK, ReaderTheme.AMOLED -> ReaderColors(
                canvasBackground = ApplePalette.DarkBackground,
                sheetBackground = ApplePalette.DarkBackground,
                text = ApplePalette.NewspaperInkDark,
                textSecondary = Color(0xFF9E9C94),
                surface = ApplePalette.DarkSurface,
                border = ApplePalette.DarkHairline,
                quoteBorder = ApplePalette.NewspaperInkDark,
                accent = ApplePalette.NewspaperInkDark
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

private fun formatReaderDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    val trimmed = rawDate.trim()
    return try {
        if (trimmed.contains("T")) {
            val dateTime = try {
                java.time.OffsetDateTime.parse(trimmed).toLocalDateTime()
            } catch (e: Exception) {
                try {
                    java.time.LocalDateTime.parse(trimmed)
                } catch (e2: Exception) {
                    java.time.LocalDate.parse(trimmed.substringBefore("T")).atStartOfDay()
                }
            }
            val formatter = if (dateTime.hour != 0 || dateTime.minute != 0) {
                java.time.format.DateTimeFormatter.ofPattern("d 'de' MMM. 'de' yyyy, HH:mm", java.util.Locale("pt", "BR"))
            } else {
                java.time.format.DateTimeFormatter.ofPattern("d 'de' MMM. 'de' yyyy", java.util.Locale("pt", "BR"))
            }
            dateTime.format(formatter)
        } else if (trimmed.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))) {
            val date = java.time.LocalDate.parse(trimmed)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d 'de' MMM. 'de' yyyy", java.util.Locale("pt", "BR"))
            date.format(formatter)
        } else {
            trimmed.replace(Regex("""(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})"""), "$1 às $2")
        }
    } catch (e: Exception) {
        trimmed
    }
}

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
    onPrintPdf: () -> Unit = {},
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

    var isActionMenuOpen by remember { mutableStateOf(false) }
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24
        }
    }

    // Intercept back button to return to browser
    BackHandler(enabled = true) {
        if (isActionMenuOpen) {
            isActionMenuOpen = false
        } else if (isSettingsOpen) {
            onToggleSettings()
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP MINIMALIST & CENTERED TOOLBAR
            ReaderTopBar(
                domain = article.domain,
                colors = colors,
                isScrolled = isScrolled,
                onClose = onClose
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
                                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "MODO LEITURA",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = colors.textSecondary
                                            )
                                        }

                                        // Theme Badge Indicator
                                        Text(
                                            text = when (theme) {
                                                ReaderTheme.SEPIA -> "Conforto Ocular"
                                                ReaderTheme.DARK -> "Modo Escuro"
                                                ReaderTheme.AMOLED -> "Preto Puro"
                                                ReaderTheme.LIGHT -> "Modo Claro"
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

                                    // Check if author is redundant (equals or contains domain)
                                    val domainClean = article.domain.lowercase().removePrefix("www.").substringBefore(".")
                                    val authorClean = article.author?.trim().orEmpty()
                                    val isAuthorRedundant = authorClean.isBlank() ||
                                            authorClean.lowercase().contains(domainClean) ||
                                            authorClean.equals(article.domain.trim(), ignoreCase = true)

                                    // Metadata Bar (Author, Date, Reading Time Badge, Highlights)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left: Author & Date (flexible with ellipsis to never push reading time)
                                        Row(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (!isAuthorRedundant) {
                                                Text(
                                                    text = "Por $authorClean",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (!article.publishDate.isNullOrBlank()) {
                                                val formattedDate = formatReaderDate(article.publishDate)
                                                if (formattedDate.isNotBlank()) {
                                                    if (!isAuthorRedundant) {
                                                        Text(
                                                            text = "•",
                                                            fontSize = 12.sp,
                                                            color = colors.textSecondary
                                                        )
                                                    }
                                                    Text(
                                                        text = formattedDate,
                                                        fontSize = 12.sp,
                                                        color = colors.textSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        // Right: Dedicated Reading Time Badge (fixed width, zero line wrapping)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = colors.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                                            ) {
                                                Text(
                                                    text = "⏱️ ${article.readingTimeMinutes} min",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textSecondary,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }

                                            if (readerHighlights.isNotEmpty()) {
                                                Text(
                                                    text = "${readerHighlights.size} destaques",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.accent,
                                                    softWrap = false
                                                )
                                            }
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

                                    val renderedBlocks = remember(article.blocks, article.title) {
                                        val titleClean = article.title.trim().lowercase()
                                        article.blocks.filterIndexed { index, block ->
                                            val blockClean = block.text.trim().lowercase()
                                            !(index == 0 && (block.type == ReaderBlockType.H1 || block.type == ReaderBlockType.H2) &&
                                                    (blockClean == titleClean || blockClean.startsWith(titleClean) || titleClean.startsWith(blockClean)))
                                        }
                                    }

                                    // RENDER ALL ARTICLE BLOCKS
                                    renderedBlocks.forEachIndexed { index, block ->
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

        // BACKDROP SCRIM WHEN ACTION MENU IS OPEN
        if (isActionMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isActionMenuOpen = false }
                    )
            )
        }

        // FLOATING APPLE ACTION MENU
        AnimatedVisibility(
            visible = isActionMenuOpen,
            enter = scaleIn(
                initialScale = 0.85f,
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.9f, 0.95f)
            ) + fadeIn() + slideInVertically { it / 4 },
            exit = scaleOut(
                targetScale = 0.85f,
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.9f, 0.95f)
            ) + fadeOut() + slideOutVertically { it / 4 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 20.dp)
        ) {
            ReaderActionMenu(
                theme = theme,
                colors = colors,
                isTtsPlaying = isTtsPlaying,
                isAudioBarVisible = isAudioBarVisible,
                isHighlighterActive = isHighlighterActive,
                onToggleSettings = {
                    isActionMenuOpen = false
                    onToggleSettings()
                },
                onToggleTheme = {
                    val nextTheme = when (theme) {
                        ReaderTheme.SEPIA -> ReaderTheme.DARK
                        ReaderTheme.DARK -> ReaderTheme.AMOLED
                        ReaderTheme.AMOLED -> ReaderTheme.LIGHT
                        ReaderTheme.LIGHT -> ReaderTheme.SEPIA
                    }
                    onSelectTheme(nextTheme)
                },
                onToggleAudio = {
                    isActionMenuOpen = false
                    onToggleAudioBar()
                    if (!isTtsPlaying && !isAudioBarVisible) {
                        onToggleTts()
                    }
                },
                onToggleHighlighter = {
                    isActionMenuOpen = false
                    onToggleHighlighterActive()
                },
                onOpenSummary = {
                    isActionMenuOpen = false
                    onOpenArcSummary()
                },
                onPrintPdf = {
                    isActionMenuOpen = false
                    onPrintPdf()
                },
                onCopyText = {
                    isActionMenuOpen = false
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
                }
            )
        }

        // APPLE FLOATING ACTION BUTTON (+)
        val fabRotation by animateFloatAsState(
            targetValue = if (isActionMenuOpen) 45f else 0f,
            animationSpec = tween(durationMillis = 240),
            label = "fab_rotation"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .size(54.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.2.dp, colors.border, CircleShape)
                .clickable {
                    isActionMenuOpen = !isActionMenuOpen
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = if (isActionMenuOpen) "Fechar menu" else "Ações do leitor",
                tint = colors.text,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(fabRotation)
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
            ReaderBlockType.LIST_ITEM -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = (fontSizeSp + 2).sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = block.text.removePrefix("•").trim(),
                        fontFamily = fontFamily,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.text,
                        lineHeight = (fontSizeSp * 1.55f).sp
                    )
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
    colors: ReaderColors,
    isScrolled: Boolean,
    onClose: () -> Unit
) {
    val barHeight by animateDpAsState(
        targetValue = if (isScrolled) 46.dp else 56.dp,
        animationSpec = tween(220),
        label = "top_bar_height"
    )
    val titleFontSize by animateFloatAsState(
        targetValue = if (isScrolled) 13.5f else 17f,
        animationSpec = tween(220),
        label = "top_bar_font_size"
    )

    Surface(
        color = colors.surface,
        shadowElevation = if (isScrolled) 4.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(barHeight)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close / Back button (Left)
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Sair do modo de leitura",
                    tint = colors.text,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Centered & Enlarged Domain / Site Name (Center)
            Text(
                text = domain.ifBlank { "Modo Leitura" },
                fontSize = titleFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            // Balance box matching the back button (Right)
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun ReaderActionMenu(
    theme: ReaderTheme,
    colors: ReaderColors,
    isTtsPlaying: Boolean,
    isAudioBarVisible: Boolean,
    isHighlighterActive: Boolean,
    onToggleSettings: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleAudio: () -> Unit,
    onToggleHighlighter: () -> Unit,
    onOpenSummary: () -> Unit,
    onPrintPdf: () -> Unit,
    onCopyText: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = Modifier.width(235.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            ReaderActionMenuItem(
                icon = Icons.Rounded.FormatSize,
                label = "Aparência & Tipografia",
                colors = colors,
                onClick = onToggleSettings
            )
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

            ReaderActionMenuItem(
                icon = when (theme) {
                    ReaderTheme.SEPIA -> Icons.Rounded.Visibility
                    ReaderTheme.DARK, ReaderTheme.AMOLED -> Icons.Rounded.DarkMode
                    ReaderTheme.LIGHT -> Icons.Rounded.LightMode
                },
                label = when (theme) {
                    ReaderTheme.SEPIA -> "Tema: Conforto Ocular"
                    ReaderTheme.DARK -> "Tema: Modo Escuro"
                    ReaderTheme.AMOLED -> "Tema: Preto Puro"
                    ReaderTheme.LIGHT -> "Tema: Modo Claro"
                },
                colors = colors,
                onClick = onToggleTheme
            )
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

            ReaderActionMenuItem(
                icon = if (isTtsPlaying || isAudioBarVisible) Icons.Rounded.Headphones else Icons.AutoMirrored.Rounded.VolumeUp,
                label = if (isTtsPlaying) "Pausar Áudio (TTS)" else "Ouvir Artigo em Áudio",
                colors = colors,
                tint = if (isTtsPlaying) colors.accent else null,
                onClick = onToggleAudio
            )
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

            ReaderActionMenuItem(
                icon = Icons.Rounded.BorderColor,
                label = if (isHighlighterActive) "Desativar Marca-texto" else "Marca-texto",
                colors = colors,
                tint = if (isHighlighterActive) colors.accent else null,
                onClick = onToggleHighlighter
            )
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

            ReaderActionMenuItem(
                icon = Icons.Rounded.AutoAwesome,
                label = "Resumo com IA",
                colors = colors,
                tint = colors.accent,
                onClick = onOpenSummary
            )
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

            ReaderActionMenuItem(
                icon = Icons.Rounded.PictureAsPdf,
                label = "Exportar para PDF",
                colors = colors,
                onClick = onPrintPdf
            )
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)

            ReaderActionMenuItem(
                icon = Icons.Rounded.ContentCopy,
                label = "Copiar Texto do Artigo",
                colors = colors,
                onClick = onCopyText
            )
        }
    }
}

@Composable
private fun ReaderActionMenuItem(
    icon: ImageVector,
    label: String,
    colors: ReaderColors,
    tint: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: colors.text,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
                    text = "Tipografia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontPill(
                        label = "Serif",
                        fontFamily = FontFamily.Serif,
                        isSelected = currentFont == ReaderFontFamily.SERIF,
                        colors = colors,
                        onClick = { onSelectFontFamily(ReaderFontFamily.SERIF) }
                    )
                    FontPill(
                        label = "Sans",
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
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
            color = if (isSelected) Color.White else colors.text,
            maxLines = 1,
            softWrap = false
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
