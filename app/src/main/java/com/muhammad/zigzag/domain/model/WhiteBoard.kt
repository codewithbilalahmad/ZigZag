package com.muhammad.zigzag.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

data class WhiteBoard(
    val id : Long?=null,
    val name : String,
    val lastEdited : LocalDate,
    val canvasColor : Color
)