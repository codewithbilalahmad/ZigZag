package com.muhammad.zigzag.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.AppLanguage
import com.muhammad.zigzag.domain.model.ColorScheme

@Composable
fun LanguageDialog(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    language: AppLanguage, onDismiss: () -> Unit, onLanguageSelected: (AppLanguage) -> Unit,
) {
    var selectedLanguage by rememberSaveable { mutableStateOf(language) }
    if (isOpen) {
        AlertDialog(modifier = modifier, onDismissRequest = onDismiss, title = {
            Text(stringResource(R.string.choose_language))
        }, text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    val selected = selectedLanguage == language
                    LabelRadioButton(selected = selected, label = language.label, onClick = {
                        selectedLanguage = language
                    })
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                onLanguageSelected(selectedLanguage)
                onDismiss()
            }) {
                Text(stringResource(R.string.ok))
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        })
    }
}

@Composable
fun ColorSchemeDialog(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    currentScheme: ColorScheme, onDismiss: () -> Unit, onThemeSelected: (ColorScheme) -> Unit,
) {
    var selectedScheme by rememberSaveable { mutableStateOf(currentScheme) }
    if (isOpen) {
        AlertDialog(modifier = modifier, onDismissRequest = onDismiss, title = {
            Text(stringResource(R.string.choose_theme))
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
            }) {
                Text(stringResource(R.string.ok))
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        })
    }
}

@Composable
private fun LabelRadioButton(label: Int, onClick: () -> Unit, selected: Boolean) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    )
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .background(background, RoundedCornerShape(16.dp))
            .clickable{
                onClick()
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onPrimary))
        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge.copy(color = color))
    }
}