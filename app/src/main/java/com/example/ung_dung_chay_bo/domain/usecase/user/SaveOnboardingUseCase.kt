package com.example.ung_dung_chay_bo.domain.usecase.user

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.UserProfile
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import javax.inject.Inject

class SaveOnboardingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(profile: UserProfile): Resource<Unit> {
        val updatedProfile = profile.copy(isOnboardingCompleted = true)
        val saveResult = userRepository.saveUserProfile(updatedProfile)
        if (saveResult is Resource.Success) {
            userRepository.setOnboardingCompleted(true)
        }
        return saveResult
    }
}
