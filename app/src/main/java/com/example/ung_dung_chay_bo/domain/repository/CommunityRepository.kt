package com.example.ung_dung_chay_bo.domain.repository

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.Challenge
import com.example.ung_dung_chay_bo.domain.model.Club
import com.example.ung_dung_chay_bo.domain.model.ClubPost
import com.example.ung_dung_chay_bo.domain.model.CommunityEvent
import com.example.ung_dung_chay_bo.domain.model.SportType
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun observeChallenges(currentUid: String): Flow<List<Challenge>>
    fun observeClubs(currentUid: String): Flow<List<Club>>
    fun observeClubPosts(clubId: String): Flow<List<ClubPost>>
    fun observeEvents(currentUid: String): Flow<List<CommunityEvent>>

    suspend fun toggleJoinChallenge(challengeId: String, currentUid: String): Resource<Boolean>
    suspend fun toggleJoinClub(clubId: String, currentUid: String): Resource<Boolean>
    suspend fun toggleEventGoing(eventId: String, currentUid: String): Resource<Boolean>

    suspend fun createClub(
        name: String,
        description: String,
        category: String,
        location: String,
        sportType: SportType,
        avatarUrl: String,
        bannerUrl: String,
        leaderUid: String,
        leaderName: String
    ): Resource<String>

    suspend fun postToClub(
        clubId: String,
        content: String,
        authorUid: String,
        authorName: String,
        authorAvatar: String,
        isLeader: Boolean,
        isAnnouncement: Boolean = false,
        distanceKm: Double? = null,
        durationFormatted: String? = null,
        pace: String? = null
    ): Resource<Boolean>

    suspend fun createClubChallenge(
        clubId: String,
        clubName: String,
        title: String,
        description: String,
        targetKm: Double,
        daysLeft: Int,
        sportType: SportType,
        badgeName: String,
        bannerUrl: String,
        creatorUid: String
    ): Resource<String>
}
