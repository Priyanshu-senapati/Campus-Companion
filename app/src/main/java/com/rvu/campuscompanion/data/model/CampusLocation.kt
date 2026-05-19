package com.rvu.campuscompanion.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CampusLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val timings: String,
    val category: String,
    val markerColor: Float
) : Parcelable
