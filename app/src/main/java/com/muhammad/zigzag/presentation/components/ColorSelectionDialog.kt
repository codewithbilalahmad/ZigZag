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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

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
                    Text("Selected Color")
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }, text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
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
                    HueBar(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp), setColor = { hue ->
                        hsv = Triple(hue, hsv.second, hsv.third)
                    })
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }, confirmButton = {
                TextButton(onClick = {
                    onColorSelected(backgroundColor)
                    onDismiss()
                }) {
                    Text("Select")
                }
            })
    }
}


@SuppressLint("UseKtx")
@Composable
fun SatValPanel(hue: Float, setSalVal: (Float, Float) -> Unit, modifier: Modifier) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val scope = rememberCoroutineScope()
    var sat: Float
    var value: Float
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    Canvas(
        modifier = modifier
            .emitDragGestures(interactionSource)
    ) {
        val cornerRadius = 12.dp.toPx()
        val satValSize = size
        val bitmap =
            Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val satValPanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val rgb = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
        val satShader = LinearGradient(
            satValPanel.left,
            satValPanel.top,
            satValPanel.right,
            satValPanel.top,
            -0x1,
            rgb,
            Shader.TileMode.CLAMP
        )
        val valShader = LinearGradient(
            satValPanel.left,
            satValPanel.top,
            satValPanel.left,
            satValPanel.bottom,
            -0x1,
            -0x1000000,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(satValPanel, cornerRadius, cornerRadius, Paint().apply {
            shader = ComposeShader(
                valShader, satShader, PorterDuff.Mode.MULTIPLY
            )
        })
        drawBitmap(bitmap = bitmap, panel = satValPanel)
        fun pointToSatVal(pointX: Float, pointY: Float): Pair<Float, Float> {
            val width = satValPanel.width()
            val height = satValPanel.height()
            val x = when {
                pointX < satValPanel.left -> 0f
                pointX > satValPanel.right -> width
                else -> pointX - satValPanel.left
            }
            val y = when {
                pointY < satValPanel.top -> 0f
                pointY > satValPanel.bottom -> height
                else -> pointY - satValPanel.top
            }
            val satPoint = 1f / width * x
            val valuePoint = 1f - 1f / height * y
            return satPoint to valuePoint
        }
        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPositionOffset = Offset(
                pressPosition.x.coerceIn(0f..satValSize.width),
                pressPosition.y.coerceIn(0f..satValSize.height)
            )
            pressOffset = pressPositionOffset
            val (satPoint, valuePoint) = pointToSatVal(
                pointX = pressPositionOffset.x,
                pointY = pressPositionOffset.y
            )
            sat = satPoint
            value = valuePoint
            setSalVal(sat, value)
        }
        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = pressOffset,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pressOffset)
    }
}

@SuppressLint("UseKtx")
@Composable
fun HueBar(setColor: (Float) -> Unit, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val pressOffset = remember {
        mutableStateOf(Offset.Zero)
    }
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .emitDragGestures(interactionSource)
    ) {
        val drawScopeSize = size
        val bitmap = Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        val hueCanvas = Canvas(bitmap)
        val huePanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val hueColors = IntArray((huePanel.width()).toInt())
        var hue = 0f
        for (i in hueColors.indices) {
            hueColors[i] = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
            hue += 360f / hueColors.size
        }
        val linePaint = Paint()
        linePaint.strokeWidth = 0F
        for (i in hueColors.indices) {
            linePaint.color = hueColors[i]
            hueCanvas.drawLine(i.toFloat(), 0F, i.toFloat(), huePanel.bottom, linePaint)
        }
        drawBitmap(
            bitmap = bitmap,
            panel = huePanel
        )
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
            pressOffset.value = Offset(pressPos, 0f)
            val selectedHue = pointToHue(pressPos)
            setColor(selectedHue)
        }

        drawCircle(
            Color.White,
            radius = size.height/2,
            center = Offset(pressOffset.value.x, size.height/2),
            style = Stroke(
                width = 2.dp.toPx()
            )
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
            detectDragGestures(onDrag = { inout, _ ->
                scope.launch {
                    interactionSource.emit(PressInteraction.Press(inout.position))
                }
            })
        }.clickable(interactionSource = interactionSource, null, onClick = {})
    }

private fun DrawScope.drawBitmap(bitmap: Bitmap, panel: RectF) {
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap, null, panel.toRect(), null
        )
    }
}
