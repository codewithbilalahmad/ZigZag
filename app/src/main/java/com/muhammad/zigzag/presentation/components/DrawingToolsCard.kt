package com.muhammad.zigzag.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.DrawingTool


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawingToolsCardHorizontal(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedTool: DrawingTool,
    onToolClick: (DrawingTool) -> Unit,
    onCloseIconButton: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(tween(durationMillis = 500)) { h -> h } + fadeIn(),
        exit = slideOutVertically(
            tween(durationMillis = 500)
        ) { h -> h } + fadeOut()
    ) {
        HorizontalFloatingToolbar(expanded = false, modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(DrawingTool.entries, key = {it.name}) { drawingTool ->
                    DrawingToolItem(
                        drawingTool = drawingTool,
                        isSelected = selectedTool == drawingTool,
                        onToolClick = { onToolClick(drawingTool) }
                    )
                }
                item {
                    FilledTonalIconButton(
                        onClick = { onCloseIconButton() },
                        modifier = Modifier.size(
                            IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)
                        ), shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_cancel),
                            contentDescription = null, modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawingToolsCardVertical(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedTool: DrawingTool,
    onToolClick: (DrawingTool) -> Unit, onCloseIconButton: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(tween(durationMillis = 500)) { h -> h },
        exit = slideOutVertically(
            tween(durationMillis = 500)
        ) { h -> h }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(DrawingTool.entries, key = {it.name}) { drawingTool ->
                DrawingToolItem(
                    drawingTool = drawingTool,
                    isSelected = selectedTool == drawingTool,
                    onToolClick = {
                        onToolClick(drawingTool)
                    })
            }
        }
        FilledTonalIconButton(
            onClick = { onCloseIconButton() }, modifier = Modifier.size(
                IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)
            ), shape = IconButtonDefaults.filledShape
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_cancel),
                contentDescription = null, modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawingToolItem(
    drawingTool: DrawingTool,
    isSelected: Boolean,
    onToolClick: () -> Unit,
) {
    IconButton(
        onClick = {
            onToolClick()
        },
        shape = MaterialShapes.Cookie12Sided.toShape(),
        colors = IconButtonDefaults.iconButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(drawingTool.res),
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
        )
    }
}
