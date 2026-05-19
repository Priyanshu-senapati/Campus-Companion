package com.rvu.campuscompanion.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.utils.Constants

class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Upcoming event"
        val venue = inputData.getString("venue") ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(Constants.CHANNEL_REMINDERS) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        Constants.CHANNEL_REMINDERS,
                        applicationContext.getString(R.string.channel_reminders),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }

        val notif = NotificationCompat.Builder(applicationContext, Constants.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder: $title")
            .setContentText("Starting in 1 hour at $venue")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notif)
        return Result.success()
    }
}
