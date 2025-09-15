package com.muhammad.zigzag.data.local.entity

import androidx.compose.ui.graphics.Path
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.muhammad.zigzag.domain.model.DrawingTool
import com.muhammad.zigzag.domain.model.PathStyle
import com.muhammad.zigzag.utils.Constants.PATH_ENTITY_TABLE_NAME

@Entity(tableName = PATH_ENTITY_TABLE_NAME)
data class PathEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long?=null,
    val drawingTool: DrawingTool,
    val path : Path,
    val style : PathStyle,
    val strokeWidth : Float,
    val strokeColor : Int,
    val backgroundColor : Int,
    val opacity : Float, val whiteBoardId : Long
)