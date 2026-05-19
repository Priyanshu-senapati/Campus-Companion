package com.rvu.campuscompanion.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.model.User
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repo = AuthRepository()
    val user = MutableLiveData<User?>()
    val updateResult = MutableLiveData<Result<Unit>>()

    fun load() {
        val uid = FirebaseSource.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) { user.postValue(repo.fetchUser(uid)) }
    }

    fun updateProfile(updated: User, newPhoto: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val photoUrl = newPhoto?.let { repo.uploadProfilePhoto(updated.uid, it) }
                    ?: updated.photoUrl
                val final = updated.copy(photoUrl = photoUrl)
                repo.updateUser(final)
                user.postValue(final)
                updateResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                updateResult.postValue(Result.failure(e))
            }
        }
    }
}
