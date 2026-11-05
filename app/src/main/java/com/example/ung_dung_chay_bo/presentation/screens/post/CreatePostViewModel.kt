package com.example.ung_dung_chay_bo.presentation.screens.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.model.PostVisibility
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.usecase.user.GetUserProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreatePostUiState(
    val title: String = "",
    val caption: String = "",
    val sportType: SportType = SportType.RUN,
    val distanceKmText: String = "5.0",
    val durationMinutesText: String = "28",
    val elevationGainMText: String = "35",
    val perceivedEffort: Int = 6,
    val visibility: PostVisibility = PostVisibility.EVERYONE,
    val selectedMediaUri: Uri? = null,
    val isLoading: Boolean = false,
    val isPostedSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private var currentUserName = "Runner"
    private var currentUserUid = ""

    init {
        currentUserUid = firebaseAuth.currentUser?.uid ?: ""
        currentUserName = firebaseAuth.currentUser?.displayName ?: "Runner"
        viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                if (profile != null && profile.displayName.isNotBlank()) {
                    currentUserName = profile.displayName
                }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, errorMessage = null) }
    fun onCaptionChange(v: String) = _uiState.update { it.copy(caption = v) }
    fun onSportTypeSelect(v: SportType) = _uiState.update { it.copy(sportType = v) }
    fun onDistanceChange(v: String) = _uiState.update { it.copy(distanceKmText = v) }
    fun onDurationChange(v: String) = _uiState.update { it.copy(durationMinutesText = v) }
    fun onElevationChange(v: String) = _uiState.update { it.copy(elevationGainMText = v) }
    fun onEffortChange(v: Int) = _uiState.update { it.copy(perceivedEffort = v) }
    fun onVisibilitySelect(v: PostVisibility) = _uiState.update { it.copy(visibility = v) }
    fun onMediaSelected(uri: Uri?) = _uiState.update { it.copy(selectedMediaUri = uri) }
    fun onRemoveMedia() = _uiState.update { it.copy(selectedMediaUri = null) }

    fun createPost(onSuccess: (FriendPost) -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên hoạt động!") }
            return
        }

        val distance = state.distanceKmText.toDoubleOrNull() ?: 5.0
        val durationMins = state.durationMinutesText.toIntOrNull() ?: 28
        val elevation = state.elevationGainMText.toIntOrNull() ?: 35

        val paceMinutes = if (distance > 0) (durationMins / distance).toInt() else 5
        val paceSeconds = if (distance > 0) (((durationMins / distance) - paceMinutes) * 60).toInt() else 30
        val avgPaceFormatted = String.format("%d:%02d /km", paceMinutes, paceSeconds)
        val durationFormatted = String.format("%02d:%02d", durationMins, 0)
        val calories = (distance * 65).toInt()

        val uid = firebaseAuth.currentUser?.uid ?: currentUserUid
        val name = firebaseAuth.currentUser?.displayName ?: currentUserName

        val newPost = FriendPost(
            id = "post_${UUID.randomUUID()}",
            authorUid = uid,
            authorName = name,
            createdAt = System.currentTimeMillis(),
            postedTimeAgo = "Vừa xong",
            title = state.title.trim(),
            caption = state.caption.trim(),
            sportType = state.sportType,
            distanceKm = distance,
            avgPace = avgPaceFormatted,
            duration = durationFormatted,
            elevationGainM = elevation,
            caloriesBurned = calories,
            perceivedEffort = state.perceivedEffort,
            mediaUri = state.selectedMediaUri?.toString(),
            kudosCount = 0,
            commentCount = 0,
            isKudosGiven = false,
            routePoints = listOf(
                0.15f to 0.75f, 0.35f to 0.25f, 0.65f to 0.2f,
                0.85f to 0.5f, 0.7f to 0.85f, 0.35f to 0.8f, 0.15f to 0.75f
            )
        )

        _uiState.update { it.copy(isPostedSuccess = true) }
        onSuccess(newPost)
    }
}
