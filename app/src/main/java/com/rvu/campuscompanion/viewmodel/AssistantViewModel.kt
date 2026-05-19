package com.rvu.campuscompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvu.campuscompanion.data.model.AiChatMessage
import com.rvu.campuscompanion.data.repository.AssistantRepository
import com.rvu.campuscompanion.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AssistantViewModel(
    private val assistantRepo: AssistantRepository,
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _messages = MutableLiveData<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                "Hi! I'm your RVU Assistant. Ask me anything — your attendance status, today's classes, study tips, or campus questions.",
                AiChatMessage.Sender.AI
            )
        )
    )
    val messages: LiveData<List<AiChatMessage>> = _messages

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    fun send(prompt: String) {
        if (prompt.isBlank()) return
        val current = _messages.value.orEmpty()
        _messages.value = current + AiChatMessage(prompt, AiChatMessage.Sender.USER)
        _isTyping.value = true

        viewModelScope.launch {
            val uid = authRepo.currentUid()
            val user = uid?.let { runCatching { authRepo.fetchUser(it) }.getOrNull() }
            val name = user?.name ?: "Student"
            val branch = user?.branch ?: "CSE"
            val sem = user?.semester ?: 5

            val result = assistantRepo.ask(prompt, name, branch, sem)
            val replyText = result.getOrElse { e -> "⚠️ ${e.message ?: "Something went wrong."}" }

            _messages.value = (_messages.value.orEmpty()) + AiChatMessage(replyText, AiChatMessage.Sender.AI)
            _isTyping.value = false
        }
    }
}
