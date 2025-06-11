package com.muhammad.zigzag.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.muhammad.zigzag.domain.model.ColorScheme
import com.muhammad.zigzag.domain.repository.SettingRepository

class SettingsViewModel(private var settingsRespository: SettingRepository) : ViewModel() {
    val currentScheme = settingsRespository.getColorScheme().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = ColorScheme.SYSTEM_DEFAULT
    )
    fun saveColorScheme(colorScheme: ColorScheme){
        viewModelScope.launch {
            settingsRespository.saveColorScheme(colorScheme)
        }
    }
}