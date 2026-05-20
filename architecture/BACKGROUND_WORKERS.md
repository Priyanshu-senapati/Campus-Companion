# ⚙️ Background Workers

Campus Companion uses **WorkManager** with Kotlin coroutine workers for background tasks that must survive process death and device restarts.

---

## Workers Overview

| Worker | Type | Schedule | Network Required |
|--------|------|----------|-----------------|
| `AttendanceSyncWorker` | `CoroutineWorker` | Periodic — once daily | ✅ Yes |
| `EventReminderWorker` | `CoroutineWorker` | One-shot — 1hr before event | ❌ No |

---

## 1. `AttendanceSyncWorker`

### Purpose
Pushes the local Room attendance snapshot to Firestore daily. This ensures the AI assistant and any future web dashboard have up-to-date attendance data.

### Schedule
```kotlin
// Scheduled in RVUApplication.kt on app startup
val syncRequest = PeriodicWorkRequestBuilder<AttendanceSyncWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.DAYS
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
    .addTag("attendance_sync")
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "AttendanceSyncWorker",
    ExistingPeriodicWorkPolicy.KEEP,   // Don't reschedule if already queued
    syncRequest
)
```

### Implementation

```kotlin
class AttendanceSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = RVUDatabase.getInstance(applicationContext)
            val firestore = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure()

            // 1. Get full attendance snapshot from Room
            val snapshot = db.attendanceDao().getSnapshot()

            // 2. Build the map to push
            val attendanceMap = snapshot.associate { entity ->
                entity.subjectCode to mapOf(
                    "subjectName" to entity.subjectName,
                    "attended" to entity.attended,
                    "total" to entity.total,
                    "percent" to entity.percentage
                )
            }

            // 3. Write to Firestore
            firestore.collection("users")
                .document(uid)
                .collection("attendance")
                .document("snapshot")
                .set(
                    mapOf(
                        "subjects" to attendanceMap,
                        "syncedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success()
        } catch (e: Exception) {
            // Retry up to 3 times with exponential backoff
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
```

---

## 2. `EventReminderWorker`

### Purpose
Fires a local notification exactly 1 hour before a registered campus event starts.

### Schedule
Scheduled when a user registers for an event:

```kotlin
// In EventRepository.kt, called after successful Firestore registration
fun scheduleEventReminder(event: Event) {
    val delay = event.date.toEpochMilli() - System.currentTimeMillis() - ONE_HOUR_MS

    if (delay <= 0) return   // Event is in less than 1 hour, skip

    val reminderRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(
            workDataOf(
                "eventId" to event.id,
                "eventTitle" to event.title,
                "eventVenue" to event.venue,
                "eventTime" to event.date.toEpochMilli()
            )
        )
        .addTag("event_reminder_${event.id}")
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "EventReminder_${event.id}",
        ExistingWorkPolicy.REPLACE,
        reminderRequest
    )
}
```

### Cancel on Unregister
```kotlin
fun cancelEventReminder(eventId: String) {
    WorkManager.getInstance(context)
        .cancelUniqueWork("EventReminder_$eventId")
}
```

### Implementation

```kotlin
class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventTitle = inputData.getString("eventTitle") ?: return Result.failure()
        val eventVenue = inputData.getString("eventVenue") ?: ""

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_event_notification)
            .setContentTitle("Upcoming Event: $eventTitle")
            .setContentText("Starts in 1 hour · $eventVenue")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(eventTitle.hashCode(), notification)

        return Result.success()
    }
}
```

---

## Notification Channels

Defined in `RVUApplication.onCreate()`:

```kotlin
fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_EVENTS,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming campus events you registered for"
            },
            NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Campus Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General announcements from RV University"
            },
            NotificationChannel(
                CHANNEL_CHAT,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New messages from peers and group chats"
            }
        )
        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
```

---

## WorkManager Monitoring

During development, inspect worker status:

```kotlin
WorkManager.getInstance(context)
    .getWorkInfosByTag("attendance_sync")
    .observe(lifecycleOwner) { workInfos ->
        workInfos.forEach { info ->
            Log.d("WorkManager", "State: ${info.state}, Attempt: ${info.runAttemptCount}")
        }
    }
```

Or use **Android Studio → App Inspection → Background Task Inspector** to visualize all scheduled workers.
