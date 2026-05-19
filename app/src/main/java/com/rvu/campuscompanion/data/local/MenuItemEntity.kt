package com.rvu.campuscompanion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Int,
    val category: String,
    val isVeg: Boolean,
    val available: Boolean = true,
    val description: String = "",
    val calories: Int = 0,
    val allergens: String = ""
) {
    companion object {
        const val CAT_BREAKFAST = "Breakfast"
        const val CAT_LUNCH = "Lunch"
        const val CAT_SNACKS = "Snacks"
        const val CAT_BEVERAGES = "Beverages"
    }
}
