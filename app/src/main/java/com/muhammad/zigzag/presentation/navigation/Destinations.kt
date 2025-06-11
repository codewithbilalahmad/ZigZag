package com.muhammad.zigzag.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Destinations{
    @Serializable
    data object HomeScreen : Destinations()
    @Serializable
    data class WhiteBoardScreen(val whiteBoardId : Long?) : Destinations()
    @Serializable
    data object SettingScreen : Destinations()
}