package com.muhammad.zigzag.presentation.screens.whiteboard

import com.muhammad.zigzag.domain.model.DrawnPath

sealed class PathAction{
    data class Add(val path : DrawnPath) : PathAction()
    data class Delete(val paths : List<DrawnPath>) : PathAction()
}