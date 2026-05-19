package com.rvu.campuscompanion.data.repository

import androidx.lifecycle.LiveData
import com.rvu.campuscompanion.data.local.TimetableDao
import com.rvu.campuscompanion.data.local.TimetableEntry

class TimetableRepository(private val dao: TimetableDao) {
    fun getByDay(day: String): LiveData<List<TimetableEntry>> = dao.getByDay(day)
    fun getAll(): LiveData<List<TimetableEntry>> = dao.getAll()
    suspend fun getByDaySync(day: String): List<TimetableEntry> = dao.getByDaySync(day)
    suspend fun add(e: TimetableEntry) = dao.insert(e)
    suspend fun update(e: TimetableEntry) = dao.update(e)
    suspend fun delete(e: TimetableEntry) = dao.delete(e)
}
