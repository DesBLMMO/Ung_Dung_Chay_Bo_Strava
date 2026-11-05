package com.example.ung_dung_chay_bo.presentation.onboarding

import com.example.ung_dung_chay_bo.domain.model.ExperienceLevel
import com.example.ung_dung_chay_bo.domain.model.Gender
import com.example.ung_dung_chay_bo.domain.model.RunningGoal

data class OnboardingUiState(
    val currentStep: Int = 1,
    val selectedGoal: RunningGoal = RunningGoal.STAMINA,
    val gender: Gender = Gender.MALE,
    val heightCm: Int = 170,
    val weightKg: Int = 65,
    val level: ExperienceLevel = ExperienceLevel.BEGINNER,
    val weeklyFrequency: Int = 3,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)
