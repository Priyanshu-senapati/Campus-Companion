package com.rvu.campuscompanion.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.rvu.campuscompanion.data.model.User
import com.rvu.campuscompanion.data.repository.AuthRepository
import com.rvu.campuscompanion.utils.PrefsManager
import com.rvu.campuscompanion.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository()
    private val prefs = PrefsManager(app)

    val authState = MutableLiveData<Resource<Any>>()
    val currentUser = MutableLiveData<User?>()

    fun isLoggedIn(): Boolean = repo.isLoggedIn()
    fun savedEmail() = prefs.savedEmail
    fun rememberMe() = prefs.rememberMe

    fun login(email: String, password: String, remember: Boolean) {
        authState.value = Resource.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.login(email, password)
            result.fold(
                onSuccess = {
                    prefs.rememberMe = remember
                    prefs.savedEmail = if (remember) email else ""
                    authState.postValue(Resource.Success(it))
                },
                onFailure = { authState.postValue(Resource.Error(it.message ?: "Login failed")) }
            )
        }
    }

    fun register(email: String, password: String, name: String, prn: String,
                 branch: String, semester: Int, photo: Uri?) {
        authState.value = Resource.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val r = repo.register(email, password, name, prn, branch, semester, photo)
            r.fold(
                onSuccess = { authState.postValue(Resource.Success(it)) },
                onFailure = { authState.postValue(Resource.Error(it.message ?: "Registration failed")) }
            )
        }
    }

    fun loginWithCredential(credential: AuthCredential) {
        authState.value = Resource.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val r = repo.loginWithCredential(credential)
            r.fold(
                onSuccess = { authState.postValue(Resource.Success(it)) },
                onFailure = { authState.postValue(Resource.Error(it.message ?: "Sign-in failed")) }
            )
        }
    }

    fun resetPassword(email: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { onDone(repo.resetPassword(email)) }
    }

    fun loadCurrentUser() {
        val uid = repo.currentUid() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            currentUser.postValue(repo.fetchUser(uid))
        }
    }

    fun signOut() {
        repo.signOut()
        prefs.clear()
        currentUser.postValue(null)
    }
}
