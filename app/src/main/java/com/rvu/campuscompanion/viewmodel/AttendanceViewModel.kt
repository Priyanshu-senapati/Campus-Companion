package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.local.AttendanceEntity
import com.rvu.campuscompanion.data.local.SubjectAttendance
import com.rvu.campuscompanion.data.repository.AttendanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repo: AttendanceRepository) : ViewModel() {

    val summary: LiveData<List<SubjectAttendance>> = repo.summary()

    private val _subject = MutableLiveData<String>()
    val recordsForSubject: LiveData<List<AttendanceEntity>> = _subject.switchMap { repo.bySubject(it) }

    fun selectSubject(name: String) { _subject.value = name }

    fun mark(entry: AttendanceEntity) = viewModelScope.launch(Dispatchers.IO) { repo.mark(entry) }

    fun classesNeededFor(target: Int, present: Int, total: Int) =
        repo.classesNeededFor(target, present, total)

    fun overallPercentage(list: List<SubjectAttendance>): Float {
        val total = list.sumOf { it.total }
        val present = list.sumOf { it.present }
        return if (total == 0) 0f else (present.toFloat() / total) * 100f
    }
}
