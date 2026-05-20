# 🔥 Firebase Firestore Schema

This document describes every Firestore collection, document structure, field types, and indexing strategy used in Campus Companion.

---

## Collections Overview

```
Firestore Root
├── users/                    # One doc per authenticated user
├── events/                   # Campus events (admin-managed)
├── announcements/            # Campus-wide announcements (admin-managed)
├── lostfound/                # Lost & found posts (user-managed)
├── chats/                    # Chat threads
│   └── {chatId}/
│       └── messages/         # Sub-collection: messages in a thread
├── canteen/                  # Daily canteen menu (admin-managed)
└── groups/                   # Group chat metadata
    └── {groupId}/
        └── messages/         # Sub-collection: group messages
```

---

## `users/{uid}`

One document per user, keyed by Firebase Auth UID.

```
users/{uid}
├── uid            : String       — Firebase Auth UID
├── name           : String       — Full name
├── email          : String       — Must be @rvu.edu.in domain
├── branch         : String       — e.g. "Computer Science & Engineering"
├── semester       : Number       — 1–8
├── rollNumber     : String       — University roll number
├── photoUrl       : String?      — Firebase Storage URL (nullable)
├── fcmToken       : String       — Latest FCM device token
├── createdAt      : Timestamp    — Account creation time
├── lastSeen       : Timestamp    — Last app open
├── darkMode       : Boolean      — User theme preference
└── attendance/                   — Sub-collection (see below)
    └── snapshot
        ├── subjects : Map<String, AttendanceSummary>
        └── syncedAt : Timestamp
```

### `AttendanceSummary` (map value)
```
{
  attended : Number   — classes attended
  total    : Number   — total classes held
  percent  : Number   — (attended/total) * 100
}
```

---

## `events/{eventId}`

Admin-created. Users cannot write to this collection.

```
events/{eventId}
├── id            : String
├── title         : String
├── description   : String
├── category      : String       — "Technical" | "Cultural" | "Sports" | "Academic"
├── venue         : String
├── date          : Timestamp
├── endDate       : Timestamp?
├── imageUrl      : String?      — Firebase Storage URL
├── registeredBy  : Array<String> — Array of UIDs who registered
├── maxCapacity   : Number?
├── isActive      : Boolean
└── createdAt     : Timestamp
```

**Firestore index needed:**
- `category ASC, date ASC` (for filtered event list sorted by date)
- `isActive ASC, date ASC` (for active upcoming events)

---

## `announcements/{announcementId}`

```
announcements/{announcementId}
├── id          : String
├── title       : String
├── body        : String
├── type        : String    — "General" | "Exam" | "Holiday" | "Result"
├── priority    : Number    — 1 (low) to 3 (high), affects sort order
├── publishedAt : Timestamp
└── expiresAt   : Timestamp?
```

---

## `lostfound/{itemId}`

User-created. Any authenticated user can read and write.

```
lostfound/{itemId}
├── id          : String
├── type        : String    — "lost" | "found"
├── title       : String
├── description : String
├── location    : String    — Where item was lost/found on campus
├── imageUrl    : String?   — Firebase Storage URL
├── category    : String    — "Electronics" | "ID Card" | "Books" | "Keys" | "Other"
├── status      : String    — "open" | "resolved"
├── postedBy    : String    — UID
├── contactInfo : String    — Phone or email (user-provided)
├── postedAt    : Timestamp
└── resolvedAt  : Timestamp?
```

**Firestore index needed:**
- `type ASC, status ASC, postedAt DESC`

---

## `chats/{chatId}`

Chat thread metadata. `chatId` is deterministically generated from the two user UIDs:
```kotlin
val chatId = listOf(uid1, uid2).sorted().joinToString("_")
```

```
chats/{chatId}
├── participants  : Array<String>  — [uid1, uid2]
├── lastMessage   : String
├── lastMessageAt : Timestamp
└── messages/                      — sub-collection
    └── {messageId}
        ├── senderId   : String
        ├── text       : String
        ├── sentAt     : Timestamp
        └── readBy     : Array<String>
```

---

## `groups/{groupId}`

```
groups/{groupId}
├── name        : String
├── description : String?
├── members     : Array<String>  — UIDs
├── admins      : Array<String>  — UIDs
├── imageUrl    : String?
├── createdAt   : Timestamp
├── createdBy   : String         — UID
└── messages/                    — sub-collection (same shape as DM messages)
```

---

## `canteen/{date}`

Keyed by date string `YYYY-MM-DD`. Updated daily by admin.

```
canteen/2025-01-15
├── date      : String
├── updatedAt : Timestamp
└── items     : Array<CanteenItem>
```

### `CanteenItem` (array element)
```
{
  id          : String
  name        : String
  description : String?
  price       : Number
  category    : String   — "Breakfast" | "Lunch" | "Snacks" | "Beverages"
  imageUrl    : String?
  isAvailable : Boolean
  rating      : Number   — average rating (0.0–5.0)
  ratingCount : Number
}
```

---

## Security Rules Reference

Full recommended rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users: only owner can read/write their own doc
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;

      // Attendance sub-collection: same restriction
      match /attendance/{doc} {
        allow read, write: if request.auth.uid == uid;
      }
    }

    // Events: any authenticated user can read, nobody can write (admin only via SDK)
    match /events/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }

    // Announcements: same as events
    match /announcements/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }

    // Lost & Found: any authenticated user can read and write
    match /lostfound/{doc} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.resource.data.postedBy == request.auth.uid;
      allow update, delete: if request.auth.uid == resource.data.postedBy;
    }

    // Direct messages: only participants can read/write
    match /chats/{chatId}/messages/{msgId} {
      allow read, write: if request.auth != null
        && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
    }

    // Groups: only members can read, admins can manage
    match /groups/{groupId}/messages/{msgId} {
      allow read, write: if request.auth != null
        && request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.members;
    }

    // Canteen: any authenticated user can read, write restricted
    match /canteen/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

---

## Firebase Storage Structure

```
Firebase Storage Root
├── profile_photos/
│   └── {uid}.jpg
├── lost_found/
│   └── {itemId}_{timestamp}.jpg
└── events/
    └── {eventId}.jpg
```

All uploads use `uid`-scoped paths to enable storage security rules that restrict users to their own files.
