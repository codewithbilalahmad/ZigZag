package com.muhammad.zigzag.presentation.screens.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.AppLanguage
import com.muhammad.zigzag.presentation.components.ColorSchemeDialog
import com.muhammad.zigzag.presentation.components.LanguageDialog
import com.muhammad.zigzag.presentation.components.SettingTopBar
import com.muhammad.zigzag.utils.getCurrentLanguage
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val activity = context as Activity
    val language = when (getCurrentLanguage()) {
        "en" -> AppLanguage.EN
        else -> AppLanguage.UR
    }
    val colorScheme by viewModel.currentScheme.collectAsStateWithLifecycle()
    var colorSchemeDialogOpen by rememberSaveable { mutableStateOf(false) }
    var languageDialogOpen by rememberSaveable { mutableStateOf(false) }
    ColorSchemeDialog(isOpen = colorSchemeDialogOpen, onDismiss = {
        colorSchemeDialogOpen = false
    }, currentScheme = colorScheme, onThemeSelected = { colorScheme ->
        viewModel.saveColorScheme(colorScheme)
    })
    LanguageDialog(isOpen = languageDialogOpen, onDismiss = {
        languageDialogOpen = false
    }, language = language) { language ->
        viewModel.saveLanguage(activity = activity, language = language)
    }
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        SettingTopBar(onBackClick = onBackClick)
    }) { innerPadding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item("app_theme") {
                SettingItem(
                    title = R.string.app_theme,
                    label = colorScheme.label,
                    icon = R.drawable.ic_theme,
                    onClick = {
                        colorSchemeDialogOpen = !colorSchemeDialogOpen
                    })
            }
            item("app_language") {
                SettingItem(
                    title = R.string.app_language,
                    label = language.label,
                    icon = R.drawable.ic_language,
                    onClick = {
                        languageDialogOpen = !languageDialogOpen
                    })
            }
        }
    }
}

@Composable
fun SettingItem(title: Int, label: Int, icon: Int, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(stringResource(title))
        },
        supportingContent = {
            Text(stringResource(label))
        },
        leadingContent = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
        })
}