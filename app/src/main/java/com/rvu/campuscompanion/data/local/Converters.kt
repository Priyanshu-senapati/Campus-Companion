package com.rvu.campuscompanion.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun listToString(value: List<String>): String = value.joinToString("|")

    @TypeConverter
    fun stringToList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|")
}
