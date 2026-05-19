package com.rvu.campuscompanion.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var rememberMe: Boolean
        get() = prefs.getBoolean(Constants.PREF_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(Constants.PREF_REMEMBER_ME, value).apply()

    var savedEmail: String
        get() = prefs.getString(Constants.PREF_SAVED_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(Constants.PREF_SAVED_EMAIL, value).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean(Constants.PREF_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(Constants.PREF_DARK_MODE, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(Constants.PREF_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(Constants.PREF_NOTIFICATIONS, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
