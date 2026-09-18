package com.tessera.browser.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tessera.browser.viewmodel.ReaderArticle
import com.tessera.browser.viewmodel.ReaderBlockType
import com.tessera.browser.viewmodel.ReaderFontFamily
import com.tessera.browser.viewmodel.ReaderTheme

private data class ReaderColors(
    val background: Color,
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
                background = Color(0xFFFBF9F5),
                text = Color(0xFF1E1E20),
                textSecondary = Color(0xFF6E6E73),
                surface = Color(0xFFF0EDE6),
                border = Color(0xFFE2DDD5),
                quoteBorder = Color(0xFFB5884B),
                accent = Color(0xFFD97706)
            )
            ReaderTheme.SEPIA -> ReaderColors(
                background = Color(0xFFF4ECD8),
                text = Color(0xFF382818),
                textSecondary = Color(0xFF7C6A53),
                surface = Color(0xFFE8DEBF),
                border = Color(0xFFDECFA6),
                quoteBorder = Color(0xFFB88A44),
                accent = Color(0xFFB45309)
            )
            ReaderTheme.DARK -> ReaderColors(
                background = Color(0xFF1C1C1E),
                text = Color(0xFFE4E4E7),
                textSecondary = Color(0xFFA1A1AA),
                surface = Color(0xFF2C2C2E),
                border = Color(0xFF38383A),
                quoteBorder = Color(0xFF60A5FA),
                accent = Color(0xFF38BDF8)
            )
            ReaderTheme.AMOLED -> ReaderColors(
                background = Color(0xFF000000),
                text = Color(0xFFD4D4D8),
                textSecondary = Color(0xFF71717A),
                surface = Color(0xFF141416),
                border = Color(0xFF27272A),
                quoteBorder = Color(0xFF3B82F6),
                accent = Color(0xFF60A5FA)
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

@Composable
fun TesseraReaderScreen(
    article: ReaderArticle,
    fontSizeSp: Int,
    theme: ReaderTheme,
    fontFamily: ReaderFontFamily,
    showImages: Boolean,
    isTtsPlaying: Boolean,
    isSettingsOpen: Boolean,
    onClose: () -> Unit,
    onToggleSettings: () -> Unit,
    onUpdateFontSize: (delta: Int) -> Unit,
    onSelectTheme: (ReaderTheme) -> Unit,
    onSelectFontFamily: (ReaderFontFamily) -> Unit,
    onToggleShowImages: () -> Unit,
    onToggleTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = rememberReaderColors(theme)
    val composeFontFamily = remember(fontFamily) { getComposeFontFamily(fontFamily) }

    // Intercept hardware/system back button to smoothly return to web page
    BackHandler(enabled = true) {
        onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP STICKY TOOLBAR
            ReaderTopBar(
                domain = article.domain,
                readTimeMinutes = article.readingTimeMinutes,
                colors = colors,
                isTtsPlaying = isTtsPlaying,
                isSettingsOpen = isSettingsOpen,
                onClose = onClose,
                onToggleTts = onToggleTts,
                onCopyText = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    if (clipboard != null && article.plainText.isNotBlank()) {
                        val clip = ClipData.newPlainText(article.title, "${article.title}\n\n${article.plainText}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Texto do artigo copiado!", Toast.LENGTH_SHORT).show()
                    }
                },
                onToggleSettings = onToggleSettings
            )

            // OPTIONAL EXPANDABLE SETTINGS PANEL
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

            // ARTICLE CONTENT SCROLLER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 700.dp)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp)
                ) {
                    // Title
                    item {
                        Text(
                            text = article.title,
                            fontFamily = composeFontFamily,
                            fontSize = (fontSizeSp + 8).sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            lineHeight = (fontSizeSp + 15).sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Metadata (Author & Published Date)
                    if (!article.author.isNullOrBlank() || !article.publishDate.isNullOrBlank()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!article.author.isNullOrBlank()) {
                                    Text(
                                        text = "Por ${article.author}",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (!article.author.isNullOrBlank() && !article.publishDate.isNullOrBlank()) {
                                    Text(
                                        text = "•",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                if (!article.publishDate.isNullOrBlank()) {
                                    Text(
                                        text = article.publishDate,
                                        fontSize = 13.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(
                                color = colors.border,
                                thickness = 1.dp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                        }
                    } else {
                        item {
                            HorizontalDivider(
                                color = colors.border,
                                thickness = 1.dp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                        }
                    }

                    // Article Blocks (Headings, Paragraphs, Quotes, Images)
                    items(article.blocks) { block ->
                        when (block.type) {
                            ReaderBlockType.H1 -> {
                                Text(
                                    text = block.text,
                                    fontFamily = composeFontFamily,
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
                                    fontFamily = composeFontFamily,
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
                                    fontFamily = composeFontFamily,
                                    fontSize = (fontSizeSp + 2).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.text,
                                    lineHeight = (fontSizeSp + 8).sp,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                                )
                            }
                            ReaderBlockType.PARAGRAPH -> {
                                Text(
                                    text = block.text,
                                    fontFamily = composeFontFamily,
                                    fontSize = fontSizeSp.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colors.text,
                                    lineHeight = (fontSizeSp * 1.65f).sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
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
                                                .height(60.dp)
                                                .background(colors.quoteBorder)
                                        )
                                        Text(
                                            text = block.text,
                                            fontFamily = composeFontFamily,
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

                    // Bottom ending flourish
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "— Fim do Artigo —",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                letterSpacing = 2.sp
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
    colors: ReaderColors,
    isTtsPlaying: Boolean,
    isSettingsOpen: Boolean,
    onClose: () -> Unit,
    onToggleTts: () -> Unit,
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

                // Domain and Reading Time
                Column {
                    Text(
                        text = domain.ifBlank { "Leitura Focada" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "⏱️ $readTimeMinutes min de leitura",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Quick Actions: TTS, Copy, Typography (Aa)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // TTS Play / Stop
                IconButton(onClick = onToggleTts) {
                    Icon(
                        imageVector = if (isTtsPlaying) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = if (isTtsPlaying) "Parar leitura em voz alta" else "Ouvir artigo em voz alta",
                        tint = if (isTtsPlaying) colors.accent else colors.textSecondary
                    )
                }

                // Copy clean text
                IconButton(onClick = onCopyText) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copiar texto limpo",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Typography & Appearance Settings
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
                    text = "Fonte",
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

            // Row 3: Reading Themes (Light, Sepia, Dark, AMOLED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tema de leitura",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeCircle(
                        color = Color(0xFFFBF9F5),
                        isSelected = currentTheme == ReaderTheme.LIGHT,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.LIGHT) }
                    )
                    ThemeCircle(
                        color = Color(0xFFF4ECD8),
                        isSelected = currentTheme == ReaderTheme.SEPIA,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.SEPIA) }
                    )
                    ThemeCircle(
                        color = Color(0xFF1C1C1E),
                        isSelected = currentTheme == ReaderTheme.DARK,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.DARK) }
                    )
                    ThemeCircle(
                        color = Color(0xFF000000),
                        isSelected = currentTheme == ReaderTheme.AMOLED,
                        colors = colors,
                        onClick = { onSelectTheme(ReaderTheme.AMOLED) }
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = fontFamily,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else colors.text
        )
    }
}

@Composable
private fun ThemeCircle(
    color: Color,
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
                contentDescription = null,
                tint = if (color == Color(0xFF000000) || color == Color(0xFF1C1C1E)) Color.White else Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
