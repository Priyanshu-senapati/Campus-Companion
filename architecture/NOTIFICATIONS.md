# 🔔 Notifications

Campus Companion uses two notification systems: **Firebase Cloud Messaging (FCM)** for server-sent push notifications, and **local notifications** fired by `EventReminderWorker`.

---

## Notification Channels

Three channels, defined in `RVUApplication.onCreate()`:

| Channel ID | Name | Importance | Used For |
|------------|------|------------|----------|
| `campus_events` | Event Reminders | HIGH | 1-hour event reminders from WorkManager |
| `campus_announcements` | Campus Announcements | DEFAULT | FCM announcements from admin |
| `campus_chat` | Messages | HIGH | FCM new message notifications from chat |

```kotlin
const val CHANNEL_EVENTS = "campus_events"
const val CHANNEL_ANNOUNCEMENTS = "campus_announcements"
const val CHANNEL_CHAT = "campus_chat"
```

---

## Channel Setup Code

```kotlin
// RVUApplication.kt
private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannels(listOf(
            NotificationChannel(CHANNEL_EVENTS, "Event Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for campus events you've registered for (1 hour before)"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            },
            NotificationChannel(CHANNEL_ANNOUNCEMENTS, "Campus Announcements", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "General announcements from RV University administration"
            },
            NotificationChannel(CHANNEL_CHAT, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "New messages from peers and group chats"
                enableVibration(true)
            }
        ))
    }
}
```

---

## Local Notifications (WorkManager)

Fired by `EventReminderWorker` — no internet required.

```kotlin
private fun buildEventReminderNotification(title: String, venue: String): Notification {
    // Deep link intent to EventDetailFragment
    val intent = Intent(applicationContext, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("destination", "event_detail")
        putExtra("eventId", inputData.getString("eventId"))
    }

    val pendingIntent = PendingIntent.getActivity(
        applicationContext, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(applicationContext, CHANNEL_EVENTS)
        .setSmallIcon(R.drawable.ic_event_notification)
        .setContentTitle("Event in 1 Hour: $title")
        .setContentText("📍 $venue — get ready!")
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("Your registered event \"$title\" starts in 1 hour at $venue."))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()
}
```

---

## FCM Push Notifications

### Manifest Registration

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".notifications.FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### FCM Token Lifecycle

```kotlin
class FCMService : FirebaseMessagingService() {

    // Called when FCM issues a new token (on install, or token rotation)
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to save token: ${e.message}")
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: ""
        val channel = message.data["channel"] ?: CHANNEL_ANNOUNCEMENTS

        showNotification(title, body, channel)
    }

    private fun showNotification(title: String, body: String, channel: String) {
        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
```

---

## Notification Permission (Android 13+)

```kotlin
// Request in MainActivity after onboarding
private fun requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED -> {
                // Already granted — nothing to do
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale dialog
                showNotificationRationaleDialog()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}
```

---

## Testing Notifications

### Test FCM from Firebase Console
1. Firebase Console → **Messaging** → **Send your first message**
2. Notification text → Title: "Test", Body: "Hello from admin"
3. Target → **Single device** → paste FCM token from Logcat
4. Send

### Test Local Notification (EventReminderWorker)
Use WorkManager's test helpers to trigger immediately:

```kotlin
// In a debug activity or test
val testRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
    .setInputData(workDataOf(
        "eventTitle" to "Test Event",
        "eventVenue" to "Main Auditorium",
        "eventId" to "test123"
    ))
    .build()

WorkManager.getInstance(context).enqueue(testRequest)
```

Or use **Android Studio → App Inspection → Background Task Inspector** → find the worker → **Run now**.
