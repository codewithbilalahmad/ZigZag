package com.muhammad.zigzag.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.muhammad.zigzag.presentation.screens.home.HomeScreen
import com.muhammad.zigzag.presentation.screens.settings.SettingScreen
import com.muhammad.zigzag.presentation.screens.whiteboard.WhiteboardScreen

@Composable
fun AppNavigation(
    navHostController: NavHostController
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
            WhiteboardScreen(navHostController = navHostController)
        }
        composable<Destinations.SettingScreen> {
            SettingScreen(
                onBackClick = {
                    navHostController.navigateUp()
                })
        }
    }
}