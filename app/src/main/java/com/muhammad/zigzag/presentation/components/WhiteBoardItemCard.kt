package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.WhiteBoard
import com.muhammad.zigzag.utils.formatDate

@Composable
fun WhiteBoardItemCard(
    modifier: Modifier = Modifier,
    whiteBoard: WhiteBoard,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(modifier = modifier) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.canvas),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    text = whiteBoard.name,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
                )
                Box {
                    IconButton(onClick = {
                        isExpanded = !isExpanded
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = null
                        )
                    }
                    WhiteBoardCardMoreOptionMenu(
                        isExpanded = isExpanded,
                        onMenuDismiss = {
                            isExpanded = false
                        },
                        onRenameClick = onRenameClick,
                        onDeleteClick = onDeleteClick,
                        modifier = Modifier.align(
                            Alignment.TopStart
                        )
                    )
                }
            }
            Text(
                text = "Last edited ${whiteBoard.lastEdited.formatDate()}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}