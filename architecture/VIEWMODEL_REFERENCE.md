# 📱 ViewModel Reference

Every ViewModel in Campus Companion, its responsibilities, exposed LiveData, and key methods.

---

## ViewModelFactory

All ViewModels are created via a shared factory to support manual dependency injection:

```kotlin
class ViewModelFactory(
    private val application: Application,
    private val repository: Any   // typed per ViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) ->
                AttendanceViewModel(application, repository as AttendanceRepository) as T
            modelClass.isAssignableFrom(EventViewModel::class.java) ->
                EventViewModel(application, repository as EventRepository) as T
            // ... etc
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
```

---

## `AuthViewModel`

**Scope:** `LoginFragment`, `RegisterFragment`

```kotlin
class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    val loginState: LiveData<Resource<User>> = _loginState
    val registerState: LiveData<Resource<User>> = _registerState
    val resetPasswordState: LiveData<Resource<Unit>> = _resetState

    fun loginWithEmail(email: String, password: String)
    fun loginWithGoogle(idToken: String)
    fun register(name: String, email: String, password: String, branch: String, semester: Int, rollNumber: String)
    fun sendPasswordReset(email: String)
    fun signOut()
}
```

---

## `DashboardViewModel`

**Scope:** `DashboardFragment`

```kotlin
class DashboardViewModel(
    application: Application,
    private val timetableRepo: TimetableRepository,
    private val attendanceRepo: AttendanceRepository,
    private val eventRepo: EventRepository,
    private val profileRepo: ProfileRepository
) : AndroidViewModel(application) {

    val todayClasses: LiveData<List<TimetableEntry>>        // today's schedule
    val attendanceSummary: LiveData<AttendanceSummary>       // overall % across subjects
    val upcomingEvents: LiveData<List<Event>>               // next 3 events
    val currentUser: LiveData<User>                        // user profile
    val motivationalQuote: LiveData<String>                // random quote from local list
    val announcements: LiveData<List<Announcement>>        // latest 3 announcements
}
```

---

## `TimetableViewModel`

**Scope:** `TimetableFragment`, `DayPageFragment`, `AddClassBottomSheet`

```kotlin
class TimetableViewModel(
    application: Application,
    private val timetableRepository: TimetableRepository
) : AndroidViewModel(application) {

    val allEntries: LiveData<List<TimetableEntity>>

    fun getEntriesForDay(dayOfWeek: Int): LiveData<List<TimetableEntity>>
    fun addClass(entry: TimetableEntity)
    fun updateClass(entry: TimetableEntity)
    fun deleteClass(entry: TimetableEntity)
}
```

**State:** No loading/error states needed — Room operations are fast and local.

---

## `AttendanceViewModel`

**Scope:** `AttendanceFragment`, `AttendanceDetailFragment`, `MarkAttendanceDialog`

```kotlin
class AttendanceViewModel(
    application: Application,
    private val attendanceRepository: AttendanceRepository
) : AndroidViewModel(application) {

    val subjects: LiveData<List<AttendanceEntity>>
    val selectedSubject: LiveData<AttendanceEntity?>

    fun markPresent(subjectCode: String)
    fun markAbsent(subjectCode: String)
    fun selectSubject(subjectCode: String)
    fun addSubject(entity: AttendanceEntity)
    fun deleteSubject(entity: AttendanceEntity)

    // Computed helpers
    fun calculateSafeAbsences(entity: AttendanceEntity): Int  // how many more can be skipped
    fun calculateRequiredAttendances(entity: AttendanceEntity): Int  // to reach 75%
}
```

---

## `EventViewModel`

**Scope:** `EventsFragment`, `EventDetailFragment`

```kotlin
class EventViewModel(
    application: Application,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    val eventsState: LiveData<Resource<List<Event>>>
    val selectedEvent: LiveData<Event?>
    val registrationState: LiveData<Resource<Unit>>

    fun loadEvents(category: String? = null)
    fun selectEvent(event: Event)
    fun registerForEvent(eventId: String)
    fun cancelRegistration(eventId: String)
}
```

