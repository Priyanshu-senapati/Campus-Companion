package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rvu.campuscompanion.data.repository.AssistantRepository
import com.rvu.campuscompanion.data.repository.AttendanceRepository
import com.rvu.campuscompanion.data.repository.CanteenRepository
import com.rvu.campuscompanion.data.repository.TimetableRepository

class ViewModelFactory(
    private val timetableRepo: TimetableRepository? = null,
    private val attendanceRepo: AttendanceRepository? = null,
    private val canteenRepo: CanteenRepository? = null,
    private val assistantRepo: AssistantRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(TimetableViewModel::class.java) ->
            TimetableViewModel(timetableRepo!!) as T
        modelClass.isAssignableFrom(AttendanceViewModel::class.java) ->
            AttendanceViewModel(attendanceRepo!!) as T
        modelClass.isAssignableFrom(CanteenViewModel::class.java) ->
            CanteenViewModel(canteenRepo!!) as T
        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(timetableRepo!!, attendanceRepo!!) as T
        modelClass.isAssignableFrom(AssistantViewModel::class.java) ->
            AssistantViewModel(assistantRepo!!) as T
        else -> throw IllegalArgumentException("Unknown VM: ${modelClass.name}")
    }
}
