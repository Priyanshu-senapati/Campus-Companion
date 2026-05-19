package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.local.MenuItemEntity
import com.rvu.campuscompanion.data.repository.CanteenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CanteenViewModel(private val repo: CanteenRepository) : ViewModel() {

    private val _category = MutableLiveData<String?>().also { it.value = null }
    val allItems: LiveData<List<MenuItemEntity>> = repo.getAll()
    val filteredItems: LiveData<List<MenuItemEntity>> = _category.switchMap { cat ->
        if (cat == null) repo.getAll() else repo.getByCategory(cat)
    }
    val feedbackResult = MutableLiveData<Result<Unit>>()

    fun filterByCategory(cat: String?) { _category.value = cat }

    fun toggleAvailability(item: MenuItemEntity) {
        viewModelScope.launch(Dispatchers.IO) { repo.update(item.copy(available = !item.available)) }
    }

    fun submitFeedback(menuItem: String, rating: Int, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackResult.postValue(repo.submitFeedback(menuItem, rating, comment))
        }
    }
}
