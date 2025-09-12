package com.muhammad.zigzag.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import com.muhammad.zigzag.ZigZagApplication
import com.muhammad.zigzag.domain.model.DrawnPath
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@SuppressLint("UseKtx")
fun drawWhiteboardThumbnail(
    paths: List<DrawnPath>,
    canvasColor: Color, originalWidth: Int, originalHeight: Int, targetWidthPx: Int,
): Bitmap {
    if(originalWidth <= 0 && originalHeight <= 0){
        return Bitmap.createBitmap(targetWidthPx,targetWidthPx, Bitmap.Config.ARGB_8888)
    }
    val targetHeightPx = (targetWidthPx.toFloat() * originalHeight / originalWidth).roundToInt()
    val bitmap = Bitmap.createBitmap(targetWidthPx,targetHeightPx, Bitmap.Config.ARGB_8888)
    val bitmapCanvas = Canvas(bitmap)
    bitmapCanvas.drawColor(canvasColor.toArgb())
    val scale = targetWidthPx.toFloat() / originalWidth.toFloat()
    bitmapCanvas.save()
    bitmapCanvas.scale(scale, scale)
    val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    paths.forEach { dp ->
        paint.strokeWidth = dp.strokeWidth
        paint.color = dp.strokeColor.toArgb()
        paint.alpha = (dp.opacity * 2.55f).coerceIn(0f, 255f).roundToInt()
        paint.style = if (dp.backgroundColor != Color.Transparent) {
            Paint.Style.FILL_AND_STROKE
        } else {
            Paint.Style.STROKE
        }
        bitmapCanvas.drawPath(dp.path.asAndroidPath(), paint)
    }

    bitmapCanvas.restore()
    return bitmap
}

fun saveBitmapToFile(bitmap: Bitmap, whiteboardId: Long): String {
    val context = ZigZagApplication.INSTANCE
    val file = File(context.filesDir, "whiteboard_preview_$whiteboardId.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file.absolutePath
}