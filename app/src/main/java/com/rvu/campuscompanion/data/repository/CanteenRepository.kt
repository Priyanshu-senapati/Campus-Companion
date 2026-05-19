package com.rvu.campuscompanion.data.repository

import androidx.lifecycle.LiveData
import com.rvu.campuscompanion.data.local.MenuItemDao
import com.rvu.campuscompanion.data.local.MenuItemEntity
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.utils.Constants
import kotlinx.coroutines.tasks.await

class CanteenRepository(private val dao: MenuItemDao) {
    fun getAll(): LiveData<List<MenuItemEntity>> = dao.getAll()
    fun getByCategory(cat: String): LiveData<List<MenuItemEntity>> = dao.getByCategory(cat)
    suspend fun update(item: MenuItemEntity) = dao.update(item)

    suspend fun submitFeedback(menuItem: String, rating: Int, comment: String): Result<Unit> = runCatching {
        val data = hashMapOf(
            "menuItem" to menuItem,
            "rating" to rating,
            "comment" to comment,
            "userId" to (FirebaseSource.currentUserId ?: "anonymous"),
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseSource.firestore.collection(Constants.COLL_FEEDBACK).add(data).await()
        Unit
    }
}
