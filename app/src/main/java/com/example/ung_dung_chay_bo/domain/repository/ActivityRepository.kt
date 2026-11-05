package com.example.ung_dung_chay_bo.domain.repository

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import kotlinx.coroutines.flow.Flow

data class UserActivityStats(
    val totalActivitiesCount: Int = 0,
    val weeklyDistanceKm: Double = 0.0,
    val weeklyDurationMinutes: Int = 0,
    val weeklyElevationGainM: Int = 0,
    val streakWeeks: Int = 0,
    val twelveWeeksProgress: List<Double> = List(12) { 0.0 }
)

interface ActivityRepository {
    suspend fun saveActivity(
        record: ActivityRecordEntity,
        publishToFeed: Boolean = true,
        caption: String = ""
    ): Resource<Unit>

    fun getAllActivities(): Flow<List<ActivityRecordEntity>>

    suspend fun getActivityById(id: Long): ActivityRecordEntity?

    suspend fun getActivityBySessionId(sessionId: String): ActivityRecordEntity?

    suspend fun deleteActivity(id: Long): Resource<Unit>

    fun getUserActivityStats(): Flow<UserActivityStats>

    suspend fun syncActivitiesFromCloud(): Resource<Unit>
}
