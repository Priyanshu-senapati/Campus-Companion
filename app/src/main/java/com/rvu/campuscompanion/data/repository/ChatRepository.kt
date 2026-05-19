package com.rvu.campuscompanion.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rvu.campuscompanion.data.model.ChatGroup
import com.rvu.campuscompanion.data.model.ChatMessage
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.utils.Constants
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseSource.firestore
    private var msgListener: ListenerRegistration? = null

    fun listGroups(branch: String, semester: Int): List<ChatGroup> = listOf(
        ChatGroup("${branch}_sem${semester}_general", "${branch} Sem $semester — General", branch, semester),
        ChatGroup("${branch}_sem${semester}_doubts", "${branch} Sem $semester — Doubts", branch, semester),
        ChatGroup("${branch}_sem${semester}_notes", "${branch} Sem $semester — Notes", branch, semester),
        ChatGroup("rvu_general", "RVU General", "ALL", 0),
        ChatGroup("rvu_placements", "RVU Placements", "ALL", 0)
    )

    fun observeMessages(groupId: String): LiveData<List<ChatMessage>> {
        val data = MutableLiveData<List<ChatMessage>>()
        msgListener?.remove()
        msgListener = firestore.collection(Constants.COLL_CHATS)
            .document(groupId)
            .collection(Constants.COLL_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                data.postValue(snap?.toObjects(ChatMessage::class.java) ?: emptyList())
            }
        return data
    }

    suspend fun send(groupId: String, msg: ChatMessage): Result<Unit> = runCatching {
        firestore.collection(Constants.COLL_CHATS).document(groupId)
            .collection(Constants.COLL_MESSAGES).add(msg).await()
        Unit
    }

    fun cleanup() { msgListener?.remove() }
}
