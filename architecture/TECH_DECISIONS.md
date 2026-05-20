# 🤔 Technology Decisions

This document explains the "why" behind each major library and pattern choice in Campus Companion.

---

## Language: Kotlin

**Chosen over Java because:**
- Null safety reduces crashes — especially important for a student-facing production app
- Coroutines make async code readable (no callback hell with Firebase)
- Data classes eliminate boilerplate for model objects
- Extension functions keep utility code organized and readable
- `sealed class` makes `Resource<T>` state handling exhaustive and safe

---

## Architecture: MVVM + Repository

**Chosen over MVC / MVP because:**
- Jetpack ViewModels survive configuration changes (rotation) — no stale data bugs
- `LiveData` is lifecycle-aware — no "fragment not attached" crashes
- Repository pattern decouples data sources from UI — swap Room for SQLDelight or Firestore for Supabase without touching Fragments
- Google's recommended Android architecture — easier to onboard contributors

---

## Local DB: Room 2.6 + KSP

**Chosen over SQLite directly / Realm because:**
- Compile-time SQL verification — typos in queries fail at build time, not runtime
- KSP (Kotlin Symbol Processing) is faster than KAPT for code generation
- Native Kotlin Flow support in DAOs
- Seamless integration with the rest of Jetpack

**Why not DataStore only?**
DataStore is great for key-value preferences (like dark mode toggle) but not for structured relational data like timetable entries with filtering by day.

---

## Remote DB: Firebase Firestore

**Chosen over a custom REST backend because:**
- Real-time listeners for chat — no polling needed
- No server to maintain for a student project
- Offline persistence built-in (Firestore SDK caches reads)
- Firebase Auth + Firestore + Storage in one ecosystem reduces integration complexity

**Tradeoffs acknowledged:**
- Vendor lock-in to Google
- Querying flexibility is limited compared to SQL
- Cost scales with reads (fine for a university-scale app)

---

## Auth: Firebase Authentication

**Email/Password + Google Sign-In chosen because:**
- Google Sign-In is familiar to all students (they use Google Workspace for RVU)
- Email/Password needed for password reset flow
- Firebase Auth handles token refresh, session persistence, and security automatically

**Domain validation** (`@rvu.edu.in`) is done client-side in `InputValidator` and enforced via Firestore security rules — users cannot create accounts with personal Gmail addresses.

---

## Navigation: Jetpack Navigation Component + Safe Args

**Chosen over manual Fragment transactions because:**
- Visual navigation graph in Android Studio
- Back stack managed automatically
- Type-safe argument passing via Safe Args (no stringly-typed bundle keys)
- Deep link support ready for future use (roadmap item)

---

## AI: Gemini 2.5 Flash via REST

**Chosen over GPT-4 / Claude because:**
- Google AI Studio provides a free tier sufficient for a student app
- Gemini models are available in India without VPN requirements
- Flash variant has low latency — important for a chat-style UX
- REST API is simple to integrate via Retrofit without a special SDK

**Why not the Gemini Android SDK?**
The REST API gives more control over the request shape, especially for injecting the system prompt with student context. The SDK abstracts too much for this use case.

---

## Networking: Retrofit 2 + OkHttp 4

**Chosen because:**
- Industry standard for Android REST calls
- OkHttp interceptors make it easy to log requests during development
- Works seamlessly with Kotlin coroutines (`suspend` function support)
- `Response<T>` wrapper gives access to HTTP status codes for error handling

---

## Image Loading: Glide 4

**Chosen over Coil / Picasso because:**
- Mature and battle-tested
- Efficient memory and disk caching
- Supports GIFs (used in Lottie fallbacks) and circular crops out of the box
- Firebase Storage URLs work without any special configuration

---

## Animations: Lottie 6

**Chosen because:**
- JSON-based animations are smaller than GIFs and smoother than frame animations
- Used for: loading dialog overlay, empty state illustrations, splash screen, onboarding slides
- Designer-friendly — animators can export from After Effects directly

---

## Charts: MPAndroidChart

**Used for:** Attendance percentage visualization (bar chart per subject, trend line over time)

**Chosen because:**
- Most feature-complete Android charting library
- No internet required — pure local rendering
- Supports interaction (tap to see exact values)

---

## Background: WorkManager

**Chosen over AlarmManager / JobScheduler / plain coroutines because:**
- Guaranteed execution even after device restart (for attendance sync)
- Handles Doze mode and battery optimization automatically
- Built-in retry with backoff
- Single API works across all API levels (21+)
- `CoroutineWorker` integrates naturally with the coroutine-first codebase

---

## DI: Manual (ViewModelFactory)

**Chosen over Hilt / Dagger because:**
- Lower learning curve for new contributors
- No annotation processing overhead on top of KSP (already used for Room)
- The app's dependency graph is simple enough not to need a full DI framework
- Can be migrated to Hilt later without changing business logic (roadmap item)

---

## Build System: Gradle KTS

**Chosen over Groovy DSL because:**
- Type-safe — IDE autocomplete works in `build.gradle.kts`
- Compile-time errors instead of runtime Groovy errors
- Consistent with Kotlin-first approach throughout the project
