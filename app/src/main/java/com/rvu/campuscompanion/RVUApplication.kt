package com.rvu.campuscompanion

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.rvu.campuscompanion.data.local.AppDatabase
import com.rvu.campuscompanion.data.repository.AssistantRepository
import com.rvu.campuscompanion.data.repository.AttendanceRepository
import com.rvu.campuscompanion.data.repository.CanteenRepository
import com.rvu.campuscompanion.data.repository.TimetableRepository
import com.rvu.campuscompanion.utils.Constants
import com.rvu.campuscompanion.utils.PrefsManager
import com.rvu.campuscompanion.workers.AttendanceSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

class RVUApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getInstance(this, appScope) }
    val timetableRepository by lazy { TimetableRepository(database.timetableDao()) }
    val attendanceRepository by lazy { AttendanceRepository(database.attendanceDao()) }
    val canteenRepository by lazy { CanteenRepository(database.menuItemDao()) }
    val assistantRepository by lazy { AssistantRepository(attendanceRepository, timetableRepository) }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        applyTheme()
        createNotificationChannels()
        scheduleAttendanceSync()
    }

    private fun applyTheme() {
        val prefs = PrefsManager(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(Constants.CHANNEL_EVENTS,
                getString(R.string.channel_events), NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(Constants.CHANNEL_REMINDERS,
                getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_HIGH)
        ).forEach { nm.createNotificationChannel(it) }
    }

    private fun scheduleAttendanceSync() {
        val request = PeriodicWorkRequestBuilder<AttendanceSyncWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "attendance_sync", ExistingPeriodicWorkPolicy.KEEP, request
        )
    }
}
