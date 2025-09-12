package com.muhammad.zigzag.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSlider(
    modifier : Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    thumbSize: Dp = 28.dp,valueRange : ClosedFloatingPointRange<Float>
) {
    val interactionSource = remember { MutableInteractionSource() }
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        steps = 0, colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            activeTickColor = MaterialTheme.colorScheme.onPrimary,
            inactiveTickColor = MaterialTheme.colorScheme.outlineVariant
        ), thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource, thumbSize = DpSize(thumbSize,thumbSize)
            )
        }, track = {sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState, modifier =Modifier.height(10.dp)
            )
        }
    )
}