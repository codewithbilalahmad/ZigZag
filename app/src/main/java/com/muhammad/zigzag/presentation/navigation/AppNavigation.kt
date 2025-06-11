package com.muhammad.zigzag.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.muhammad.zigzag.domain.model.ColorScheme
import com.muhammad.zigzag.presentation.screens.home.HomeScreen
import com.muhammad.zigzag.presentation.screens.settings.SettingScreen
import com.muhammad.zigzag.presentation.screens.whiteboard.WhiteboardScreen
import com.muhammad.zigzag.presentation.screens.whiteboard.WhiteboardViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    currentScheme: ColorScheme,
    onThemeSelected: (ColorScheme) -> Unit,
) {
    NavHost(
        navController = navHostController,
        modifier = Modifier.fillMaxSize(),
        startDestination = Destinations.HomeScreen
    ) {
        composable<Destinations.HomeScreen> {
            HomeScreen(onSettingClick = {
                navHostController.navigate(Destinations.SettingScreen)
            }, onCardClick = { whiteBoardId ->
                navHostController.navigate(Destinations.WhiteBoardScreen(whiteBoardId = whiteBoardId))
            }, onAddWhiteBoardClick = {
                navHostController.navigate(Destinations.WhiteBoardScreen(whiteBoardId = null))
            })
        }
        composable<Destinations.WhiteBoardScreen> {
            val viewModel = koinViewModel<WhiteboardViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            WhiteboardScreen(state = state, onEvent = viewModel::onEvent, onHomeClick = {
                navHostController.navigateUp()
            })
        }
        composable<Destinations.SettingScreen> {
            SettingScreen(
                currentScheme = currentScheme,
                onThemeSelected = onThemeSelected,
                onBackClick = {
                    navHostController.navigateUp()
                })
        }
    }
}