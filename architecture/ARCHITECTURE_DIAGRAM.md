# 🗺️ Architecture Diagrams

All diagrams use [Mermaid](https://mermaid.js.org/) syntax. GitHub renders these natively in `.md` files.

---

## 1. Full App Architecture

```mermaid
graph TD
    A[Fragment / UI] -->|user action| B[ViewModel]
    B -->|LiveData| A
    B -->|suspend call| C[Repository]
    C -->|read/write| D[(Room DB\nLocal)]
    C -->|read/write| E[(Firestore\nRemote)]
    C -->|REST call| F[Retrofit / OkHttp]
    F -->|Gemini 2.5 Flash| G[AI Response]
    F -->|Google Maps| H[Map Data]
```

---

## 2. Single-Activity Navigation

```mermaid
graph LR
    MA[MainActivity] --> BN[BottomNavigation]
    BN --> Home
    BN --> Timetable
    BN --> Attendance
    BN --> Chat
    BN --> Profile

    Home --> Events
    Home --> Canteen
    Home --> LostFound
    Home --> Map
    Home --> AIAssistant

    Auth[SplashFragment] -->|not logged in| Login
    Login --> Register
    Login --> MA
    Register --> MA
```

---

## 3. Data Flow — Offline-First (Canteen/Timetable)

```mermaid
sequenceDiagram
    participant F as Fragment
    participant VM as ViewModel
    participant R as Repository
    participant Room as Room DB
    participant FS as Firestore

    F->>VM: observe()
    VM->>R: getData()
    R->>Room: queryCache()
    Room-->>R: cached data (or empty)
    R-->>VM: Resource.Success(cached)
    VM-->>F: show data immediately

    R->>FS: fetchRemote()
    FS-->>R: fresh data
    R->>Room: updateCache(freshData)
    R-->>VM: Resource.Success(fresh)
    VM-->>F: refresh UI
```

---

## 4. Authentication Flow

```mermaid
flowchart TD
    A[App Launch] --> B{Session valid?}
    B -->|Yes| C[Dashboard]
    B -->|No| D[Login Screen]

    D --> E{Login method}
    E -->|Email + Password| F[Firebase Auth\nEmail sign-in]
    E -->|Google| G[Google Sign-In Intent\nFirebase credential]

    F --> H{RVU domain\n@rvu.edu.in?}
    H -->|No| I[Show domain error]
    H -->|Yes| J[Save session\nSessionManager]

    G --> J
    J --> C

    D --> K[Forgot Password]
    K --> L[Firebase\nsendPasswordResetEmail]
    L --> M[Email sent toast]
    M --> D
```

---

## 5. Attendance Sync Flow

```mermaid
sequenceDiagram
    participant U as User
    participant F as AttendanceFragment
    participant VM as AttendanceViewModel
    participant R as AttendanceRepository
    participant Room as Room DB
    participant Worker as AttendanceSyncWorker
    participant FS as Firestore

    U->>F: Mark Present/Absent
    F->>VM: markAttendance(subjectId, status)
    VM->>R: updateAttendance()
    R->>Room: insert/update AttendanceEntity
    Room-->>R: success
    R-->>VM: Resource.Success
    VM-->>F: update UI

    Note over Worker: Daily scheduled by WorkManager
    Worker->>Room: getAllAttendance()
    Room-->>Worker: full snapshot
    Worker->>FS: users/{uid}/attendance/snapshot
    FS-->>Worker: write confirmed
```

---

## 6. AI Assistant Context Flow

```mermaid
flowchart LR
    A[User types question] --> B[AiAssistantViewModel]
    B --> C[Fetch student context]

    C --> D[Room: today's timetable]
    C --> E[Room: attendance %  per subject]
    C --> F[Firestore: user profile\nbranch + semester]

    D & E & F --> G[Build system prompt\nwith full context]
    G --> H[Gemini 2.5 Flash\nREST API]
    H --> I[Stream response]
    I --> J[Display in ChatBubble UI]
```

---

## 7. WorkManager Schedule

```mermaid
gantt
    title Background Workers
    dateFormat HH:mm
    axisFormat %H:%M

    section AttendanceSyncWorker
    Daily sync window (requires network) :00:00, 24h

    section EventReminderWorker
    One-shot: 1hr before registered event :crit, active, 09:00, 1h
```

---

## 8. Module Dependency Map

```mermaid
graph TD
    UI --> ViewModel
    ViewModel --> Repository
    Repository --> LocalDataSource
    Repository --> RemoteDataSource
    LocalDataSource --> RoomDB
    RemoteDataSource --> FirebaseAuth
    RemoteDataSource --> Firestore
    RemoteDataSource --> FirebaseStorage
    RemoteDataSource --> RetrofitClient
    RetrofitClient --> GeminiAPI
    RetrofitClient --> GoogleMapsAPI
    ViewModel --> Utils
    UI --> Utils
```
