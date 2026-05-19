package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.local.TimetableEntry
import com.rvu.campuscompanion.data.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimetableViewModel(private val repo: TimetableRepository) : ViewModel() {

    private val _day = MutableLiveData<String>().also { it.value = "Monday" }
    val classesForDay: LiveData<List<TimetableEntry>> = _day.switchMap { repo.getByDay(it) }
    val allClasses: LiveData<List<TimetableEntry>> = repo.getAll()

    fun setDay(day: String) { _day.value = day }

    fun add(e: TimetableEntry) = viewModelScope.launch(Dispatchers.IO) { repo.add(e) }
    fun update(e: TimetableEntry) = viewModelScope.launch(Dispatchers.IO) { repo.update(e) }
    fun delete(e: TimetableEntry) = viewModelScope.launch(Dispatchers.IO) { repo.delete(e) }
}