---

## `CanteenViewModel`

**Scope:** `CanteenFragment`, `CanteenItemDetailFragment`

```kotlin
class CanteenViewModel(
    application: Application,
    private val canteenRepository: CanteenRepository
) : AndroidViewModel(application) {

    val menuState: LiveData<Resource<List<CanteenItemEntity>>>
    val feedbackState: LiveData<Resource<Unit>>

    fun loadTodayMenu()
    fun submitFeedback(itemId: String, rating: Float, comment: String)
}
```

---

## `LostFoundViewModel`

**Scope:** `LostFoundFragment`, `LostFoundDetailFragment`, `PostItemBottomSheet`

```kotlin
class LostFoundViewModel(
    application: Application,
    private val lostFoundRepository: LostFoundRepository
) : AndroidViewModel(application) {

    val itemsState: LiveData<Resource<List<LostFoundCacheEntity>>>
    val postState: LiveData<Resource<Unit>>
    val uploadState: LiveData<Resource<String>>  // download URL

    fun loadItems(type: String? = null)  // null = all
    fun postItem(item: LostFoundItem, imageUri: Uri?)
    fun markResolved(itemId: String)
}
```

---

## `ChatViewModel`

**Scope:** `ChatFragment`, `DirectMessageFragment`, `GroupChatFragment`

```kotlin
class ChatViewModel(
    application: Application,
    private val chatRepository: ChatRepository
) : AndroidViewModel(application) {

    val conversations: LiveData<List<Conversation>>
    val messages: LiveData<List<ChatMessage>>

    fun loadConversations()
    fun openDirectChat(otherUserId: String)
    fun openGroupChat(groupId: String)
    fun sendMessage(chatId: String, text: String)
    fun startListeningToMessages(chatId: String)   // attaches Firestore SnapshotListener
    fun stopListeningToMessages()                  // removes listener on Fragment destroy
}
```

---

## `MapViewModel`

**Scope:** `MapFragment`, `LocationInfoBottomSheet`

```kotlin
class MapViewModel(
    application: Application,
    private val mapRepository: MapRepository
) : AndroidViewModel(application) {

    val campusLocations: LiveData<List<CampusLocation>>
    val selectedLocation: LiveData<CampusLocation?>

    fun loadLocations()
    fun selectLocation(location: CampusLocation)
}
```

---

## `ProfileViewModel`

**Scope:** `ProfileFragment`, `EditProfileFragment`

```kotlin
class ProfileViewModel(
    application: Application,
    private val profileRepository: ProfileRepository
) : AndroidViewModel(application) {

    val userState: LiveData<Resource<User>>
    val updateState: LiveData<Resource<Unit>>
    val uploadState: LiveData<Resource<String>>   // photo URL

    fun loadProfile()
    fun updateProfile(name: String, branch: String, semester: Int, rollNumber: String)
    fun uploadProfilePhoto(uri: Uri)
    fun toggleDarkMode(enabled: Boolean)
}
```

---

## `AiAssistantViewModel`

**Scope:** `AiAssistantFragment`

```kotlin
class AiAssistantViewModel(
    application: Application,
    private val aiRepository: AiAssistantRepository,
    private val timetableRepository: TimetableRepository,
    private val attendanceRepository: AttendanceRepository,
    private val profileRepository: ProfileRepository
) : AndroidViewModel(application) {

    val messages: LiveData<List<AiMessage>>     // conversation UI state
    val isLoading: LiveData<Boolean>
    val errorState: LiveData<String?>

    fun sendMessage(userText: String)
    fun clearConversation()

    // Internal: builds context + system prompt before API call
    private suspend fun buildContextualRequest(userText: String): GeminiRequest
}
```

**`AiMessage`** — UI model for chat bubbles:
```kotlin
data class AiMessage(
    val text: String,
    val role: String,   // "user" | "assistant"
    val timestamp: Long = System.currentTimeMillis()
)
```
