# RVU Campus Companion

A complete Android application for RV University, Bengaluru students.

## Features
1. Authentication (Email/Google Sign-In via Firebase)
2. Home Dashboard with quick actions
3. Timetable Management (Room DB)
4. Attendance Tracker
5. Campus Map (Google Maps SDK)
6. Events & Announcements (Firestore real-time)
7. Lost & Found (Firebase Storage + Firestore)
8. Mingo's Canteen Menu
9. Student Profile
10. Study Group Chat (Firestore real-time)

## Tech Stack
- Kotlin + MVVM + ViewBinding
- Firebase (Auth, Firestore, Storage, FCM)
- Room Database
- Retrofit2 + OkHttp3
- Glide, Material Design 3
- Google Maps SDK
- WorkManager, Coroutines, LiveData/StateFlow
- MPAndroidChart, Lottie

## Setup Instructions

### 1. Firebase Setup
1. Go to https://console.firebase.google.com/
2. Create new project named `rvu-campus-companion`
3. Add Android app with package: `com.rvu.campuscompanion`
4. Download `google-services.json` and place it in `app/` folder
5. Enable in Firebase Console:
   - Authentication > Email/Password + Google providers
   - Firestore Database (start in test mode for jury demo)
   - Storage
   - Cloud Messaging

### 2. Firestore Rules (Console > Firestore > Rules)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 3. Storage Rules
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

### 4. Google Maps API Key
1. Go to https://console.cloud.google.com/
2. Enable: Maps SDK for Android, Places API
3. Create API Key
4. Add to `local.properties`:
```
MAPS_API_KEY=your_api_key_here
```

### 5. Build & Run
- Open project in Android Studio (Hedgehog or newer)
- Sync Gradle
- Run on emulator or physical device (min SDK 26)

### 6. Demo Login
After registration, use any `@rvu.edu.in` email. The app pre-populates timetable, canteen menu, and campus locations on first launch.

## Project Structure
See `app/src/main/java/com/rvu/campuscompanion/` for organized MVVM packages:
- `data/` - models, repositories, Room DB, Firebase services
- `ui/` - fragments grouped by feature
- `viewmodel/` - all ViewModels
- `utils/` - extensions, constants, helpers
- `notifications/` - FCM service
- `workers/` - WorkManager background tasks
