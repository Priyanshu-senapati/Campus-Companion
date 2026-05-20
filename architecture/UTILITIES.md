# 🛠️ Utility Classes Reference

All utility classes live in `app/utils/`. This document describes each one, its purpose, and usage examples.

---

## `Resource<T>` — State Wrapper

The central sealed class wrapping all async operation results.

```kotlin
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    object Empty : Resource<Nothing>()
}
```

**Usage in Repository:**
```kotlin
fun getEvents(): Flow<Resource<List<Event>>> = flow {
    emit(Resource.Loading)
    try {
        val events = firestore.collection("events").get().await()
            .documents.mapNotNull { it.toObject(Event::class.java) }
        if (events.isEmpty()) emit(Resource.Empty)
        else emit(Resource.Success(events))
    } catch (e: Exception) {
        emit(Resource.Error("Failed to load events: ${e.message}", e))
    }
}
```

---

## `UiStateHandler` — State-Driven UI

Drives shimmer → content → empty → error state transitions from a single call. Eliminates repetitive `when(resource)` blocks in every Fragment.

```kotlin
object UiStateHandler {
    fun <T> handle(
        resource: Resource<T>,
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View,
        errorView: View,
        onSuccess: (T) -> Unit
    ) {
        when (resource) {
            is Resource.Loading -> {
                shimmerView.show(); shimmerView.startShimmer()
                contentView.hide(); emptyView.hide(); errorView.hide()
            }
            is Resource.Success -> {
                shimmerView.stopShimmer(); shimmerView.hide()
                contentView.show(); emptyView.hide(); errorView.hide()
                onSuccess(resource.data)
            }
            is Resource.Empty -> {
                shimmerView.stopShimmer(); shimmerView.hide()
                contentView.hide(); emptyView.show(); errorView.hide()
            }
            is Resource.Error -> {
                shimmerView.stopShimmer(); shimmerView.hide()
                contentView.hide(); emptyView.hide(); errorView.show()
            }
        }
    }
}
```

---

## `InputValidator` — Form Validation

Centralised validation for all form fields. Returns `ValidationResult` (valid or error message).

```kotlin
object InputValidator {
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult.Error("Email is required")
        if (!email.endsWith("@rvu.edu.in")) return ValidationResult.Error("Must be an @rvu.edu.in email")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return ValidationResult.Error("Invalid email format")
        return ValidationResult.Valid
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.length < 8) return ValidationResult.Error("Min 8 characters")
        if (!password.any { it.isDigit() }) return ValidationResult.Error("Must contain a number")
        return ValidationResult.Valid
    }

    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) return ValidationResult.Error("Name is required")
        if (name.length < 2) return ValidationResult.Error("Name too short")
        return ValidationResult.Valid
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult.Valid  // optional
        if (!phone.matches(Regex("^[6-9]\\d{9}$"))) return ValidationResult.Error("Invalid Indian phone number")
        return ValidationResult.Valid
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

---

## `ConnectivityObserver` — Network Status

Flow-based network state observer. Survaps the lifecycle safely.

```kotlin
class ConnectivityObserver(context: Context) {
    enum class Status { Available, Lost, Unavailable }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus: Flow<Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(Status.Available) }
            override fun onLost(network: Network) { trySend(Status.Lost) }
            override fun onUnavailable() { trySend(Status.Unavailable) }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
```

**Usage in Fragment:**
```kotlin
lifecycleScope.launch {
    connectivityObserver.networkStatus.collect { status ->
        offlineBanner.isVisible = status != ConnectivityObserver.Status.Available
    }
}
```

---

## `SessionManager` — Login Persistence

Wraps `SharedPreferences` to persist login state across process death.

```kotlin
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("rvu_session", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit { putBoolean("is_logged_in", value) }

    var userId: String?
        get() = prefs.getString("user_id", null)
        set(value) = prefs.edit { putString("user_id", value) }

    var userEmail: String?
        get() = prefs.getString("user_email", null)
        set(value) = prefs.edit { putString("user_email", value) }

    fun clearSession() = prefs.edit { clear() }
}
```

---

## `LoadingDialog` — Full-Screen Loader

A Lottie-backed overlay that blocks interaction during async operations.

```kotlin
class LoadingDialog(private val context: Context) {
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_loading)  // contains LottieAnimationView
            .setCancelable(false)
            .create()
            .apply { window?.setBackgroundDrawableResource(android.R.color.transparent) }
    }

    fun show() { if (!dialog.isShowing) dialog.show() }
    fun dismiss() { if (dialog.isShowing) dialog.dismiss() }
}
```

---

## `ApiHelper` — Safe Retrofit Wrapper

Wraps Retrofit calls and maps to `Resource<T>`.

```kotlin
object ApiHelper {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Resource.Success(body)
                else Resource.Error("Empty response body")
            } else {
                Resource.Error("API error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: check your connection", e)
        } catch (e: HttpException) {
            Resource.Error("HTTP ${e.code()}: ${e.message()}", e)
        }
    }
}
```

---

## `Extensions.kt` — View & Data Extensions

```kotlin
// View visibility
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// Relative timestamps
fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(this))
    }
}

// Attendance color coding
fun Float.toAttendanceColor(context: Context): Int = when {
    this >= 75f -> ContextCompat.getColor(context, R.color.attendance_safe)    // green
    this >= 65f -> ContextCompat.getColor(context, R.color.attendance_warn)    // amber
    else -> ContextCompat.getColor(context, R.color.attendance_danger)         // red
}

// Safe attendance calculation
fun safeAttendancePercent(attended: Int, total: Int): Float =
    if (total == 0) 0f else (attended.toFloat() / total) * 100f

// Snackbar shortcuts
fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) =
    Snackbar.make(this, message, duration).show()

fun View.snackError(message: String) =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setBackgroundTint(ContextCompat.getColor(context, R.color.error))
        .show()

// Fragment nav shortcut
fun Fragment.navigate(action: Int, args: Bundle? = null) =
    findNavController().navigate(action, args)
```

---

## `ThemeConstants`

```kotlin
object ThemeConstants {
    const val ELEVATION_CARD = 4f         // dp
    const val ELEVATION_FAB = 8f
    const val RADIUS_CARD = 16f           // dp
    const val RADIUS_BOTTOM_SHEET = 24f
    const val ANIM_DURATION_SHORT = 200L  // ms
    const val ANIM_DURATION_MEDIUM = 350L
    const val ALPHA_DISABLED = 0.38f
    const val ALPHA_ENABLED = 1.0f
}
```
