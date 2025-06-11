package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.domain.model.ColorScheme

@Composable
fun ColorSchemeDialog(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    currentScheme: ColorScheme, onDismiss: () -> Unit, onThemeSelected: (ColorScheme) -> Unit,
) {
    var selectedScheme by rememberSaveable { mutableStateOf(currentScheme) }
    if (isOpen) {
        AlertDialog(modifier = modifier, onDismissRequest = onDismiss, title = {
            Text("Color Scheme")
        }, text = {
            Column {
                ColorScheme.entries.forEach { colorScheme ->
                    val selected = selectedScheme == colorScheme
                    LabelRadioButton(selected = selected, label = colorScheme.label, onClick = {
                        selectedScheme = colorScheme
                    })
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                onThemeSelected(selectedScheme)
                onDismiss()
            }){
                Text("OK")
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss){
                Text("Close")
            }
        })
    }
}

@Composable
private fun LabelRadioButton(label: String, onClick: () -> Unit, selected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}