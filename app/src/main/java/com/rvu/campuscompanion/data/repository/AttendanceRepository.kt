package com.rvu.campuscompanion.data.repository

import androidx.lifecycle.LiveData
import com.rvu.campuscompanion.data.local.AttendanceDao
import com.rvu.campuscompanion.data.local.AttendanceEntity
import com.rvu.campuscompanion.data.local.SubjectAttendance

class AttendanceRepository(private val dao: AttendanceDao) {
    fun summary(): LiveData<List<SubjectAttendance>> = dao.getSubjectSummary()
    suspend fun summarySync(): List<SubjectAttendance> = dao.getSubjectSummarySync()
    fun bySubject(subject: String): LiveData<List<AttendanceEntity>> = dao.getBySubject(subject)
    suspend fun bySubjectSync(subject: String) = dao.getBySubjectSync(subject)
    suspend fun mark(entry: AttendanceEntity) = dao.insert(entry)

    /** Returns classes needed to reach target percentage. */
    fun classesNeededFor(target: Int, present: Int, total: Int): Int {
        if (total == 0) return 0
        val current = present.toFloat() / total
        if (current * 100 >= target) return 0
        // ((present + x) / (total + x)) * 100 >= target  =>  x >= (target*total - 100*present) / (100 - target)
        val numerator = target * total - 100 * present
        val denominator = 100 - target
        if (denominator <= 0) return Int.MAX_VALUE
        return ((numerator + denominator - 1) / denominator).coerceAtLeast(0)
    }
}
