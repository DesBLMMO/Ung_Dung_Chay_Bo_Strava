package com.example.ung_dung_chay_bo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.usecase.auth.GetCurrentUserUseCase
import com.example.ung_dung_chay_bo.domain.usecase.auth.LoginWithEmailUseCase
import com.example.ung_dung_chay_bo.domain.usecase.auth.RegisterWithEmailUseCase
import com.example.ung_dung_chay_bo.domain.usecase.auth.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    val isUserLoggedIn: Boolean get() = getCurrentUserUseCase.isLoggedIn()

    fun onLoginEmailChange(email: String) {
        _loginUiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onLoginPasswordChange(password: String) {
        _loginUiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onLoginPasswordVisibilityToggle() {
        _loginUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginErrorConsumed() {
        _loginUiState.update { it.copy(errorMessage = null) }
    }

    fun loginWithEmail() {
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginWithEmailUseCase(
                email = _loginUiState.value.email,
                password = _loginUiState.value.password
            )
            when (result) {
                is Resource.Success -> _loginUiState.update {
                    it.copy(isLoading = false, isLoginSuccess = true)
                }
                is Resource.Error -> _loginUiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signInWithGoogleUseCase(idToken)
            when (result) {
                is Resource.Success -> _loginUiState.update {
                    it.copy(isLoading = false, isLoginSuccess = true)
                }
                is Resource.Error -> _loginUiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onRegisterNameChange(name: String) {
        _registerUiState.update { it.copy(displayName = name, errorMessage = null) }
    }

    fun onRegisterEmailChange(email: String) {
        _registerUiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onRegisterPasswordChange(password: String) {
        _registerUiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChange(confirmPassword: String) {
        _registerUiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun onRegisterPasswordVisibilityToggle() {
        _registerUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRegisterConfirmPasswordVisibilityToggle() {
        _registerUiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onRegisterErrorConsumed() {
        _registerUiState.update { it.copy(errorMessage = null) }
    }

    fun registerWithEmail() {
        viewModelScope.launch {
            _registerUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = registerWithEmailUseCase(
                displayName = _registerUiState.value.displayName,
                email = _registerUiState.value.email,
                password = _registerUiState.value.password,
                confirmPassword = _registerUiState.value.confirmPassword
            )
            when (result) {
                is Resource.Success -> _registerUiState.update {
                    it.copy(isLoading = false, isRegisterSuccess = true)
                }
                is Resource.Error -> _registerUiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
