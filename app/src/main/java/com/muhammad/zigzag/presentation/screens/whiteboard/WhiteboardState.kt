package com.muhammad.zigzag.presentation.screens.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.muhammad.canvos.domain.model.ColorPaletteType
import com.muhammad.zigzag.domain.model.DrawingTool
import com.muhammad.zigzag.domain.model.DrawnPath
import com.muhammad.zigzag.presentation.theme.defaultCanvasColors
import com.muhammad.zigzag.presentation.theme.defaultDrawingColors

data class WhiteboardState(
    val paths: List<DrawnPath> = emptyList(),
    val canvasSize : IntSize = IntSize.Zero,
    val pathToBeDeleted: List<DrawnPath> = emptyList(),
    val laserPenPath: DrawnPath? = null,
    val currentPath: DrawnPath? = null,
    val startingOffset: Offset = Offset.Zero,
    val selectedDrawingTool: DrawingTool = DrawingTool.PEN,
    val isDrawingToolsCardVisible: Boolean = false,
    val isGeometryToolsCardVisible: Boolean = false,
    val isColorSelectionDialogOpen : Boolean = false,
    val strokeWidth: Float = 5f,
    val opacity: Float = 100f,
    val strokeColor: Color = Color.Black,
    val backgroundColor: Color = Color.Transparent,
    val selectedColorPaletteType: ColorPaletteType = ColorPaletteType.STROKE,
    val canvasColor: Color = Color.White,
    val fillColor: Color = Color.Transparent,
    val whiteBoardName: String= "Untitled",
    val preferredStrokeColors: List<Color> = defaultDrawingColors,
    val preferredFillColors : List<Color> = defaultDrawingColors,
    val preferredCanvasColor : List<Color> = defaultCanvasColors,
    val showCommandPaletteDialog : Boolean = false,
    val undoStack : ArrayDeque<DrawnPath> = ArrayDeque(),
    val redoStack : ArrayDeque<DrawnPath> = ArrayDeque(),
    val isUndoEnable : Boolean = undoStack.isNotEmpty(),
    val isRedoEnable : Boolean = redoStack.isNotEmpty()
)