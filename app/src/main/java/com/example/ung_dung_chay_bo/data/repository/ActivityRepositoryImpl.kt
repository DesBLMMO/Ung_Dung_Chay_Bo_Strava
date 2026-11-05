package com.example.ung_dung_chay_bo.data.repository

import android.util.Log
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.data.local.dao.ActivityRecordDao
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.ActivityRepository
import com.example.ung_dung_chay_bo.domain.repository.PostRepository
import com.example.ung_dung_chay_bo.domain.repository.UserActivityStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityRecordDao: ActivityRecordDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val postRepository: PostRepository
) : ActivityRepository {

    companion object {
        private const val TAG = "ActivityRepository"
    }

    override suspend fun saveActivity(
        record: ActivityRecordEntity,
        publishToFeed: Boolean,
        caption: String
    ): Resource<Unit> {
        return try {
            activityRecordDao.insertActivity(record)

            val currentUid = firebaseAuth.currentUser?.uid
            val displayName = firebaseAuth.currentUser?.displayName ?: "Runner"

            if (!currentUid.isNullOrEmpty()) {
                val activityMap = hashMapOf(
                    "sessionId" to record.sessionId,
                    "title" to record.title,
                    "description" to record.description,
                    "sportType" to record.sportType,
                    "distanceKm" to record.distanceKm,
                    "durationSeconds" to record.durationSeconds,
                    "avgSpeedKmh" to record.avgSpeedKmh,
                    "avgPace" to record.avgPace,
                    "calories" to record.calories,
                    "routePointsJson" to record.routePointsJson,
                    "photoUri" to record.photoUri,
                    "timestamp" to record.timestamp
                )

                try {
                    firestore.collection(Constants.USERS_COLLECTION)
                        .document(currentUid)
                        .collection("activities")
                        .document(record.sessionId)
                        .set(activityMap, SetOptions.merge())
                        .await()

                    firestore.collection(Constants.USERS_COLLECTION)
                        .document(currentUid)
                        .update(
                            "totalDistanceKm", FieldValue.increment(record.distanceKm),
                            "activitiesCount", FieldValue.increment(1),
                            "lastActivityAt", record.timestamp
                        )
                } catch (cloudError: Exception) {
                    Log.w(TAG, "Không thể lưu hoạt động lên Firestore: ${cloudError.message}")
                }

                if (publishToFeed) {
                    val sport = runCatching { SportType.valueOf(record.sportType) }.getOrDefault(SportType.RUN)
                    val mins = record.durationSeconds / 60
                    val secs = record.durationSeconds % 60
                    val durationStr = String.format("%02d:%02d", mins, secs)

                    val newPost = FriendPost(
                        id = "post_${record.sessionId}",
                        authorUid = currentUid,
                        authorName = displayName,
                        title = record.title,
                        caption = caption.ifEmpty { record.description },
                        sportType = sport,
                        distanceKm = record.distanceKm,
                        avgPace = record.avgPace,
                        duration = durationStr,
                        elevationGainM = 15,
                        caloriesBurned = record.calories,
                        mediaUri = record.photoUri,
                        createdAt = record.timestamp
                    )
                    postRepository.publishPost(newPost)
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi saveActivity: ${e.message}", e)
            Resource.Error(e.message ?: "Lỗi lưu hoạt động")
        }
    }

    override fun getAllActivities(): Flow<List<ActivityRecordEntity>> {
        return activityRecordDao.getAllActivities()
    }

    override fun getUserActivityStats(): Flow<UserActivityStats> {
        return activityRecordDao.getAllActivities().map { activities ->
            calculateStats(activities)
        }
    }

    override suspend fun syncActivitiesFromCloud(): Resource<Unit> {
        val currentUid = firebaseAuth.currentUser?.uid ?: return Resource.Success(Unit)
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUid)
                .collection("activities")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val sessionId = doc.getString("sessionId") ?: doc.id
                val existing = activityRecordDao.getActivityBySessionId(sessionId)
                if (existing == null) {
                    val entity = ActivityRecordEntity(
                        sessionId = sessionId,
                        title = doc.getString("title") ?: "Chạy bộ",
                        description = doc.getString("description") ?: "",
                        sportType = doc.getString("sportType") ?: "RUN",
                        distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                        durationSeconds = doc.getLong("durationSeconds") ?: 0L,
                        avgSpeedKmh = (doc.getDouble("avgSpeedKmh") ?: 0.0).toFloat(),
                        avgPace = doc.getString("avgPace") ?: "--:--",
                        calories = doc.getLong("calories")?.toInt() ?: 0,
                        routePointsJson = doc.getString("routePointsJson") ?: "",
                        photoUri = doc.getString("photoUri"),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                    activityRecordDao.insertActivity(entity)
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Lỗi syncActivitiesFromCloud: ${e.message}")
            Resource.Error(e.message ?: "Lỗi đồng bộ hoạt động từ đám mây")
        }
    }

    private fun calculateStats(activities: List<ActivityRecordEntity>): UserActivityStats {
        if (activities.isEmpty()) {
            return UserActivityStats()
        }

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfThisWeek = cal.timeInMillis

        var weeklyDist = 0.0
        var weeklyDur = 0
        var weeklyElev = 0

        val twelveWeeks = DoubleArray(12) { 0.0 }
        val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L

        for (act in activities) {
            if (act.timestamp >= startOfThisWeek) {
                weeklyDist += act.distanceKm
                weeklyDur += (act.durationSeconds / 60).toInt()
                weeklyElev += 15
            }

            val weeksAgo = ((startOfThisWeek - act.timestamp) / oneWeekMillis).toInt()
            val index = 11 - weeksAgo
            if (index in 0..11) {
                twelveWeeks[index] += act.distanceKm
            }
        }

        var streak = 0
        for (i in 11 downTo 0) {
            if (twelveWeeks[i] > 0.1) {
                streak++
            } else if (i != 11) {
                break
            }
        }

        return UserActivityStats(
            totalActivitiesCount = activities.size,
            weeklyDistanceKm = (weeklyDist * 10).toInt() / 10.0,
            weeklyDurationMinutes = weeklyDur,
            weeklyElevationGainM = weeklyElev,
            streakWeeks = maxOf(streak, if (activities.isNotEmpty()) 1 else 0),
            twelveWeeksProgress = twelveWeeks.toList()
        )
    }
}
