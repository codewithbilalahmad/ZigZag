package com.muhammad.zigzag.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.zigzag.domain.model.WhiteBoard
import com.muhammad.zigzag.domain.repository.SettingRepository
import com.muhammad.zigzag.domain.repository.WhiteBoardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WhiteBoardRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = combine(_state, repository.getAllWhiteBoards(), settingRepository.getIsListOption()) { state, whiteBoards, isListLayout ->
        state.copy(whiteboardsLoading = false, whiteBoards = whiteBoards, isListLayout = isListLayout)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initialValue = HomeState())
    fun onAction(action: HomeEvent) {
        when (action) {
            is HomeEvent.OnDeleteWhiteboardClick -> {
                onDeleteWhiteboardClick(action.id)
            }

            is HomeEvent.OnEditWhiteboardClick -> {
                onEditWhiteboardClick(action.whiteBoard)
            }

            HomeEvent.OnToggleDeleteWhiteboardDialog -> {
                _state.update { it.copy(showDeleteWhiteboardDialog = !state.value.showDeleteWhiteboardDialog) }
            }

            HomeEvent.OnToggleEditWhiteboardDialog -> {
                _state.update { it.copy(showEditWhiteboardDialog = !state.value.showEditWhiteboardDialog) }
            }

            is HomeEvent.OnSelectWhiteboard -> {
                _state.update { it.copy(selectedWhiteboard = action.whiteboard) }
            }

            is HomeEvent.OnNewWhiteboardNameChange -> {
                _state.update { it.copy(newWhiteboardName = action.name) }
            }

            HomeEvent.OnToggleListOption ->{
                viewModelScope.launch {
                    settingRepository.toggleListOption()
                }
            }
        }
    }

    private fun onDeleteWhiteboardClick(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWhiteboardById(id)
            _state.update { it.copy(showDeleteWhiteboardDialog = false) }
        }
    }

    private fun onEditWhiteboardClick(whiteBoard: WhiteBoard) {
        viewModelScope.launch {
            repository.updateWhiteboardName(
                id = whiteBoard.id ?: return@launch,
                name = state.value.newWhiteboardName
            )
            _state.update { it.copy(showEditWhiteboardDialog = false) }
        }
    }
}