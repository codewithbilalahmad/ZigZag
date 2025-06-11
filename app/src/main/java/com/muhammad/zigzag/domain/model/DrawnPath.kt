package com.muhammad.zigzag.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class DrawnPath(
    val id: Long? = null,
    val path: Path,
    val drawnPath: DrawingTool,
    val strokeWidth: Float,
    val strokeColor: Color,
    val backgroundColor: Color,
    val opacity: Float, val whiteBoardId : Long
)