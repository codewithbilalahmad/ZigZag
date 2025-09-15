package com.muhammad.zigzag.domain.model

import androidx.annotation.StringRes
import com.muhammad.zigzag.R

enum class AppLanguage(
    @get:StringRes val label : Int,
){
    EN(R.string.english),
    UR(R.string.urdu),
    FR(R.string.french),
    RU(R.string.russian),
}