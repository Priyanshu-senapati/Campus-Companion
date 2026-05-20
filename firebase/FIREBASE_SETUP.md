# 🔥 Firebase Setup Guide

Step-by-step instructions to configure a Firebase project for Campus Companion from scratch.

---

## Step 1: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project**
3. Name it `Campus-Companion` (or any name)
4. Disable Google Analytics (optional for dev)
5. Click **Create project**

---

## Step 2: Register Android App

1. In the Firebase Console, click the **Android icon** (➕ Add app)
2. Enter package name: `com.rvu.campuscompanion` (match your `applicationId` in `build.gradle.kts`)
3. App nickname: `Campus Companion`
4. Debug signing certificate SHA-1 (required for Google Sign-In):

```bash
# Run this in your project root to get the debug SHA-1
./gradlew signingReport

# Or via keytool:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

5. Click **Register app**
6. Download `google-services.json` → place at `app/google-services.json`

---

## Step 3: Enable Authentication

1. Firebase Console → **Authentication** → **Get started**
2. **Sign-in method** tab → Enable:
   - **Email/Password** → Enable → Save
   - **Google** → Enable → set support email → Save
3. **Authorized domains** tab — `localhost` is already there (fine for dev)

---

## Step 4: Set Up Firestore

1. Firebase Console → **Firestore Database** → **Create database**
2. Choose **Start in test mode** (allows all reads/writes for 30 days)
3. Select a region: `asia-south1` (Mumbai — closest to Bengaluru)
4. Click **Done**

### Apply Security Rules
Go to **Firestore → Rules** tab and replace with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
      match /attendance/{doc} {
        allow read, write: if request.auth.uid == uid;
      }
    }
    match /events/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    match /announcements/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    match /lostfound/{doc} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.resource.data.postedBy == request.auth.uid;
      allow update, delete: if request.auth.uid == resource.data.postedBy;
    }
    match /chats/{chatId}/messages/{msgId} {
      allow read, write: if request.auth != null;
    }
    match /groups/{groupId}/messages/{msgId} {
      allow read, write: if request.auth != null;
    }
    match /canteen/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

Click **Publish**.

### Create Firestore Indexes
Some queries require composite indexes. Create these manually:

| Collection | Fields | Order |
|------------|--------|-------|
| `events` | `category ASC`, `date ASC` | — |
| `events` | `isActive ASC`, `date ASC` | — |
| `lostfound` | `type ASC`, `status ASC`, `postedAt DESC` | — |

Go to **Firestore → Indexes → Composite** → **Add index** for each.

---

## Step 5: Set Up Firebase Storage

1. Firebase Console → **Storage** → **Get started**
2. Start in test mode (update rules later)
3. Same region as Firestore: `asia-south1`

### Storage Rules
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_photos/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == uid;
    }
    match /lost_found/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    match /events/{fileName} {
      allow read: if request.auth != null;
      allow write: if false;  // admin only
    }
  }
}
```

---

## Step 6: Set Up FCM (Push Notifications)

FCM is enabled by default when you create a Firebase project. Nothing to configure manually.

To send a test notification:
1. Firebase Console → **Messaging** → **Send your first message**
2. Enter notification title and text
3. Target: **Single device** → paste the FCM token logged in Logcat when app first launches

### Get FCM Token in App
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "Token: $token")
        // Save to Firestore: users/{uid}.fcmToken = token
    }
}
```

---

## Step 7: Configure API Keys

Create or edit `local.properties` in your project root (this file is git-ignored):

```properties
sdk.dir=/Users/yourname/Library/Android/sdk

MAPS_API_KEY=AIza...yourkey...
GEMINI_API_KEY=AIza...yourkey...
```

### Get Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Same project linked to Firebase (or create a new one)
3. APIs & Services → **Enable APIs** → Enable:
   - **Maps SDK for Android**
   - **Places API**
4. APIs & Services → **Credentials** → **Create Credentials** → API Key
5. Restrict key to **Android apps** → add your package name + SHA-1

### Get Gemini API Key
1. Go to [Google AI Studio](https://aistudio.google.com/)
2. Sign in with your Google account
3. **Get API key** → **Create API key** → select your Cloud project
4. Copy the key to `local.properties`

---

## Step 8: Verify Everything

Build the project:
```bash
./gradlew assembleDebug
```

Test checklist:
- [ ] App launches without crash
- [ ] Login with email/password works
- [ ] Google Sign-In works (needs SHA-1 registered)
- [ ] Firestore reads work (check Logcat for no permission errors)
- [ ] Profile photo upload to Storage works
- [ ] AI assistant responds (check `GEMINI_API_KEY` is correct)
- [ ] Campus map loads (check `MAPS_API_KEY` is correct)
- [ ] FCM token is logged on launch

---

## Common Errors

| Error | Fix |
|-------|-----|
| `google-services.json` not found | Place it at `app/google-services.json` exactly |
| `FirebaseException: PERMISSION_DENIED` | Check Firestore security rules — likely test mode expired |
| Google Sign-In fails silently | SHA-1 fingerprint not registered in Firebase Console |
| Maps shows grey tiles | Maps API key not set or wrong package name restriction |
| Gemini returns 400 | Check prompt length — may exceed token limit |
| Gemini returns 403 | API key invalid or billing not enabled |
