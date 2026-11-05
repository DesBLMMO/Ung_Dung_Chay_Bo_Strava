package com.example.ung_dung_chay_bo.domain.usecase.auth

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.User
import com.example.ung_dung_chay_bo.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Resource<User> {
        if (displayName.isBlank()) return Resource.Error("Họ và tên không được để trống")
        if (email.isBlank()) return Resource.Error("Email không được để trống")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Resource.Error("Email không hợp lệ")
        if (password.length < 6) return Resource.Error("Mật khẩu phải có ít nhất 6 ký tự")
        if (password != confirmPassword) return Resource.Error("Mật khẩu xác nhận không khớp")
        return authRepository.registerWithEmail(email.trim(), password, displayName.trim())
    }
}
