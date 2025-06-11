package com.muhammad.zigzag.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.ColorScheme
import com.muhammad.zigzag.presentation.components.ColorSchemeDialog
import com.muhammad.zigzag.presentation.components.SettingTopBar

@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    currentScheme: ColorScheme,
    onThemeSelected: (ColorScheme) -> Unit,
) {
    var colorSchemeDialogOpen by rememberSaveable { mutableStateOf(false) }
    ColorSchemeDialog(isOpen = colorSchemeDialogOpen, onDismiss = {
        colorSchemeDialogOpen = false
    }, currentScheme = currentScheme, onThemeSelected = onThemeSelected)
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        SettingTopBar(onBackClick = onBackClick)
    }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            ListItem(
                modifier = Modifier.clickable { colorSchemeDialogOpen = true },
                headlineContent = {
                    Text("Color Scheme")
                },
                supportingContent = {
                    Text(currentScheme.label)
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_theme),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                })
        }
    }
}