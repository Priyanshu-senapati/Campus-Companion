package com.rvu.campuscompanion.data.model

import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatGroup(
    val id: String = "",
    val name: String = "",
    val branch: String = "",
    val semester: Int = 1,
    val memberCount: Int = 0,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)
