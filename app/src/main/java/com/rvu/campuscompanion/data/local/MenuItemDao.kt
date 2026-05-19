package com.rvu.campuscompanion.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MenuItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MenuItemEntity>)

    @Update suspend fun update(item: MenuItemEntity)

    @Query("SELECT * FROM menu_items ORDER BY category, name")
    fun getAll(): LiveData<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE category = :category")
    fun getByCategory(category: String): LiveData<List<MenuItemEntity>>

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun count(): Int
}
