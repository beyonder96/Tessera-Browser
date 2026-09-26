package com.tessera.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.data.DownloadFileType
import com.tessera.browser.data.DownloadItem
import com.tessera.browser.data.DownloadStatus
import com.tessera.browser.data.SavedPageItem
import com.tessera.browser.data.SpeedDialItem
import com.tessera.browser.viewmodel.HistoryEntry
import androidx.compose.material.icons.rounded.OfflinePin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryBookmarksModal(
    bookmarks: List<SpeedDialItem>,
    history: List<HistoryEntry>,
    downloads: List<DownloadItem> = emptyList(),
    savedPages: List<SavedPageItem> = emptyList(),
    initialTab: Int = 0,
    onTabSelected: ((Int) -> Unit)? = null,
    onSelectUrl: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenDownload: (DownloadItem) -> Unit = {},
    onShareDownload: (DownloadItem) -> Unit = {},
    onRemoveDownload: (Long) -> Unit = {},
    onClearDownloads: () -> Unit = {},
    onOpenSavedPage: (SavedPageItem) -> Unit = {},
    onDeleteSavedPage: (SavedPageItem) -> Unit = {},
    onDismiss: () -> Unit,
    accentColor: Color = Color(0xFF64B5F6),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .clip(panelShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF8201A17), Color(0xFC14100E))
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
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header & Tab switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Favoritos
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 0) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (selectedTab == 0) accentColor else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedTab = 0
                            onTabSelected?.invoke(0)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Favoritos",
                        color = if (selectedTab == 0) accentColor else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Tab Histórico
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 1) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (selectedTab == 1) accentColor else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedTab = 1
                            onTabSelected?.invoke(1)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Histórico",
                        color = if (selectedTab == 1) accentColor else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Tab Downloads
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 2) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (selectedTab == 2) accentColor else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedTab = 2
                            onTabSelected?.invoke(2)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Downloads",
                        color = if (selectedTab == 2) accentColor else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Tab Salvos (Offline)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 3) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (selectedTab == 3) accentColor else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedTab = 3
                            onTabSelected?.invoke(3)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Salvos",
                        color = if (selectedTab == 3) accentColor else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // BOOKMARKS LIST
            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum favorito salvo ainda.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookmarks, key = { it.id }) { item ->
                        val cardShape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickable { onSelectUrl(item.url) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                ShortcutIcon(item = item, size = 20.dp)
                                Column {
                                    Text(
                                        text = item.title,
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.url,
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onRemoveBookmark(item.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Remover",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // HISTORY LIST
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Histórico de navegação limpo.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Limpar Histórico",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onClearHistory)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history, key = { it.id }) { item ->
                        val cardShape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onSelectUrl(item.url) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title.ifBlank { item.url },
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.url,
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 2) {
            // DOWNLOADS LIST (selectedTab == 2)
            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = "Nenhum download recente",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Arquivos baixados da web aparecerão aqui.",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${downloads.size} ${if (downloads.size == 1) "item" else "itens"}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Limpar Downloads",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onClearDownloads)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(downloads, key = { it.id }) { item ->
                        val cardShape = RoundedCornerShape(16.dp)
                        val iconInfo = when (item.fileType) {
                            DownloadFileType.APK -> Pair(Icons.Rounded.Android, Color(0xFF66BB6A))
                            DownloadFileType.PDF -> Pair(Icons.Rounded.PictureAsPdf, Color(0xFFEF5350))
                            DownloadFileType.IMAGE -> Pair(Icons.Rounded.Image, Color(0xFF42A5F5))
                            DownloadFileType.VIDEO -> Pair(Icons.Rounded.VideoFile, Color(0xFFAB47BC))
                            DownloadFileType.AUDIO -> Pair(Icons.Rounded.AudioFile, Color(0xFFFFA726))
                            DownloadFileType.ARCHIVE -> Pair(Icons.Rounded.FolderZip, Color(0xFFFFCA28))
                            DownloadFileType.DOCUMENT -> Pair(Icons.Rounded.Description, Color(0xFF26A69A))
                            DownloadFileType.GENERIC -> Pair(Icons.AutoMirrored.Rounded.InsertDriveFile, Color.White.copy(alpha = 0.7f))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onOpenDownload(item) }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(iconInfo.second.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconInfo.first,
                                    contentDescription = null,
                                    tint = iconInfo.second,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.fileName,
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.formattedSize,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.5.sp
                                    )
                                    Text(
                                        text = "•",
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = item.formattedDate,
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 11.sp
                                    )
                                    if (item.status == DownloadStatus.RUNNING || item.status == DownloadStatus.PENDING) {
                                        Text(
                                            text = "Baixando...",
                                            color = accentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else if (item.status == DownloadStatus.FAILED) {
                                        Text(
                                            text = "Falha",
                                            color = Color(0xFFFF6B6B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Share button
                            IconButton(
                                onClick = { onShareDownload(item) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Compartilhar",
                                    tint = Color.White.copy(alpha = 0.65f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // Remove button
                            IconButton(
                                onClick = { onRemoveDownload(item.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Remover",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 3) {
            // SAVED OFFLINE PAGES LIST
            if (savedPages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OfflinePin,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Nenhuma página salva offline",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "No menu de Configurações, toque em \"Salvar para ler offline\" para acessar artigos mesmo sem sinal de internet.",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 12.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedPages, key = { it.id }) { item ->
                        val cardShape = RoundedCornerShape(16.dp)
                        val formattedSize = remember(item.fileSize) {
                            if (item.fileSize > 1024 * 1024) {
                                String.format(Locale.getDefault(), "%.1f MB", item.fileSize / (1024.0 * 1024.0))
                            } else if (item.fileSize > 1024) {
                                "${item.fileSize / 1024} KB"
                            } else {
                                "${item.fileSize} B"
                            }
                        }
                        val formattedDate = remember(item.timestamp) {
                            SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), cardShape)
                                .clickable {
                                    onOpenSavedPage(item)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.OfflinePin,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title.ifBlank { "Página Salva" },
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formattedSize,
                                            color = accentColor.copy(alpha = 0.85f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "•",
                                            color = Color.White.copy(alpha = 0.3f),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = formattedDate,
                                            color = Color.White.copy(alpha = 0.45f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Delete button
                            IconButton(
                                onClick = { onDeleteSavedPage(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Excluir página offline",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
