# 🔄 UI Flows

This document describes user journey flowcharts for all major features in Campus Companion.

---

## 1. App Launch Flow

```mermaid
flowchart TD
    START([App Launch]) --> SPLASH[SplashFragment\n2s Lottie animation]
    SPLASH --> CHECK{SessionManager:\nlogged in?}
    CHECK -->|Yes| DASH[Dashboard]
    CHECK -->|No| ONBOARD{First launch?}
    ONBOARD -->|Yes| ONBOARDING[Onboarding\nViewPager2\n3 slides]
    ONBOARDING --> LOGIN
    ONBOARD -->|No| LOGIN[LoginFragment]
```

---

## 2. Registration Flow

```mermaid
flowchart TD
    LOGIN[LoginFragment] --> REG[RegisterFragment]
    REG --> FORM[Fill: name, email,\npassword, branch, semester,\nroll number]
    FORM --> VALIDATE{InputValidator\ncheck all fields}
    VALIDATE -->|Invalid| ERR[Show field errors]
    ERR --> FORM
    VALIDATE -->|Valid| DOMAIN{email ends with\n@rvu.edu.in?}
    DOMAIN -->|No| DOMAIN_ERR[Show domain error:\n'Use your RVU email']
    DOMAIN -->|Yes| FIREBASE[Firebase Auth\ncreateUserWithEmailAndPassword]
    FIREBASE -->|Success| FIRESTORE[Create users/uid doc\nin Firestore]
    FIRESTORE --> SESSION[Save session\nSessionManager]
    SESSION --> DASH[Dashboard]
    FIREBASE -->|Failure| FB_ERR[Show error toast]
```

---

## 3. Timetable Flow

```mermaid
flowchart TD
    TT[TimetableFragment\nViewPager2\n7 day tabs] --> DAY[DayPageFragment\nshows today's classes]
    DAY --> EMPTY{No classes?}
    EMPTY -->|Yes| EMPTY_STATE[Show empty state:\n'No classes today']
    EMPTY -->|No| LIST[RecyclerView\nclass cards]

    LIST --> CLICK[Click a class]
    CLICK --> OPTIONS{Options}
    OPTIONS --> EDIT[Edit class\nbottom sheet]
    OPTIONS --> DELETE[Confirm delete dialog]

    TT --> FAB[+ FAB]
    FAB --> ADD[AddClassBottomSheet\nfill subject, time,\nroom, professor, type]
    ADD --> SAVE[Room insert\nTimetableDao]
    SAVE --> REFRESH[LiveData update\nUI refreshes]
```

---

## 4. Attendance Flow

```mermaid
flowchart TD
    ATT[AttendanceFragment\nSubject list] --> COLOR{Attendance %}
    COLOR -->|≥75%| GREEN[Green card]
    COLOR -->|65–74%| YELLOW[Yellow card\n⚠️ warning]
    COLOR -->|<65%| RED[Red card\n🚨 danger]

    ATT --> CLICK[Tap subject]
    CLICK --> DETAIL[AttendanceDetailFragment\ncalendar + history]

    ATT --> MARK[Mark button]
    MARK --> DIALOG[MarkAttendanceDialog\nPresent / Absent / Cancel]
    DIALOG -->|Present| PRESENT[attendanceDao.markPresent]
    DIALOG -->|Absent| ABSENT[attendanceDao.markAbsent]
    PRESENT & ABSENT --> RECALC[Recalculate %\nupdate LiveData]
    RECALC --> SYNC_SCHED[AttendanceSyncWorker\nwill sync to Firestore\nnext scheduled run]
```

---

## 5. Events Flow

```mermaid
flowchart TD
    EV[EventsFragment\ntabs: All / Technical /\nCultural / Sports / Academic] --> LIST[Event cards\nwith date + venue]
    LIST --> CLICK[Tap event]
    CLICK --> DETAIL[EventDetailFragment\nfull description + register]

    DETAIL --> REG{Already registered?}
    REG -->|No| REGISTER[Register button]
    REGISTER --> FS[Firestore: add UID\nto event.registeredBy]
    FS --> WORKER[Schedule EventReminderWorker\n1hr before event via WorkManager]
    REG -->|Yes| CANCEL[Cancel registration]
```

