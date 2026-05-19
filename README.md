<div align="center">

# 🎓 Campus Companion

### The all-in-one student companion app for RV University, Bengaluru

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![API](https://img.shields.io/badge/Min%20SDK-26-brightgreen)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📱 Overview

Campus Companion is a native Android application built for students of **RV University (RVU), Bengaluru**. It consolidates everything a student needs — timetable, attendance tracking, campus events, canteen menu, lost & found, peer chat, campus map, and an AI-powered assistant — into a single, cohesive experience.

> Built with Kotlin, MVVM architecture, Firebase, Room, Retrofit, and Gemini AI.

---

## ✨ Features

| Module | Description |
|---|---|
| 🔐 **Auth** | Email/password login with RVU domain validation, Google Sign-In, password reset |
| 🏠 **Dashboard** | Personalized home with today's classes, attendance summary, announcements, and motivational quotes |
| 📅 **Timetable** | Day-wise class schedule with add/edit/delete, stored locally in Room |
| ✅ **Attendance** | Per-subject attendance tracking with percentage, color-coded warnings, and daily sync to Firestore |
| 📢 **Events** | Browse and register for campus events (Technical, Cultural, Sports, Academic) with event reminders via WorkManager |
| 🍽️ **Canteen** | Daily menu with ratings, feedback submission, and offline-first Room caching |
| 🔍 **Lost & Found** | Post and browse lost/found items with image upload, contact info, and status tracking |
| 💬 **Chat** | Real-time peer messaging and group chat powered by Firestore |
| 🗺️ **Campus Map** | Interactive Google Maps view of all RVU buildings, labs, and facilities |
| 🤖 **AI Assistant** | Gemini 2.5 Flash-powered chatbot with full student context (attendance, timetable, branch, semester) |
| 👤 **Profile** | Edit profile, photo upload to Firebase Storage, dark mode toggle |
| 🔔 **Notifications** | FCM push notifications for events and reminders via custom notification channels |

---

## 🏗️ Architecture

Campus Companion follows **MVVM (Model-View-ViewModel)** with a clean repository pattern.

```
UI Layer          →  Fragments + ViewBinding
ViewModel Layer   →  AndroidViewModel + LiveData + Coroutines
Repository Layer  →  Single source of truth (Room + Firestore + Retrofit)
Data Layer        →  Room (local) · Firebase (remote) · Retrofit/Gemini (API)
```

```
app/
├── adapter/              # RecyclerView & ViewPager2 adapters
├── data/
│   ├── local/            # Room database, DAOs, entities, seed data
│   ├── model/            # Kotlin data classes (User, Event, LostFoundItem …)
│   ├── remote/           # Retrofit client, Gemini API, Firebase source
│   └── repository/       # One repository per feature domain
├── notifications/        # FCM messaging service
├── ui/
│   ├── auth/             # Login, Register, Splash
│   ├── attendance/       # Attendance list, detail, mark dialog
│   ├── canteen/          # Menu, detail, feedback
│   ├── chat/             # Direct & group chat
│   ├── common/           # Reusable custom views (CampusToolbar)
│   ├── events/           # Events list & detail
│   ├── home/             # Dashboard, More
│   ├── lostfound/        # Lost & Found list, detail, post sheet
│   ├── map/              # Google Maps fragment, location sheet
│   ├── onboarding/       # Onboarding ViewPager2 slides
│   ├── profile/          # Profile view & edit
│   └── timetable/        # Timetable pager, day page, add class sheet
├── utils/                # Extensions, validators, constants, helpers
├── viewmodel/            # One ViewModel per feature + shared factory
├── workers/              # WorkManager workers (attendance sync, event reminders)
├── MainActivity.kt       # Single-activity host with bottom navigation
└── RVUApplication.kt     # App-level init (Firebase, WorkManager, theme)
```

---

## 🛠️ Tech Stack

| Category | Library / Tool |
|---|---|
| Language | Kotlin 1.9 |
| UI | Material Design 3, ViewBinding, ConstraintLayout, ViewPager2 |
| Navigation | Jetpack Navigation Component + Safe Args |
| Architecture | MVVM, LiveData, ViewModel, SavedState |
| Local DB | Room 2.6 + KSP |
| Remote DB | Firebase Firestore |
| Auth | Firebase Authentication (Email + Google) |
| Storage | Firebase Storage |
| Messaging | Firebase Cloud Messaging (FCM) |
| Networking | Retrofit 2 + OkHttp 4 + Gson |
| AI | Google Gemini 2.5 Flash via REST API |
| Maps | Google Maps SDK + Places API |
| Image Loading | Glide 4 |
| Animations | Lottie 6, Shimmer |
| Background | WorkManager (Coroutine workers) |
| Charts | MPAndroidChart |
| Async | Kotlin Coroutines + Flow |
| DI | Manual (ViewModelFactory) |
| Build | Gradle KTS, KSP |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- A Firebase project with **Authentication**, **Firestore**, **Storage**, and **FCM** enabled
- Google Maps API key
- Google Gemini API key

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/<your-username>/Campus-Companion.git
cd Campus-Companion
```

**2. Add `google-services.json`**

Download your `google-services.json` from the Firebase Console and place it at:
```
app/google-services.json
```

**3. Configure API keys**

Add the following to your `local.properties` (never commit this file):
```properties
MAPS_API_KEY=your_google_maps_api_key_here
GEMINI_API_KEY=your_gemini_api_key_here
```

**4. Firebase setup**

Enable the following in your Firebase Console:
- Authentication → Email/Password + Google Sign-In
- Firestore Database → Start in test mode, then apply security rules
- Storage → Default bucket
- Cloud Messaging → Enabled by default

**5. Build and run**
```bash
./gradlew assembleDebug
```
Or press **Run ▶** in Android Studio.

---

## 🔐 Firebase Security Rules

Firestore rules (recommended starting point):

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
    match /events/{doc} {
      allow read: if request.auth != null;
      allow write: if false; // admin only
    }
    match /announcements/{doc} {
      allow read: if request.auth != null;
    }
    match /lostfound/{doc} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    match /chats/{chatId}/messages/{msgId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## 📸 Screenshots

> _Add screenshots here once the app UI is finalized._

| Splash / Login | Dashboard | Timetable |
|---|---|---|
| _coming soon_ | _coming soon_ | _coming soon_ |

| Attendance | Events | AI Assistant |
|---|---|---|
| _coming soon_ | _coming soon_ | _coming soon_ |

---

## 🤖 AI Assistant

The built-in assistant is powered by **Gemini 2.5 Flash**. On every query it automatically injects:

- Student name, branch, and semester
- Today's timetable entries
- Per-subject attendance percentages
- RVU-specific context (75% attendance rule, campus info)

This gives the model enough context to answer questions like:
> *"Can I skip tomorrow's lab?"*
> *"What's my weakest subject this semester?"*
> *"Summarise today's schedule for me."*

---

## 📦 Key Utility Classes

| Class | Purpose |
|---|---|
| `Resource<T>` | Sealed class wrapping Loading / Success / Error / Empty states |
| `UiStateHandler` | Drives shimmer → content → empty → error transitions from one call |
| `InputValidator` | Centralised form validation (email, password, name, phone) |
| `ConnectivityObserver` | Flow-based network status observer (Available / Lost / Unavailable) |
| `SessionManager` | Persists login session across process death |
| `LoadingDialog` | Lottie-backed full-screen loading overlay |
| `ThemeConstants` | Single source for elevation, radius, duration, alpha constants |
| `ApiHelper` | Retrofit safe-call wrapper mapping responses to `Resource<T>` |
| `Extensions.kt` | `View.show/hide`, `Long.toRelativeTime`, `Int.toAttendanceColor`, etc. |

---

## 🔄 Background Work

| Worker | Schedule | Purpose |
|---|---|---|
| `AttendanceSyncWorker` | Daily, requires network | Pushes local attendance snapshot to Firestore |
| `EventReminderWorker` | One-shot, 1 hr before event | Fires a local notification for registered events |

---

## 📋 Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | All network calls |
| `ACCESS_NETWORK_STATE` | Connectivity checks |
| `ACCESS_FINE_LOCATION` | Campus map user location |
| `CAMERA` | Profile photo capture |
| `READ_MEDIA_IMAGES` | Profile photo picker (API 33+) |
| `POST_NOTIFICATIONS` | Event and reminder notifications (API 33+) |

---

## 🗺️ Roadmap

- [ ] Admin panel for posting events and announcements
- [ ] Offline-first events and announcements with Room cache
- [ ] Biometric login support
- [ ] Widget for today's timetable
- [ ] Deep links for event sharing
- [ ] Tablet / large-screen layout support

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feat/your-feature`
5. Open a Pull Request

Please follow the existing code style — Kotlin idioms, MVVM separation, and no business logic in Fragments.

---

## 📄 License

```
MIT License

Copyright (c) 2026 RVU Campus Companion Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<div align="center">

Built with ❤️ for RV University students

</div>
