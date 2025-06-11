package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.muhammad.zigzag.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBarHorizontal(
    modifier: Modifier = Modifier,
    onHomeIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
) {
    Row(modifier = modifier) {
        IconButton(onClick = { onHomeIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_home),
                contentDescription = null
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onUndoIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_undo),
                contentDescription = null
            )
        }
        IconButton(onClick = { onRedoIconClick() }, shapes = IconButtonDefaults.shapes()) {
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
fun TopBarVertical(
    modifier: Modifier = Modifier,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onRedoIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
) {
    Column(modifier) {
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
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onUndoIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_undo),
                contentDescription = null
            )
        }
        IconButton(onClick = { onRedoIconClick() }, shapes = IconButtonDefaults.shapes()) {
            Icon(
                painter = painterResource(R.drawable.ic_redo),
                contentDescription = null
            )
        }
    }
}

@Composable
fun MoreOptionsMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onStrokeWidthClick: () -> Unit,
    onDrawingColorClick: () -> Unit,
    onBackgroundColorClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = onMenuDismiss
    ) {
        DropdownMenuItem(text = {
            Text("Stroke Width")
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null)
        }, onClick = {
            onStrokeWidthClick()
            onMenuDismiss()
        })
        DropdownMenuItem(text = {
            Text("Drawing Color")
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_circle), contentDescription = null)
        }, onClick = {
            onDrawingColorClick()
            onMenuDismiss()
        })
        DropdownMenuItem(text = {
            Text("Background Color")
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_circle), contentDescription = null)
        }, onClick = {
            onBackgroundColorClick()
            onMenuDismiss()
        })
        DropdownMenuItem(text = {
            Text("Settings")
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_setting), contentDescription = null)
        }, onClick = {
            onSettingsClick()
            onMenuDismiss()
        })
    }
}