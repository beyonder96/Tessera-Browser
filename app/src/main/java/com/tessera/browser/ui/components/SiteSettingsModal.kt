package com.tessera.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tessera.browser.data.SiteSettings

@Composable
fun SiteSettingsModal(
    origin: String,
    currentUrl: String,
    settings: SiteSettings,
    isDarkMode: Boolean,
    accentColor: Color = Color(0xFF0288D1),
    onUpdatePermission: (update: (SiteSettings) -> SiteSettings) -> Unit,
    onClearSiteData: () -> Unit,
    onDismiss: () -> Unit
) {
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    val isSecure = currentUrl.startsWith("https://", ignoreCase = true)
    val sheetShape = RoundedCornerShape(28.dp)

    val bgBrush = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(Color(0xF5241E1B), Color(0xF8191512))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF7F8FA))
        )
    }

    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E4E8)
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E1E1E)
    val mutedColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF757575)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F3F5)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(
                    elevation = 20.dp,
                    shape = sheetShape,
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(sheetShape)
                .background(bgBrush)
                .border(1.dp, borderColor, sheetShape)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header: Security Icon + Domain + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSecure) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    else Color(0xFFF44336).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSecure) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = if (isSecure) "Seguro" else "Inseguro",
                                tint = if (isSecure) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = origin.ifBlank { "Configurações do Site" },
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isSecure) "Conexão segura (HTTPS)" else "Conexão não segura",
                                color = if (isSecure) Color(0xFF4CAF50) else Color(0xFFE57373),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = mutedColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Text(
                    text = "Permissões de acesso",
                    color = accentColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Permissions List Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // 1. Localização
                    SitePermissionRow(
                        icon = Icons.Rounded.LocationOn,
                        title = "Localização",
                        checked = settings.locationGranted,
                        onCheckedChange = { checked ->
                            onUpdatePermission { it.copy(locationGranted = checked, locationConfigured = true) }
                        },
                        textColor = textColor,
                        accentColor = accentColor,
                        isDarkMode = isDarkMode
                    )

                    // 2. Câmera
                    SitePermissionRow(
                        icon = Icons.Rounded.Videocam,
                        title = "Câmera",
                        checked = settings.cameraGranted,
                        onCheckedChange = { checked ->
                            onUpdatePermission { it.copy(cameraGranted = checked, cameraConfigured = true) }
                        },
                        textColor = textColor,
                        accentColor = accentColor,
                        isDarkMode = isDarkMode
                    )

                    // 3. Microfone
                    SitePermissionRow(
                        icon = Icons.Rounded.Mic,
                        title = "Microfone",
                        checked = settings.micGranted,
                        onCheckedChange = { checked ->
                            onUpdatePermission { it.copy(micGranted = checked, micConfigured = true) }
                        },
                        textColor = textColor,
                        accentColor = accentColor,
                        isDarkMode = isDarkMode
                    )

                    // 4. Cookies e Armazenamento
                    SitePermissionRow(
                        icon = Icons.Rounded.Cookie,
                        title = "Cookies e rastreadores",
                        checked = settings.cookiesAllowed,
                        onCheckedChange = { checked ->
                            onUpdatePermission { it.copy(cookiesAllowed = checked) }
                        },
                        textColor = textColor,
                        accentColor = accentColor,
                        isDarkMode = isDarkMode
                    )

                    // 5. JavaScript
                    SitePermissionRow(
                        icon = Icons.Rounded.Code,
                        title = "Execução de JavaScript",
                        checked = settings.javascriptAllowed,
                        onCheckedChange = { checked ->
                            onUpdatePermission { it.copy(javascriptAllowed = checked) }
                        },
                        textColor = textColor,
                        accentColor = accentColor,
                        isDarkMode = isDarkMode
                    )
                }

                // Storage & Clear Section
                Text(
                    text = "Dados e Armazenamento",
                    color = accentColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE53935).copy(alpha = if (isDarkMode) 0.12f else 0.08f))
                        .border(
                            1.dp,
                            Color(0xFFE53935).copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { showConfirmClearDialog = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Limpar dados deste site",
                                color = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Exclui cookies, cache local e permissões de $origin",
                                color = mutedColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("Limpar dados do site?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Isso desconectará você de $origin e excluirá todos os dados salvos localmente por este site. O histórico de navegação geral não será afetado."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmClearDialog = false
                        onClearSiteData()
                        onDismiss()
                    }
                ) {
                    Text("Limpar", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SitePermissionRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) accentColor else if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF9E9E9E),
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = title,
                color = textColor,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.White,
                uncheckedTrackColor = if (isDarkMode) Color(0xFF3E3935) else Color(0xFFD6D9DE)
            )
        )
    }
}
