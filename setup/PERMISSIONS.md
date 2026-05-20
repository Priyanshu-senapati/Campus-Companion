# 📋 Android Permissions

This document explains every permission used in Campus Companion, why it's needed, when it's requested, and how to handle denial.

---

## Permissions Summary

| Permission | Type | When Requested | Feature |
|------------|------|----------------|---------|
| `INTERNET` | Normal | Automatic (no prompt) | All network calls |
| `ACCESS_NETWORK_STATE` | Normal | Automatic | Connectivity check |
| `ACCESS_FINE_LOCATION` | Dangerous | On campus map open | User location on map |
| `CAMERA` | Dangerous | On profile photo capture | Profile photo |
| `READ_MEDIA_IMAGES` | Dangerous (API 33+) | On profile photo gallery pick | Profile photo + lost & found |
| `READ_EXTERNAL_STORAGE` | Dangerous (API <33) | On profile photo gallery pick | Profile photo + lost & found |
| `POST_NOTIFICATIONS` | Dangerous (API 33+) | On first launch | Event reminders, FCM |

---

## Normal Permissions (Granted Automatically)

These are declared in `AndroidManifest.xml` and granted at install time — no runtime prompt:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## Dangerous Permissions (Runtime Request Required)

### 1. `ACCESS_FINE_LOCATION`

**Why:** Google Maps SDK needs the user's precise location to show the blue dot on the campus map and calculate distance to buildings.

**When requested:** When the user navigates to the Map screen for the first time.

**Implementation:**
```kotlin
// MapFragment.kt
private val locationPermissionRequest = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) enableUserLocation()
    else showLocationDeniedBanner()
}

override fun onViewCreated(...) {
    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
        enableUserLocation()
    } else {
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
```

**If denied:** Show a banner: *"Enable location to see your position on campus."* Map still loads with building markers — location dot is just hidden.

---

### 2. `CAMERA`

**Why:** Users can take a photo directly with the camera for their profile picture.

**When requested:** When the user taps "Take photo" in the profile photo options dialog.

**Implementation:**
```kotlin
private val cameraPermissionRequest = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) launchCamera()
    else Toast.makeText(requireContext(), "Camera permission needed for photo", Toast.LENGTH_SHORT).show()
}
```

**If denied:** The gallery option still works. Camera option is greyed out with explanation.

---

### 3. `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`

**Why:** To pick an existing image from the gallery for profile photo or lost & found post image.

**API level handling:**
```kotlin
val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES   // API 33+
} else {
    Manifest.permission.READ_EXTERNAL_STORAGE  // API < 33
}
```

**AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"
    android:minSdkVersion="33" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

**If denied:** User can still type a URL manually (not ideal — may be removed in future UX pass).

---

### 4. `POST_NOTIFICATIONS` (API 33+)

**Why:** Required on Android 13+ to show push notifications for event reminders and FCM messages.

**When requested:** On first app launch, after onboarding, using a rationale dialog:
*"Allow notifications to get reminders for events you register for."*

**Implementation:**
```kotlin
// In MainActivity or SplashFragment, after onboarding
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
    }
}
```

**If denied:** App works fully. Event reminders and FCM notifications silently fail. No crash. User can enable later in Android Settings → Apps → Campus Companion → Notifications.

---

## Rationale Dialog Best Practices

For any permission where `shouldShowRequestPermissionRationale()` returns `true` (user denied once before):

```kotlin
if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
    // Show a dialog explaining WHY before requesting again
    showPermissionRationaleDialog(
        title = "Location needed",
        message = "We need your location to show you where you are on the campus map.",
        onConfirm = { locationPermissionRequest.launch(...) },
        onDismiss = { showLocationDeniedBanner() }
    )
} else {
    locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
}
```

If the user selects "Don't ask again", `shouldShowRequestPermissionRationale()` returns `false` and launching the request does nothing. In this case, guide them to Settings:

```kotlin
val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
    data = Uri.fromParts("package", requireContext().packageName, null)
}
startActivity(intent)
```
