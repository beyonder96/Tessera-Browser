package com.tessera.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TesseraAirBar(
    progress: Float,
    displayUrl: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onReload: () -> Unit,
    onSearch: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf(displayUrl) }
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(32.dp)

    // Keep queryText in sync with displayUrl when page changes
    LaunchedEffect(displayUrl) {
        queryText = displayUrl
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "tessera_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .shadow(20.dp, shape = shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xEE1C1715), Color(0xDD120E0D))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    ) {
        // Progress indicator at top of bar
        AnimatedVisibility(
            visible = progress > 0f && progress < 1f,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = Color(0xFF64B5F6),
                trackColor = Color.Transparent
            )
        }

        // Action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Back button
            AirActionIcon(
                icon = Icons.Rounded.ArrowBack,
                description = "Voltar",
                enabled = canGoBack,
                onClick = onBack
            )

            // Home button (returns to Start Page / Discagem Rápida)
            AirActionIcon(
                icon = Icons.Rounded.Home,
                description = "Início",
                enabled = true,
                onClick = onHome
            )

            // Search / URL input field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Placeholder
                if (queryText.isEmpty()) {
                    Text(
                        text = "Pesquisar ou digitar endereço...",
                        color = Color.White.copy(alpha = 0.38f),
                        fontSize = 13.sp
                    )
                }

                BasicTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = SolidColor(Color(0xFF64B5F6)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (queryText.isNotBlank()) {
                                onSearch(queryText.trim())
                                focusManager.clearFocus()
                            }
                        }
                    )
                )
            }

            // Reload button
            AirActionIcon(
                icon = Icons.Rounded.Refresh,
                description = "Recarregar",
                enabled = true,
                onClick = onReload
            )

            // Settings button
            AirActionIcon(
                icon = Icons.Rounded.Tune,
                description = "Configuração fácil",
                enabled = true,
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun AirActionIcon(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White.copy(alpha = if (enabled) 0.9f else 0.25f),
            modifier = Modifier.size(18.dp)
        )
    }
}
