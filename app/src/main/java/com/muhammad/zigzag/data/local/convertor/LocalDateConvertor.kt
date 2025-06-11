package com.muhammad.zigzag.data.local.convertor

import androidx.room.*
import kotlinx.datetime.*

class LocalDateConvertor{
    private val sortableDateFormat = LocalDate.Format {
        year()
        chars("/")
        monthNumber()
        chars("/")
        dayOfMonth()
    }
    @TypeConverter
    fun fromLocalDate(date : LocalDate) : String{
        return date.format(sortableDateFormat)
    }
    @TypeConverter
    fun toLocalDate(date : String) : LocalDate{
        return LocalDate.parse(input = date, format =  sortableDateFormat)
    }
}