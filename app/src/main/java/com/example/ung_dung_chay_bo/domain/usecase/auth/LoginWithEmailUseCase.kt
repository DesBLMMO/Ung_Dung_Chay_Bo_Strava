package com.example.ung_dung_chay_bo.domain.usecase.auth

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.User
import com.example.ung_dung_chay_bo.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("Email không được để trống")
        if (password.isBlank()) return Resource.Error("Mật khẩu không được để trống")
        if (password.length < 6) return Resource.Error("Mật khẩu phải có ít nhất 6 ký tự")
        return authRepository.loginWithEmail(email.trim(), password)
    }
}
