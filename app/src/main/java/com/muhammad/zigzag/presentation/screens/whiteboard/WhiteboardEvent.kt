package com.muhammad.zigzag.presentation.screens.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.muhammad.canvos.domain.model.ColorPaletteType
import com.muhammad.zigzag.domain.model.DrawingTool

sealed class WhiteboardEvent {
    data class StartDrawing(val offset: Offset) : WhiteboardEvent()
    data class ContinueDrawing(val offset: Offset) : WhiteboardEvent()
    data object FinishDrawing : WhiteboardEvent()
    data class OnDrawingToolSelected(val drawingTool: DrawingTool) : WhiteboardEvent()
    data object OnDrawingToolClose : WhiteboardEvent()
    data object OnFabClick : WhiteboardEvent()
    data class StrokeSlideValueChange(val strokeWidth: Float) : WhiteboardEvent()
    data class OpacitySlideValueChange(val opacity: Float) : WhiteboardEvent()
    data class StrokeColorChange(val strokeColor: Color) : WhiteboardEvent()
    data class BackgroundColorChange(val backgroundColor: Color) : WhiteboardEvent()
    data class CanvasColorChange(val canvasColor : Color) : WhiteboardEvent()
    data class OnColorPaletteIconClick(val colorPalatteType: ColorPaletteType) : WhiteboardEvent()
    data class OnColorSelected(val color: Color) : WhiteboardEvent()
    data object ColorSelectionDialogDismiss : WhiteboardEvent()
    data object OnLaserPathAnimationComplete : WhiteboardEvent()
    data object OnToggleCommandPaletteBottomSheet : WhiteboardEvent()
}