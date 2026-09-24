package com.tessera.browser.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tessera.browser.data.BrowserSpace
import com.tessera.browser.data.NoteItem
import com.tessera.browser.data.NoteType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotebookModal(
    notes: List<NoteItem>,
    spaces: List<BrowserSpace>,
    activeSpaceId: String,
    searchQuery: String,
    selectedTag: String?,
    isDarkMode: Boolean,
    accentColor: Color = Color(0xFF00E5FF),
    summarizingNoteId: String? = null,
    onSearchQueryChanged: (String) -> Unit,
    onTagSelected: (String?) -> Unit,
    onOpenSourceUrl: (String) -> Unit,
    onTogglePinNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSaveNote: (NoteItem) -> Unit,
    onSummarizeNote: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    var noteToEdit by remember { mutableStateOf<NoteItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var spaceFilter by remember { mutableStateOf<String?>(null) } // null = todos

    // Filtragem das anotações
    val filteredNotes = remember(notes, searchQuery, selectedTag, spaceFilter) {
        notes.filter { note ->
            val matchesQuery = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesTag = selectedTag == null || when (selectedTag) {
                "📌 Fixadas" -> note.isPinned
                "📝 Trechos" -> note.type == NoteType.TEXT_CLIP
                "💻 Códigos" -> note.type == NoteType.CODE_SNIPPET
                "✨ Resumos IA" -> note.type == NoteType.AI_SUMMARY || note.aiSummary != null
                "📰 Artigos" -> note.type == NoteType.FULL_ARTICLE
                "💡 Anotações" -> note.type == NoteType.QUICK_NOTE
                else -> note.tags.contains(selectedTag)
            }

            val matchesSpace = spaceFilter == null || note.spaceId == spaceFilter

            matchesQuery && matchesTag && matchesSpace
        }.sortedWith(
            compareByDescending<NoteItem> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(panelShape)
            .background(
                Brush.verticalGradient(
                    if (isDarkMode) {
                        listOf(Color(0xF81A1614), Color(0xFC120F0D))
                    } else {
                        listOf(Color(0xFFFFFFFF), Color(0xFFF7F8FA))
                    }
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
                ),
                shape = panelShape
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // HEADER: Título, contador, botão Nova Nota e Fechar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Caderno de Notas",
                            color = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "${filteredNotes.size}",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Web Clipper & Síntese Inteligente",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray,
                        fontSize = 11.5.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botão + Nova Anotação
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor)
                        .clickable { isCreatingNew = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Nova",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BARRA DE PESQUISA
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            placeholder = {
                Text(
                    text = "Buscar em anotações, trechos, tags...",
                    fontSize = 12.5.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Limpar",
                            tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                focusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFF0F2F5),
                unfocusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color(0xFFF5F6F8),
                focusedTextColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E),
                unfocusedTextColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // CARROSSEL DE TAGS & FILTROS
        val tagFilters = listOf("Todas", "📌 Fixadas", "📝 Trechos", "💻 Códigos", "✨ Resumos IA", "📰 Artigos", "💡 Anotações")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filtro por Espaço
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (spaceFilter != null) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (spaceFilter != null) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        // Alterna entre todos os espaços e o espaço ativo atual
                        spaceFilter = if (spaceFilter == null) activeSpaceId else null
                    }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                val currentSpaceObj = spaces.find { it.id == activeSpaceId }
                val emoji = currentSpaceObj?.iconEmoji ?: "🌐"
                val name = currentSpaceObj?.name ?: "Espaço"
                Text(
                    text = if (spaceFilter == null) "🌐 Todos Espaços" else "$emoji $name",
                    color = if (spaceFilter != null) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF424242),
                    fontSize = 11.5.sp,
                    fontWeight = if (spaceFilter != null) FontWeight.Bold else FontWeight.Normal
                )
            }

            tagFilters.forEach { tag ->
                val isSelected = (selectedTag == null && tag == "Todas") || selectedTag == tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onTagSelected(if (tag == "Todas") null else tag)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (isSelected) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.75f) else Color(0xFF424242),
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LISTA DE NOTAS
        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0xFFF0F0F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = null,
                            tint = if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedTag != null) "Nenhum resultado encontrado" else "Seu caderno está vazio",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF212121),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedTag != null) {
                            "Tente alterar os termos da busca ou os filtros aplicados."
                        } else {
                            "Use 'Clipar no Caderno' nas ferramentas da página ou selecione texto em qualquer site para salvar trechos instantaneamente."
                        },
                        color = if (isDarkMode) Color.White.copy(alpha = 0.55f) else Color.Gray,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { isCreatingNew = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ Criar Anotação Manual",
                            color = accentColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        spaces = spaces,
                        isDarkMode = isDarkMode,
                        accentColor = accentColor,
                        isSummarizing = summarizingNoteId == note.id,
                        onOpenSource = { onOpenSourceUrl(note.sourceUrl) },
                        onTogglePin = { onTogglePinNote(note.id) },
                        onEdit = { noteToEdit = note },
                        onCopy = {
                            val copyText = buildString {
                                append(note.title)
                                append("\n\n")
                                append(note.content)
                                if (!note.aiSummary.isNullOrBlank()) {
                                    append("\n\n-- Síntese com IA --\n")
                                    append(note.aiSummary)
                                }
                                if (note.sourceUrl.isNotBlank()) {
                                    append("\n\nOrigem: ${note.sourceUrl}")
                                }
                            }
                            clipboardManager.setText(AnnotatedString(copyText))
                            Toast.makeText(context, "Anotação copiada!", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            val shareText = buildString {
                                append("📝 ${note.title}\n\n")
                                append(note.content)
                                if (!note.aiSummary.isNullOrBlank()) {
                                    append("\n\n✨ Síntese IA:\n${note.aiSummary}")
                                }
                                if (note.sourceUrl.isNotBlank()) {
                                    append("\n\n🔗 ${note.sourceUrl}")
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Anotação"))
                        },
                        onDelete = { onDeleteNote(note.id) },
                        onSummarize = { onSummarizeNote(note.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // DIÁLOGO DE EDIÇÃO / CRIAÇÃO DE NOTA
    if (isCreatingNew || noteToEdit != null) {
        val initial = noteToEdit ?: NoteItem(
            title = "",
            content = "",
            spaceId = activeSpaceId,
            tags = listOf("Geral")
        )

        NoteEditorDialog(
            initialNote = initial,
            isNew = isCreatingNew,
            spaces = spaces,
            isDarkMode = isDarkMode,
            accentColor = accentColor,
            onDismiss = {
                isCreatingNew = false
                noteToEdit = null
            },
            onSave = { updated ->
                onSaveNote(updated)
                isCreatingNew = false
                noteToEdit = null
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: NoteItem,
    spaces: List<BrowserSpace>,
    isDarkMode: Boolean,
    accentColor: Color,
    isSummarizing: Boolean,
    onOpenSource: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onSummarize: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    var isExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(note.updatedAt) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("pt", "BR"))
        sdf.format(Date(note.updatedAt))
    }

    val spaceObj = remember(note.spaceId, spaces) {
        spaces.find { it.id == note.spaceId }
    }

    val cardBorderBrush = if (note.isPinned) {
        Brush.linearGradient(
            listOf(
                Color(0xFFFFB300).copy(alpha = 0.6f),
                Color(0xFFFF8F00).copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.02f)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                if (isDarkMode) {
                    if (note.isPinned) Color(0xFF221C16) else Color(0xFF1B1614)
                } else {
                    if (note.isPinned) Color(0xFFFFFDE7) else Color(0xFFFFFFFF)
                }
            )
            .border(if (note.isPinned) 1.5.dp else 1.dp, cardBorderBrush, cardShape)
            .padding(14.dp)
    ) {
        // TOP ROW: Type Badge, Space Badge, Date, and Pin Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (note.type) {
                                NoteType.CODE_SNIPPET -> Color(0xFF00E5FF).copy(alpha = 0.15f)
                                NoteType.AI_SUMMARY -> Color(0xFFAB47BC).copy(alpha = 0.18f)
                                NoteType.FULL_ARTICLE -> Color(0xFF42A5F5).copy(alpha = 0.15f)
                                NoteType.QUICK_NOTE -> Color(0xFFFFB300).copy(alpha = 0.15f)
                                else -> Color(0xFF66BB6A).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${note.type.iconEmoji} ${note.type.displayName}",
                        color = when (note.type) {
                            NoteType.CODE_SNIPPET -> Color(0xFF00E5FF)
                            NoteType.AI_SUMMARY -> Color(0xFFCE93D8)
                            NoteType.FULL_ARTICLE -> Color(0xFF90CAF9)
                            NoteType.QUICK_NOTE -> Color(0xFFFFD54F)
                            else -> Color(0xFFA5D6A7)
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Space Badge (se vinculado a um espaço)
                if (spaceObj != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(spaceObj.colorArgb).copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${spaceObj.iconEmoji} ${spaceObj.name}",
                            color = Color(spaceObj.colorArgb),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = formattedDate,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color.Gray,
                    fontSize = 10.5.sp
                )
            }

            // Pin Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = if (note.isPinned) "Desafixar" else "Fixar",
                    tint = if (note.isPinned) Color(0xFFFFB300) else if (isDarkMode) Color.White.copy(alpha = 0.35f) else Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TITLE
        Text(
            text = note.title,
            color = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color(0xFF1E1E1E),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // SOURCE URL PILL
        if (note.sourceUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F3F5))
                    .clickable(onClick = onOpenSource)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val domain = try {
                    android.net.Uri.parse(note.sourceUrl).host?.replace("www.", "") ?: note.sourceUrl
                } catch (e: Exception) {
                    note.sourceUrl
                }
                Text(
                    text = "🔗 $domain",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CONTENT (Text or Code Block)
        if (note.type == NoteType.CODE_SNIPPET) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF121214))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = if (isExpanded || note.content.length <= 250) note.content else note.content.take(250) + "...",
                    color = Color(0xFF80D8FF),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        } else {
            Text(
                text = if (isExpanded || note.content.length <= 220) note.content else note.content.take(220) + "...",
                color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF333333),
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )
        }

        if (note.content.length > 220) {
            Text(
                text = if (isExpanded) "Ver menos ▲" else "Ver mais ▼",
                color = accentColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 4.dp)
            )
        }

        // AI SUMMARY BOX (Se gerado)
        if (!note.aiSummary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.08f),
                                Color(0xFF7C4DFF).copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color(0xFF7C4DFF).copy(alpha = 0.35f))
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Síntese Inteligente (IA)",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = note.aiSummary,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF1E1E1E),
                        fontSize = 12.sp,
                        lineHeight = 16.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ACTION BUTTONS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Resumir com IA (se ainda não tem resumo)
            if (note.aiSummary.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable(enabled = !isSummarizing, onClick = onSummarize)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSummarizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color(0xFF00E5FF),
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (isSummarizing) "Resumindo..." else "Resumir com IA",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Quick actions: Copiar, Editar, Compartilhar, Excluir
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copiar",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Editar",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Compartilhar",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Excluir",
                        tint = Color(0xFFEF5350).copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    initialNote: NoteItem,
    isNew: Boolean,
    spaces: List<BrowserSpace>,
    isDarkMode: Boolean,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit
) {
    var title by remember { mutableStateOf(initialNote.title) }
    var content by remember { mutableStateOf(initialNote.content) }
    var selectedType by remember { mutableStateOf(initialNote.type) }
    var selectedSpaceId by remember { mutableStateOf(initialNote.spaceId) }

    Dialog(onDismissRequest = onDismiss) {
        val dialogShape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dialogShape)
                .background(if (isDarkMode) Color(0xFF1E1A17) else Color.White)
                .border(
                    1.dp,
                    if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                    dialogShape
                )
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isNew) "Nova Anotação" else "Editar Anotação",
                    color = if (isDarkMode) Color.White else Color(0xFF1E1E1E),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da anotação") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
                    focusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color(0xFFF7F8FA),
                    unfocusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.02f) else Color(0xFFF9FAFC)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Seletor de Tipo de Anotação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NoteType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = type }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${type.iconEmoji} ${type.displayName}",
                            color = if (isSelected) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seletor de Espaço
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Espaço:",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )

                spaces.forEach { space ->
                    val isSelected = selectedSpaceId == space.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(space.colorArgb).copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) Color(space.colorArgb) else if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedSpaceId = space.id }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${space.iconEmoji} ${space.name}",
                            color = if (isSelected) Color(space.colorArgb) else if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Campo Conteúdo
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Conteúdo da anotação") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
                    focusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color(0xFFF7F8FA),
                    unfocusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.02f) else Color(0xFFF9FAFC)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor)
                        .clickable(enabled = title.isNotBlank() || content.isNotBlank()) {
                            val saved = initialNote.copy(
                                title = title.ifBlank { "Anotação sem título" },
                                content = content,
                                type = selectedType,
                                spaceId = selectedSpaceId,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(saved)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Salvar Anotação",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
