package com.muhammad.zigzag.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.muhammad.zigzag.utils.Constants.WHITEBOARD_TABLE_NAME
import kotlinx.datetime.LocalDate

@Entity(tableName = WHITEBOARD_TABLE_NAME)
data class WhiteBoardEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long?=null,
    val name : String,
    val lastEdited : LocalDate,
    val canvasColor : Int,
    val previewUrl : String?=null
)