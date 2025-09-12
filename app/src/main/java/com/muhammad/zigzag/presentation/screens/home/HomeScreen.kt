package com.muhammad.zigzag.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhammad.zigzag.R
import com.muhammad.zigzag.presentation.components.WhiteBoardItemCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    onSettingClick: () -> Unit,
    onAddWhiteBoardClick: () -> Unit,
    onCardClick: (Long) -> Unit, viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val newWhiteboardName = state.newWhiteboardName
    val selectedWhiteboard = state.selectedWhiteboard
    val isListLayout = state.isListLayout ?: true
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        DashboardTopBar(
            modifier = Modifier,
            onSettingClick = onSettingClick,
            isListLayout = isListLayout,
            onToggleListLayout = {
                viewModel.onAction(HomeEvent.OnToggleListOption)
            })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = onAddWhiteBoardClick, shape = CircleShape
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                contentDescription = null
            )
        }
    }) { paddingValues ->
        AnimatedContent(targetState = isListLayout, transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }) {isListLayout ->
            if (isListLayout) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(state.whiteBoards) { index, whiteboard ->
                        WhiteBoardItemCard(whiteBoard = whiteboard, onRenameClick = {
                            viewModel.onAction(HomeEvent.OnSelectWhiteboard(whiteboard))
                            viewModel.onAction(HomeEvent.OnNewWhiteboardNameChange(whiteboard.name))
                            viewModel.onAction(HomeEvent.OnToggleEditWhiteboardDialog)
                        }, onClick = {
                            whiteboard.id?.let { onCardClick(it) }
                        }, onDeleteClick = {
                            viewModel.onAction(HomeEvent.OnSelectWhiteboard(whiteboard))
                            viewModel.onAction(HomeEvent.OnToggleDeleteWhiteboardDialog)
                        })
                    }
                }
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.whiteBoards) { index, whiteboard ->
                        WhiteBoardItemCard(whiteBoard = whiteboard, onRenameClick = {
                            viewModel.onAction(HomeEvent.OnSelectWhiteboard(whiteboard))
                            viewModel.onAction(HomeEvent.OnNewWhiteboardNameChange(whiteboard.name))
                            viewModel.onAction(HomeEvent.OnToggleEditWhiteboardDialog)
                        }, onClick = {
                            whiteboard.id?.let { onCardClick(it) }
                        }, onDeleteClick = {
                            viewModel.onAction(HomeEvent.OnSelectWhiteboard(whiteboard))
                            viewModel.onAction(HomeEvent.OnToggleDeleteWhiteboardDialog)
                        })
                    }
                }
            }
        }
    }
    if (state.showDeleteWhiteboardDialog) {
        AlertDialog(onDismissRequest = {
            viewModel.onAction(HomeEvent.OnToggleDeleteWhiteboardDialog)
        }, title = {
            Text(text = "Delete Drawing?")
        }, text = {
            Text(text = "Are you sure to delete this drawing. This cant be undone")
        }, confirmButton = {
            TextButton(onClick = {
                viewModel.onAction(HomeEvent.OnToggleDeleteWhiteboardDialog)
                viewModel.onAction(HomeEvent.OnDeleteWhiteboardClick(selectedWhiteboard?.id ?: 0L))
            }) {
                Text("Confirm")
            }
        }, dismissButton = {
            TextButton(onClick = {
                viewModel.onAction(HomeEvent.OnToggleDeleteWhiteboardDialog)
            }) {
                Text("Discard")
            }
        })
    }
    if (state.showEditWhiteboardDialog) {
        AlertDialog(onDismissRequest = {
            viewModel.onAction(HomeEvent.OnToggleEditWhiteboardDialog)
        }, title = {
            Text(text = "Edit Drawing")
        }, text = {
            OutlinedTextField(value = state.newWhiteboardName, onValueChange = { newValue ->
                viewModel.onAction(HomeEvent.OnNewWhiteboardNameChange(newValue))
            }, modifier = Modifier.fillMaxWidth())
        }, confirmButton = {
            TextButton(onClick = {
                viewModel.onAction(HomeEvent.OnToggleEditWhiteboardDialog)
                viewModel.onAction(
                    HomeEvent.OnEditWhiteboardClick(
                        selectedWhiteboard ?: return@TextButton
                    )
                )
            }, enabled = newWhiteboardName != selectedWhiteboard?.name) {
                Text("Confirm")
            }
        }, dismissButton = {
            TextButton(onClick = {
                viewModel.onAction(HomeEvent.OnToggleEditWhiteboardDialog)
            }) {
                Text("Cancel")
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DashboardTopBar(
    modifier: Modifier = Modifier,
    onSettingClick: () -> Unit,
    isListLayout: Boolean,
    onToggleListLayout: () -> Unit,
) {
    TopAppBar(modifier = modifier, title = {
        Text("Dashboard")
    }, actions = {
        IconButton(onClick = {
            onToggleListLayout()
        }, shapes = IconButtonDefaults.shapes()) {
            val icon = if (isListLayout) R.drawable.ic_grid else R.drawable.ic_list
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null
            )
        }
        IconButton(onClick = onSettingClick, shapes = IconButtonDefaults.shapes()) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_setting),
                contentDescription = null
            )
        }
    })
}