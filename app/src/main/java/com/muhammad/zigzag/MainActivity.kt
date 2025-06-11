package com.muhammad.zigzag

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.muhammad.zigzag.domain.model.ColorScheme
import com.muhammad.zigzag.presentation.navigation.AppNavigation
import com.muhammad.zigzag.presentation.screens.settings.SettingsViewModel
import com.muhammad.zigzag.presentation.theme.ZigZagTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT
                ), navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
            val navHostController = rememberNavController()
            val settingViewModel = koinViewModel<SettingsViewModel>()
            val colorScheme by settingViewModel.currentScheme.collectAsStateWithLifecycle()
            val isDarkTheme = when (colorScheme) {
                ColorScheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                ColorScheme.LIGHT -> false
                ColorScheme.DARK -> true
            }
            ZigZagTheme(isDarkTheme) {
                AppNavigation(navHostController = navHostController, currentScheme = colorScheme) {
                    settingViewModel.saveColorScheme(it)
                }
            }
        }
    }
}