package com.rvu.campuscompanion.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val prn: String = "",
    val branch: String = "",
    val semester: Int = 1,
    val phone: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val eventsAttended: Int = 0,
    val itemsPosted: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
