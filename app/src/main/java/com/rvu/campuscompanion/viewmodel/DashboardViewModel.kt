package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.local.SubjectAttendance
import com.rvu.campuscompanion.data.local.TimetableEntry
import com.rvu.campuscompanion.data.model.Announcement
import com.rvu.campuscompanion.data.model.Event
import com.rvu.campuscompanion.data.model.Quote
import com.rvu.campuscompanion.data.model.QuoteRepository
import com.rvu.campuscompanion.data.model.User
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.data.repository.AttendanceRepository
import com.rvu.campuscompanion.data.repository.AuthRepository
import com.rvu.campuscompanion.data.repository.EventsRepository
import com.rvu.campuscompanion.data.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val timetableRepo: TimetableRepository,
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    private val authRepo = AuthRepository()
    private val eventsRepo = EventsRepository()

    val user = MutableLiveData<User?>()
    val todayClasses = MutableLiveData<List<TimetableEntry>>(emptyList())
    val attendanceSummary: LiveData<List<SubjectAttendance>> = attendanceRepo.summary()
    val upcomingEvents: LiveData<List<Event>> = eventsRepo.observeEvents()
    val announcements: LiveData<List<Announcement>> = eventsRepo.observeAnnouncements()
    val quote = MutableLiveData<Quote>(QuoteRepository.ofTheDay())

    init { refreshToday() }

    fun loadUser() {
        val uid = FirebaseSource.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) { user.postValue(authRepo.fetchUser(uid)) }
    }

    fun refreshToday() {
        val today = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            todayClasses.postValue(timetableRepo.getByDaySync(today))
        }
    }

    fun overallAttendance(list: List<SubjectAttendance>): Float {
        val total = list.sumOf { it.total }
        val present = list.sumOf { it.present }
        return if (total == 0) 0f else (present.toFloat() / total) * 100f
    }
}
