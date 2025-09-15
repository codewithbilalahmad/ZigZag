package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingTopBar(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    TopAppBar(modifier = modifier, title = {
        Text(stringResource(R.string.settings))
    }, navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    })
}