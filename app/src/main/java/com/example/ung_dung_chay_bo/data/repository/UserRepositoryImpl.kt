package com.example.ung_dung_chay_bo.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.AppNotification
import com.example.ung_dung_chay_bo.domain.model.CommunityChecklistState
import com.example.ung_dung_chay_bo.domain.model.ExperienceLevel
import com.example.ung_dung_chay_bo.domain.model.Gender
import com.example.ung_dung_chay_bo.domain.model.RunningGoal
import com.example.ung_dung_chay_bo.domain.model.SuggestedUser
import com.example.ung_dung_chay_bo.domain.model.UserProfile
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
        private val KEY_UID = stringPreferencesKey("user_uid")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_GOAL = stringPreferencesKey("running_goal")
        private val KEY_GENDER = stringPreferencesKey("gender")
        private val KEY_HEIGHT = intPreferencesKey("height_cm")
        private val KEY_WEIGHT = intPreferencesKey("weight_kg")
        private val KEY_LEVEL = stringPreferencesKey("experience_level")
        private val KEY_FREQUENCY = intPreferencesKey("weekly_frequency")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")

        private val KEY_CHECKLIST_FIRST_RUN = booleanPreferencesKey("checklist_first_run")
        private val KEY_CHECKLIST_FOLLOW_COUNT = intPreferencesKey("checklist_follow_count")
        private val KEY_CHECKLIST_PROFILE_PHOTO = booleanPreferencesKey("checklist_profile_photo")
        private val KEY_CHECKLIST_DEVICE_DONE = booleanPreferencesKey("checklist_device_done")
        private val KEY_CONNECTED_DEVICE_NAME = stringPreferencesKey("connected_device_name")
        private val KEY_CHECKLIST_DISMISSED = booleanPreferencesKey("checklist_banner_dismissed")
    }

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val currentUid = firebaseAuth.currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            val sub = dataStore.data.catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
                .map { prefs ->
                    UserProfile(
                        uid = "",
                        displayName = prefs[KEY_DISPLAY_NAME] ?: "Runner",
                        email = prefs[KEY_EMAIL] ?: "",
                        goal = RunningGoal.valueOf(prefs[KEY_GOAL] ?: RunningGoal.STAMINA.name),
                        gender = Gender.valueOf(prefs[KEY_GENDER] ?: Gender.MALE.name),
                        heightCm = prefs[KEY_HEIGHT] ?: 170,
                        weightKg = prefs[KEY_WEIGHT] ?: 65,
                        level = ExperienceLevel.valueOf(prefs[KEY_LEVEL] ?: ExperienceLevel.BEGINNER.name),
                        weeklyFrequency = prefs[KEY_FREQUENCY] ?: 3,
                        isOnboardingCompleted = prefs[KEY_ONBOARDING_DONE] ?: false
                    )
                }
            val job = CoroutineScope(Dispatchers.IO).launch {
                sub.collect { trySend(it) }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Lỗi đọc UserProfile từ Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val goalName = snapshot.getString("goal") ?: RunningGoal.STAMINA.name
                    val genderName = snapshot.getString("gender") ?: Gender.MALE.name
                    val levelName = snapshot.getString("level") ?: ExperienceLevel.BEGINNER.name

                    val profile = UserProfile(
                        uid = currentUid,
                        displayName = snapshot.getString("displayName") ?: firebaseAuth.currentUser?.displayName ?: "Runner",
                        email = snapshot.getString("email") ?: firebaseAuth.currentUser?.email ?: "",
                        goal = runCatching { RunningGoal.valueOf(goalName) }.getOrDefault(RunningGoal.STAMINA),
                        gender = runCatching { Gender.valueOf(genderName) }.getOrDefault(Gender.MALE),
                        heightCm = snapshot.getLong("heightCm")?.toInt() ?: 170,
                        weightKg = snapshot.getLong("weightKg")?.toInt() ?: 65,
                        level = runCatching { ExperienceLevel.valueOf(levelName) }.getOrDefault(ExperienceLevel.BEGINNER),
                        weeklyFrequency = snapshot.getLong("weeklyFrequency")?.toInt() ?: 3,
                        isOnboardingCompleted = snapshot.getBoolean("isOnboardingCompleted") ?: true
                    )
                    trySend(profile)

                    CoroutineScope(Dispatchers.IO).launch {
                        dataStore.edit { prefs ->
                            prefs[KEY_UID] = profile.uid
                            prefs[KEY_DISPLAY_NAME] = profile.displayName
                            prefs[KEY_EMAIL] = profile.email
                            prefs[KEY_GOAL] = profile.goal.name
                            prefs[KEY_GENDER] = profile.gender.name
                            prefs[KEY_HEIGHT] = profile.heightCm
                            prefs[KEY_WEIGHT] = profile.weightKg
                            prefs[KEY_LEVEL] = profile.level.name
                            prefs[KEY_FREQUENCY] = profile.weeklyFrequency
                            prefs[KEY_ONBOARDING_DONE] = profile.isOnboardingCompleted
                        }
                    }
                } else {
                    val authUser = firebaseAuth.currentUser
                    val defaultProfile = UserProfile(
                        uid = currentUid,
                        displayName = authUser?.displayName ?: "Runner",
                        email = authUser?.email ?: "",
                        goal = RunningGoal.STAMINA,
                        gender = Gender.MALE,
                        heightCm = 170,
                        weightKg = 65,
                        level = ExperienceLevel.BEGINNER,
                        weeklyFrequency = 3,
                        isOnboardingCompleted = false
                    )
                    trySend(defaultProfile)
                }
            }

        awaitClose { listener.remove() }
    }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                prefs[KEY_ONBOARDING_DONE] ?: false
            }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            val uid = profile.uid.ifEmpty { firebaseAuth.currentUser?.uid ?: "" }

            dataStore.edit { prefs ->
                prefs[KEY_UID] = uid
                prefs[KEY_DISPLAY_NAME] = profile.displayName
                prefs[KEY_EMAIL] = profile.email
                prefs[KEY_GOAL] = profile.goal.name
                prefs[KEY_GENDER] = profile.gender.name
                prefs[KEY_HEIGHT] = profile.heightCm
                prefs[KEY_WEIGHT] = profile.weightKg
                prefs[KEY_LEVEL] = profile.level.name
                prefs[KEY_FREQUENCY] = profile.weeklyFrequency
                prefs[KEY_ONBOARDING_DONE] = true
            }

            if (uid.isNotEmpty()) {
                val profileMap = hashMapOf(
                    "uid" to uid,
                    "displayName" to profile.displayName,
                    "email" to profile.email,
                    "goal" to profile.goal.name,
                    "gender" to profile.gender.name,
                    "heightCm" to profile.heightCm,
                    "weightKg" to profile.weightKg,
                    "level" to profile.level.name,
                    "weeklyFrequency" to profile.weeklyFrequency,
                    "isOnboardingCompleted" to true,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                try {
                    firestore.collection(Constants.USERS_COLLECTION)
                        .document(uid)
                        .set(profileMap, SetOptions.merge())
                        .await()
                } catch (firestoreError: Exception) {
                    Log.w(TAG, "Không thể đồng bộ Firestore: ${firestoreError.message}")
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi lưu UserProfile: ${e.message}", e)
            Resource.Error("Không thể lưu thông tin hồ sơ: ${e.localizedMessage}")
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean): Resource<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[KEY_ONBOARDING_DONE] = completed
            }
            val uid = firebaseAuth.currentUser?.uid
            if (!uid.isNullOrEmpty()) {
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(uid)
                    .set(mapOf("isOnboardingCompleted" to completed), SetOptions.merge())
                    .await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi lưu trạng thái")
        }
    }

    override fun getCommunityChecklistState(): Flow<CommunityChecklistState> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                CommunityChecklistState(
                    isFirstRunUploaded = prefs[KEY_CHECKLIST_FIRST_RUN] ?: false,
                    followedUsersCount = prefs[KEY_CHECKLIST_FOLLOW_COUNT] ?: 0,
                    isProfilePhotoAdded = prefs[KEY_CHECKLIST_PROFILE_PHOTO] ?: false,
                    isDeviceConnected = prefs[KEY_CHECKLIST_DEVICE_DONE] ?: false,
                    connectedDeviceName = prefs[KEY_CONNECTED_DEVICE_NAME],
                    isDismissed = prefs[KEY_CHECKLIST_DISMISSED] ?: false
                )
            }
    }

    override suspend fun updateChecklistFirstRun(completed: Boolean): Resource<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[KEY_CHECKLIST_FIRST_RUN] = completed
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi cập nhật")
        }
    }

    override suspend fun updateChecklistFollowCount(count: Int): Resource<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[KEY_CHECKLIST_FOLLOW_COUNT] = count
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi cập nhật")
        }
    }

    override suspend fun updateChecklistProfilePhoto(completed: Boolean): Resource<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[KEY_CHECKLIST_PROFILE_PHOTO] = completed
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi cập nhật")
        }
    }

    override suspend fun updateChecklistDevice(connected: Boolean, deviceName: String?): Resource<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[KEY_CHECKLIST_DEVICE_DONE] = connected
                if (deviceName != null) {
                    prefs[KEY_CONNECTED_DEVICE_NAME] = deviceName
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi cập nhật thiết bị")
        }
    }

    override suspend fun dismissCommunityBanner(): Resource<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[KEY_CHECKLIST_DISMISSED] = true
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi cập nhật")
        }
    }

    override fun getSuggestedUsers(currentUid: String): Flow<List<SuggestedUser>> = callbackFlow {
        val followingSub = firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUid.ifEmpty { "anonymous" })
            .collection("following")

        val usersSub = firestore.collection(Constants.USERS_COLLECTION)
            .limit(20)

        val listener = usersSub.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Lỗi lấy suggested users: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            followingSub.get().addOnSuccessListener { followingSnap ->
                val followingUids = followingSnap.documents.map { it.id }.toSet()

                val users = snapshot?.documents?.filter { it.id != currentUid }?.map { doc ->
                    val displayName = doc.getString("displayName") ?: "Runner"
                    SuggestedUser(
                        id = doc.id,
                        name = displayName,
                        username = "@" + (doc.getString("email")?.substringBefore("@") ?: displayName.lowercase().replace(" ", "_")),
                        role = doc.getString("role") ?: "Vận động viên phong trào",
                        location = doc.getString("location") ?: "Việt Nam",
                        weeklyDistanceKm = doc.getDouble("weeklyDistanceKm") ?: 15.0,
                        isFollowing = doc.id in followingUids
                    )
                } ?: emptyList()

                if (users.isEmpty()) {
                    seedDefaultUsersIfEmpty()
                    trySend(getDefaultFallbackUsers())
                } else {
                    trySend(users)
                }
            }.addOnFailureListener {
                val users = snapshot?.documents?.filter { it.id != currentUid }?.map { doc ->
                    val displayName = doc.getString("displayName") ?: "Runner"
                    SuggestedUser(
                        id = doc.id,
                        name = displayName,
                        username = "@" + (doc.getString("email")?.substringBefore("@") ?: displayName.lowercase().replace(" ", "_")),
                        role = doc.getString("role") ?: "Vận động viên",
                        location = doc.getString("location") ?: "Việt Nam",
                        weeklyDistanceKm = doc.getDouble("weeklyDistanceKm") ?: 12.0,
                        isFollowing = false
                    )
                } ?: getDefaultFallbackUsers()
                trySend(users)
            }
        }

        awaitClose { listener.remove() }
    }

    override fun searchUsers(query: String, currentUid: String): Flow<List<SuggestedUser>> = callbackFlow {
        val q = query.trim().lowercase()
        val listener = firestore.collection(Constants.USERS_COLLECTION)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val followingSub = firestore.collection(Constants.USERS_COLLECTION)
                    .document(currentUid.ifEmpty { "anonymous" })
                    .collection("following")

                followingSub.get().addOnSuccessListener { followingSnap ->
                    val followingUids = followingSnap.documents.map { it.id }.toSet()
                    val filtered = snapshot?.documents?.filter { doc ->
                        doc.id != currentUid && (
                            q.isEmpty() ||
                            doc.getString("displayName")?.lowercase()?.contains(q) == true ||
                            doc.getString("email")?.lowercase()?.contains(q) == true
                        )
                    }?.map { doc ->
                        val name = doc.getString("displayName") ?: "Runner"
                        SuggestedUser(
                            id = doc.id,
                            name = name,
                            username = "@" + (doc.getString("email")?.substringBefore("@") ?: name.lowercase().replace(" ", "_")),
                            role = doc.getString("role") ?: "Vận động viên",
                            location = doc.getString("location") ?: "Việt Nam",
                            weeklyDistanceKm = doc.getDouble("weeklyDistanceKm") ?: 15.0,
                            isFollowing = doc.id in followingUids
                        )
                    } ?: emptyList()

                    trySend(filtered)
                }.addOnFailureListener {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getFollowingUids(currentUid: String): Flow<Set<String>> = callbackFlow {
        if (currentUid.isEmpty()) {
            trySend(emptySet())
            awaitClose { }
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUid)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                val uids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                trySend(uids)
            }
        awaitClose { listener.remove() }
    }

    override fun getFollowersCount(uid: String): Flow<Int> = callbackFlow {
        if (uid.isEmpty()) {
            trySend(0)
            awaitClose { }
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    override fun getFollowingCount(uid: String): Flow<Int> = callbackFlow {
        if (uid.isEmpty()) {
            trySend(0)
            awaitClose { }
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleFollow(
        currentUid: String,
        targetUid: String,
        targetName: String
    ): Resource<Boolean> {
        if (currentUid.isEmpty() || targetUid.isEmpty() || currentUid == targetUid) {
            return Resource.Error("Thao tác không hợp lệ")
        }
        return try {
            val followingRef = firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUid)
                .collection("following")
                .document(targetUid)

            val targetFollowersRef = firestore.collection(Constants.USERS_COLLECTION)
                .document(targetUid)
                .collection("followers")
                .document(currentUid)

            val doc = followingRef.get().await()
            val isNowFollowing: Boolean

            if (doc.exists()) {
                followingRef.delete().await()
                targetFollowersRef.delete().await()
                isNowFollowing = false
            } else {
                val data = mapOf(
                    "targetUid" to targetUid,
                    "targetName" to targetName,
                    "followedAt" to com.google.firebase.Timestamp.now()
                )
                followingRef.set(data).await()
                targetFollowersRef.set(mapOf("followerUid" to currentUid, "followedAt" to com.google.firebase.Timestamp.now())).await()
                isNowFollowing = true

                val myName = firebaseAuth.currentUser?.displayName ?: "Người dùng"
                addNotification(
                    targetUid = targetUid,
                    notification = AppNotification(
                        id = "notif_${System.currentTimeMillis()}",
                        senderName = myName,
                        actionText = "đã bắt đầu theo dõi bạn.",
                        timeAgo = "Vừa xong"
                    )
                )
            }

            val followingCount = firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUid)
                .collection("following")
                .get().await().size()
            updateChecklistFollowCount(followingCount)

            Resource.Success(isNowFollowing)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi toggleFollow: ${e.message}", e)
            Resource.Error("Không thể cập nhật theo dõi: ${e.message}")
        }
    }

    override fun getNotifications(currentUid: String): Flow<List<AppNotification>> = callbackFlow {
        if (currentUid.isEmpty()) {
            trySend(getDefaultNotifications())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUid)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Lỗi đọc notifications: ${error.message}")
                    trySend(getDefaultNotifications())
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val sender = doc.getString("senderName") ?: "Hệ thống"
                    val action = doc.getString("actionText") ?: ""
                    val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    AppNotification(
                        id = doc.id,
                        senderName = sender,
                        actionText = action,
                        timeAgo = formatTimeAgo(ts),
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                } ?: emptyList()

                if (list.isEmpty()) {
                    trySend(getDefaultNotifications())
                } else {
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addNotification(
        targetUid: String,
        notification: AppNotification
    ): Resource<Unit> {
        return try {
            val data = hashMapOf(
                "senderName" to notification.senderName,
                "actionText" to notification.actionText,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )
            firestore.collection(Constants.USERS_COLLECTION)
                .document(targetUid)
                .collection("notifications")
                .document(notification.id)
                .set(data)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Lỗi thêm thông báo: ${e.message}")
            Resource.Error(e.message ?: "Lỗi thêm thông báo")
        }
    }

    private fun seedDefaultUsersIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val seedUsers = listOf(
                    mapOf("uid" to "user_seed_1", "displayName" to "Trần Hoàng Minh", "role" to "Marathon Finisher 42K", "weeklyDistanceKm" to 38.5, "location" to "Hà Nội", "email" to "minh.tran@strava.vn"),
                    mapOf("uid" to "user_seed_2", "displayName" to "Lê Mai Phương", "role" to "Hà Nội Runners Club", "weeklyDistanceKm" to 24.0, "location" to "Hà Nội", "email" to "phuong.le@strava.vn"),
                    mapOf("uid" to "user_seed_3", "displayName" to "Vũ Quang Dũng", "role" to "Pacer 5:30 • Ultra Trail", "weeklyDistanceKm" to 52.1, "location" to "Đà Nẵng", "email" to "dung.vu@strava.vn"),
                    mapOf("uid" to "user_seed_4", "displayName" to "Đặng Thùy Linh", "role" to "Triathlon Enthusiast", "weeklyDistanceKm" to 31.0, "location" to "TP. Hồ Chí Minh", "email" to "linh.dang@strava.vn")
                )
                for (u in seedUsers) {
                    val uid = u["uid"] as String
                    firestore.collection(Constants.USERS_COLLECTION)
                        .document(uid)
                        .set(u, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Seed user failed: ${e.message}")
            }
        }
    }

    private fun getDefaultFallbackUsers() = listOf(
        SuggestedUser("user_seed_1", "Trần Hoàng Minh", "@minh_runner", "Marathon Finisher 42K", "Hà Nội", 2, 38.5),
        SuggestedUser("user_seed_2", "Lê Mai Phương", "@phuong_le", "Hà Nội Runners Club", "Hà Nội", 4, 24.0),
        SuggestedUser("user_seed_3", "Vũ Quang Dũng", "@dung_pacer", "Pacer 5:30 • Ultra Trail", "Đà Nẵng", 1, 52.1)
    )

    private fun getDefaultNotifications() = listOf(
        AppNotification("notif_1", "Nguyễn Văn Nam", "đã thả Kudos cho buổi chạy của bạn.", "10 phút trước"),
        AppNotification("notif_2", "Đặng Thùy Linh", "đã bình luận: 'Pace chạy xuất sắc quá!'", "1 giờ trước"),
        AppNotification("notif_3", "CLB Sài Gòn Runner", "đã mời bạn tham gia thử thách 50km tháng 10.", "Hôm qua")
    )

    private fun formatTimeAgo(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        return when {
            diff < 60_000L -> "Vừa xong"
            diff < 3_600_000L -> "${diff / 60_000L} phút trước"
            diff < 86_400_000L -> "${diff / 3_600_000L} giờ trước"
            diff < 172_800_000L -> "Hôm qua"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
                sdf.format(Date(epochMillis))
            }
        }
    }
}
