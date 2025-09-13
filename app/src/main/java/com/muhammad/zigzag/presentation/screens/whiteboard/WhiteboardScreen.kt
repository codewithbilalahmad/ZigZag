package com.muhammad.zigzag.presentation.screens.whiteboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.muhammad.zigzag.domain.model.DrawnPath
import com.muhammad.zigzag.presentation.components.ColorSelectionDialog
import com.muhammad.zigzag.presentation.components.CommandPaletteCard
import com.muhammad.zigzag.presentation.components.CommandPaletteDialog
import com.muhammad.zigzag.presentation.components.DrawingToolFab
import com.muhammad.zigzag.presentation.components.DrawingToolsCardHorizontal
import com.muhammad.zigzag.presentation.components.DrawingToolsCardVertical
import com.muhammad.zigzag.presentation.components.ToolBarHorizontal
import com.muhammad.zigzag.presentation.components.ToolBarVertical
import com.muhammad.zigzag.presentation.theme.defaultDrawingColors
import com.muhammad.zigzag.utils.UIType
import com.muhammad.zigzag.utils.getUIType
import com.muhammad.zigzag.utils.rememberScreenSize
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WhiteboardScreen(
    viewModel: WhiteboardViewModel = koinViewModel(),navHostController: NavHostController
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhiteboardScreenContent(onEvent = viewModel::onEvent, navHostController = navHostController, state = state)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WhiteboardScreenContent(
    state: WhiteboardState,
    navHostController: NavHostController,
    onEvent: (WhiteboardEvent) -> Unit,
) {
    val screenSize = rememberScreenSize()
    val uiType = screenSize.getUIType()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var isCommandPaletteOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(canvasSize) {
        onEvent(WhiteboardEvent.OnCanvasSizeChange(canvasSize))
    }
    BackHandler {
        onEvent(WhiteboardEvent.UpdateWhiteboardPreview)
        navHostController.navigateUp()
    }
    ColorSelectionDialog(isOpen = state.isColorSelectionDialogOpen, onColorSelected = { color ->
        onEvent(WhiteboardEvent.OnColorSelected(color))
    }, onDismiss = {
        onEvent(WhiteboardEvent.ColorSelectionDialogDismiss)
    })
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                canvasSize = size
            }, floatingActionButton = {
            DrawingToolFab(
                selectedTool = state.selectedDrawingTool,
                onClick = {
                    onEvent(WhiteboardEvent.OnDrawingToolFabClick)
                }
            )
        }) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(state.canvasColor)
                .padding(contentPadding)
        ) {
            when (uiType) {
                UIType.COMPACT -> {
                    DrawingCanvas(
                        modifier = Modifier.fillMaxSize(),
                        state,
                        onEvent = onEvent
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = -FloatingToolbarDefaults.ScreenOffset),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        DrawingToolsCardHorizontal(
                            modifier = Modifier.fillMaxWidth(),
                            isVisible = state.isDrawingToolsCardVisible,
                            selectedTool = state.selectedDrawingTool,
                            onToolClick = { tool ->
                                onEvent(WhiteboardEvent.OnDrawingToolSelected(tool))
                            }, onCloseIconButton = {
                                onEvent(WhiteboardEvent.OnDrawingToolClose)
                            })
                        ToolBarHorizontal(
                            onHomeIconClick = {
                                onEvent(WhiteboardEvent.UpdateWhiteboardPreview)
                                navHostController.navigateUp()
                            },
                            isRedoEnable = state.isRedoEnable,
                            isUndoEnable = state.isUndoEnable,
                            onRedoIconClick = {
                                onEvent(WhiteboardEvent.OnRedoPath)
                            },
                            onUndoIconClick = {
                                onEvent(WhiteboardEvent.OnUndoPath)
                            },
                            onMenuIconClick = {
                                onEvent(WhiteboardEvent.OnToggleCommandPaletteDialog)
                            })
                    }
                    CommandPaletteDialog(
                        showDialog = state.showCommandPaletteDialog, onDismiss = {
                            onEvent(WhiteboardEvent.OnToggleCommandPaletteDialog)
                        }, modifier = Modifier.fillMaxWidth(),
                        selectedDrawingTool = state.selectedDrawingTool,
                        strokeColors = defaultDrawingColors,
                        selectedStrokeColor = state.strokeColor,
                        onStrokeColorChange = { onEvent(WhiteboardEvent.StrokeColorChange(it)) },
                        backgroundColors = defaultDrawingColors,
                        selectedBackgroundColor = state.backgroundColor,
                        onBackgroundColorChange = {
                            onEvent(WhiteboardEvent.BackgroundColorChange(it))
                        },
                        strokeSliderValue = state.strokeWidth,
                        onStrokeSliderValueChange = {
                            onEvent(WhiteboardEvent.StrokeSlideValueChange(it))
                        },
                        opacitySliderValue = state.opacity,
                        onOpacitySliderValueChange = {
                            onEvent(WhiteboardEvent.OpacitySlideValueChange(it))
                        },
                        canvasColors = state.preferredCanvasColor,
                        onSelectedCanvasColor = {
                            onEvent(WhiteboardEvent.CanvasColorChange(it))
                        },
                        onColorPaletteIconClick = { colorPaletteType ->
                            onEvent(WhiteboardEvent.OnColorPaletteIconClick(colorPaletteType))
                        },
                        selectedCanvasColor = state.canvasColor,
                        drawingName = state.whiteBoardName
                    )
                }

                else -> {
                    DrawingCanvas(modifier = Modifier.fillMaxSize(), state, onEvent = onEvent)
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.TopStart)
                            .padding(20.dp)
                    ) {
                        ToolBarVertical(
                            onHomeIconClick = {
                                onEvent(WhiteboardEvent.UpdateWhiteboardPreview)
                                navHostController.navigateUp()
                            },
                            isRedoEnable = state.isRedoEnable,
                            isUndoEnable = state.isUndoEnable,
                            onUndoIconClick = {
                                onEvent(WhiteboardEvent.OnUndoPath)
                            },
                            onRedoIconClick = {
                                onEvent(WhiteboardEvent.OnRedoPath)
                            },
                            onMenuIconClick = {
                                isCommandPaletteOpen = true
                            })
                        Spacer(Modifier.width(16.dp))
                        CommandPaletteCard(
                            isVisible = isCommandPaletteOpen,
                            selectedDrawingTool = state.selectedDrawingTool,
                            strokeColors = defaultDrawingColors,
                            selectedStrokeColor = state.strokeColor,
                            onStrokeColorChange = {
                                onEvent(WhiteboardEvent.StrokeColorChange(it))
                            },
                            backgroundColors = defaultDrawingColors,
                            selectedBackgroundColor = state.backgroundColor,
                            onBackgroundColorChange = {
                                onEvent(WhiteboardEvent.BackgroundColorChange(it))
                            },
                            strokeSlideValue = state.strokeWidth,
                            onStrokeSlideValueChange = {
                                onEvent(WhiteboardEvent.StrokeSlideValueChange(it))
                            },
                            opacitySlideValue = state.opacity,
                            onOpacitySliderValueChange = {
                                onEvent(WhiteboardEvent.OpacitySlideValueChange(it))
                            },
                            canvasColors = state.preferredCanvasColor,
                            selectedCanvasColor = state.canvasColor,
                            onSelectedCanvasColor = {
                                onEvent(WhiteboardEvent.CanvasColorChange(it))
                            }, onColorPaletteIconClick = {
                                onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                            }, drawingName = state.whiteBoardName
                        )
                    }

                    DrawingToolsCardVertical(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp),
                        isVisible = state.isDrawingToolsCardVisible,
                        onToolClick = {
                            onEvent(WhiteboardEvent.OnDrawingToolSelected(it))
                        },
                        onCloseIconButton = {
                            onEvent(WhiteboardEvent.OnDrawingToolClose)
                        },
                        selectedTool = state.selectedDrawingTool
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    state: WhiteboardState,
    onEvent: (WhiteboardEvent) -> Unit
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onEvent(WhiteboardEvent.StartDrawing(offset))
                    }, onDrag = { change, _ ->
                        val offset = Offset(x = change.position.x, y = change.position.y)
                        onEvent(WhiteboardEvent.ContinueDrawing(offset))
                    }, onDragEnd = {
                        onEvent(WhiteboardEvent.FinishDrawing)
                    }
                )
            }) {
        state.undoStack.forEach { path ->
            drawCustomPath(path)
        }
        state.currentPath?.let { path ->
            drawCustomPath(path)
        }
    }
    AnimateLaserPen(laserPenPath = state.laserPenPath, onPathAnimationComplete = {
        onEvent(WhiteboardEvent.OnLaserPathAnimationComplete)
    })
}



