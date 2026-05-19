package com.rvu.campuscompanion.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rvu.campuscompanion.data.model.LostFoundItem
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.utils.Constants
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LostFoundRepository {
    private val firestore = FirebaseSource.firestore
    private val storage = FirebaseSource.storage
    private var listener: ListenerRegistration? = null

    fun observe(type: String): LiveData<List<LostFoundItem>> {
        val data = MutableLiveData<List<LostFoundItem>>()
        listener?.remove()
        listener = firestore.collection(Constants.COLL_LOSTFOUND)
            .whereEqualTo("type", type)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                data.postValue(snap?.toObjects(LostFoundItem::class.java) ?: emptyList())
            }
        return data
    }

    suspend fun fetchItem(id: String): LostFoundItem? {
        return firestore.collection(Constants.COLL_LOSTFOUND).document(id).get().await()
            .toObject(LostFoundItem::class.java)
    }

    suspend fun post(item: LostFoundItem, imageUri: Uri?): Result<Unit> = runCatching {
        val imgUrl = imageUri?.let {
            runCatching {
                val ref = storage.reference.child("lostfound/${UUID.randomUUID()}.jpg")
                ref.putFile(it).await()
                ref.downloadUrl.await().toString()
            }.getOrElse { "" }
        } ?: ""
        val toSave = item.copy(imageUrl = imgUrl)
        firestore.collection(Constants.COLL_LOSTFOUND).add(toSave).await()
        Unit
    }

    suspend fun updateStatus(id: String, status: String): Result<Unit> = runCatching {
        firestore.collection(Constants.COLL_LOSTFOUND).document(id)
            .update("status", status).await()
        Unit
    }
}
