package com.muhammad.zigzag.presentation.screens.whiteboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.domain.model.DrawnPath
import com.muhammad.zigzag.presentation.components.ColorSelectionDialog
import com.muhammad.zigzag.presentation.components.CommandPaletteCard
import com.muhammad.zigzag.presentation.components.CommandPaletteDrawerContent
import com.muhammad.zigzag.presentation.components.DrawingToolFab
import com.muhammad.zigzag.presentation.components.DrawingToolsCardHorizontal
import com.muhammad.zigzag.presentation.components.DrawingToolsCardVertical
import com.muhammad.zigzag.presentation.components.TopBarHorizontal
import com.muhammad.zigzag.presentation.components.TopBarVertical
import com.muhammad.zigzag.presentation.theme.defaultDrawingColors
import com.muhammad.zigzag.utils.UIType
import com.muhammad.zigzag.utils.getUIType
import com.muhammad.zigzag.utils.rememberScreenSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteboardScreen(
    modifier: Modifier = Modifier,
    state: WhiteboardState,
    onEvent: (WhiteboardEvent) -> Unit,
    onHomeClick: () -> Unit,
) {
    val screenSize = rememberScreenSize()
    val uiType = screenSize.getUIType()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCommandPaletteOpen by rememberSaveable { mutableStateOf(false) }
    ColorSelectionDialog(isOpen = state.isColorSelectionDialogOpen, onColorSelected = {
        onEvent(WhiteboardEvent.OnColorSelected(it))
    }, onDismiss = {
        onEvent(WhiteboardEvent.ColorSelectionDialogDismiss)
    })
    Scaffold(modifier = Modifier.fillMaxSize()
        , floatingActionButton = {
        DrawingToolFab(
            isVisible = !state.isDrawingToolsCardVisible,
            selectedTool = state.selectedDrawingTool,
            onClick = {
                onEvent(WhiteboardEvent.OnFabClick)
            }
        )
    }) { contentPadding ->
        Box(
            modifier = modifier
                .fillMaxSize().background(state.canvasColor)
                .padding(contentPadding)
        ) {
            when (uiType) {
                UIType.COMPACT -> {
                    DrawingCanvas(modifier = Modifier.fillMaxSize(), state, onEvent)
                    TopBarHorizontal(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(10.dp),
                        onHomeIconClick = onHomeClick,
                        onRedoIconClick = {},
                        onUndoIconClick = {},
                        onMenuIconClick = {
                            onEvent(WhiteboardEvent.OnToggleCommandPaletteBottomSheet)
                        })
                    DrawingToolsCardHorizontal(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp),
                        isVisible = state.isDrawingToolsCardVisible,
                        selectedTool = state.selectedDrawingTool,
                        onToolClick = {
                            onEvent(WhiteboardEvent.OnDrawingToolSelected(it))
                        }, onCloseIconButton = {
                            onEvent(WhiteboardEvent.OnDrawingToolClose)
                        })
                    if (state.showCommandPaletteBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                onEvent(WhiteboardEvent.OnToggleCommandPaletteBottomSheet)
                            },
                            sheetMaxWidth = 500.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp),
                            dragHandle = {}, sheetState = sheetState) {
                            CommandPaletteDrawerContent(modifier = Modifier.fillMaxWidth(),
                                onCloseIconClick = {
                                    onEvent(WhiteboardEvent.OnToggleCommandPaletteBottomSheet)
                                },
                                selectedDrawingTool = state.selectedDrawingTool,
                                strokeColors = defaultDrawingColors,
                                selectedStrokeColor = state.strokeColor,
                                onStrokeColorChange = { onEvent(WhiteboardEvent.StrokeColorChange(it)) },
                                backgroundColors = defaultDrawingColors,
                                selectedBackgroundColor = state.backgroundColor,
                                onBackgroundColorChange = {
                                    onEvent(WhiteboardEvent.BackgroundColorChange(it))
                                }, strokeSliderValue = state.strokeWidth, onStrokeSliderValueChange = {
                                    onEvent(WhiteboardEvent.StrokeSlideValueChange(it))
                                }, opacitySliderValue = state.opacity, onOpacitySliderValueChange = {
                                    onEvent(WhiteboardEvent.OpacitySlideValueChange(it))
                                }, canvasColors = state.preferredCanvasColor, onSelectedCanvasColor = {
                                    onEvent(WhiteboardEvent.CanvasColorChange(it))
                                }, onColorPaletteIconClick = {
                                    onEvent(WhiteboardEvent.OnColorPaletteIconClick(it))
                                }, selectedCanvasColor = state.canvasColor
                            )
                        }
                    }
                }

                else -> {
                    DrawingCanvas(modifier = Modifier.fillMaxSize(), state, onEvent)
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.TopStart)
                            .padding(20.dp)
                    ) {
                        TopBarVertical(
                            onHomeIconClick = onHomeClick,
                            onUndoIconClick = {},
                            onRedoIconClick = {},
                            onMenuIconClick = {
                                isCommandPaletteOpen = true
                            })
                        Spacer(Modifier.width(16.dp))
                        CommandPaletteCard(
                            isVisible = isCommandPaletteOpen,
                            onCloseIconClick = {
                                isCommandPaletteOpen = false
                            },
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
                            }
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
    onEvent: (WhiteboardEvent) -> Unit,
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
        state.paths.forEach { path ->
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
                style = Stroke(width = path.strokeWidth.dp.toPx())
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
        laserPenPath?.let {
            drawPath(
                path = trimmedPath,
                color = laserPenPath.strokeColor,
                style = Stroke(width = laserPenPath.strokeWidth.dp.toPx())
            )
        }
    }
}