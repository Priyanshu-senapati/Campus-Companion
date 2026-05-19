package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rvu.campuscompanion.data.model.CampusLocation
import com.rvu.campuscompanion.data.repository.MapRepository

class MapViewModel : ViewModel() {
    private val _query = MutableLiveData("")
    private val _all = MutableLiveData(MapRepository.locations)

    val locations: LiveData<List<CampusLocation>> = MutableLiveData<List<CampusLocation>>().apply {
        val update: () -> Unit = {
            val q = (_query.value ?: "").trim().lowercase()
            val list = _all.value.orEmpty()
            value = if (q.isEmpty()) list
                    else list.filter { it.name.lowercase().contains(q) ||
                                       it.category.lowercase().contains(q) }
        }
        _query.observeForever { update() }
        _all.observeForever { update() }
    }

    fun search(text: String) { _query.value = text }
}
