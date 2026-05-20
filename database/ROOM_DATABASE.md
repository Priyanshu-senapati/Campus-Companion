# 🗄️ Room Database Schema

Campus Companion uses **Room 2.6** for offline-first local storage. This document describes all entities, DAOs, relationships, and the database itself.

---

## Database

```kotlin
@Database(
    entities = [
        TimetableEntity::class,
        AttendanceEntity::class,
        CanteenItemEntity::class,
        LostFoundCacheEntity::class,
        EventCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RVUDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun canteenDao(): CanteenDao
    abstract fun lostFoundDao(): LostFoundCacheDao
    abstract fun eventDao(): EventCacheDao

    companion object {
        @Volatile private var INSTANCE: RVUDatabase? = null

        fun getInstance(context: Context): RVUDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, RVUDatabase::class.java, "rvu_database.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
```

---

## Entities

### 1. `TimetableEntity`

Stores the user's weekly class schedule. Edited locally, synced to Firestore optionally.

```kotlin
@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val subjectName: String,
    val subjectCode: String,
    val professorName: String,
    val room: String,
    val dayOfWeek: Int,          // 1 = Monday ... 7 = Sunday
    val startTime: String,       // "09:00"
    val endTime: String,         // "10:00"
    val type: String,            // "Lecture" | "Lab" | "Tutorial"
    val color: Int,              // ARGB color int for UI
    val createdAt: Long = System.currentTimeMillis()
)
```

**Indexes:** `dayOfWeek` for filtering today's classes.

---

### 2. `AttendanceEntity`

One row per subject. Updated every time the user marks attendance.

```kotlin
@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey
    val subjectCode: String,     // unique per subject

    val subjectName: String,
    val attended: Int,           // classes attended
    val total: Int,              // total classes held
    val lastMarkedAt: Long,      // epoch ms
    val professorName: String,
    val targetPercentage: Float = 75f
)
```

**Computed property (not stored):**
```kotlin
val percentage: Float get() = if (total == 0) 0f else (attended.toFloat() / total) * 100f
val isCritical: Boolean get() = percentage < 75f
val isDanger: Boolean get() = percentage < 65f
```

---

### 3. `CanteenItemEntity`

Cache of the daily canteen menu fetched from Firestore.

```kotlin
@Entity(tableName = "canteen_items")
data class CanteenItemEntity(
    @PrimaryKey
    val id: String,

    val name: String,
    val description: String?,
    val price: Double,
    val category: String,        // "Breakfast" | "Lunch" | "Snacks" | "Beverages"
    val imageUrl: String?,
    val isAvailable: Boolean,
    val rating: Float,
    val ratingCount: Int,
    val menuDate: String,        // "YYYY-MM-DD" — for cache invalidation
    val cachedAt: Long = System.currentTimeMillis()
)
```

---

### 4. `LostFoundCacheEntity`

Local cache of lost & found items (read-only mirror of Firestore).

```kotlin
@Entity(tableName = "lost_found_cache")
data class LostFoundCacheEntity(
    @PrimaryKey
    val id: String,

    val type: String,            // "lost" | "found"
    val title: String,
    val description: String,
    val location: String,
    val imageUrl: String?,
    val category: String,
    val status: String,          // "open" | "resolved"
    val postedBy: String,        // UID
    val contactInfo: String,
    val postedAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)
```

---

### 5. `EventCacheEntity`

Local cache of campus events.

```kotlin
@Entity(tableName = "event_cache")
data class EventCacheEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val description: String,
    val category: String,
    val venue: String,
    val date: Long,              // epoch ms
    val imageUrl: String?,
    val isRegistered: Boolean,   // has this user registered?
    val cachedAt: Long = System.currentTimeMillis()
)
```

---

## DAOs

### `TimetableDao`

```kotlin
@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllEntries(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getEntriesForDay(day: Int): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimetableEntity)

    @Update
    suspend fun update(entry: TimetableEntity)

    @Delete
    suspend fun delete(entry: TimetableEntity)

    @Query("DELETE FROM timetable")
    suspend fun deleteAll()
}
```

---

### `AttendanceDao`

```kotlin
@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY subjectName ASC")
    fun getAllSubjects(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE subjectCode = :code")
    suspend fun getSubject(code: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AttendanceEntity)

    @Query("UPDATE attendance SET attended = attended + 1, total = total + 1, lastMarkedAt = :time WHERE subjectCode = :code")
    suspend fun markPresent(code: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE attendance SET total = total + 1, lastMarkedAt = :time WHERE subjectCode = :code")
    suspend fun markAbsent(code: String, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM attendance")
    suspend fun getSnapshot(): List<AttendanceEntity>
}
```

---

### `CanteenDao`

```kotlin
@Dao
interface CanteenDao {
    @Query("SELECT * FROM canteen_items WHERE menuDate = :date ORDER BY category ASC")
    fun getMenuForDate(date: String): Flow<List<CanteenItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CanteenItemEntity>)

    @Query("DELETE FROM canteen_items WHERE menuDate != :today")
    suspend fun evictOldCache(today: String)
}
```

---

### `LostFoundCacheDao`

```kotlin
@Dao
interface LostFoundCacheDao {
    @Query("SELECT * FROM lost_found_cache WHERE status = 'open' ORDER BY postedAt DESC")
    fun getOpenItems(): Flow<List<LostFoundCacheEntity>>

    @Query("SELECT * FROM lost_found_cache WHERE type = :type ORDER BY postedAt DESC")
    fun getItemsByType(type: String): Flow<List<LostFoundCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LostFoundCacheEntity>)

    @Query("DELETE FROM lost_found_cache")
    suspend fun clearCache()
}
```

---

### `EventCacheDao`

```kotlin
@Dao
interface EventCacheDao {
    @Query("SELECT * FROM event_cache WHERE date >= :now ORDER BY date ASC")
    fun getUpcomingEvents(now: Long = System.currentTimeMillis()): Flow<List<EventCacheEntity>>

    @Query("SELECT * FROM event_cache WHERE category = :cat AND date >= :now ORDER BY date ASC")
    fun getByCategory(cat: String, now: Long = System.currentTimeMillis()): Flow<List<EventCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventCacheEntity>)
}
```

---

## Database Migrations

Currently at version 1. When schema changes are needed in future:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: add a new column
        database.execSQL("ALTER TABLE attendance ADD COLUMN notes TEXT DEFAULT ''")
    }
}

// Register in builder:
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()
```

> ⚠️ Never use `fallbackToDestructiveMigration()` in production — it wipes user data. Switch to proper migrations before releasing to real users.

---

## Seed Data

The `data/local/` directory contains seed files for development:

- `TimetableSeedData.kt` — sample 5-day timetable for testing
- `AttendanceSeedData.kt` — pre-populated subjects with dummy attendance %
- `CanteenSeedData.kt` — sample canteen menu items

These are only inserted when the database is created fresh (dev builds only).
