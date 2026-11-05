package com.example.ung_dung_chay_bo.domain.repository

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: User?
    fun isUserLoggedIn(): Boolean
    suspend fun loginWithEmail(email: String, password: String): Resource<User>
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Resource<User>
    suspend fun signInWithGoogle(idToken: String): Resource<User>
    suspend fun logout(): Resource<Unit>
}
