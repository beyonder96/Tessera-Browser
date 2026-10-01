package com.tessera.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.tessera.browser.data.ContextMenuTarget

/**
 * Menu de Contexto Completo ao toque longo (Long-Press Context Menu).
 * Design Aero-Dark / Glassmorphism de alta fidelidade com suporte a:
 * - Ações de Links (Prévia Arc Peek, Nova Aba, Aba em 2º Plano, Aba Anônima, Copiar, Compartilhar, Baixar)
 * - Ações de Imagens (Salvar Imagem, Pesquisar no Google Lens, Abrir Imagem, Copiar Link, Compartilhar)
 * - Telefone, E-mail e Alvos Especiais.
 */
@Composable
fun ContextMenuModal(
    target: ContextMenuTarget,
    isDarkMode: Boolean = false,
    accentColor: Color = Color(0xFF0288D1),
    onOpenInNewTab: (String) -> Unit,
    onOpenInBackground: (String) -> Unit,
    onOpenInIncognito: (String) -> Unit,
    onPeekPreview: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onShareLink: (String) -> Unit,
    onDownloadLink: (String) -> Unit,
    onSaveImage: (String) -> Unit,
    onCopyImageUrl: (String) -> Unit,
    onShareImage: (String) -> Unit,
    onOpenImageInNewTab: (String) -> Unit,
    onSearchImageOnWeb: (String) -> Unit,
    onDialPhone: ((String) -> Unit)? = null,
    onSendEmail: ((String) -> Unit)? = null,
    onOpenInExternalApp: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val panelBg = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xF0201B17), Color(0xF514100E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xF8FFFFFF), Color(0xF8F0F2F5)))
    }
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val cardBg = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val textColor = if (isDarkMode) Color(0xFFEEEEEE) else Color(0xFF1E1E1E)
    val subColor = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF757575)
    val sectionHeaderColor = if (isDarkMode) accentColor else Color(0xFF0277BD)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .navigationBarsPadding(),
        shape = panelShape,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(panelBg)
                .border(1.dp, borderColor, panelShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Drag handle superior
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(subColor.copy(alpha = 0.35f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cabeçalho com informações do elemento inspecionado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val headerIcon = when {
                            target.isImage && !target.isLink -> Icons.Rounded.Image
                            target.isPhone -> Icons.Rounded.Phone
                            target.isEmail -> Icons.Rounded.Email
                            else -> Icons.Rounded.Language
                        }
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val titleText = when {
                            !target.title.isNullOrBlank() -> target.title
                            target.isPhone -> "Telefone: ${target.cleanPhoneNumber}"
                            target.isEmail -> "E-mail: ${target.cleanEmailAddress}"
                            target.isBothLinkAndImage -> "Imagem com Link"
                            target.isImage -> "Imagem da Web"
                            else -> target.displayHost.ifBlank { "Link da Página" }
                        }
                        Text(
                            text = titleText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subText = target.linkUrl ?: target.imageUrl ?: ""
                        if (subText.isNotBlank()) {
                            Text(
                                text = subText,
                                fontSize = 11.5.sp,
                                color = subColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = subColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Preview visual da imagem (se houver)
            if (target.isImage && !target.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color(0xFF161616) else Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = target.imageUrl,
                        contentDescription = target.title ?: "Imagem da web",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = borderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Lista rolável de ações
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // SEÇÃO 1: AÇÕES DE LINK
                if (target.isLink && !target.linkUrl.isNullOrBlank()) {
                    val link = target.linkUrl

                    if (target.isBothLinkAndImage) {
                        Text(
                            text = "AÇÕES DO LINK",
                            color = sectionHeaderColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // 1. Pré-visualizar (Arc Peek)
                    ContextMenuItem(
                        icon = Icons.Rounded.Visibility,
                        title = "Pré-visualizar Página",
                        subtitle = "Abrir prévia rápida em janela flutuante",
                        iconTint = Color(0xFF00E5FF),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onPeekPreview(link)
                        }
                    )

                    // 2. Abrir em Nova Aba
                    ContextMenuItem(
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        title = "Abrir em Nova Aba",
                        subtitle = "Carregar página em primeiro plano",
                        iconTint = accentColor,
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onOpenInNewTab(link)
                        }
                    )

                    // 3. Abrir em Segundo Plano
                    ContextMenuItem(
                        icon = Icons.Rounded.Bolt,
                        title = "Abrir em Segundo Plano",
                        subtitle = "Criar aba silenciosa sem sair daqui",
                        iconTint = Color(0xFFFFA726),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onOpenInBackground(link)
                        }
                    )

                    // 4. Abrir em Aba Anônima
                    ContextMenuItem(
                        icon = Icons.Rounded.Security,
                        title = "Abrir em Aba Anônima",
                        subtitle = "Navegar sem salvar histórico nem cookies",
                        iconTint = Color(0xFFAB47BC),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onOpenInIncognito(link)
                        }
                    )

                    // 4.1 Abrir no Aplicativo Instalado (Instagram, YouTube, etc.)
                    if (onOpenInExternalApp != null) {
                        ContextMenuItem(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            title = "Abrir no Aplicativo",
                            subtitle = "Abrir no aplicativo instalado no aparelho",
                            iconTint = Color(0xFFFFB74D),
                            textColor = textColor,
                            subColor = subColor,
                            cardBg = cardBg,
                            onClick = {
                                onDismiss()
                                onOpenInExternalApp(link)
                            }
                        )
                    }

                    // 5. Copiar Endereço do Link
                    ContextMenuItem(
                        icon = Icons.Rounded.ContentCopy,
                        title = "Copiar Endereço do Link",
                        iconTint = Color(0xFF66BB6A),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onCopyLink(link)
                        }
                    )

                    // 6. Compartilhar Link
                    ContextMenuItem(
                        icon = Icons.Rounded.Share,
                        title = "Compartilhar Link",
                        iconTint = Color(0xFF42A5F5),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onShareLink(link)
                        }
                    )

                    // 7. Fazer Download do Link
                    ContextMenuItem(
                        icon = Icons.Rounded.Download,
                        title = "Fazer Download do Link",
                        iconTint = Color(0xFF26A69A),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onDownloadLink(link)
                        }
                    )
                }

                // SEÇÃO 2: AÇÕES DE IMAGEM
                if (target.isImage && !target.imageUrl.isNullOrBlank()) {
                    val imgUrl = target.imageUrl

                    if (target.isBothLinkAndImage) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "AÇÕES DA IMAGEM",
                            color = sectionHeaderColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // 1. Salvar Imagem
                    ContextMenuItem(
                        icon = Icons.Rounded.Download,
                        title = "Salvar Imagem",
                        subtitle = "Baixar para o armazenamento local",
                        iconTint = Color(0xFF00E676),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onSaveImage(imgUrl)
                        }
                    )

                    // 2. Pesquisar Imagem na Web (Google Lens)
                    ContextMenuItem(
                        icon = Icons.Rounded.Search,
                        title = "Pesquisar Imagem na Web",
                        subtitle = "Buscar itens visuais similares no Google",
                        iconTint = Color(0xFF42A5F5),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onSearchImageOnWeb(imgUrl)
                        }
                    )

                    // 3. Abrir Imagem em Nova Aba
                    ContextMenuItem(
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        title = "Abrir Imagem em Nova Aba",
                        iconTint = accentColor,
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onOpenImageInNewTab(imgUrl)
                        }
                    )

                    // 4. Copiar Endereço da Imagem
                    ContextMenuItem(
                        icon = Icons.Rounded.ContentCopy,
                        title = "Copiar Endereço da Imagem",
                        iconTint = Color(0xFFFFA726),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onCopyImageUrl(imgUrl)
                        }
                    )

                    // 5. Compartilhar Imagem
                    ContextMenuItem(
                        icon = Icons.Rounded.Share,
                        title = "Compartilhar Imagem",
                        iconTint = Color(0xFFAB47BC),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onShareImage(imgUrl)
                        }
                    )
                }

                // SEÇÃO 3: TELEFONE / EMAIL (SE HOUVER)
                if (target.isPhone && !target.cleanPhoneNumber.isBlank()) {
                    ContextMenuItem(
                        icon = Icons.Rounded.Phone,
                        title = "Ligar para ${target.cleanPhoneNumber}",
                        iconTint = Color(0xFF00E676),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onDialPhone?.invoke(target.cleanPhoneNumber)
                        }
                    )
                }

                if (target.isEmail && !target.cleanEmailAddress.isBlank()) {
                    ContextMenuItem(
                        icon = Icons.Rounded.Email,
                        title = "Enviar e-mail para ${target.cleanEmailAddress}",
                        iconTint = Color(0xFF42A5F5),
                        textColor = textColor,
                        subColor = subColor,
                        cardBg = cardBg,
                        onClick = {
                            onDismiss()
                            onSendEmail?.invoke(target.cleanEmailAddress)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color,
    textColor: Color,
    subColor: Color,
    cardBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
