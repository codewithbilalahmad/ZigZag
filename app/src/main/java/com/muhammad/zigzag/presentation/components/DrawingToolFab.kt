package com.muhammad.zigzag.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.domain.model.DrawingTool

@Composable
fun DrawingToolFab(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedTool: DrawingTool,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(tween(durationMillis = 500)) { h -> h },
        exit = fadeOut() + scaleOut()
    ) {
        val tint = if(selectedTool.isColored) Color.Unspecified else LocalContentColor.current
        FloatingActionButton(onClick = { onClick() }) {
            Icon(
                modifier = Modifier.size(25.dp),
                painter = painterResource(selectedTool.res),
                contentDescription = null,
                tint = tint
            )
        }
    }
}