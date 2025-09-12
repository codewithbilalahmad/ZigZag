package com.muhammad.zigzag.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.muhammad.zigzag.data.local.entity.WhiteBoardEntity
import com.muhammad.zigzag.domain.model.WhiteBoard

fun WhiteBoard.toWhiteBoardEntity(): WhiteBoardEntity{
    return WhiteBoardEntity(id, name, lastEdited, canvasColor.toArgb(), previewUrl = previewUrl)
}
fun WhiteBoardEntity.toWhiteBoard() : WhiteBoard{
    return WhiteBoard(
        id = id, name = name, lastEdited = lastEdited, canvasColor = Color(canvasColor), previewUrl = previewUrl
    )
}
fun List<WhiteBoardEntity>.toWhiteBoardList() = this.map { it.toWhiteBoard() }
