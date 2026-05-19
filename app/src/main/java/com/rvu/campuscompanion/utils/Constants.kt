package com.rvu.campuscompanion.utils

object Constants {
    // Firestore
    const val COLL_USERS = "users"
    const val COLL_EVENTS = "events"
    const val COLL_ANNOUNCEMENTS = "announcements"
    const val COLL_LOSTFOUND = "lostfound"
    const val COLL_CHATS = "chats"
    const val COLL_MESSAGES = "messages"
    const val COLL_FEEDBACK = "feedback"

    // SharedPreferences
    const val PREFS_NAME = "rvu_prefs"
    const val PREF_REMEMBER_ME = "remember_me"
    const val PREF_SAVED_EMAIL = "saved_email"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_NOTIFICATIONS = "notifications_enabled"

    // Notification channels
    const val CHANNEL_EVENTS = "rvu_channel_events"
    const val CHANNEL_REMINDERS = "rvu_channel_reminders"

    // RVU GPS coordinates
    const val RVU_LAT = 12.9141
    const val RVU_LNG = 77.4966
    const val RVU_ZOOM = 17f

    // Email domain
    const val ALLOWED_EMAIL_DOMAIN = "@rvu.edu.in"

    // Branches
    val BRANCHES = listOf(
        "CSE", "ECE", "ME", "DS", "AI&ML",
        "Design", "Business", "Law", "Liberal Arts"
    )

    // Event categories
    val EVENT_CATEGORIES = listOf("Technical", "Cultural", "Sports", "Academic")

    // Attendance thresholds
    const val ATTENDANCE_GOOD = 75
    const val ATTENDANCE_WARN = 65
}
