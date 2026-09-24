package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.BrowserSpace
import com.tessera.browser.viewmodel.BrowserTab

/**
 * Barra carrossel horizontal de Espaços (Arc Spaces) com indicador de abas ativas,
 * emoji temático, cor customizada e menu de gerenciamento.
 */
@Composable
fun SpacesCarouselBar(
    spaces: List<BrowserSpace>,
    activeSpaceId: String,
    tabs: List<BrowserTab>,
    isDarkMode: Boolean,
    onSelectSpace: (String) -> Unit,
    onCreateSpace: (name: String, emoji: String, colorArgb: Long) -> Unit,
    onUpdateSpace: (spaceId: String, name: String, emoji: String, colorArgb: Long) -> Unit,
    onDeleteSpace: (spaceId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var spaceToEdit by remember { mutableStateOf<BrowserSpace?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(spaces, key = { it.id }) { space ->
            val isActive = space.id == activeSpaceId
            val spaceTabCount = tabs.count { it.spaceId == space.id }
            val spaceColor = Color(space.colorArgb)
            val pillShape = RoundedCornerShape(18.dp)
            var showMenu by remember { mutableStateOf(false) }

            val pillBg = if (isActive) {
                Brush.horizontalGradient(
                    listOf(
                        spaceColor.copy(alpha = if (isDarkMode) 0.35f else 0.22f),
                        spaceColor.copy(alpha = if (isDarkMode) 0.20f else 0.12f)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    listOf(
                        if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color(0xFFF1F3F5),
                        if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color(0xFFE9ECEF)
                    )
                )
            }

            val pillBorder = if (isActive) {
                spaceColor.copy(alpha = 0.70f)
            } else {
                if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
            }

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(pillBg)
                    .border(width = if (isActive) 1.5.dp else 1.dp, color = pillBorder, shape = pillShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectSpace(space.id)
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Emoji do Espaço
                    Text(
                        text = space.iconEmoji,
                        fontSize = 14.sp
                    )

                    // Nome e contador de abas
                    Text(
                        text = space.name,
                        color = if (isActive) {
                            if (isDarkMode) Color.White else spaceColor
                        } else {
                            if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF495057)
                        },
                        fontSize = 12.5.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Badge de contagem
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isActive) spaceColor.copy(alpha = 0.30f)
                                else if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = spaceTabCount.toString(),
                            color = if (isActive) {
                                if (isDarkMode) Color.White else spaceColor
                            } else {
                                if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF6C757D)
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Mais Opções (...)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "Opções do Espaço",
                            tint = if (isActive) spaceColor else if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF868E96),
                            modifier = Modifier.size(14.dp)
                        )

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
                                        Text("Editar Espaço")
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    spaceToEdit = space
                                }
                            )

                            if (spaces.size > 1) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Rounded.DeleteOutline, null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                            Text("Excluir Espaço", color = Color(0xFFFF5252))
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDeleteSpace(space.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botão "+ Novo Espaço"
        item {
            val addShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .clip(addShape)
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F3F5))
                    .border(
                        1.dp,
                        if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f),
                        addShape
                    )
                    .clickable { showCreateDialog = true }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Criar Novo Espaço",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF212529),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Novo Espaço",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF212529),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Diálogo de Criação de Espaço
    if (showCreateDialog) {
        SpaceEditDialog(
            space = null,
            isDarkMode = isDarkMode,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, emoji, colorArgb ->
                showCreateDialog = false
                onCreateSpace(name, emoji, colorArgb)
            }
        )
    }

    // Diálogo de Edição de Espaço
    spaceToEdit?.let { space ->
        SpaceEditDialog(
            space = space,
            isDarkMode = isDarkMode,
            onDismiss = { spaceToEdit = null },
            onConfirm = { name, emoji, colorArgb ->
                onUpdateSpace(space.id, name, emoji, colorArgb)
                spaceToEdit = null
            }
        )
    }
}

/**
 * Diálogo de criação ou edição de Espaço (Nome, Seletor de Emoji, Seletor de Cor Temática).
 */
