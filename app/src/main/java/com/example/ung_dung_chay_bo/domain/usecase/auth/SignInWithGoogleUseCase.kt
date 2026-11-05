package com.example.ung_dung_chay_bo.domain.usecase.auth

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.User
import com.example.ung_dung_chay_bo.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Resource<User> {
        return authRepository.signInWithGoogle(idToken)
    }
}
