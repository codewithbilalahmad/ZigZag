package com.muhammad.zigzag.presentation.screens.home

import com.muhammad.zigzag.domain.model.WhiteBoard

sealed interface HomeEvent{
    data object OnToggleDeleteWhiteboardDialog : HomeEvent
    data object OnToggleEditWhiteboardDialog : HomeEvent
    data class OnEditWhiteboardClick(val whiteBoard: WhiteBoard) : HomeEvent
    data class OnDeleteWhiteboardClick(val id  :Long) : HomeEvent
    data class OnSelectWhiteboard(val whiteboard : WhiteBoard) : HomeEvent
    data class OnNewWhiteboardNameChange(val name : String) : HomeEvent
    data object OnToggleListOption : HomeEvent
}