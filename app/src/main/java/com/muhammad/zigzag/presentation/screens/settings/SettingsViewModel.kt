package com.muhammad.zigzag.presentation.screens.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.zigzag.domain.model.AppLanguage
import com.muhammad.zigzag.domain.model.ColorScheme
import com.muhammad.zigzag.domain.repository.SettingRepository
import com.muhammad.zigzag.utils.setAppLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private var settingsRespository: SettingRepository) : ViewModel() {
    val currentScheme = settingsRespository.getColorScheme().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = ColorScheme.SYSTEM_DEFAULT
    )
    fun saveLanguage(activity : Activity, language: AppLanguage) {
        setAppLocale(language.name.lowercase())
        activity.recreate()
    }

    fun saveColorScheme(colorScheme: ColorScheme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRespository.saveColorScheme(colorScheme)
        }
    }
}