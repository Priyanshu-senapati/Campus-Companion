package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.model.Announcement
import com.rvu.campuscompanion.data.model.Event
import com.rvu.campuscompanion.data.repository.EventsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {
    private val repo = EventsRepository()

    val events: LiveData<List<Event>> = repo.observeEvents()
    val announcements: LiveData<List<Announcement>> = repo.observeAnnouncements()
    val event = MutableLiveData<Event?>()
    val actionResult = MutableLiveData<Result<Unit>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.seedSampleEvents() }
        }
    }

    fun loadEvent(id: String) {
        viewModelScope.launch(Dispatchers.IO) { event.postValue(repo.fetchEvent(id)) }
    }

    fun register(eventId: String, uid: String) {
        viewModelScope.launch(Dispatchers.IO) { actionResult.postValue(repo.register(eventId, uid)) }
    }

    fun unregister(eventId: String, uid: String) {
        viewModelScope.launch(Dispatchers.IO) { actionResult.postValue(repo.unregister(eventId, uid)) }
    }

    override fun onCleared() { repo.cleanup(); super.onCleared() }
}
