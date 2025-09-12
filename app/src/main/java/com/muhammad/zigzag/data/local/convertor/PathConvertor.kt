package com.muhammad.zigzag.data.local.convertor

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.room.TypeConverter
import android.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.PathParser
import java.util.Locale
import kotlin.math.max

class PathConvertor {

    @TypeConverter
    fun fromPath(path: Path): String {
        return serializePath(path)
    }

    @TypeConverter
    fun toPath(pathString: String): Path {
        return deserializePath(pathString)
    }

    private fun serializePath(path: Path): String {
        val sb = StringBuilder()
        val pm = PathMeasure(path.asAndroidPath(), false)
        val pos = FloatArray(2)

        do {
            val length = pm.length
            if (length > 0f) {
                val step = max(1f, length / 300f)
                var distance = 0f

                pm.getPosTan(0f, pos, null)
                sb.append("M${pos[0].fmt()},${pos[1].fmt()}")

                while (distance < length) {
                    pm.getPosTan(distance, pos, null)
                    sb.append("L${pos[0].fmt()},${pos[1].fmt()}")
                    distance += step
                }

                pm.getPosTan(length, pos, null)
                sb.append("L${pos[0].fmt()},${pos[1].fmt()}")
            }
        } while (pm.nextContour())

        return sb.toString()
    }

    private fun deserializePath(pathString: String): Path {
        return PathParser().parsePathString(pathString).toPath()
    }

    private fun Float.fmt(): String = String.format(Locale.US, "%.2f", this)
}
