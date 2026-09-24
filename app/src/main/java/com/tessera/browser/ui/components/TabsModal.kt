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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.tessera.browser.data.BrowserSpace
import com.tessera.browser.data.TabGroup
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
    tabGroups: List<TabGroup> = emptyList(),
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit,
    onCreateGroup: (title: String, colorArgb: Long) -> Unit = { _, _ -> },
    onDeleteGroup: (groupId: String, closeTabs: Boolean) -> Unit = { _, _ -> },
    onAddTabToGroup: (tabId: String, groupId: String) -> Unit = { _, _ -> },
    onRemoveTabFromGroup: (tabId: String) -> Unit = {},
    onTogglePin: (String) -> Unit = {},
    onCloseAllTabs: () -> Unit = {},
    onArchiveInactiveTabs: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    spaces: List<BrowserSpace> = BrowserSpace.DEFAULT_SPACES,
    activeSpaceId: String = "space_general",
    onSelectSpace: (String) -> Unit = {},
    onCreateSpace: (name: String, emoji: String, colorArgb: Long) -> Unit = { _, _, _ -> },
    onUpdateSpace: (spaceId: String, name: String, emoji: String, colorArgb: Long) -> Unit = { _, _, _, _ -> },
    onDeleteSpace: (spaceId: String) -> Unit = {},
    onMoveTabToSpace: (tabId: String, targetSpaceId: String) -> Unit = { _, _ -> },
    accentColor: Color = Color(0xFF0288D1),
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var filterPinnedOnly by remember { mutableStateOf(false) }
    var activeFilterGroupId by remember { mutableStateOf<String?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newGroupTitle by remember { mutableStateOf("") }
    var newGroupColor by remember { mutableStateOf(TabGroup.PRESET_COLORS.first()) }
    var tabToAssignGroup by remember { mutableStateOf<BrowserTab?>(null) }
    var tabToMoveSpace by remember { mutableStateOf<BrowserTab?>(null) }

    val currentSpaceTabs = tabs.filter { it.spaceId == activeSpaceId }
    val filteredTabs = currentSpaceTabs.filter { tab ->
        val matchesQuery = searchQuery.isBlank() ||
                tab.title.contains(searchQuery, ignoreCase = true) ||
                tab.url.contains(searchQuery, ignoreCase = true)
        val matchesPin = !filterPinnedOnly || tab.isPinned
        val matchesGroup = activeFilterGroupId == null || tab.groupId == activeFilterGroupId
        matchesQuery && matchesPin && matchesGroup
    }

    val contentColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF8E8E93)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // -1. SELETOR DE ESPAÇOS (Arc Spaces Carousel)
        SpacesCarouselBar(
            spaces = spaces,
            activeSpaceId = activeSpaceId,
            tabs = tabs,
            isDarkMode = isDarkMode,
            onSelectSpace = onSelectSpace,
            onCreateSpace = onCreateSpace,
            onUpdateSpace = onUpdateSpace,
            onDeleteSpace = onDeleteSpace
        )

        // 0. SELETOR DE GRUPOS DE ABAS (Pills Horizontais)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip "Todas"
            item {
                val isAllSelected = activeFilterGroupId == null
                val allShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .clip(allShape)
                        .background(
                            if (isAllSelected) accentColor
                            else if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE5E7EB)
                        )
                        .clickable { activeFilterGroupId = null }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Todas (${tabs.size})",
                        color = if (isAllSelected) Color.White else contentColor,
                        fontSize = 12.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Chips dos grupos existentes
            items(tabGroups, key = { it.id }) { group ->
                val isSelected = activeFilterGroupId == group.id
                val groupTabCount = tabs.count { it.groupId == group.id }
                val groupShape = RoundedCornerShape(16.dp)
                var showGroupMenu by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .clip(groupShape)
                        .background(
                            if (isSelected) Color(group.colorArgb)
                            else Color(group.colorArgb).copy(alpha = if (isDarkMode) 0.22f else 0.14f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.3f) else Color(group.colorArgb).copy(alpha = 0.4f),
                            groupShape
                        )
                        .clickable {
                            activeFilterGroupId = if (isSelected) null else group.id
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color(group.colorArgb))
                        )
                        Text(
                            text = "${group.title} ($groupTabCount)",
                            color = if (isSelected) Color.White else contentColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { showGroupMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "Opções do grupo",
                                tint = if (isSelected) Color.White.copy(alpha = 0.8f) else mutedColor,
                                modifier = Modifier.size(13.dp)
                            )

                            DropdownMenu(
                                expanded = showGroupMenu,
                                onDismissRequest = { showGroupMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Excluir grupo (manter abas)") },
                                    onClick = {
                                        showGroupMenu = false
                                        if (activeFilterGroupId == group.id) activeFilterGroupId = null
                                        onDeleteGroup(group.id, false)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Fechar grupo e abas") },
                                    onClick = {
                                        showGroupMenu = false
                                        if (activeFilterGroupId == group.id) activeFilterGroupId = null
                                        onDeleteGroup(group.id, true)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Botão "+ Grupo"
            item {
                val addShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .clip(addShape)
                        .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F3F5))
                        .border(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE0E0E0), addShape)
                        .clickable {
                            newGroupTitle = ""
                            newGroupColor = TabGroup.PRESET_COLORS.first()
                            showCreateGroupDialog = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Novo grupo",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Grupo",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

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
                        text = when {
                            filterPinnedOnly -> "Nenhuma aba fixada"
                            activeFilterGroupId != null -> "Nenhuma aba neste grupo"
                            else -> "Nenhuma aba encontrada"
                        },
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
                        val tabGroup = tabGroups.find { it.id == tab.groupId }
                        TabCardItem(
                            tab = tab,
                            isActive = isActive,
                            tabGroup = tabGroup,
                            allGroups = tabGroups,
                            allSpaces = spaces,
                            isDarkMode = isDarkMode,
                            accentColor = accentColor,
                            onClick = { onSelectTab(tab.id) },
                            onClose = { onCloseTab(tab.id) },
                            onAssignGroup = { gid -> onAddTabToGroup(tab.id, gid) },
                            onRemoveFromGroup = { onRemoveTabFromGroup(tab.id) },
                            onMoveToSpaceClick = { tabToMoveSpace = tab },
                            onCreateNewGroup = {
                                tabToAssignGroup = tab
                                newGroupTitle = ""
                                newGroupColor = TabGroup.PRESET_COLORS.first()
                                showCreateGroupDialog = true
                            }
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
                            text = { Text("Novo grupo de abas") },
                            onClick = {
                                showMenu = false
                                newGroupTitle = ""
                                newGroupColor = TabGroup.PRESET_COLORS.first()
                                showCreateGroupDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Arquivar abas inativas (24h)") },
                            onClick = {
                                showMenu = false
                                onArchiveInactiveTabs()
                            }
                        )
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

    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateGroupDialog = false
                tabToAssignGroup = null
            },
            title = { Text("Novo Grupo de Abas", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = newGroupTitle,
                        onValueChange = { newGroupTitle = it },
                        label = { Text("Nome do grupo") },
                        placeholder = { Text("Ex: Trabalho, Compras, Pesquisa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Escolha uma cor:",
                        fontSize = 13.sp,
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabGroup.PRESET_COLORS.forEach { colorArgb ->
                            val isSelected = newGroupColor == colorArgb
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorArgb))
                                    .clickable { newGroupColor = colorArgb },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newGroupTitle.isNotBlank()) {
                            onCreateGroup(newGroupTitle.trim(), newGroupColor)
                            showCreateGroupDialog = false
                            tabToAssignGroup = null
                        }
                    }
                ) {
                    Text("Criar", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateGroupDialog = false
                    tabToAssignGroup = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    tabToMoveSpace?.let { targetTab ->
        MoveTabToSpaceDialog(
            tabTitle = targetTab.title,
            spaces = spaces,
            currentSpaceId = activeSpaceId,
            onSelectSpace = { newSpaceId ->
                onMoveTabToSpace(targetTab.id, newSpaceId)
                tabToMoveSpace = null
            },
            onDismiss = { tabToMoveSpace = null }
        )
    }
}

@Composable
private fun TabCardItem(
    tab: BrowserTab,
    isActive: Boolean,
    tabGroup: TabGroup? = null,
    allGroups: List<TabGroup> = emptyList(),
    allSpaces: List<BrowserSpace> = emptyList(),
    isDarkMode: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onAssignGroup: (String) -> Unit = {},
    onRemoveFromGroup: () -> Unit = {},
    onMoveToSpaceClick: () -> Unit = {},
    onCreateNewGroup: () -> Unit = {}
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

            // Ações da Aba (Menu de Grupo + Fechar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                var showTabMenu by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF38322E) else Color(0xFFEBECEF))
                        .clickable { showTabMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Opções da aba",
                        tint = mutedColor,
                        modifier = Modifier.size(12.dp)
                    )

                    DropdownMenu(
                        expanded = showTabMenu,
                        onDismissRequest = { showTabMenu = false }
                    ) {
                        if (allGroups.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Mover para grupo:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = mutedColor) },
                                onClick = {},
                                enabled = false
                            )
                            allGroups.forEach { g ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(g.colorArgb))
                                            )
                                            Text(g.title)
                                        }
                                    },
                                    onClick = {
                                        showTabMenu = false
                                        onAssignGroup(g.id)
                                    }
                                )
                            }
                        }
                        if (tabGroup != null) {
                            DropdownMenuItem(
                                text = { Text("Remover do grupo") },
                                onClick = {
                                    showTabMenu = false
                                    onRemoveFromGroup()
                                }
                            )
                        }
                        if (allSpaces.size > 1) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.SwapHoriz, null, modifier = Modifier.size(16.dp))
                                        Text("Mover para Espaço...")
                                    }
                                },
                                onClick = {
                                    showTabMenu = false
                                    onMoveToSpaceClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Criar novo grupo...") },
                            onClick = {
                                showTabMenu = false
                                onCreateNewGroup()
                            }
                        )
                    }
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
        }

        // Tag do Grupo de Abas (se houver)
        if (tabGroup != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(tabGroup.colorArgb).copy(alpha = 0.18f))
                    .border(0.5.dp, Color(tabGroup.colorArgb).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(tabGroup.colorArgb))
                    )
                    Text(
                        text = tabGroup.title,
                        color = Color(tabGroup.colorArgb),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

