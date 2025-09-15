package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.WhiteBoard
import com.muhammad.zigzag.utils.formatDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WhiteBoardItemCard(
    modifier: Modifier = Modifier,
    whiteBoard: WhiteBoard,
    onClick : () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                AsyncImage(
                    model = whiteBoard.previewUrl.orEmpty(),
                    contentDescription = whiteBoard.previewUrl.orEmpty(),
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.padding(4.dp)) {
                    IconButton(
                        onClick = {
                            isExpanded = !isExpanded
                        },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_menu),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    WhiteBoardCardMoreOptionMenu(isExpanded = isExpanded, onMenuDismiss = {
                        isExpanded = false
                    }, onDeleteClick = onDeleteClick, onRenameClick = onRenameClick)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = MaterialShapes.Cookie12Sided.toShape()
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_canvas),
                        contentDescription = null, modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = whiteBoard.name,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Text(
                text = "${stringResource(R.string.last_edited)}: ${whiteBoard.lastEdited.formatDate()}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 6.dp, start = 8.dp)
            )
        }
    }
}