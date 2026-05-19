package com.rvu.campuscompanion.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rvu.campuscompanion.data.model.Announcement
import com.rvu.campuscompanion.data.model.Event
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.utils.Constants
import kotlinx.coroutines.tasks.await

class EventsRepository {
    private val firestore = FirebaseSource.firestore
    private var eventsListener: ListenerRegistration? = null
    private var announcementsListener: ListenerRegistration? = null

    fun observeEvents(): LiveData<List<Event>> {
        val data = MutableLiveData<List<Event>>()
        eventsListener?.remove()
        eventsListener = firestore.collection(Constants.COLL_EVENTS)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                data.postValue(snap?.toObjects(Event::class.java) ?: emptyList())
            }
        return data
    }

    fun observeAnnouncements(): LiveData<List<Announcement>> {
        val data = MutableLiveData<List<Announcement>>()
        announcementsListener?.remove()
        announcementsListener = firestore.collection(Constants.COLL_ANNOUNCEMENTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                data.postValue(snap?.toObjects(Announcement::class.java) ?: emptyList())
            }
        return data
    }

    suspend fun fetchEvent(id: String): Event? {
        return firestore.collection(Constants.COLL_EVENTS).document(id).get().await()
            .toObject(Event::class.java)
    }

    suspend fun register(eventId: String, uid: String): Result<Unit> = runCatching {
        firestore.collection(Constants.COLL_EVENTS).document(eventId)
            .update("registeredUsers", FieldValue.arrayUnion(uid)).await()
        Unit
    }

    suspend fun unregister(eventId: String, uid: String): Result<Unit> = runCatching {
        firestore.collection(Constants.COLL_EVENTS).document(eventId)
            .update("registeredUsers", FieldValue.arrayRemove(uid)).await()
        Unit
    }

    suspend fun seedSampleEvents() {
        val col = firestore.collection(Constants.COLL_EVENTS)
        val snapshot = col.limit(1).get().await()
        if (!snapshot.isEmpty) return
        val now = System.currentTimeMillis()
        val day = 86400000L
        val samples = listOf(
            Event(title = "RVU Tech Fest 2026", description = "Annual technology festival featuring hackathons, robotics, AI showcases and guest talks from industry leaders.", date = now + 7 * day, venue = "Main Auditorium", organizer = "CSE Dept", category = "Technical", tags = listOf("hackathon", "robotics")),
            Event(title = "Cultural Night", description = "An evening of music, dance, and theatrical performances by RVU students.", date = now + 14 * day, venue = "Open Air Theater", organizer = "Cultural Committee", category = "Cultural", tags = listOf("music", "dance")),
            Event(title = "Inter-College Sports Meet", description = "Three-day sports tournament with cricket, basketball, football and athletics.", date = now + 21 * day, venue = "Sports Complex", organizer = "Sports Dept", category = "Sports", tags = listOf("cricket", "basketball")),
            Event(title = "AI/ML Workshop", description = "Hands-on workshop on building deep learning models with PyTorch.", date = now + 3 * day, venue = "Lab 105", organizer = "AI&ML Dept", category = "Technical", tags = listOf("workshop", "AI")),
            Event(title = "Industry Connect Seminar", description = "Talks from senior engineers at Google, Microsoft and Amazon.", date = now + 10 * day, venue = "Auditorium B", organizer = "Placement Cell", category = "Academic", tags = listOf("seminar", "career"))
        )
        samples.forEach { col.add(it).await() }
    }

    fun cleanup() {
        eventsListener?.remove(); announcementsListener?.remove()
    }
}