@Composable
fun SpaceEditDialog(
    space: BrowserSpace? = null,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, colorArgb: Long) -> Unit
) {
    var name by remember { mutableStateOf(space?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(space?.iconEmoji ?: BrowserSpace.PRESET_EMOJIS.first()) }
    var selectedColor by remember { mutableLongStateOf(space?.colorArgb ?: BrowserSpace.PRESET_COLORS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (space == null) "Criar Novo Espaço" else "Editar Espaço",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nome do Espaço
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Espaço") },
                    placeholder = { Text("Ex: Trabalho, Estudos, Pessoal...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(selectedColor),
                        cursorColor = Color(selectedColor)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Seletor de Emoji
                Text(
                    text = "Ícone do Espaço",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BrowserSpace.PRESET_EMOJIS) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(selectedColor).copy(alpha = 0.25f)
                                    else if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F3F5)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(selectedColor) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                // Seletor de Cor Temática
                Text(
                    text = "Cor de Destaque",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BrowserSpace.PRESET_COLORS) { colorArgb ->
                        val isSelected = selectedColor == colorArgb
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorArgb))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorArgb },
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
                    val finalName = name.trim().ifBlank { "Espaço" }
                    onConfirm(finalName, selectedEmoji, selectedColor)
                }
            ) {
                Text(
                    text = if (space == null) "Criar" else "Salvar",
                    color = Color(selectedColor),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para transferir uma aba para outro Espaço.
 */
@Composable
fun MoveTabToSpaceDialog(
    tabTitle: String,
    spaces: List<BrowserSpace>,
    currentSpaceId: String,
    onSelectSpace: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mover Aba para Espaço", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Selecione o destino para: $tabTitle",
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                spaces.forEach { space ->
                    val isCurrent = space.id == currentSpaceId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color.Gray.copy(alpha = 0.15f) else Color(space.colorArgb).copy(alpha = 0.12f))
                            .clickable(enabled = !isCurrent) {
                                onSelectSpace(space.id)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(space.iconEmoji, fontSize = 16.sp)
                        Text(
                            text = space.name + if (isCurrent) " (Atual)" else "",
                            fontWeight = if (isCurrent) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (isCurrent) Color.Gray else Color(space.colorArgb),
                            modifier = Modifier.weight(1f)
                        )
                        if (!isCurrent) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = null,
                                tint = Color(space.colorArgb),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Bottom Sheet Modal de Alternância Rápida de Espaços (Space Switcher).
 */
@Composable
fun SpaceQuickSwitcherModal(
    spaces: List<BrowserSpace>,
    activeSpaceId: String,
    tabs: List<BrowserTab>,
    isDarkMode: Boolean,
    onSelectSpace: (String) -> Unit,
    onCreateSpace: ((String, String, Long) -> Unit)? = null,
    onCreateNewSpace: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showCreateDialog by remember { mutableStateOf(false) }
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    val bgColor = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xF81E1917), Color(0xFC120F0E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA)))
    }

    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF212529)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.55f) else Color(0xFF6C757D)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(bgColor)
            .border(
                1.dp,
                if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                panelShape
            )
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // Pull handle
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.20f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Espaços de Navegação",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar",
                    tint = mutedColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grade / Lista de Espaços
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            spaces.forEach { space ->
                val isActive = space.id == activeSpaceId
                val spaceTabs = tabs.filter { it.spaceId == space.id }
                val spaceColor = Color(space.colorArgb)
                val cardShape = RoundedCornerShape(16.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isActive) 6.dp else 1.dp, cardShape)
                        .clip(cardShape)
                        .background(
                            if (isActive) {
                                Brush.horizontalGradient(
                                    listOf(spaceColor.copy(alpha = 0.28f), spaceColor.copy(alpha = 0.10f))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0xFFF1F3F5),
                                        if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color(0xFFE9ECEF)
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isActive) 1.5.dp else 0.5.dp,
                            color = if (isActive) spaceColor else if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f),
                            shape = cardShape
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectSpace(space.id)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(spaceColor.copy(alpha = 0.20f))
                                .border(1.dp, spaceColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = space.iconEmoji, fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = space.name,
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
                            )
                            Text(
                                text = "${spaceTabs.size} abas ativas",
                                color = mutedColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(spaceColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ATIVO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Botão Novo Espaço
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F3F5))
                .border(
                    1.dp,
                    if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    RoundedCornerShape(14.dp)
                )
                .clickable {
                    if (onCreateSpace != null) {
                        showCreateDialog = true
                    } else {
                        onDismiss()
                        onCreateNewSpace()
                    }
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Criar Novo Espaço",
                color = textColor,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showCreateDialog && onCreateSpace != null) {
        SpaceEditDialog(
            space = null,
            isDarkMode = isDarkMode,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, emoji, color ->
                showCreateDialog = false
                onCreateSpace(name, emoji, color)
            }
        )
    }
}
