package com.example.ung_dung_chay_bo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ung_dung_chay_bo.data.local.dao.ActivityRecordDao
import com.example.ung_dung_chay_bo.data.local.dao.TrackPointDao
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import com.example.ung_dung_chay_bo.data.local.entity.TrackPointEntity

@Database(
    entities = [TrackPointEntity::class, ActivityRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TrackingDatabase : RoomDatabase() {
    abstract fun trackPointDao(): TrackPointDao
    abstract fun activityRecordDao(): ActivityRecordDao
}
