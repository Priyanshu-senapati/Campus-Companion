package com.rvu.campuscompanion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val professor: String,
    val room: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val type: String,
    val semester: Int,
    val branch: String
) {
    companion object {
        const val TYPE_LECTURE = "Lecture"
        const val TYPE_LAB = "Lab"
        const val TYPE_TUTORIAL = "Tutorial"
        val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }
}
