package com.rvu.campuscompanion.data.model

data class Quote(val text: String, val author: String)

object QuoteRepository {
    val quotes = listOf(
        Quote("The expert in anything was once a beginner.", "Helen Hayes"),
        Quote("Success is not final, failure is not fatal.", "Winston Churchill"),
        Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
        Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
        Quote("Education is the most powerful weapon to change the world.", "Nelson Mandela"),
        Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
        Quote("Strive for progress, not perfection.", "Anonymous")
    )

    fun ofTheDay(): Quote {
        val day = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        return quotes[day % quotes.size]
    }
}
