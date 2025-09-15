package com.muhammad.zigzag.domain.model

import com.muhammad.zigzag.R


enum class DrawingTool(val res: Int) {
    PEN(res = R.drawable.ic_pen),
    HIGHLIGHTER(res = R.drawable.ic_highlighter),
    LASER_PEN(res = R.drawable.ic_laser_pen),
    SPRAY_PAINT(res = R.drawable.ic_spray),
    ERASER(res = R.drawable.ic_eraser),
    LINE(res = R.drawable.ic_line),
    DOTTED_LINE(res = R.drawable.ic_dotted_line),
    DASHED_LINE(res = R.drawable.ic_dashed_line),
    ARROW(res = R.drawable.ic_arrow),
    DOUBLE_ARROW(res = R.drawable.ic_double_arrow),
    RECTANGLE(res = R.drawable.ic_rectangle),
    CIRCLE(res = R.drawable.ic_circle),
    TRIANGLE(res = R.drawable.ic_triangle),
    STAR(res = R.drawable.ic_star),
    HEART(res = R.drawable.ic_heart),
    CLOUD(res = R.drawable.ic_cloud),
}