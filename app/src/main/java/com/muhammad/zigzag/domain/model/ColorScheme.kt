package com.muhammad.zigzag.domain.model

import androidx.annotation.StringRes
import com.muhammad.zigzag.R

enum class ColorScheme(
    @get:StringRes val label : Int
){
    SYSTEM_DEFAULT(R.string.system_default),
    LIGHT(R.string.light),
    DARK(R.string.dark)
}