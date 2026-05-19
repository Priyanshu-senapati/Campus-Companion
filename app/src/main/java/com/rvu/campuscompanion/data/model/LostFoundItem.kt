package com.rvu.campuscompanion.data.model

import com.google.firebase.firestore.DocumentId

data class LostFoundItem(
    @DocumentId val id: String = "",
    val type: String = "LOST",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val date: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    val contactPhone: String = "",
    val status: String = "ACTIVE",
    val postedBy: String = "",
    val postedByName: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_LOST = "LOST"
        const val TYPE_FOUND = "FOUND"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_RESOLVED = "RESOLVED"
    }
}
