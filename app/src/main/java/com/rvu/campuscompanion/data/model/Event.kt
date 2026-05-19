package com.rvu.campuscompanion.data.model

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val posterUrl: String = "",
    val date: Long = 0L,
    val venue: String = "",
    val organizer: String = "",
    val category: String = "Technical",
    val tags: List<String> = emptyList(),
    val registeredUsers: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val postedBy: String = "",
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
