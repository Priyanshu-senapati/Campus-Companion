# 📡 API Reference

All external API calls made by Campus Companion — endpoints, request shapes, response shapes, and error handling.

---

## 1. Gemini API

**Base URL:** `https://generativelanguage.googleapis.com/v1beta/`

**Auth:** Query parameter `?key=GEMINI_API_KEY`

### `POST /models/gemini-2.5-flash:generateContent`

Generates a response from the AI assistant with full student context.

**Request:**
```json
{
  "system_instruction": {
    "parts": [{ "text": "<system prompt with student context>" }]
  },
  "contents": [
    { "role": "user", "parts": [{ "text": "Can I skip tomorrow's lab?" }] },
    { "role": "model", "parts": [{ "text": "Based on your 71% attendance..." }] },
    { "role": "user", "parts": [{ "text": "What's my weakest subject?" }] }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 1024,
    "topP": 0.95
  }
}
```

**Response:**
```json
{
  "candidates": [
    {
      "content": {
        "role": "model",
        "parts": [{ "text": "Your weakest subject by attendance is..." }]
      },
      "finishReason": "STOP",
      "index": 0
    }
  ],
  "usageMetadata": {
    "promptTokenCount": 312,
    "candidatesTokenCount": 89,
    "totalTokenCount": 401
  }
}
```

**Error responses:**

| HTTP Code | Meaning | App behaviour |
|-----------|---------|---------------|
| 400 | Bad request (malformed JSON, token limit exceeded) | Trim history, retry |
| 401 | Invalid API key | Show "AI unavailable" |
| 403 | Quota exceeded or billing issue | Show "AI unavailable" |
| 429 | Rate limit hit | Retry after 1 minute with backoff |
| 500 | Gemini server error | Retry once, then show error |

---

## 2. Google Maps SDK (Android)

Not a REST API — uses the Maps SDK for Android directly.

### Key SDK calls used:

```kotlin
// Initialize map
val mapFragment = SupportMapFragment.newInstance()
mapFragment.getMapAsync { googleMap ->
    this.googleMap = googleMap
    setupMap(googleMap)
}

// Add a campus building marker
googleMap.addMarker(
    MarkerOptions()
        .position(LatLng(12.9237, 77.4987))  // RVU coordinates
        .title("Main Academic Block")
        .snippet("Tap for directions")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building_marker))
)

// Enable user location dot
if (hasLocationPermission()) {
    googleMap.isMyLocationEnabled = true
}

// Camera to campus center
googleMap.animateCamera(
    CameraUpdateFactory.newLatLngZoom(
        LatLng(12.9237, 77.4987),  // RVU campus center
        17f                         // zoom level
    )
)
```

### Places API (for search):
```kotlin
val placesClient = Places.createClient(context)
val request = FindAutocompletePredictionsRequest.builder()
    .setQuery(query)
    .setLocationBias(RectangularBounds.newInstance(rvuSouthWest, rvuNorthEast))
    .build()
placesClient.findAutocompletePredictions(request)
```

---

## 3. Firebase REST APIs (via SDK)

Firebase Firestore, Auth, and Storage are accessed via their Android SDKs, not raw HTTP. Key operations:

### Firestore Reads

```kotlin
// Get a single document
firestore.collection("users").document(uid).get().await()

// Get a collection with filter
firestore.collection("events")
    .whereEqualTo("category", "Technical")
    .whereGreaterThan("date", Timestamp.now())
    .orderBy("date", Query.Direction.ASCENDING)
    .get().await()

// Real-time listener (chat messages)
firestore.collection("chats").document(chatId)
    .collection("messages")
    .orderBy("sentAt", Query.Direction.ASCENDING)
    .addSnapshotListener { snapshot, error -> ... }
```

### Firestore Writes

```kotlin
// Create/overwrite
firestore.collection("users").document(uid).set(user).await()

// Partial update
firestore.collection("users").document(uid)
    .update(mapOf("name" to newName, "semester" to newSemester)).await()

// Array union (register for event)
firestore.collection("events").document(eventId)
    .update("registeredBy", FieldValue.arrayUnion(uid)).await()

// Array remove (cancel registration)
firestore.collection("events").document(eventId)
    .update("registeredBy", FieldValue.arrayRemove(uid)).await()
```

### Firebase Auth

```kotlin
// Email sign-in
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword(email, password).await()

// Google sign-in
val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
FirebaseAuth.getInstance().signInWithCredential(credential).await()

// Password reset
FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
```

### Firebase Storage

```kotlin
// Upload
val ref = FirebaseStorage.getInstance().reference
    .child("profile_photos/$uid.jpg")
val uploadTask = ref.putBytes(compressedBytes).await()
val downloadUrl = ref.downloadUrl.await().toString()

// Get URL of existing file
val url = FirebaseStorage.getInstance().reference
    .child("profile_photos/$uid.jpg")
    .downloadUrl.await().toString()
```

---

## 4. FCM (Firebase Cloud Messaging)

Push notifications are sent server-side (from Firebase Console or a Cloud Function) to device FCM tokens. The app only needs to:

### Receive and handle messages:

```kotlin
// notifications/FCMService.kt
class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Update token in Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .update("fcmToken", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: return
        val body = remoteMessage.notification?.body ?: ""
        val channel = remoteMessage.data["channel"] ?: CHANNEL_ANNOUNCEMENTS

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

### FCM Payload shape (sent from server):
```json
{
  "to": "<device_fcm_token>",
  "notification": {
    "title": "New Announcement",
    "body": "Mid-semester exams begin next week"
  },
  "data": {
    "channel": "campus_announcements",
    "targetId": "announcement_abc123"
  }
}
```

---

## Retrofit Client Setup

```kotlin
object RetrofitClient {
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = if (BuildConfig.DEBUG)
                            HttpLoggingInterceptor.Level.BODY
                        else
                            HttpLoggingInterceptor.Level.NONE
                    })
                    .build()
            )
            .build()
            .create(GeminiApiService::class.java)
    }
}
```
