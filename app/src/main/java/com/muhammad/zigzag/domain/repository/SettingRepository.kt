package com.muhammad.zigzag.domain.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import org.muhammad.canvos.domain.model.ColorPaletteType
import com.muhammad.zigzag.domain.model.ColorScheme

interface SettingRepository {
    suspend fun saveColorScheme(colorScheme: ColorScheme)
    fun getColorScheme(): Flow<ColorScheme>
    fun getPreferredFillColors(): Flow<List<Color>>
    fun getPreferredCanvasColors(): Flow<List<Color>>
    fun getPreferredStrokeColors(): Flow<List<Color>>
    suspend fun toggleListOption()
    fun getIsListOption(): Flow<Boolean>
    suspend fun savePreferredColors(colors: List<Color>, colorPaletteType: ColorPaletteType)
}