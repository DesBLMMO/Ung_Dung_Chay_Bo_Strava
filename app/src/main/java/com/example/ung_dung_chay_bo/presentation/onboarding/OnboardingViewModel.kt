package com.example.ung_dung_chay_bo.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.ExperienceLevel
import com.example.ung_dung_chay_bo.domain.model.Gender
import com.example.ung_dung_chay_bo.domain.model.RunningGoal
import com.example.ung_dung_chay_bo.domain.model.UserProfile
import com.example.ung_dung_chay_bo.domain.usecase.auth.GetCurrentUserUseCase
import com.example.ung_dung_chay_bo.domain.usecase.user.SaveOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveOnboardingUseCase: SaveOnboardingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onGoalSelected(goal: RunningGoal) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onHeightChange(heightCm: Int) {
        _uiState.update { it.copy(heightCm = heightCm) }
    }

    fun onWeightChange(weightKg: Int) {
        _uiState.update { it.copy(weightKg = weightKg) }
    }

    fun onLevelSelected(level: ExperienceLevel) {
        _uiState.update { it.copy(level = level) }
    }

    fun onFrequencyChange(frequency: Int) {
        _uiState.update { it.copy(weeklyFrequency = frequency) }
    }

    fun onNextStep() {
        val current = _uiState.value.currentStep
        if (current < 3) {
            _uiState.update { it.copy(currentStep = current + 1) }
        } else {
            submitOnboarding()
        }
    }

    fun onPreviousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.update { it.copy(currentStep = current - 1) }
        }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun submitOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentUser = getCurrentUserUseCase()
            val profile = UserProfile(
                uid = currentUser?.uid ?: "",
                displayName = currentUser?.displayName ?: "Runner",
                email = currentUser?.email ?: "",
                goal = _uiState.value.selectedGoal,
                gender = _uiState.value.gender,
                heightCm = _uiState.value.heightCm,
                weightKg = _uiState.value.weightKg,
                level = _uiState.value.level,
                weeklyFrequency = _uiState.value.weeklyFrequency,
                isOnboardingCompleted = true
            )

            val result = saveOnboardingUseCase(profile)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isCompleted = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
