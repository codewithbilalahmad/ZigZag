package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.domain.model.DrawingTool

@Composable
fun DrawingToolFab(
    selectedTool: DrawingTool,
    onClick: () -> Unit,
) {
    FloatingActionButton(onClick = { onClick() }, shape = CircleShape) {
        Icon(
            modifier = Modifier.size(25.dp),
            painter = painterResource(selectedTool.res),
            contentDescription = null
        )
    }
}