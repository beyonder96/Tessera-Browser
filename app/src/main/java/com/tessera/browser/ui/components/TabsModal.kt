package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.R
import com.tessera.browser.viewmodel.BrowserTab

@Composable
fun TabsModal(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit,
    onTogglePin: (String) -> Unit = {},
    onCloseAllTabs: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    accentColor: Color = Color(0xFF0288D1),
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var filterPinnedOnly by remember { mutableStateOf(false) }

    val filteredTabs = tabs.filter { tab ->
        val matchesQuery = searchQuery.isBlank() ||
                tab.title.contains(searchQuery, ignoreCase = true) ||
                tab.url.contains(searchQuery, ignoreCase = true)
        val matchesPin = !filterPinnedOnly || tab.isPinned
        matchesQuery && matchesPin
    }

    val contentColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF8E8E93)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. CARROSSEL HORIZONTAL DE ABAS FLUTUANTES (Layout Imagem 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            if (filteredTabs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filterPinnedOnly) "Nenhuma aba fixada" else "Nenhuma aba encontrada",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.White,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(filteredTabs, key = { it.id }) { tab ->
                        val isActive = tab.id == activeTabId
                        TabCardItem(
                            tab = tab,
                            isActive = isActive,
                            isDarkMode = isDarkMode,
                            accentColor = accentColor,
                            onClick = { onSelectTab(tab.id) },
                            onClose = { onCloseTab(tab.id) }
                        )
                    }
                }
            }
        }

        // 2. BARRA DE PESQUISA DE ABAS: "🔍 Search tabs" FLUTUANTE (Layout Imagem 3)
        val searchPillShape = RoundedCornerShape(24.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(46.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = searchPillShape,
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(searchPillShape)
                .background(if (isDarkMode) Color(0xFF2B2623) else Color.White)
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE5E7EB),
                    shape = searchPillShape
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Buscar abas",
                    tint = mutedColor,
                    modifier = Modifier.size(19.dp)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search tabs",
                            color = mutedColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = contentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(accentColor)
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Limpar busca",
                            tint = mutedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. BARRA DE RODAPÉ COM 5 AÇÕES FLUTUANTE (DOCK)
        val dockShape = RoundedCornerShape(26.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = dockShape,
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(dockShape)
                .background(if (isDarkMode) Color(0xFF2B2623) else Color.White)
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE5E7EB),
                    shape = dockShape
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Relógio / Histórico
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenHistory),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Histórico",
                        tint = contentColor,
                        modifier = Modifier.size(23.dp)
                    )
                }

                // 2. Pin / Fixar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (filterPinnedOnly) accentColor.copy(alpha = 0.18f) else Color.Transparent
                        )
                        .clickable { filterPinnedOnly = !filterPinnedOnly },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Abas Fixadas",
                        tint = if (filterPinnedOnly) accentColor else contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 3. Botão [+] Nova Guia em Cápsula Cinza Suave (Layout Imagem 3)
                val plusPillShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(52.dp)
                        .clip(plusPillShape)
                        .background(if (isDarkMode) Color(0xFF38322E) else Color(0xFFE2E4E8))
                        .clickable(onClick = onNewTab),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Nova Guia",
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 4. Badge Numérico com Contagem de Abas (Pílula Preta com Número Branco)
                val badgeShape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 28.dp)
                        .clip(badgeShape)
                        .background(Color(0xFF191919)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabs.size.toString(),
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 5. Mais Opções (•••)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "Mais opções",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Fechar todas as guias") },
                            onClick = {
                                showMenu = false
                                onCloseAllTabs()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fixar aba atual") },
                            onClick = {
                                showMenu = false
                                onTogglePin(activeTabId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fechar menu") },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabCardItem(
    tab: BrowserTab,
    isActive: Boolean,
    isDarkMode: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    val cardBg = if (isDarkMode) Color(0xFF26211E) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)

    Column(
        modifier = Modifier
            .width(156.dp)
            .fillMaxHeight()
            .shadow(
                elevation = if (isActive) 16.dp else 8.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.16f),
                spotColor = if (isActive) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.20f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(
                width = if (isActive) 2.5.dp else 1.dp,
                color = if (isActive) (if (isDarkMode) Color.White else Color(0xFF1A1A1A)) else if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB),
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header inside Tab Card: Favicon + Title + Close '✕'
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Favicon representation
                val isTwitter = tab.url.contains("twitter.com") || tab.url.contains("x.com") || tab.title.contains("Twitter", true)
                val isDine = tab.title.contains("Dine", true) || tab.url.contains("dine", true)
                val isGoogle = tab.url.contains("google.com")

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDine -> Color(0xFFFFF3E0)
                                isTwitter -> Color(0xFF000000)
                                isGoogle -> Color(0xFFE8F0FE)
                                else -> Color(0xFFE0F2FE)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isDine -> Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(13.dp)
                        )
                        isTwitter -> Text(
                            text = "𝕏",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        isGoogle -> Text(
                            text = "G",
                            color = Color(0xFF4285F4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        else -> Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Text(
                    text = if (tab.isHomePage) "Início" else tab.title.ifBlank { "Página Web" },
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Close '✕' Button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF38322E) else Color(0xFFEBECEF))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar aba",
                    tint = mutedColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Body Preview Thumbnail (Rounded container with realistic miniature layout)
        val previewShape = RoundedCornerShape(12.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(previewShape)
                .background(if (isDarkMode) Color(0xFF171311) else Color(0xFFF3F4F6))
                .border(
                    width = 0.5.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFE5E7EB),
                    shape = previewShape
                )
        ) {
            if (tab.isHomePage || tab.url.contains("summer") || tab.title.contains("Dine")) {
                // Summer Villa Preview Thumbnail (Identical to Image 3)
                Image(
                    painter = painterResource(R.drawable.wallpaper_summer_villa),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Text / Badge on thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x00000000), Color(0x99000000))
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "Autumn '23",
                            color = Color(0xFFCCFF00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Collection",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Generic Webpage Mockup Preview (Header banner + profile / text lines)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Avatar / Logo header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title.firstOrNull()?.uppercase() ?: "W",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(contentColor.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(mutedColor.copy(alpha = 0.3f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mock body lines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(mutedColor.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(mutedColor.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(mutedColor.copy(alpha = 0.25f))
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Pill button in card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFF2C2C2C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Acessar",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

