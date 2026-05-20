# 🗺️ Detailed Roadmap

This document expands the README roadmap into actionable tasks with implementation notes.

---

## Priority Levels
- 🔴 **High** — core functionality, many users impacted
- 🟡 **Medium** — improves experience significantly
- 🟢 **Low** — nice to have, polish

---

## 1. 🔴 Admin Panel for Events and Announcements

**Problem:** Currently events and announcements can only be added directly via Firestore Console, which is unusable for non-technical admins.

**Solution:** A separate admin web app (or a hidden admin section in the Android app behind a role flag).

**Implementation plan:**
- Add `role: "admin" | "student"` field to `users/{uid}` Firestore document
- In-app: show admin FAB on Events/Announcements screens if `user.role == "admin"`
- Admin bottom sheet: title, description, category, venue, date picker, image upload
- Write to `events/` or `announcements/` collection (currently blocked for all users)

**Files to create/modify:**
- `ui/admin/AdminEventSheet.kt`
- `viewmodel/AdminViewModel.kt`
- `data/repository/AdminRepository.kt`
- Firestore rules: add admin write permission check

---

## 2. 🔴 Offline-First Events and Announcements

**Problem:** Events and Announcements currently load only from Firestore. No internet = empty screen.

**Solution:** Cache events and announcements in Room, same pattern as canteen.

**Implementation plan:**
- `EventCacheEntity` already defined in Room schema (partially done)
- Create `AnnouncementEntity` and `AnnouncementDao`
- In `EventRepository`: emit cached data first, then fetch Firestore, update cache
- Add cache invalidation: evict events older than 7 days

**Estimated effort:** ~1 day

---

## 3. 🟡 Biometric Login

**Problem:** Students must enter email/password on every launch if they clear session.

**Solution:** Add fingerprint/face unlock using `BiometricPrompt` API after initial login.

**Implementation plan:**
```kotlin
// After successful first login, offer to enable biometric
val biometricManager = BiometricManager.from(context)
if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS) {
    // Show "Enable fingerprint login?" dialog
    // Store encrypted credentials in EncryptedSharedPreferences
}

// On subsequent launches, show BiometricPrompt before restoring session
```

**Files to create:**
- `utils/BiometricHelper.kt`
- `ui/auth/BiometricPromptManager.kt`
- Update `SessionManager` to store encrypted session

**Permissions needed:** `USE_BIOMETRIC`

---

## 4. 🟡 Home Screen Widget — Today's Timetable

**Problem:** Students have to open the app to check their schedule.

**Solution:** An Android App Widget showing today's next 2-3 classes.

**Implementation plan:**
- Create `TimetableWidget` extending `AppWidgetProvider`
- Widget layout: `widget_timetable.xml` — shows subject name, time, room for next 3 classes
- `TimetableWidgetWorker` — updates widget every hour via WorkManager
- Widget taps deep-link to TimetableFragment

**Files to create:**
- `widget/TimetableWidget.kt`
- `widget/TimetableWidgetWorker.kt`
- `res/layout/widget_timetable.xml`
- `res/xml/timetable_widget_info.xml`
- Register in `AndroidManifest.xml`

---

## 5. 🟡 Deep Links for Event Sharing

**Problem:** Students cannot share a specific event with a friend who doesn't have the app.

**Solution:** Implement Firebase Dynamic Links (or App Links) for event detail pages.

**Implementation plan:**
- URL scheme: `https://campuscompanion.rvu.edu.in/event/{eventId}`
- In `AndroidManifest.xml`: add intent filter for the scheme
- In `MainActivity`: parse incoming deep link and navigate to `EventDetailFragment` with `eventId`
- Firebase Dynamic Links: handle the case where app is not installed — redirect to Play Store

**Example:**
```kotlin
// In MainActivity.onNewIntent
val link = intent.data
if (link?.pathSegments?.firstOrNull() == "event") {
    val eventId = link.pathSegments[1]
    findNavController(R.id.nav_host).navigate(
        R.id.eventDetailFragment,
        bundleOf("eventId" to eventId)
    )
}
```

---

## 6. 🟢 Tablet / Large-Screen Layout

**Problem:** On tablets, the single-column layout looks stretched and wastes screen space.

**Solution:** Two-pane layout for tablets using `SlidingPaneLayout` or `WindowSizeClass` from Jetpack.

**Implementation plan:**
- Use `WindowSizeClass` to detect screen width category (Compact / Medium / Expanded)
- For Expanded (tablets): master-detail layout for Events, Lost & Found, Chat
- Create `layout-w600dp/` resource folder with tablet-specific layouts
- No logic changes needed in ViewModels — only layout XML changes

---

## 7. 🟢 Hilt Dependency Injection Migration

**Problem:** Manual ViewModelFactory requires boilerplate for every new ViewModel.

**Solution:** Migrate to Hilt for automatic DI.

**Implementation plan:**
- Add Hilt dependencies to `build.gradle.kts`
- Annotate `RVUApplication` with `@HiltAndroidApp`
- Replace `ViewModelFactory` with `@HiltViewModel` + `@Inject constructor`
- Replace manual repository instantiation with `@Singleton` scoped Hilt modules

**Effort:** Medium — affects every ViewModel and their constructors, but purely mechanical.

---

## 8. 🟢 Canteen Feedback Aggregation

**Problem:** Users can submit ratings for canteen items, but ratings are not aggregated back to Firestore (currently stored only locally or lost).

**Solution:** Cloud Function triggered on new rating submission to update `canteen/{date}/items[].rating` atomically.

**Implementation plan:**
- Firebase Cloud Function: `onWrite` trigger on a new `ratings/{uid}_{itemId}` document
- Computes new average using Firestore `FieldValue.increment()` and `ratingCount`
- Students see live-updated ratings without needing to fetch all individual ratings

---

## Known Technical Debt

| Issue | Impact | Fix |
|-------|--------|-----|
| `fallbackToDestructiveMigration()` in Room | Data loss on schema change | Write proper `Migration` objects before v2 |
| FCM token not refreshed on token rotation | Push notifications stop working | Implement `onNewToken` in `FCMService` and update Firestore |
| No pagination on Lost & Found / Events lists | Performance degrades with many items | Add Firestore cursor-based pagination with Paging 3 |
| No image compression before upload | Large profile photos slow upload | Add Glide or BitmapFactory compression before Storage upload |
| Chat has no read receipts UI | UX gap | Track `readBy` array already exists in schema — just render it |
