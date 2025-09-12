package com.muhammad.zigzag.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

@Immutable
data class WhiteBoard(
    val id : Long?=null,
    val name : String,
    val lastEdited : LocalDate,
    val canvasColor : Color,
    val previewUrl : String?=null
)