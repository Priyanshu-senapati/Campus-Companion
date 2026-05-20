# 🏗️ Architecture Overview

Campus Companion follows **MVVM (Model-View-ViewModel)** with a clean repository pattern. This document explains each layer, what lives in it, and how data flows through the app.

---

## Layers at a Glance

```
┌──────────────────────────────────────────────┐
│                  UI LAYER                    │
│  Fragments + ViewBinding + Navigation Comp.  │
└────────────────────┬─────────────────────────┘
                     │ observes LiveData
┌────────────────────▼─────────────────────────┐
│             VIEWMODEL LAYER                  │
│  AndroidViewModel + LiveData + Coroutines    │
└────────────────────┬─────────────────────────┘
                     │ calls suspend functions
┌────────────────────▼─────────────────────────┐
│            REPOSITORY LAYER                  │
│  Single source of truth per feature domain   │
│  Decides: Room first? Firestore? Both?       │
└──────┬─────────────────────────┬─────────────┘
       │                         │
┌──────▼──────┐         ┌────────▼────────┐
│  LOCAL DB   │         │   REMOTE/API    │
│    Room     │         │ Firebase + REST  │
└─────────────┘         └─────────────────┘
```

---

## 1. UI Layer

**Location:** `app/ui/`

The UI layer consists exclusively of **Fragments** using **ViewBinding**. Fragments:
- Observe `LiveData` from their ViewModel
- Dispatch user actions (clicks, form submits) to the ViewModel
- Contain **zero business logic** — no if/else for data transformation, no direct Firestore calls

### Navigation
Single-Activity architecture (`MainActivity.kt`) hosts all Fragments via the **Jetpack Navigation Component**. Bottom navigation switches between the 5 main destinations. Safe Args plugin is used for type-safe fragment arguments.

### Key UI Modules

| Package | Screens |
|---------|---------|
| `ui/auth` | Splash, Login, Register |
| `ui/home` | Dashboard, More |
| `ui/timetable` | Timetable pager, day view, add/edit class bottom sheet |
| `ui/attendance` | Subject list, detail view, mark attendance dialog |
| `ui/events` | Events list, event detail |
| `ui/canteen` | Menu list, item detail, feedback sheet |
| `ui/lostfound` | Lost & found list, detail, post item bottom sheet |
| `ui/chat` | Chat list, direct message, group chat |
| `ui/map` | Google Maps fragment, location info sheet |
| `ui/profile` | Profile view, edit profile |
| `ui/onboarding` | ViewPager2 onboarding slides |

---

## 2. ViewModel Layer

**Location:** `app/viewmodel/`

One ViewModel per feature domain. All ViewModels extend `AndroidViewModel` to access the Application context (needed for Room database access). ViewModels:
- Expose `LiveData<Resource<T>>` to Fragments
- Call repository functions inside `viewModelScope.launch {}`
- Handle loading/success/error state via the `Resource<T>` sealed class
- Survive configuration changes (screen rotation)

### ViewModelFactory
A shared `ViewModelFactory` is used for manual dependency injection. Repositories are passed into ViewModels via the factory.

---

## 3. Repository Layer

**Location:** `app/data/repository/`

One repository per feature. The repository is the **single source of truth** — it decides whether to:
- Serve data from Room (offline-first)
- Fetch from Firestore / REST API
- Write to both (Room as cache, Firestore as remote)

Repositories emit `Resource<T>` states. Example flow:

```
Repository.getCanteenMenu()
  1. Emit Resource.Loading
  2. Check Room cache → if valid, emit Resource.Success(cachedData)
  3. Fetch from Firestore
  4. On success: update Room, emit Resource.Success(freshData)
  5. On failure: emit Resource.Error(message)
```

---

## 4. Data Layer

**Location:** `app/data/`

### `data/local/`
- **Room Database** (`RVUDatabase.kt`) — single database with multiple DAOs
- Entities: `TimetableEntity`, `AttendanceEntity`, `CanteenItemEntity`, etc.
- DAOs: one per entity, using `suspend` functions and `Flow`

### `data/model/`
- Pure Kotlin data classes used across all layers
- Examples: `User`, `Event`, `LostFoundItem`, `ChatMessage`, `TimetableEntry`

### `data/remote/`
- `FirebaseDataSource.kt` — all Firestore read/write operations
- `GeminiApiService.kt` — Retrofit interface for Gemini REST API
- `RetrofitClient.kt` — OkHttp + Retrofit setup with logging interceptor

### `data/repository/`
- `AuthRepository`, `TimetableRepository`, `AttendanceRepository`, `EventRepository`, `CanteenRepository`, `LostFoundRepository`, `ChatRepository`, `MapRepository`, `ProfileRepository`, `AiAssistantRepository`

---

## Design Decisions

**Why not Hilt/Dagger?** Manual DI with a ViewModelFactory was chosen to keep the project simple and understandable for new contributors without a DI framework learning curve.

**Why single Activity?** Jetpack Navigation Component works best with a single Activity host. Shared ViewModel instances across fragments (e.g., profile data shared between Dashboard and Profile screen) are easier to manage.

**Why Room + Firestore?** Room gives offline-first capability and instant loads. Firestore gives real-time sync across devices. The repository layer bridges both without the UI knowing.

**Why LiveData over Flow in UI?** LiveData is lifecycle-aware by default and requires no extra setup in Fragments. Internal repository logic uses `Flow` and converts at the ViewModel boundary with `asLiveData()`.
