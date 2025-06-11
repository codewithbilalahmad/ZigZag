package com.muhammad.zigzag.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class ScreenSizeInfo(val heightDp : Dp, val widthDp : Dp)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun rememberScreenSize() : ScreenSizeInfo{
    val config = LocalConfiguration.current
    val width = config.screenWidthDp.dp
    val height = config.screenHeightDp.dp
    return remember(config) {
        ScreenSizeInfo(widthDp = width,heightDp =  height)
    }
}

fun ScreenSizeInfo.getUIType() : UIType{
    return when(widthDp){
        in 0.dp..600.dp -> UIType.COMPACT
        in 601.dp..840.dp -> UIType.MEDIUM
        else -> UIType.EXPANDED
    }
}

enum class UIType{
    COMPACT, MEDIUM, EXPANDED
}