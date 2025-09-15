package com.muhammad.zigzag.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.muhammad.zigzag.data.local.entity.PathEntity
import com.muhammad.zigzag.domain.model.DrawnPath

fun DrawnPath.toPathEntity(): PathEntity {
    return PathEntity(
        id = id,
        path = path,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor.toArgb(),
        opacity = opacity,
        backgroundColor = backgroundColor.toArgb(),
        drawingTool = drawnPath, whiteBoardId = whiteBoardId, style = style
    )
}

fun PathEntity.toDrawnPath(): DrawnPath {
    return DrawnPath(
        id = id,
        drawnPath = drawingTool,
        path = path,
        backgroundColor = Color(backgroundColor),
        opacity = opacity, style = style,
        strokeColor = Color(strokeColor),
        strokeWidth = strokeWidth, whiteBoardId = whiteBoardId
    )
}
fun List<PathEntity>.toDrawnPaths() : List<DrawnPath>{
    return map{ it.toDrawnPath()}
}
