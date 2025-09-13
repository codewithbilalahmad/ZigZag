package com.muhammad.zigzag.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muhammad.zigzag.R
import com.muhammad.zigzag.domain.model.DrawingTool
import org.muhammad.canvos.domain.model.ColorPaletteType

@Composable
fun CommandPaletteDrawerContent(
    modifier: Modifier = Modifier,
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
    Surface(modifier = modifier) {
        ColorPaletteContent(
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
    }
}

@Composable
fun CommandPaletteCard(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedDrawingTool: DrawingTool,
    strokeColors: List<Color>,
    selectedStrokeColor: Color,
    onStrokeColorChange: (Color) -> Unit,
    backgroundColors: List<Color>,
    selectedBackgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    strokeSlideValue: Float,
    onStrokeSlideValueChange: (Float) -> Unit,
    opacitySlideValue: Float,
    drawingName: String,
    onOpacitySliderValueChange: (Float) -> Unit,
    onSelectedCanvasColor: (Color) -> Unit, selectedCanvasColor: Color,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit, canvasColors: List<Color>,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ElevatedCard(modifier = Modifier.width(250.dp)) {
            ColorPaletteContent(
                selectedDrawingTool = selectedDrawingTool,
                strokeColors = strokeColors,
                selectedStrokeColor = selectedStrokeColor,
                onStrokeColorChange = onStrokeColorChange,
                backgroundColors = backgroundColors,
                selectedBackgroundColor = selectedBackgroundColor,
                onBackgroundColorChange = onBackgroundColorChange,
                strokeSlideValue = strokeSlideValue,
                onStrokeSlideValueChange = onStrokeSlideValueChange,
                opacitySlideValue = opacitySlideValue,
                onOpacitySliderValueChange = onOpacitySliderValueChange,
                selectedCanvasColor = selectedCanvasColor,
                canvasColors = canvasColors,
                onColorPaletteIconClick = onColorPaletteIconClick,
                onSelectedCanvasColor = onSelectedCanvasColor, drawingName = drawingName
            )
        }
    }
}

@Composable
fun ColorPaletteContent(
    modifier: Modifier = Modifier,
    drawingName: String,
    selectedDrawingTool: DrawingTool,
    strokeColors: List<Color>,
    selectedStrokeColor: Color, selectedCanvasColor: Color,
    onStrokeColorChange: (Color) -> Unit, onSelectedCanvasColor: (Color) -> Unit,
    backgroundColors: List<Color>,
    selectedBackgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    strokeSlideValue: Float,
    onStrokeSlideValueChange: (Float) -> Unit,
    opacitySlideValue: Float,
    onOpacitySliderValueChange: (Float) -> Unit,
    onColorPaletteIconClick: (ColorPaletteType) -> Unit, canvasColors: List<Color>,
) {
    val updatedCanvasColors = listOf(Color.White) + canvasColors
    val updatedStrokeColors = listOf(Color.Black) + strokeColors
    Column(modifier = modifier) {
        Text(drawingName, style = MaterialTheme.typography.titleLarge)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 1.dp
        )
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            ColorSection(
                sectionTitle = "Canvas",
                colors = updatedCanvasColors,
                selectedColor = selectedCanvasColor,
                onColorChange = onSelectedCanvasColor,
                onColorPlatteClick = {
                    onColorPaletteIconClick(ColorPaletteType.CANVAS)
                })
            Spacer(Modifier.height(20.dp))
            ColorSection(
                sectionTitle = "Stroke",
                colors = updatedStrokeColors,
                selectedColor = selectedStrokeColor,
                onColorChange = onStrokeColorChange,
                onColorPlatteClick = {
                    onColorPaletteIconClick(ColorPaletteType.STROKE)
                })
            when (selectedDrawingTool) {
                DrawingTool.RECTANGLE, DrawingTool.CIRCLE, DrawingTool.TRIANGLE, DrawingTool.STAR -> {
                    Spacer(Modifier.height(20.dp))
                    ColorSection(
                        sectionTitle = "Background",
                        colors = backgroundColors,
                        isBackgroundColor = true,
                        selectedColor = selectedBackgroundColor,
                        onColorChange = onBackgroundColorChange,
                        onColorPlatteClick = {
                            onColorPaletteIconClick(ColorPaletteType.FILL)
                        })
                }

                else -> Unit
            }
            Spacer(Modifier.height(20.dp))
            SliderSection(
                sectionTitle = "Stroke Width",
                sliderValue = strokeSlideValue,
                slideValueRange = 1f..25f,
                onSliderValueChange = onStrokeSlideValueChange
            )
            Spacer(Modifier.height(15.dp))
            SliderSection(
                sectionTitle = "Opacity",
                sliderValue = opacitySlideValue,
                onSliderValueChange = onOpacitySliderValueChange,
                slideValueRange = 1f..100f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorSection(
    sectionTitle: String,
    isBackgroundColor: Boolean = false,
    colors: List<Color>,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    onColorPlatteClick: () -> Unit,
) {
    Column {
        Text(sectionTitle, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBackgroundColor) {
                item {
                    Icon(
                        modifier = Modifier
                            .size(30.dp)
                            .border(
                                1.dp,
                                color = if (selectedColor == Color.Transparent) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .clickable { onColorChange(Color.Transparent) },
                        painter = painterResource(
                            R.drawable.ic_transparent_bg
                        ),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(
                            1.dp,
                            color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .background(color, CircleShape)
                        .clickable { onColorChange(color) }
                )
            }
            item {
                Spacer(Modifier.width(5.dp))
                IconButton(
                    onClick = { onColorPlatteClick() },
                    modifier = Modifier.size(25.dp),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_color_wheel),
                        contentDescription = null, tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderSection(
    modifier: Modifier = Modifier,
    sectionTitle: String,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    slideValueRange: ClosedFloatingPointRange<Float>,
) {
    Column(modifier) {
        Text(sectionTitle, style = MaterialTheme.typography.titleSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppSlider(
                modifier = Modifier.weight(1f),
                value = sliderValue,
                onValueChange = onSliderValueChange,
                valueRange = slideValueRange
            )
        }
    }
}