package com.example.ung_dung_chay_bo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities_record")
data class ActivityRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val title: String,
    val description: String = "",
    val sportType: String,
    val distanceKm: Double,
    val durationSeconds: Long,
    val avgSpeedKmh: Float = 0f,
    val avgPace: String = "--:--",
    val calories: Int = 0,
    val routePointsJson: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
