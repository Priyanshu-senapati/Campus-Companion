package com.rvu.campuscompanion.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimetableEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimetableEntry>)

    @Update
    suspend fun update(entry: TimetableEntry)

    @Delete
    suspend fun delete(entry: TimetableEntry)

    @Query("SELECT * FROM timetable WHERE day = :day ORDER BY startTime ASC")
    fun getByDay(day: String): LiveData<List<TimetableEntry>>

    @Query("SELECT * FROM timetable WHERE semester = :semester AND branch = :branch ORDER BY day, startTime")
    suspend fun getBySemesterBranch(semester: Int, branch: String): List<TimetableEntry>

    @Query("SELECT * FROM timetable ORDER BY day, startTime")
    fun getAll(): LiveData<List<TimetableEntry>>

    @Query("SELECT * FROM timetable WHERE day = :day ORDER BY startTime ASC")
    suspend fun getByDaySync(day: String): List<TimetableEntry>

    @Query("SELECT COUNT(*) FROM timetable")
    suspend fun count(): Int
}
