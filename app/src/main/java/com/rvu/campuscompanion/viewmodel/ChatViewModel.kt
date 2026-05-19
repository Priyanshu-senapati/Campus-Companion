package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.model.ChatGroup
import com.rvu.campuscompanion.data.model.ChatMessage
import com.rvu.campuscompanion.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()
    val groups = MutableLiveData<List<ChatGroup>>(emptyList())
    private var currentGroupId: String? = null
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    fun loadGroups(branch: String, semester: Int) {
        groups.value = repo.listGroups(branch, semester)
    }

    fun openGroup(groupId: String) {
        currentGroupId = groupId
        repo.observeMessages(groupId).observeForever { _messages.postValue(it) }
    }

    fun send(text: String, senderId: String, senderName: String) {
        val gid = currentGroupId ?: return
        val msg = ChatMessage(senderId = senderId, senderName = senderName, text = text)
        viewModelScope.launch(Dispatchers.IO) { repo.send(gid, msg) }
    }

    override fun onCleared() { repo.cleanup(); super.onCleared() }
}
