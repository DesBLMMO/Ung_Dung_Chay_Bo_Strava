package com.example.ung_dung_chay_bo.domain.repository

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.AppNotification
import com.example.ung_dung_chay_bo.domain.model.CommunityChecklistState
import com.example.ung_dung_chay_bo.domain.model.SuggestedUser
import com.example.ung_dung_chay_bo.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile?>
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun saveUserProfile(profile: UserProfile): Resource<Unit>
    suspend fun setOnboardingCompleted(completed: Boolean): Resource<Unit>

    fun getCommunityChecklistState(): Flow<CommunityChecklistState>
    suspend fun updateChecklistFirstRun(completed: Boolean): Resource<Unit>
    suspend fun updateChecklistFollowCount(count: Int): Resource<Unit>
    suspend fun updateChecklistProfilePhoto(completed: Boolean): Resource<Unit>
    suspend fun updateChecklistDevice(connected: Boolean, deviceName: String?): Resource<Unit>
    suspend fun dismissCommunityBanner(): Resource<Unit>

    fun getSuggestedUsers(currentUid: String): Flow<List<SuggestedUser>>
    fun searchUsers(query: String, currentUid: String): Flow<List<SuggestedUser>>
    fun getFollowingUids(currentUid: String): Flow<Set<String>>
    fun getFollowersCount(uid: String): Flow<Int>
    fun getFollowingCount(uid: String): Flow<Int>
    suspend fun toggleFollow(currentUid: String, targetUid: String, targetName: String): Resource<Boolean>

    fun getNotifications(currentUid: String): Flow<List<AppNotification>>
    suspend fun addNotification(targetUid: String, notification: AppNotification): Resource<Unit>
}
