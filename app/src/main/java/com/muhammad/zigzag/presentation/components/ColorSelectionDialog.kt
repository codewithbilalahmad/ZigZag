package com.muhammad.zigzag.presentation.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRect
import com.muhammad.zigzag.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorSelectionDialog(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    var hsv by remember {
        val hsv = floatArrayOf(0f, 0f, 0f)
        AndroidColor.colorToHSV(Color.Blue.toArgb(), hsv)
        mutableStateOf(
            Triple(hsv[0], hsv[1], hsv[2])
        )
    }
    val backgroundColor by remember(hsv) {
        mutableStateOf(Color.hsv(hsv.first, hsv.second, hsv.third))
    }
    if (isOpen) {
        AlertDialog(
            modifier = modifier.widthIn(max = 350.dp),
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.choose_color), style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = backgroundColor,
                                shape = MaterialShapes.Cookie12Sided.toShape()
                            )
                    )
                }
            }, text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    SatValPanel(
                        hue = hsv.first,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        setSalVal = { sat, value ->
                            hsv = Triple(hsv.first, sat, value)
                        })
                    Spacer(Modifier.height(20.dp))
                    HueBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp), onHueChanged = { hue ->
                            hsv = Triple(hue, hsv.second, hsv.third)
                        })
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }, confirmButton = {
                TextButton(onClick = {
                    onColorSelected(backgroundColor)
                    onDismiss()
                }) {
                    Text(stringResource(R.string.select))
                }
            })
    }
}


@SuppressLint("UseKtx")
@Composable
fun SatValPanel(
    hue: Float,
    setSalVal: (Float, Float) -> Unit,
    modifier: Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    var sat: Float
    var value: Float
    var pressOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier.emitDragGestures(interactionSource)
    ) {
        val cornerRadius = 12.dp.toPx()
        val circleRadius = 10.dp.toPx()
        val strokeWidth = 2.dp.toPx()
        val satValSize = size

        if (pressOffset == null) {
            pressOffset = Offset(size.width / 2, size.height / 2)
            val (satPoint, valuePoint) = pointToSatVal(
                size.width / 2,
                size.height / 2,
                size.width,
                size.height
            )
            setSalVal(satPoint, valuePoint)
        }

        val bitmap = Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val satValPanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val rgb = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))

        val satShader = LinearGradient(
            satValPanel.left, satValPanel.top,
            satValPanel.right, satValPanel.top,
            -0x1, rgb, Shader.TileMode.CLAMP
        )
        val valShader = LinearGradient(
            satValPanel.left, satValPanel.top,
            satValPanel.left, satValPanel.bottom,
            -0x1, -0x1000000, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            satValPanel, cornerRadius, cornerRadius,
            Paint().apply {
                shader = ComposeShader(valShader, satShader, PorterDuff.Mode.MULTIPLY)
            }
        )
        drawBitmap(bitmap = bitmap, panel = satValPanel)

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPositionOffset = Offset(
                pressPosition.x.coerceIn(circleRadius..(satValSize.width - circleRadius)),
                pressPosition.y.coerceIn(circleRadius..(satValSize.height - circleRadius))
            )
            pressOffset = pressPositionOffset
            val (satPoint, valuePoint) = pointToSatVal(
                pressPositionOffset.x,
                pressPositionOffset.y,
                satValPanel.width(),
                satValPanel.height()
            )
            sat = satPoint
            value = valuePoint
            setSalVal(sat, value)
        }

        pressOffset?.let {
            drawCircle(
                color = Color.White,
                radius = circleRadius,
                center = it,
                style = Stroke(width = strokeWidth)
            )
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = it)
        }
    }
}

private fun pointToSatVal(pointX: Float, pointY: Float, width: Float, height: Float): Pair<Float, Float> {
    val x = pointX.coerceIn(0f, width)
    val y = pointY.coerceIn(0f, height)
    val satPoint = 1f / width * x
    val valuePoint = 1f - 1f / height * y
    return satPoint to valuePoint
}


@SuppressLint("UseKtx")
@Composable
fun HueBar(onHueChanged: (Float) -> Unit, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember {
        MutableInteractionSource()
    }
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .emitDragGestures(interactionSource = interactionSource)
    ) {
        val drawScopeSize = size
        val bitmap =
            Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        val hueCanvas = Canvas(bitmap)
        val huePanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val hueColors = IntArray((huePanel.width()).toInt())
        var hue = 0f
        for (i in hueColors.indices) {
            hueColors[i] = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
            hue += 360f / hueColors.size
        }
        val linePaint = Paint()
        linePaint.strokeWidth = 0f
        for (i in hueColors.indices) {
            linePaint.color = hueColors[i]
            hueCanvas.drawLine(i.toFloat(), 0f, i.toFloat(), huePanel.bottom, linePaint)
        }
        drawBitmap(bitmap = bitmap, panel = huePanel)
        fun pointToHue(pointX: Float): Float {
            val width = huePanel.width()
            val x = when {
                pointX < huePanel.left -> 0F
                pointX > huePanel.right -> width
                else -> pointX - huePanel.left
            }
            return x * 360f / width
        }
        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPos = pressPosition.x.coerceIn(0f..drawScopeSize.width)
            pressOffset = Offset(x = pressPos, 0f)
            val selectedHue = pointToHue(pressPos)
            onHueChanged(selectedHue)
        }
        drawCircle(
            color = Color.White,
            radius = size.height / 2,
            center = Offset(x = pressOffset.x.coerceIn(size.height /2,size.width - size.height / 2), size.height / 2),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}



fun CoroutineScope.collectForPress(
    interactionSource: InteractionSource,
    setOffset: (Offset) -> Unit,
) {
    launch {
        interactionSource.interactions.collect { interaction ->
            (interaction as? PressInteraction.Press)?.pressPosition?.let(setOffset)
        }
    }
}

private fun Modifier.emitDragGestures(interactionSource: MutableInteractionSource): Modifier =
    composed {
        val scope = rememberCoroutineScope()
        pointerInput(Unit) {
            detectDragGestures(onDrag = { change, _ ->
                scope.launch {
                    interactionSource.emit(PressInteraction.Press(change.position))
                }
            }, onDragStart = { change ->
                scope.launch {
                    interactionSource.emit(PressInteraction.Press(change))
                }
            })
        }
    }

private fun DrawScope.drawBitmap(bitmap: Bitmap, panel: RectF) {
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap, null, panel.toRect(), null
        )
    }
}
