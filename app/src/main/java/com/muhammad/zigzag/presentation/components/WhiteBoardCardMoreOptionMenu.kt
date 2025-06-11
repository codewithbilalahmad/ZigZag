package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R

@Composable
fun WhiteBoardCardMoreOptionMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    DropdownMenu(modifier = modifier, expanded = isExpanded, onDismissRequest = {
        onMenuDismiss()
    },) {
        DropdownMenuItem(text = {
            Text("Rename")
        }, onClick = {
            onRenameClick()
            onMenuDismiss()
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null)
        })
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        DropdownMenuItem(text = {
            Text("Delete")
        }, onClick = {
            onDeleteClick()
            onMenuDismiss()
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null)
        })
    }
}