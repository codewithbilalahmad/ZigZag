package com.muhammad.zigzag.domain.model

import com.muhammad.zigzag.R


enum class DrawingTool(val res: Int, val isColored: Boolean = false) {
    PEN(res = R.drawable.img_pen, isColored = true),
    HIGHLIGHTER(res = R.drawable.img_highlighter, isColored = true),
    LASER_PEN(res = R.drawable.img_laser_pen, isColored = true),
    ERASER(res = R.drawable.img_eraser, isColored = true),
    LINE(res = R.drawable.ic_line),
    ARROW(res = R.drawable.ic_arrow),
    RECTANGLE(res = R.drawable.ic_rectangle),
    CIRCLE(res = R.drawable.ic_circle),
    TRIANGLE(res = R.drawable.ic_triangle),
}