package com.muhammad.zigzag.presentation.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.muhammad.zigzag.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolBarHorizontal(
    modifier: Modifier = Modifier,
    isUndoEnable : Boolean,
    isRedoEnable : Boolean,
    onHomeIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
) {
    HorizontalFloatingToolbar(
        expanded = false,
        modifier = modifier
    ) {
        IconButton(onClick = { onHomeIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_home),
                contentDescription = null
            )
        }
        IconButton(onClick = { onUndoIconClick() }, enabled = isUndoEnable, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_undo),
                contentDescription = null
            )
        }
        IconButton(onClick = { onRedoIconClick() },enabled = isRedoEnable, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_redo),
                contentDescription = null
            )
        }
        IconButton(onClick = { onMenuIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolBarVertical(
    modifier: Modifier = Modifier,
    isUndoEnable : Boolean,
    isRedoEnable : Boolean,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onRedoIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
) {
    VerticalFloatingToolbar(
        expanded = false,
        modifier = modifier
    ) {
        IconButton(onClick = { onHomeIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_home),
                contentDescription = null
            )
        }
        IconButton(onClick = { onMenuIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null
            )
        }
        IconButton(onClick = { onUndoIconClick() }, enabled = isUndoEnable, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_undo),
                contentDescription = null
            )
        }
        IconButton(onClick = { onRedoIconClick() },enabled = isRedoEnable, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_redo),
                contentDescription = null
            )
        }
    }
}