package com.rvu.campuscompanion.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<AttendanceEntity>)

    @Delete
    suspend fun delete(entry: AttendanceEntity)

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAll(): LiveData<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE subject = :subject ORDER BY date DESC")
    fun getBySubject(subject: String): LiveData<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE subject = :subject ORDER BY date DESC")
    suspend fun getBySubjectSync(subject: String): List<AttendanceEntity>

    @Query("SELECT subject, " +
            "SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) AS present, " +
            "COUNT(*) AS total " +
            "FROM attendance GROUP BY subject")
    fun getSubjectSummary(): LiveData<List<SubjectAttendance>>

    @Query("SELECT subject, " +
            "SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) AS present, " +
            "COUNT(*) AS total " +
            "FROM attendance GROUP BY subject")
    suspend fun getSubjectSummarySync(): List<SubjectAttendance>

    @Query("SELECT COUNT(*) FROM attendance")
    suspend fun count(): Int
}
