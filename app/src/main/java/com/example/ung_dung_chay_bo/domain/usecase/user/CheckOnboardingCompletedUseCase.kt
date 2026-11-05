package com.example.ung_dung_chay_bo.domain.usecase.user

import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckOnboardingCompletedUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Boolean> = userRepository.isOnboardingCompleted()
}
