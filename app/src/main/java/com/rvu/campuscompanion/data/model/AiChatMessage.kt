package com.rvu.campuscompanion.data.model

data class AiChatMessage(
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Sender { USER, AI }
}