private fun DrawScope.drawCustomPath(path: DrawnPath) {
    val pathOpacity = path.opacity / 100
    when (path.backgroundColor) {
        Color.Transparent -> {
            drawPath(
                path = path.path,
                color = path.strokeColor.copy(alpha = pathOpacity),
                style = Stroke(width = path.strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        else -> {
            drawPath(
                path = path.path,
                color = path.backgroundColor.copy(alpha = pathOpacity),
                style = Fill
            )
        }
    }
}

@Composable
fun AnimateLaserPen(laserPenPath: DrawnPath?, onPathAnimationComplete: () -> Unit) {
    val animationProgress = remember { Animatable(initialValue = 1f) }
    LaunchedEffect(laserPenPath) {
        laserPenPath?.let {
            animationProgress.animateTo(targetValue = 0f, animationSpec = tween(1000))
            onPathAnimationComplete()
            animationProgress.snapTo(targetValue = 1f)
        }
    }
    val trimmedPath = Path()
    PathMeasure().apply {
        setPath(path = laserPenPath?.path, forceClosed = false)
        getSegment(
            startDistance = length * (1 - animationProgress.value),
            stopDistance = length,
            destination = trimmedPath
        )
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        laserPenPath?.let { path ->
            drawPath(
                path = trimmedPath,
                color = path.strokeColor,
                style = Stroke(width = path.strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
