package com.muhammad.zigzag.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.DrawingTool
import org.muhammad.canvos.domain.model.ColorPaletteType

@Composable
fun CommandPaletteDialog(
    modifier: Modifier = Modifier,
    showDialog : Boolean,
    onDismiss : () -> Unit,
    selectedDrawingTool: DrawingTool,
    drawingName: String,
    strokeColors: List<Color>,
    selectedStrokeColor: Color,
    onStrokeColorChange: (Color) -> Unit,
    backgroundColors: List<Color>, selectedCanvasColor: Color,
    selectedBackgroundColor: Color,
    canvasColors: List<Color>,
    onSelectedCanvasColor: (Color) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    strokeSliderValue: Float,
    onStrokeSliderValueChange: (Float) -> Unit,
    opacitySliderValue: Float,
    onOpacitySliderValueChange: (Float) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit,
) {
    if(showDialog){
        AlertDialog(onDismissRequest = onDismiss, confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.done))
            }
        }, title = {
            ColorPaletteContent(
                modifier = modifier,
                selectedDrawingTool = selectedDrawingTool,
                strokeColors = strokeColors,
                selectedStrokeColor = selectedStrokeColor,
                onStrokeColorChange = onStrokeColorChange,
                backgroundColors = backgroundColors,
                selectedBackgroundColor = selectedBackgroundColor,
                onBackgroundColorChange = onBackgroundColorChange,
                strokeSlideValue = strokeSliderValue,
                onStrokeSlideValueChange = onStrokeSliderValueChange,
                opacitySlideValue = opacitySliderValue,
                onOpacitySliderValueChange = onOpacitySliderValueChange,
                onColorPaletteIconClick = onColorPaletteIconClick,
                canvasColors = canvasColors,
                selectedCanvasColor = selectedCanvasColor,
                onSelectedCanvasColor = onSelectedCanvasColor, drawingName = drawingName
            )
        })
    }
}