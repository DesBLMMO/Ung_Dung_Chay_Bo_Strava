package com.example.ung_dung_chay_bo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityRecordEntity): Long

    @Query("SELECT * FROM activities_record ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityRecordEntity>>

    @Query("SELECT * FROM activities_record WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getActivityBySessionId(sessionId: String): ActivityRecordEntity?

    @Query("SELECT COUNT(*) FROM activities_record")
    fun getActivitiesCount(): Flow<Int>

    @Query("DELETE FROM activities_record WHERE id = :id")
    suspend fun deleteActivity(id: Long)
}
