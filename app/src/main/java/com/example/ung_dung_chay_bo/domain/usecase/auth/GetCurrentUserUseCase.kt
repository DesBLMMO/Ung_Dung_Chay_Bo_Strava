package com.example.ung_dung_chay_bo.domain.usecase.auth

import com.example.ung_dung_chay_bo.domain.model.User
import com.example.ung_dung_chay_bo.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? = authRepository.currentUser
    fun isLoggedIn(): Boolean = authRepository.isUserLoggedIn()
}
