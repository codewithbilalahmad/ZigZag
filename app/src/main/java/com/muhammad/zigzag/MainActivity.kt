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
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    val viewModel: SettingsViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ), navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            val navHostController = rememberNavController()
            val colorScheme by viewModel.currentScheme.collectAsStateWithLifecycle()
            val isDarkTheme = when (colorScheme) {
                ColorScheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                ColorScheme.LIGHT -> false
                ColorScheme.DARK -> true
            }
            ZigZagTheme(isDarkTheme) {
                AppNavigation(
                    navHostController = navHostController,
                )
            }
        }
    }
}