---

## 6. Lost & Found Flow

```mermaid
flowchart TD
    LF[LostFoundFragment\ntabs: Lost / Found] --> LIST[Item cards]
    LIST --> CLICK[Tap item]
    CLICK --> DETAIL[LostFoundDetailFragment\nphoto + contact info]
    DETAIL --> CONTACT[Open dialer/email\nwith contact info]

    LF --> FAB[+ Post item]
    FAB --> SHEET[PostItemBottomSheet\ntype: lost/found\ntitle, description,\ncategory, location,\nimage pick, contact]
    SHEET --> UPLOAD{Has image?}
    UPLOAD -->|Yes| STORAGE[Upload to\nFirebase Storage]
    STORAGE --> URL[Get download URL]
    URL --> POST
    UPLOAD -->|No| POST[Post to Firestore\nlostfound collection]
    POST --> REFRESH[Refresh list]
```

---

## 7. Chat Flow

```mermaid
flowchart TD
    CHAT[ChatFragment\nlist of conversations] --> DM[Tap user → DirectMessageFragment]
    DM --> MSG[Type message]
    MSG --> SEND[Send → Firestore\nchats/chatId/messages]
    SEND --> LISTEN[SnapshotListener\nreal-time update]
    LISTEN --> DISPLAY[New bubble in RecyclerView]

    CHAT --> GROUP[Tap group → GroupChatFragment]
    GROUP --> GMSG[Same flow but\ngroups/groupId/messages]
```

---

## 8. AI Assistant Flow

```mermaid
flowchart TD
    AI[AiAssistantFragment\nchat-style UI] --> INPUT[User types question]
    INPUT --> CTX[Fetch context:\n• today's timetable\n• attendance %\n• user profile]
    CTX --> PROMPT[Build system prompt\nwith injected context]
    PROMPT --> API[Gemini 2.5 Flash\nREST call via Retrofit]
    API --> STREAM[Response received]
    STREAM --> BUBBLE[Display in chat bubble]
    BUBBLE --> HISTORY[Add to conversation history]
    HISTORY --> INPUT
```

---

## 9. Profile Flow

```mermaid
flowchart TD
    PROF[ProfileFragment\nread-only view] --> EDIT[Edit button]
    EDIT --> EDIT_FRAG[EditProfileFragment\nname, branch, semester,\nroll number, photo]
    EDIT_FRAG --> PHOTO{Change photo?}
    PHOTO -->|Camera| CAMERA[CameraProvider intent]
    PHOTO -->|Gallery| GALLERY[Image picker\n/READ_MEDIA_IMAGES/]
    CAMERA & GALLERY --> COMPRESS[Compress bitmap]
    COMPRESS --> UPLOAD[Upload to\nFirebase Storage\nprofile_photos/uid.jpg]
    UPLOAD --> UPDATE[Update photoUrl\nin Firestore users/uid]

    EDIT_FRAG --> SAVE[Save other fields\nFirestore update]
    SAVE --> BACK[Navigate back\nto ProfileFragment]
```

---

## 10. Campus Map Flow

```mermaid
flowchart TD
    MAP[MapFragment\nGoogle Maps SDK] --> LOAD[Load RVU campus\nbuilding markers]
    LOAD --> LOCATE[Show user location\nACCESS_FINE_LOCATION]
    LOAD --> MARKERS[Place markers for:\nlabs, library, canteen,\nadmin, hostels, sports]
    MARKERS --> TAP[Tap a marker]
    TAP --> SHEET[LocationInfoBottomSheet\nbuilding name + details\n+ directions button]
    SHEET --> DIR[Open Google Maps app\nfor turn-by-turn]
```
