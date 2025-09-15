package com.muhammad.zigzag.presentation.screens.home

import com.muhammad.zigzag.domain.model.WhiteBoard

data class HomeState(
    val whiteBoards : List<WhiteBoard> = emptyList(),
    val whiteboardsLoading : Boolean = true,
    val showDeleteWhiteboardDialog : Boolean = false,
    val showEditWhiteboardDialog : Boolean = false,
    val selectedWhiteboard : WhiteBoard?=null,
    val newWhiteboardName : String = "",
    val isListLayout : Boolean?=null,
)