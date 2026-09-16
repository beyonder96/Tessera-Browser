package com.tessera.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AddShortcutDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, url: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(24.dp)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = shape
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xF0201A17), Color(0xF015110F))
                    ),
                    shape = shape
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Adicionar à Discagem Rápida",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nome",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                DialogInputField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Ex: Google, Notícias..."
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Endereço Web",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                DialogInputField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = "Ex: google.com",
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        if (title.isNotBlank() && url.isNotBlank()) {
                            onConfirm(title.trim(), url.trim())
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text("Cancelar")
                    }

                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && url.isNotBlank()) {
                                onConfirm(title.trim(), url.trim())
                            }
                        },
                        enabled = title.isNotBlank() && url.isNotBlank(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF64B5F6),
                            disabledContentColor = Color.White.copy(alpha = 0.25f)
                        )
                    ) {
                        Text("Adicionar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val fieldShape = RoundedCornerShape(12.dp)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(fieldShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), fieldShape)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 14.sp
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = SolidColor(Color(0xFF64B5F6)),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onImeAction() })
        )
    }
}
