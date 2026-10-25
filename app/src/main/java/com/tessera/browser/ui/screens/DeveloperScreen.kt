package com.tessera.browser.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.browser.R
import com.tessera.browser.ui.theme.ApplePalette
import kotlin.math.roundToInt

@Composable
fun DeveloperScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bg = if (isDarkMode) ApplePalette.DarkBackground else ApplePalette.LightBackground
    val cardBg = if (isDarkMode) ApplePalette.DarkElevated else ApplePalette.LightSurface
    val cardBorder = if (isDarkMode) ApplePalette.DarkHairline else ApplePalette.LightHairline
    val textMain = if (isDarkMode) ApplePalette.DarkLabelPrimary else ApplePalette.LightLabelPrimary
    val textSub = if (isDarkMode) ApplePalette.DarkLabelSecondary else ApplePalette.LightLabelSecondary
    val accentColor = if (isDarkMode) ApplePalette.BlueDark else ApplePalette.Blue

    BackHandler(enabled = true) {
        onBack()
    }

    // Animação Notion Infinite Float
    val infiniteTransition = rememberInfiniteTransition(label = "notion_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_float"
    )
    val avatarRotate by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_rotate"
    )
    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_pulse"
    )

    val pInfo = remember(context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Throwable) {
            null
        }
    }
    val versionName = pInfo?.versionName ?: "3.2.0"
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pInfo?.longVersionCode ?: 36L
    } else {
        @Suppress("DEPRECATION")
        (pInfo?.versionCode?.toLong() ?: 36L)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Barra Superior de Navegação
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF1E1E24) else Color(0xFFECE7DA))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textMain,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Sobre o Desenvolvedor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cartão de Destaque com Animação Estilo Notion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(26.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Contêiner do Avatar Flutuante
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .offset { IntOffset(0, floatOffset.roundToInt()) }
                            .rotate(avatarRotate),
                        contentAlignment = Alignment.Center
                    ) {
                        // Halo de sombra suave
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDarkMode) Color(0x33FFFFFF) else Color(0x15000000)
                                )
                        )

                        // Avatar do Desenvolvedor
                        Image(
                            painter = painterResource(id = R.drawable.ic_developer_avatar),
                            contentDescription = "Developer Avatar",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(2.5.dp, if (isDarkMode) Color(0xFFE5E7EB) else Color(0xFF222222)),
                                    CircleShape
                                )
                        )

                        // Partícula / Sparkle Notion flutuante
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .scale(sparklePulse)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color.White else Color(0xFF1C1917)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = if (isDarkMode) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Tessera Browser Core",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Desenvolvido com foco absoluto em minimalismo, velocidade, ergonomia e privacidade sem concessões.",
                        fontSize = 13.sp,
                        color = textSub,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge de Versão Dinâmica
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkMode) Color(0xFF1E1E24) else Color(0xFFE8E4D8))
                            .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "v$versionName ($versionCode) • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSub,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção de Filosofia & Privacidade
            Text(
                text = "FILOSOFIA DO PROJETO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DeveloperFeatureRow(
                        icon = Icons.Rounded.Shield,
                        title = "Zero Telemetria Comercial",
                        subtitle = "Sem rastreadores de terceiros, sem coleta invasiva de telemetria e sem publicidade incorporada.",
                        textMain = textMain,
                        textSub = textSub,
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

                    DeveloperFeatureRow(
                        icon = Icons.Outlined.Code,
                        title = "Arquitetura 100% Nativa Jetpack Compose",
                        subtitle = "Código limpo em Kotlin com transições fluidas e alta performance de recomposição.",
                        textMain = textMain,
                        textSub = textSub,
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

                    DeveloperFeatureRow(
                        icon = Icons.Outlined.Security,
                        title = "Modo Journal & AMOLED Black",
                        subtitle = "Cuidado ocular com paleta editorial quente de jornal e preto puro para telas OLED.",
                        textMain = textMain,
                        textSub = textSub,
                        accentColor = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informações do Sistema & Diagnóstico
            Text(
                text = "INFORMAÇÕES DE AMBIENTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DeveloperInfoRow("Dispositivo", "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}", textMain, textSub)
                    HorizontalDivider(color = cardBorder, thickness = 0.8.dp)
                    DeveloperInfoRow("Versão Android", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", textMain, textSub)
                    HorizontalDivider(color = cardBorder, thickness = 0.8.dp)
                    DeveloperInfoRow("Compile Target SDK", "35 (Android 15 Ready)", textMain, textSub)
                    HorizontalDivider(color = cardBorder, thickness = 0.8.dp)
                    DeveloperInfoRow("Pacote", context.packageName, textMain, textSub)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ações & Compartilhamento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(16.dp))
                        .clickable {
                            val clipText = "Tessera Browser v$versionName ($versionCode) | ${Build.MANUFACTURER} ${Build.MODEL} | Android ${Build.VERSION.RELEASE}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Tessera Specs", clipText))
                            Toast.makeText(context, "Especificações copiadas!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Copiar Specs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textMain
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(16.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kennedybarreto/tessera-browser"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Navegador indisponível", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "GitHub Repo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textMain
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeveloperFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textMain: Color,
    textSub: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textMain
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = textSub,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun DeveloperInfoRow(
    label: String,
    value: String,
    textMain: Color,
    textSub: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = textSub
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = textMain,
            fontFamily = FontFamily.Monospace
        )
    }
}
