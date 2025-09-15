package com.muhammad.zigzag.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.muhammad.zigzag.ZigZagApplication
import com.muhammad.zigzag.domain.model.AppLanguage
import com.muhammad.zigzag.domain.model.ColorScheme
import com.muhammad.zigzag.domain.repository.SettingRepository
import com.muhammad.zigzag.presentation.theme.defaultCanvasColors
import com.muhammad.zigzag.presentation.theme.defaultDrawingColors
import com.muhammad.zigzag.utils.Constants.CANVAS_COLORS_PREF_KEY
import com.muhammad.zigzag.utils.Constants.COLOR_SCHEME_PREF_KEY
import com.muhammad.zigzag.utils.Constants.DATA_STORE_FILE_NAME
import com.muhammad.zigzag.utils.Constants.FILL_COLORS_PREF_KEY
import com.muhammad.zigzag.utils.Constants.LIST_OPTION_PREF_KEY
import com.muhammad.zigzag.utils.Constants.STROKE_COLORS_PREF_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muhammad.canvos.domain.model.ColorPaletteType


class SettingRepositoryImp() : SettingRepository{
    private val context = ZigZagApplication.INSTANCE
    companion object{
        private val Context.prefs by preferencesDataStore(DATA_STORE_FILE_NAME)
        private val COLOR_SCHEME_KEY = stringPreferencesKey(COLOR_SCHEME_PREF_KEY)
        private val PREFERRED_STROKE_COLORS_KEY = stringPreferencesKey(STROKE_COLORS_PREF_KEY)
        private val PREFERRED_FILL_COLORS_KEY = stringPreferencesKey(FILL_COLORS_PREF_KEY)
        private val PREFERRED_CANVAS_COLORS_KEY = stringPreferencesKey(CANVAS_COLORS_PREF_KEY)
        private val PREFERRED_LIST_KEY = booleanPreferencesKey(LIST_OPTION_PREF_KEY)
    }
    override suspend fun saveColorScheme(colorScheme: ColorScheme) {
        context.prefs.edit {prefs->
            prefs[COLOR_SCHEME_KEY] = colorScheme.name
        }
    }


    override fun getColorScheme(): Flow<ColorScheme> {
        return context.prefs.data.map {prefs ->
            val colorScheme = prefs[COLOR_SCHEME_KEY] ?: ColorScheme.SYSTEM_DEFAULT.name
            ColorScheme.valueOf(colorScheme)
        }
    }


    override fun getPreferredFillColors(): Flow<List<Color>> {
        return context.prefs.data.map {prefs ->
            val colorsString = prefs[PREFERRED_FILL_COLORS_KEY]
            colorsString?.parseColors() ?: defaultDrawingColors
        }
    }

    override fun getPreferredCanvasColors(): Flow<List<Color>> {
        return context.prefs.data.map {prefs ->
            val colorsString = prefs[PREFERRED_CANVAS_COLORS_KEY]
            colorsString?.parseColors() ?: defaultCanvasColors
        }
    }

    override fun getPreferredStrokeColors(): Flow<List<Color>> {
        return context.prefs.data.map {prefs ->
            val colorsString = prefs[PREFERRED_STROKE_COLORS_KEY]
            colorsString?.parseColors() ?: defaultDrawingColors
        }
    }

    override suspend fun toggleListOption() {
        context.prefs.edit {preferences ->
            val currentOption = preferences[PREFERRED_LIST_KEY] ?: true
            preferences[PREFERRED_LIST_KEY] = !currentOption
        }
    }

    override fun getIsListOption(): Flow<Boolean> {
        return context.prefs.data.map {preferences ->
           preferences[PREFERRED_LIST_KEY] ?: true
        }
    }

    override suspend fun savePreferredColors(
        colors: List<Color>,
        colorPaletteType: ColorPaletteType,
    ) {
        val colorString = colors.map { it.toArgb() }.joinToString()
        val key = when(colorPaletteType){
            ColorPaletteType.CANVAS -> PREFERRED_CANVAS_COLORS_KEY
            ColorPaletteType.STROKE -> PREFERRED_STROKE_COLORS_KEY
            ColorPaletteType.FILL -> PREFERRED_FILL_COLORS_KEY
        }
        context.prefs.edit {pref ->
            pref[key] = colorString
        }
    }
    private fun String.parseColors() = this.split(", ").map { it.toInt() }.map { Color(it) }
}