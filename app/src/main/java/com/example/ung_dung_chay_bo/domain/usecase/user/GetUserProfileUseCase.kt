package com.example.ung_dung_chay_bo.domain.usecase.user

import com.example.ung_dung_chay_bo.domain.model.UserProfile
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile?> = userRepository.getUserProfile()
}
