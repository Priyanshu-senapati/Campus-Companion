package com.rvu.campuscompanion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val date: Long,
    val status: String,
    val semester: Int
) {
    companion object {
        const val PRESENT = "PRESENT"
        const val ABSENT = "ABSENT"
    }
}

data class SubjectAttendance(
    val subject: String,
    val present: Int,
    val total: Int
) {
    val percentage: Float get() = if (total == 0) 0f else (present.toFloat() / total) * 100f
}
