package com.muhammad.zigzag.utils

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import androidx.core.os.LocaleListCompat
import com.muhammad.zigzag.ZigZagApplication
import com.muhammad.zigzag.domain.model.DrawnPath
import com.muhammad.zigzag.domain.model.PathStyle
import com.muhammad.zigzag.presentation.screens.whiteboard.WhiteboardState
import com.muhammad.zigzag.presentation.screens.whiteboard.WhiteboardViewModel
import com.muhammad.zigzag.presentation.screens.whiteboard.drawCustomPath
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

@SuppressLint("UseKtx")
fun drawWhiteboardThumbnail(
    paths: List<DrawnPath>,
    canvasColor: Color, originalWidth: Int, originalHeight: Int, targetWidthPx: Int,
): Bitmap {
    if (originalWidth <= 0 && originalHeight <= 0) {
        return Bitmap.createBitmap(targetWidthPx, targetWidthPx, Bitmap.Config.ARGB_8888)
    }
    val targetHeightPx = (targetWidthPx.toFloat() * originalHeight / originalWidth).roundToInt()
    val bitmap = Bitmap.createBitmap(targetWidthPx, targetHeightPx, Bitmap.Config.ARGB_8888)
    val bitmapCanvas = Canvas(bitmap)
    bitmapCanvas.drawColor(canvasColor.toArgb())
    val scale = targetWidthPx.toFloat() / originalWidth.toFloat()
    bitmapCanvas.save()
    bitmapCanvas.scale(scale, scale)
    val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    paths.forEach { path ->
        if (path.backgroundColor != Color.Transparent) {
            paint.style = Paint.Style.FILL
            paint.color = path.backgroundColor.toArgb()
            paint.alpha = (path.opacity * 2.55f).coerceIn(0f, 255f).roundToInt()
            bitmapCanvas.drawPath(path.path.asAndroidPath(), paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = path.strokeWidth
        paint.color = path.strokeColor.toArgb()
        paint.alpha = (path.opacity * 2.55f).coerceIn(0f, 255f).roundToInt()
        paint.pathEffect = when(path.style){
            PathStyle.DASHED -> DashPathEffect(floatArrayOf(30f, 20f), 0f)
            PathStyle.DOTTED -> DashPathEffect(floatArrayOf(0f, path.strokeWidth * 2), 0f)
            else -> null
        }
        bitmapCanvas.drawPath(path.path.asAndroidPath(), paint)
        paint.pathEffect = null
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

fun getCurrentLanguage(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    if (!locales.isEmpty) {
        return locales[0]?.language ?: Locale.getDefault().language
    }
    return Locale.getDefault().language
}

fun setAppLocale(language: String) {
    val context = ZigZagApplication.INSTANCE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(language)
    } else {
        val localeList = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun shareBitmap(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.cacheDir, "drawing_${UUID.randomUUID()}.png")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val sharedIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out my drawing made with ZigZag \uD83C\uDFA8")
            putExtra(Intent.EXTRA_TITLE, "My drawing")
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
       val chooser = Intent.createChooser(sharedIntent, "Share Drawing")
        val resInfoList = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        for(resolveInfo in resInfoList){
            context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@SuppressLint("UseKtx")
fun captureCanvasAsBitmap(
    width: Int,
    height: Int,
    state : WhiteboardState,
    viewModel: WhiteboardViewModel,
) : Bitmap{
    val imageBitmap = ImageBitmap(width = width, height = height)
    val canvas = ComposeCanvas(imageBitmap)
    val drawScope = CanvasDrawScope()
    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(width = width.toFloat(), height = height.toFloat())
    ) {
        drawRect(state.canvasColor)
        viewModel.actionsToAddedPaths(state.undoStack).forEach { path ->
            drawCustomPath(path = path)
        }
        state.currentPath?.let { path ->
            drawCustomPath(path)
        }
    }
    return imageBitmap.asAndroidBitmap()
}

