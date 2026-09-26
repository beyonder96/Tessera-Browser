package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.tessera.browser.data.BrowserSpace
import com.tessera.browser.data.TabGroup
import com.tessera.browser.viewmodel.BrowserTab

/**
 * Modal profissional e despoluído de gerenciamento de abas do Tessera Browser.
 * Oferece visualização em grade 2 colunas limpa, seletor compacto de espaços,
 * filtragem sutil por grupos e navegação intuitiva.
 */
@OptIn(ExperimentalFoundationApi::class)
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
    var isSearchVisible by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSpaceSelectorMenu by remember { mutableStateOf(false) }
    var filterPinnedOnly by remember { mutableStateOf(false) }
    var activeFilterGroupId by remember { mutableStateOf<String?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateSpaceDialog by remember { mutableStateOf(false) }
    var newGroupTitle by remember { mutableStateOf("") }
    var newGroupColor by remember { mutableStateOf(TabGroup.PRESET_COLORS.first()) }
    var newSpaceName by remember { mutableStateOf("") }
    var newSpaceEmoji by remember { mutableStateOf("📁") }
    var tabToAssignGroup by remember { mutableStateOf<BrowserTab?>(null) }
    var tabToMoveSpace by remember { mutableStateOf<BrowserTab?>(null) }

    val currentSpace = spaces.find { it.id == activeSpaceId } ?: spaces.firstOrNull() ?: BrowserSpace.DEFAULT_SPACES.first()
    val currentSpaceTabs = tabs.filter { it.spaceId == activeSpaceId }
    val filteredTabs = currentSpaceTabs.filter { tab ->
        val matchesQuery = searchQuery.isBlank() ||
                tab.title.contains(searchQuery, ignoreCase = true) ||
                tab.url.contains(searchQuery, ignoreCase = true)
        val matchesPin = !filterPinnedOnly || tab.isPinned
        val matchesGroup = activeFilterGroupId == null || tab.groupId == activeFilterGroupId
        matchesQuery && matchesPin && matchesGroup
    }

    val sheetBg = if (isDarkMode) Color(0xF71C1715) else Color(0xFAFBFBFD)
    val cardBg = if (isDarkMode) Color(0xFF26201D) else Color.White
    val cardBorder = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.07f)
    val contentColor = if (isDarkMode) Color(0xFFF3F3F5) else Color(0xFF1C1C1E)
    val mutedColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF73737A)
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .shadow(24.dp, sheetShape)
            .clip(sheetShape)
            .background(sheetBg)
            .border(1.dp, cardBorder, sheetShape)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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

            // 2. HEADER BAR: Space Selector Capsule + Counter + Actions (Search & Done)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Space Selector Dropdown Pill
                Box {
                    val spaceShape = RoundedCornerShape(16.dp)
                    val spaceColor = Color(currentSpace.colorArgb)
                    Row(
                        modifier = Modifier
                            .clip(spaceShape)
                            .background(spaceColor.copy(alpha = if (isDarkMode) 0.18f else 0.12f))
                            .border(1.dp, spaceColor.copy(alpha = 0.35f), spaceShape)
                            .clickable { showSpaceSelectorMenu = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentSpace.iconEmoji,
                            fontSize = 14.sp
                        )
                        Text(
                            text = currentSpace.name,
                            color = contentColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(spaceColor.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${currentSpaceTabs.size}",
                                color = if (isDarkMode) Color.White else spaceColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Trocar Espaço",
                            tint = mutedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Dropdown de Espaços
                    DropdownMenu(
                        expanded = showSpaceSelectorMenu,
                        onDismissRequest = { showSpaceSelectorMenu = false }
                    ) {
                        Text(
                            text = "SELECIONAR ESPAÇO",
                            color = mutedColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        spaces.forEach { sp ->
                            val isCurrent = sp.id == activeSpaceId
                            val count = tabs.count { it.spaceId == sp.id }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = sp.iconEmoji, fontSize = 15.sp)
                                        Text(
                                            text = sp.name,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) accentColor else contentColor
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "$count",
                                            color = mutedColor,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    showSpaceSelectorMenu = false
                                    onSelectSpace(sp.id)
                                },
                                trailingIcon = if (isCurrent) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Novo Espaço...",
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            },
                            onClick = {
                                showSpaceSelectorMenu = false
                                newSpaceName = ""
                                newSpaceEmoji = "🚀"
                                showCreateSpaceDialog = true
                            }
                        )
                    }
                }

                // Top Right Controls (Search Toggle + Concluído)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSearchVisible) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) searchQuery = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Buscar guias",
                            tint = if (isSearchVisible) accentColor else mutedColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Concluído",
                            color = accentColor,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. SEARCH BAR EXPANSÍVEL
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                ) {
                    val searchShape = RoundedCornerShape(12.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(searchShape)
                            .background(cardBg)
                            .border(1.dp, cardBorder, searchShape)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = mutedColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Filtrar por título ou endereço...",
                                    color = mutedColor,
                                    fontSize = 12.5.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = contentColor,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(accentColor),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Limpar busca",
                                tint = mutedColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }

            // 4. SUB-BAR: Tab Groups Filter Chips (Somente se existirem grupos cadastrados)
            if (tabGroups.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip "Todas"
                    item {
                        val isAllSelected = activeFilterGroupId == null
                        val pillShape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(
                                    if (isAllSelected) accentColor
                                    else if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                )
                                .clickable { activeFilterGroupId = null }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Todas (${currentSpaceTabs.size})",
                                color = if (isAllSelected) Color.White else contentColor,
                                fontSize = 11.5.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Chips dos grupos criados
                    items(tabGroups, key = { it.id }) { group ->
                        val isSelected = activeFilterGroupId == group.id
                        val count = tabs.count { it.groupId == group.id }
                        val pillShape = RoundedCornerShape(14.dp)
                        var showGroupMenu by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(
                                    if (isSelected) Color(group.colorArgb)
                                    else Color(group.colorArgb).copy(alpha = if (isDarkMode) 0.18f else 0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.35f) else Color(group.colorArgb).copy(alpha = 0.3f),
                                    pillShape
                                )
                                .clickable {
                                    activeFilterGroupId = if (isSelected) null else group.id
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else Color(group.colorArgb))
                                )
                                Text(
                                    text = "${group.title} ($count)",
                                    color = if (isSelected) Color.White else contentColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .clickable { showGroupMenu = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreHoriz,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White.copy(alpha = 0.85f) else mutedColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    DropdownMenu(
                                        expanded = showGroupMenu,
                                        onDismissRequest = { showGroupMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Excluir grupo (manter guias)") },
                                            onClick = {
                                                showGroupMenu = false
                                                if (activeFilterGroupId == group.id) activeFilterGroupId = null
                                                onDeleteGroup(group.id, false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Fechar grupo e guias", color = Color(0xFFEF5350)) },
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
                        val addShape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier
                                .clip(addShape)
                                .background(if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                                .clickable {
                                    newGroupTitle = ""
                                    newGroupColor = TabGroup.PRESET_COLORS.first()
                                    showCreateGroupDialog = true
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Grupo",
                                    color = accentColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 5. GRADE PRINCIPAL DE ABAS (2 COLUNAS MODERNAS E LIMPAS)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredTabs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = mutedColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = when {
                                    filterPinnedOnly -> "Nenhuma guia fixada"
                                    searchQuery.isNotBlank() -> "Nenhum resultado para \"$searchQuery\""
                                    activeFilterGroupId != null -> "Nenhuma guia neste grupo"
                                    else -> "Nenhuma guia aberta neste espaço"
                                },
                                color = contentColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Abra uma nova guia para continuar navegando",
                                color = mutedColor,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredTabs, key = { it.id }) { tab ->
                            val isActive = tab.id == activeTabId
                            val tabGroup = tabGroups.find { it.id == tab.groupId }

                            TabGridCard(
                                tab = tab,
                                isActive = isActive,
                                tabGroup = tabGroup,
                                allGroups = tabGroups,
                                allSpaces = spaces,
                                isDarkMode = isDarkMode,
                                accentColor = accentColor,
                                onClick = { onSelectTab(tab.id) },
                                onClose = { onCloseTab(tab.id) },
                                onTogglePin = { onTogglePin(tab.id) },
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

            // 6. BARRA INFERIOR DE AÇÕES (Dock Minimalista com Botão Central em Destaque)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(sheetBg)
                    .border(width = 0.8.dp, color = cardBorder)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Menu de Opções Adicionais (•••)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                        .border(1.dp, cardBorder, CircleShape)
                        .clickable { showMoreMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "Mais opções",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Histórico de navegação") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMoreMenu = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (filterPinnedOnly) "Mostrar todas as guias" else "Apenas guias fixadas")
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMoreMenu = false
                                filterPinnedOnly = !filterPinnedOnly
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Novo grupo de abas") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMoreMenu = false
                                newGroupTitle = ""
                                newGroupColor = TabGroup.PRESET_COLORS.first()
                                showCreateGroupDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Arquivar guias inativas (24h)") },
                            leadingIcon = {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMoreMenu = false
                                onArchiveInactiveTabs()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fechar todas as guias", color = Color(0xFFEF5350)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Close, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMoreMenu = false
                                onCloseAllTabs()
                            }
                        )
                    }
                }

                // Botão Central: [+ Nova Guia] em Cápsula Elegante com Glow
                val newTabShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = newTabShape,
                            ambientColor = accentColor.copy(alpha = 0.35f),
                            spotColor = accentColor.copy(alpha = 0.25f)
                        )
                        .clip(newTabShape)
                        .background(accentColor)
                        .clickable(onClick = onNewTab)
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Nova Guia",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Nova Guia",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Indicador de Filtro Fixado ou Espaço
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (filterPinnedOnly) accentColor.copy(alpha = 0.15f) else cardBg)
                        .border(
                            1.dp,
                            if (filterPinnedOnly) accentColor.copy(alpha = 0.4f) else cardBorder,
                            CircleShape
                        )
                        .clickable { filterPinnedOnly = !filterPinnedOnly },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Filtrar Fixadas",
                        tint = if (filterPinnedOnly) accentColor else mutedColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // DIÁLOGO: CRIAR NOVO GRUPO
    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateGroupDialog = false
                tabToAssignGroup = null
            },
            title = { Text("Novo Grupo de Guias", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = newGroupTitle,
                        onValueChange = { newGroupTitle = it },
                        label = { Text("Nome do grupo") },
                        placeholder = { Text("Ex: Trabalho, Estudos, Finanças") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Escolha uma cor de identificação:",
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
                                    .size(28.dp)
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
                                        modifier = Modifier.size(15.dp)
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
                    Text("Criar Grupo", color = accentColor, fontWeight = FontWeight.Bold)
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

    // DIÁLOGO: CRIAR NOVO ESPAÇO
    if (showCreateSpaceDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSpaceDialog = false },
            title = { Text("Novo Espaço", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSpaceName,
                        onValueChange = { newSpaceName = it },
                        label = { Text("Nome do Espaço") },
                        placeholder = { Text("Ex: Pessoal, Trabalho, Projetos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Ícone Emoji:", fontSize = 13.sp)
                        val emojiPresets = listOf("🚀", "💼", "📚", "🎨", "🛍️", "🏖️", "🎧")
                        emojiPresets.forEach { emoji ->
                            val isSel = newSpaceEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { newSpaceEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSpaceName.isNotBlank()) {
                            onCreateSpace(newSpaceName.trim(), newSpaceEmoji, TabGroup.PRESET_COLORS[1])
                            showCreateSpaceDialog = false
                        }
                    }
                ) {
                    Text("Criar Espaço", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSpaceDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // DIÁLOGO: MOVER GUIA PARA OUTRO ESPAÇO
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

/**
 * Card de aba individual para a grade 2 colunas.
 * Despoluído, moderno, sem elementos estáticos falsos, com preview realista.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabGridCard(
    tab: BrowserTab,
    isActive: Boolean,
    tabGroup: TabGroup? = null,
    allGroups: List<TabGroup> = emptyList(),
    allSpaces: List<BrowserSpace> = emptyList(),
    isDarkMode: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onTogglePin: () -> Unit,
    onAssignGroup: (String) -> Unit = {},
    onRemoveFromGroup: () -> Unit = {},
    onMoveToSpaceClick: () -> Unit = {},
    onCreateNewGroup: () -> Unit = {}
) {
    val cardShape = RoundedCornerShape(16.dp)
    val cardBg = if (isDarkMode) Color(0xFF26201D) else Color.White
    val contentColor = if (isDarkMode) Color(0xFFF3F3F5) else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color(0xFF9E9EA4) else Color(0xFF73737A)
    var showContextMenu by remember { mutableStateOf(false) }

    val hostDomain = remember(tab.url, tab.isHomePage) {
        if (tab.isHomePage) {
            "tessera://inicio"
        } else {
            tab.url.removePrefix("https://").removePrefix("http://").takeWhile { it != '/' && it != '?' }
                .ifBlank { "web" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .shadow(
                elevation = if (isActive) 12.dp else 4.dp,
                shape = cardShape,
                ambientColor = if (isActive) accentColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.12f),
                spotColor = if (isActive) accentColor.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.16f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.07f),
                shape = cardShape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HEADER: Favicon/Monogram + Title & Domain + Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Favicon representation
                val isGoogle = hostDomain.contains("google")
                val isX = hostDomain.contains("twitter") || hostDomain.contains("x.com")
                val isGithub = hostDomain.contains("github")

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                tab.isHomePage -> accentColor.copy(alpha = 0.2f)
                                isGoogle -> Color(0xFFE8F0FE)
                                isX -> Color.Black
                                isGithub -> Color(0xFF24292E)
                                else -> accentColor.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        tab.isHomePage -> Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        isGoogle -> Text("G", color = Color(0xFF4285F4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        isX -> Text("𝕏", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        isGithub -> Text("gh", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        else -> Text(
                            text = hostDomain.firstOrNull()?.uppercase() ?: "W",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (tab.isHomePage) "Página Inicial" else tab.title.ifBlank { hostDomain },
                        color = contentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = hostDomain,
                        color = mutedColor,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Close '✕' Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar guia",
                    tint = mutedColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // MINIATURE REALISTIC PREVIEW CONTAINER
        val previewShape = RoundedCornerShape(10.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(previewShape)
                .background(if (isDarkMode) Color(0xFF191412) else Color(0xFFF3F4F6))
                .border(
                    width = 0.8.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f),
                    shape = previewShape
                )
        ) {
            if (tab.isHomePage) {
                // Realistic Start Page Preview with wallpaper
                Image(
                    painter = painterResource(R.drawable.wallpaper_summer_villa),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x33000000), Color(0x99000000))
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = "Tessera Start",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Sleek, minimal webpage miniature preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini browser address pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.04f))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "https://$hostDomain",
                            color = mutedColor,
                            fontSize = 8.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Page title watermark / excerpt preview
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = tab.title.ifBlank { hostDomain },
                            color = contentColor.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(mutedColor.copy(alpha = 0.2f))
                        )
                    }

                    // Bottom mini status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Guia ativa",
                            color = if (isActive) accentColor else mutedColor.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                        }
                    }
                }
            }
        }

        // FOOTER / BADGES: Pin indicator + Group pill + 3-dots Context Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (tab.isPinned) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Fixada",
                                color = accentColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (tabGroup != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(tabGroup.colorArgb).copy(alpha = 0.18f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(tabGroup.colorArgb))
                            )
                            Text(
                                text = tabGroup.title,
                                color = Color(tabGroup.colorArgb),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Discrete 3-dots Context Menu
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { showContextMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Ações da guia",
                    tint = mutedColor,
                    modifier = Modifier.size(14.dp)
                )

                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (tab.isPinned) "Desafixar guia" else "Fixar guia") },
                        leadingIcon = {
                            Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            showContextMenu = false
                            onTogglePin()
                        }
                    )

                    if (allGroups.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Atribuir a grupo:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = mutedColor) },
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
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(g.colorArgb))
                                        )
                                        Text(g.title)
                                    }
                                },
                                onClick = {
                                    showContextMenu = false
                                    onAssignGroup(g.id)
                                }
                            )
                        }
                    }

                    if (tabGroup != null) {
                        DropdownMenuItem(
                            text = { Text("Remover do grupo") },
                            onClick = {
                                showContextMenu = false
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
                                showContextMenu = false
                                onMoveToSpaceClick()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("+ Criar novo grupo...") },
                        onClick = {
                            showContextMenu = false
                            onCreateNewGroup()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Fechar esta guia", color = Color(0xFFEF5350)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            showContextMenu = false
                            onClose()
                        }
                    )
                }
            }
        }
    }
}
