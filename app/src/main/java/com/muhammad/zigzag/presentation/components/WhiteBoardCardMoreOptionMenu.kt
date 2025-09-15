package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = {
            onMenuDismiss()
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp,
    ) {
        DropdownMenuItem(text = {
            Text(stringResource(R.string.rename))
        }, onClick = {
            onRenameClick()
            onMenuDismiss()
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null)
        })
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        DropdownMenuItem(text = {
            Text(stringResource(R.string.delete))
        }, onClick = {
            onDeleteClick()
            onMenuDismiss()
        }, leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null)
        })
    }
}