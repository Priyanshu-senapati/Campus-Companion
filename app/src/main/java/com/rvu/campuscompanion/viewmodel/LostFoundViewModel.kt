package com.rvu.campuscompanion.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.model.LostFoundItem
import com.rvu.campuscompanion.data.repository.LostFoundRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LostFoundViewModel : ViewModel() {
    private val repo = LostFoundRepository()

    private val _type = MutableLiveData<String>().also { it.value = LostFoundItem.TYPE_LOST }
    val items: LiveData<List<LostFoundItem>> = _type.switchMap { repo.observe(it) }
    val item = MutableLiveData<LostFoundItem?>()
    val postResult = MutableLiveData<Result<Unit>>()
    val statusResult = MutableLiveData<Result<Unit>>()

    fun setType(type: String) { _type.value = type }
    fun currentType(): String = _type.value ?: LostFoundItem.TYPE_LOST

    fun loadItem(id: String) {
        viewModelScope.launch(Dispatchers.IO) { item.postValue(repo.fetchItem(id)) }
    }

    fun post(it: LostFoundItem, image: Uri?) {
        viewModelScope.launch(Dispatchers.IO) { postResult.postValue(repo.post(it, image)) }
    }

    fun markResolved(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            statusResult.postValue(repo.updateStatus(id, LostFoundItem.STATUS_RESOLVED))
        }
    }
}
