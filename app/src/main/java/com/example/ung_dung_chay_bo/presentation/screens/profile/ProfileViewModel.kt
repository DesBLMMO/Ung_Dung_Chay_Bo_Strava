package com.example.ung_dung_chay_bo.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.domain.model.ExperienceLevel
import com.example.ung_dung_chay_bo.domain.model.Gender
import com.example.ung_dung_chay_bo.domain.model.RunningGoal
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.UserProfile
import com.example.ung_dung_chay_bo.domain.repository.ActivityRepository
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import com.example.ung_dung_chay_bo.domain.usecase.auth.LogoutUseCase
import com.example.ung_dung_chay_bo.domain.usecase.user.GetUserProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ProfileSubTab(val title: String) {
    PROGRESS("Tiến trình"),
    ACTIVITIES("Hoạt động"),
    MORE("Xem thêm")
}

data class UserActivityItem(
    val id: String,
    val recordId: Long = 0L,
    val title: String,
    val sportType: SportType,
    val distanceKm: Double,
    val durationFormatted: String,
    val paceOrSpeed: String,
    val dateFormatted: String,
    val elevationGainM: Int = 15,
    val kudosCount: Int = 0
)

data class ProfileUiState(
    val profile: UserProfile? = null,
    val selectedSubTab: ProfileSubTab = ProfileSubTab.PROGRESS,
    val selectedSport: SportType = SportType.RUN,
    val activitiesCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val weeklyDistanceKm: Double = 0.0,
    val weeklyDurationMinutes: Int = 0,
    val weeklyElevationGainM: Int = 0,
    val twelveWeeksProgress: List<Double> = List(12) { 0.0 },
    val streakWeeks: Int = 0,
    val weeklyGoalKm: Double = 25.0,
    val recentActivities: List<UserActivityItem> = emptyList(),
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository,
    private val logoutUseCase: LogoutUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val currentUid: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    init {
        loadUserProfile()
        observeRealActivities()
        observeActivityStats()
        observeSocialCounts()
        syncActivitiesFromCloud()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().collect { userProfile ->
                _uiState.update {
                    it.copy(
                        profile = userProfile ?: UserProfile(
                            displayName = firebaseAuth.currentUser?.displayName ?: "Runner",
                            email = firebaseAuth.currentUser?.email ?: "",
                            goal = RunningGoal.STAMINA,
                            heightCm = 170,
                            weightKg = 65
                        )
                    )
                }
            }
        }
    }

    private fun observeRealActivities() {
        viewModelScope.launch {
            activityRepository.getAllActivities().collect { entities ->
                val items = entities.map { entity ->
                    val sport = runCatching { SportType.valueOf(entity.sportType) }.getOrDefault(SportType.RUN)
                    UserActivityItem(
                        id = entity.sessionId,
                        recordId = entity.id,
                        title = entity.title,
                        sportType = sport,
                        distanceKm = entity.distanceKm,
                        durationFormatted = GpsUtils.formatDuration(entity.durationSeconds),
                        paceOrSpeed = entity.avgPace,
                        dateFormatted = formatActivityDate(entity.timestamp),
                        elevationGainM = 15,
                        kudosCount = 0
                    )
                }
                _uiState.update { it.copy(recentActivities = items) }
            }
        }
    }

    private fun observeActivityStats() {
        viewModelScope.launch {
            activityRepository.getUserActivityStats().collect { stats ->
                _uiState.update {
                    it.copy(
                        activitiesCount = stats.totalActivitiesCount,
                        weeklyDistanceKm = stats.weeklyDistanceKm,
                        weeklyDurationMinutes = stats.weeklyDurationMinutes,
                        weeklyElevationGainM = stats.weeklyElevationGainM,
                        streakWeeks = stats.streakWeeks,
                        twelveWeeksProgress = stats.twelveWeeksProgress
                    )
                }
            }
        }
    }

    private fun observeSocialCounts() {
        val uid = currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            userRepository.getFollowersCount(uid).collect { count ->
                _uiState.update { it.copy(followersCount = count) }
            }
        }

        viewModelScope.launch {
            userRepository.getFollowingCount(uid).collect { count ->
                _uiState.update { it.copy(followingCount = count) }
            }
        }
    }

    private fun syncActivitiesFromCloud() {
        viewModelScope.launch {
            activityRepository.syncActivitiesFromCloud()
        }
    }

    fun selectSubTab(tab: ProfileSubTab) {
        _uiState.update { it.copy(selectedSubTab = tab) }
    }

    fun selectSport(sport: SportType) {
        _uiState.update { it.copy(selectedSport = sport) }
    }

    fun updateProfile(
        displayName: String,
        heightCm: Int,
        weightKg: Int,
        goal: RunningGoal
    ) {
        viewModelScope.launch {
            val current = _uiState.value.profile ?: UserProfile()
            val updated = current.copy(
                displayName = displayName,
                heightCm = heightCm,
                weightKg = weightKg,
                goal = goal
            )
            userRepository.saveUserProfile(updated)
            _uiState.update { it.copy(profile = updated) }
        }
    }

    fun updateWeeklyGoal(goalKm: Double) {
        _uiState.update { it.copy(weeklyGoalKm = goalKm) }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    private fun formatActivityDate(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        if (diff < 24 * 60 * 60 * 1000L) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return "Hôm nay lúc ${sdf.format(Date(epochMillis))}"
        }
        val sdf = SimpleDateFormat("EEEE, dd/MM", Locale("vi"))
        return sdf.format(Date(epochMillis))
    }
}
