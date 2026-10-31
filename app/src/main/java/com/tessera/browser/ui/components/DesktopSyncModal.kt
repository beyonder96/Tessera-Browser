package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.sync.RemoteDesktopTab
import com.tessera.browser.ui.theme.ApplePalette
import com.tessera.browser.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopSyncModal(
    viewModel: BrowserViewModel,
    isDarkMode: Boolean = false,
    onDismiss: () -> Unit
) {
    val syncManager = viewModel.desktopSyncManager
    val desktopDevice by syncManager?.desktopDevice?.collectAsState() ?: remember { mutableStateOf(null) }
    val isDiscovering by syncManager?.isDiscovering?.collectAsState() ?: remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isSendingTab by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isManualIpExpanded by remember { mutableStateOf(false) }
    var manualIpInput by remember { mutableStateOf("") }
    var isConnectingManual by remember { mutableStateOf(false) }

    var desktopTabs by remember { mutableStateOf<List<RemoteDesktopTab>>(emptyList()) }
    var isLoadingTabs by remember { mutableStateOf(false) }

    // Colors matching Tessera ApplePalette
    val sheetBg = if (isDarkMode) ApplePalette.DarkSurface else ApplePalette.LightSurface
    val textPrimary = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val textSecondary = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val cardBg = if (isDarkMode) Color(0xFF1E1E22) else Color(0xFFECE7DE)
    val cardBorder = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline

    // Load desktop tabs on launch
    LaunchedEffect(desktopDevice?.isOnline) {
        if (desktopDevice?.isOnline == true && syncManager != null) {
            isLoadingTabs = true
            desktopTabs = syncManager.fetchDesktopTabs()
            isLoadingTabs = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(textSecondary.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ApplePalette.Blue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Computer, contentDescription = null, tint = ApplePalette.Blue)
                    }
                    Column {
                        Text(
                            text = "Tessera Desktop Sync",
                            color = textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sincronização direta na rede Wi-Fi local",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Card
            val isOnline = desktopDevice?.isOnline == true
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF34C759) else Color(0xFFFF9F0A))
                            )
                            Column {
                                Text(
                                    text = desktopDevice?.deviceName ?: "Procurando PC na rede Wi-Fi...",
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isOnline) {
                                        "Conectado • IP: ${desktopDevice?.ipAddress}:${desktopDevice?.port}"
                                    } else if (desktopDevice != null) {
                                        "PC não respondeu no IP ${desktopDevice?.ipAddress}:${desktopDevice?.port}"
                                    } else {
                                        "Abra o Tessera Browser no computador"
                                    },
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    syncManager?.sendDiscoveryQuery()
                                    if (syncManager != null) {
                                        isLoadingTabs = true
                                        desktopTabs = syncManager.fetchDesktopTabs()
                                        isLoadingTabs = false
                                    }
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Redescobrir",
                                tint = textSecondary
                            )
                        }
                    }

                    // Opção de inserir IP manualmente
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isManualIpExpanded = !isManualIpExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isManualIpExpanded) "Ocultar conexão manual por IP" else "Conectar por IP / QR da Ponte",
                            fontSize = 11.5.sp,
                            color = ApplePalette.Blue,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (isManualIpExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ApplePalette.Blue,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    AnimatedVisibility(visible = isManualIpExpanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualIpInput,
                                    onValueChange = { manualIpInput = it },
                                    placeholder = {
                                        Text("192.168.1.15 ou URL da Ponte", fontSize = 12.sp, color = textSecondary)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ApplePalette.Blue,
                                        unfocusedBorderColor = cardBorder,
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary
                                    )
                                )
                                Button(
                                    onClick = {
                                        if (manualIpInput.isNotBlank()) {
                                            isConnectingManual = true
                                            focusManager.clearFocus()
                                            scope.launch {
                                                val res = syncManager?.connectByIpOrBridgeUrl(manualIpInput)
                                                isConnectingManual = false
                                                if (res?.isSuccess == true) {
                                                    statusMessage = "Conectado ao PC com sucesso!"
                                                    desktopTabs = syncManager.fetchDesktopTabs()
                                                } else {
                                                    statusMessage = "Não foi possível conectar a esse IP"
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ApplePalette.Blue),
                                    enabled = !isConnectingManual && manualIpInput.isNotBlank()
                                ) {
                                    Text(if (isConnectingManual) "..." else "Ligar", fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Enviar Aba Atual
            Button(
                onClick = {
                    isSendingTab = true
                    viewModel.sendActiveTabToDesktop { ok ->
                        isSendingTab = false
                        statusMessage = if (ok) "Aba enviada para o computador!" else "Não foi possível enviar para o PC"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApplePalette.Blue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                        if (isSendingTab) "Enviando para o PC..." else "Enviar Aba Atual para o Computador",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Sincronizar Tudo (Bidirecional)
            OutlinedButton(
                onClick = {
                    isSyncing = true
                    viewModel.syncAllWithDesktop { ok, err ->
                        isSyncing = false
                        statusMessage = if (ok) {
                            scope.launch {
                                desktopTabs = syncManager?.fetchDesktopTabs() ?: emptyList()
                            }
                            "Favoritos, histórico e espaços sincronizados!"
                        } else {
                            err ?: "Erro ao sincronizar com o PC"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = ApplePalette.Blue)
                    Text(
                        if (isSyncing) "Sincronizando..." else "Sincronizar Favoritos, Histórico & Spaces",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp
                    )
                }
            }

            // Status feedback message
            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = msg,
                    color = if (msg.contains("não", ignoreCase = true) || msg.contains("erro", ignoreCase = true)) Color(0xFFFF453A) else Color(0xFF34C759),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seção: Abas Abertas no Windows (Continuar Lendo)
            Text(
                text = "ABAS ABERTAS NO WINDOWS (CONTINUAR LENDO)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            if (isLoadingTabs) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ApplePalette.Blue)
                }
            } else if (desktopTabs.isEmpty()) {
                Text(
                    text = if (isOnline) "Nenhuma outra aba aberta no Windows no momento." else "Conecte com o PC para ver as abas abertas no computador.",
                    fontSize = 12.sp,
                    color = textSecondary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(desktopTabs, key = { it.id }) { tab ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (tab.url.isNotBlank()) {
                                        viewModel.openUrl(tab.url)
                                        onDismiss()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Tab,
                                    contentDescription = null,
                                    tint = ApplePalette.Blue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tab.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tab.url,
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Abrir aba",
                                tint = textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
