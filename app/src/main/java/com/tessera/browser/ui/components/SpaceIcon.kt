package com.tessera.browser.ui.components

import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Retorna o ícone vetorial moderno correspondente ao identificador ou emoji do Espaço.
 * Elimina o aspecto genérico de emojis e padroniza a interface com ícones vetoriais elegantes.
 */
fun getSpaceVectorIcon(identifier: String): ImageVector {
    return when (identifier.trim()) {
        "🌐", "globe", "general", "geral" -> Icons.Rounded.Public
        "💼", "work", "trabalho" -> Icons.Rounded.WorkOutline
        "🏠", "home", "pessoal" -> Icons.Rounded.Home
        "📚", "book", "estudos" -> Icons.AutoMirrored.Rounded.MenuBook
        "🚀", "rocket", "projetos" -> Icons.Rounded.RocketLaunch
        "🎨", "art", "design" -> Icons.Rounded.Palette
        "🎮", "game", "jogos" -> Icons.Rounded.SportsEsports
        "⚡", "bolt", "rapido" -> Icons.Rounded.Bolt
        "🔬", "science", "pesquisa" -> Icons.Rounded.Science
        "💡", "idea", "ideias" -> Icons.Rounded.Lightbulb
        "🎧", "audio", "musica" -> Icons.Rounded.Headphones
        "☕", "coffee", "lazer" -> Icons.Rounded.Coffee
        "📊", "chart", "financas" -> Icons.Rounded.BarChart
        "🎯", "target", "foco" -> Icons.Rounded.TrackChanges
        "🛍️", "shopping", "compras" -> Icons.Rounded.ShoppingBag
        "🏖️", "travel", "viagens" -> Icons.Rounded.Flight
        else -> Icons.Rounded.Folder
    }
}

/**
 * Componente Compose para renderizar o ícone de um Espaço de navegação.
 */
@Composable
fun SpaceVectorIcon(
    identifier: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = getSpaceVectorIcon(identifier),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
