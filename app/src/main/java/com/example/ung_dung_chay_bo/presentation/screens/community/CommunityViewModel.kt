package com.example.ung_dung_chay_bo.presentation.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.Challenge
import com.example.ung_dung_chay_bo.domain.model.Club
import com.example.ung_dung_chay_bo.domain.model.ClubPost
import com.example.ung_dung_chay_bo.domain.model.CommunityEvent
import com.example.ung_dung_chay_bo.domain.model.CommunityTab
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.CommunityRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val selectedTab: CommunityTab = CommunityTab.CHALLENGES_EVENTS,
    val selectedSportFilter: SportType? = null,
    val challenges: List<Challenge> = emptyList(),
    val clubs: List<Club> = emptyList(),
    val events: List<CommunityEvent> = emptyList(),
    val userJoinedChallengeIds: Set<String> = emptySet(),
    val userJoinedClubIds: Set<String> = emptySet(),
    val selectedClubForDetail: Club? = null,
    val selectedClubPosts: List<ClubPost> = emptyList(),
    val showCreateClubDialog: Boolean = false,
    val showCreatePostDialog: Boolean = false,
    val showCreateChallengeDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private var clubPostsJob: Job? = null

    val currentUid: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    val currentDisplayName: String
        get() = firebaseAuth.currentUser?.displayName ?: "Thành viên"

    val currentPhotoUrl: String
        get() = firebaseAuth.currentUser?.photoUrl?.toString() ?: ""

    init {
        observeCommunityData()
    }

    private fun observeCommunityData() {
        val uid = currentUid
        viewModelScope.launch {
            communityRepository.observeChallenges(uid).collect { challenges ->
                val joinedIds = challenges.filter { it.isJoined }.map { it.id }.toSet()
                _uiState.update {
                    it.copy(
                        challenges = challenges,
                        userJoinedChallengeIds = joinedIds,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            communityRepository.observeClubs(uid).collect { clubs ->
                val joinedClubIds = clubs.filter { it.isJoined }.map { it.id }.toSet()
                _uiState.update { state ->
                    val currentSelected = state.selectedClubForDetail
                    val updatedSelected = if (currentSelected != null) {
                        clubs.find { it.id == currentSelected.id } ?: currentSelected
                    } else null

                    state.copy(
                        clubs = clubs,
                        userJoinedClubIds = joinedClubIds,
                        selectedClubForDetail = updatedSelected
                    )
                }
            }
        }

        viewModelScope.launch {
            communityRepository.observeEvents(uid).collect { events ->
                _uiState.update {
                    it.copy(events = events)
                }
            }
        }
    }

    fun selectTab(tab: CommunityTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectSportFilter(sport: SportType?) {
        _uiState.update { it.copy(selectedSportFilter = sport) }
    }

    fun openClubDetail(club: Club) {
        _uiState.update { it.copy(selectedClubForDetail = club) }
        observeClubPosts(club.id)
    }

    fun closeClubDetail() {
        clubPostsJob?.cancel()
        _uiState.update { it.copy(selectedClubForDetail = null, selectedClubPosts = emptyList()) }
    }

    private fun observeClubPosts(clubId: String) {
        clubPostsJob?.cancel()
        clubPostsJob = viewModelScope.launch {
            communityRepository.observeClubPosts(clubId).collect { posts ->
                _uiState.update { it.copy(selectedClubPosts = posts) }
            }
        }
    }

    fun openCreateClubDialog() {
        _uiState.update { it.copy(showCreateClubDialog = true) }
    }

    fun closeCreateClubDialog() {
        _uiState.update { it.copy(showCreateClubDialog = false) }
    }

    fun openCreatePostDialog() {
        _uiState.update { it.copy(showCreatePostDialog = true) }
    }

    fun closeCreatePostDialog() {
        _uiState.update { it.copy(showCreatePostDialog = false) }
    }

    fun openCreateChallengeDialog() {
        _uiState.update { it.copy(showCreateChallengeDialog = true) }
    }

    fun closeCreateChallengeDialog() {
        _uiState.update { it.copy(showCreateChallengeDialog = false) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }

    fun createClub(
        name: String,
        description: String,
        category: String,
        location: String,
        sportType: SportType,
        avatarUrl: String,
        bannerUrl: String
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên câu lạc bộ") }
            return
        }

        val uid = currentUid
        val leaderName = currentDisplayName

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = communityRepository.createClub(
                name = name,
                description = description,
                category = category,
                location = location,
                sportType = sportType,
                avatarUrl = avatarUrl,
                bannerUrl = bannerUrl,
                leaderUid = uid,
                leaderName = leaderName
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showCreateClubDialog = false,
                            userMessage = "Đã tạo câu lạc bộ thành công!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message ?: "Không thể tạo câu lạc bộ"
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun sharePostToClub(
        clubId: String,
        content: String,
        isAnnouncement: Boolean = false,
        distanceKm: Double? = null,
        durationFormatted: String? = null,
        pace: String? = null
    ) {
        if (content.isBlank()) return
        val uid = currentUid
        val name = currentDisplayName
        val avatar = currentPhotoUrl
        val isLeader = _uiState.value.selectedClubForDetail?.isLeader == true

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = communityRepository.postToClub(
                clubId = clubId,
                content = content,
                authorUid = uid,
                authorName = name,
                authorAvatar = avatar,
                isLeader = isLeader,
                isAnnouncement = isAnnouncement,
                distanceKm = distanceKm,
                durationFormatted = durationFormatted,
                pace = pace
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showCreatePostDialog = false,
                            userMessage = if (isAnnouncement) "Đã đăng thông báo nhóm!" else "Đã chia sẻ buổi chạy vào nhóm!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message ?: "Lỗi đăng bài"
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun createClubChallenge(
        clubId: String,
        clubName: String,
        title: String,
        description: String,
        targetKm: Double,
        daysLeft: Int,
        sportType: SportType,
        badgeName: String
    ) {
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên thử thách") }
            return
        }
        val uid = currentUid

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = communityRepository.createClubChallenge(
                clubId = clubId,
                clubName = clubName,
                title = title,
                description = description,
                targetKm = targetKm,
                daysLeft = daysLeft,
                sportType = sportType,
                badgeName = badgeName,
                bannerUrl = "",
                creatorUid = uid
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showCreateChallengeDialog = false,
                            userMessage = "Đã tạo thử thách mới cho CLB $clubName!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message ?: "Lỗi tạo thử thách"
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun toggleJoinChallenge(challengeId: String) {
        val uid = currentUid
        _uiState.update { state ->
            val updatedJoined = state.userJoinedChallengeIds.toMutableSet()
            val wasJoined = updatedJoined.contains(challengeId)
            if (wasJoined) updatedJoined.remove(challengeId) else updatedJoined.add(challengeId)
            val updatedChallenges = state.challenges.map { c ->
                if (c.id == challengeId) {
                    val isNowJoined = !wasJoined
                    c.copy(
                        isJoined = isNowJoined,
                        participantsCount = if (isNowJoined) c.participantsCount + 1 else maxOf(0, c.participantsCount - 1)
                    )
                } else c
            }
            state.copy(userJoinedChallengeIds = updatedJoined, challenges = updatedChallenges)
        }
        viewModelScope.launch { communityRepository.toggleJoinChallenge(challengeId, uid) }
    }

    fun toggleJoinClub(clubId: String) {
        val uid = currentUid
        _uiState.update { state ->
            val updatedJoined = state.userJoinedClubIds.toMutableSet()
            val wasJoined = updatedJoined.contains(clubId)
            if (wasJoined) updatedJoined.remove(clubId) else updatedJoined.add(clubId)
            val updatedClubs = state.clubs.map { club ->
                if (club.id == clubId) {
                    val isNowJoined = !wasJoined
                    club.copy(
                        isJoined = isNowJoined,
                        memberCount = if (isNowJoined) club.memberCount + 1 else maxOf(0, club.memberCount - 1)
                    )
                } else club
            }
            val updatedSelectedClub = if (state.selectedClubForDetail?.id == clubId) {
                state.selectedClubForDetail.copy(
                    isJoined = !wasJoined,
                    memberCount = if (!wasJoined) state.selectedClubForDetail.memberCount + 1 else maxOf(0, state.selectedClubForDetail.memberCount - 1)
                )
            } else state.selectedClubForDetail
            state.copy(userJoinedClubIds = updatedJoined, clubs = updatedClubs, selectedClubForDetail = updatedSelectedClub)
        }
        viewModelScope.launch { communityRepository.toggleJoinClub(clubId, uid) }
    }

    fun toggleEventGoing(eventId: String) {
        val uid = currentUid
        _uiState.update { state ->
            val updatedEvents = state.events.map { event ->
                if (event.id == eventId) {
                    val newGoing = !event.isGoing
                    event.copy(
                        isGoing = newGoing,
                        participantsCount = if (newGoing) event.participantsCount + 1 else maxOf(0, event.participantsCount - 1)
                    )
                } else event
            }
            state.copy(events = updatedEvents)
        }
        viewModelScope.launch { communityRepository.toggleEventGoing(eventId, uid) }
    }
}
