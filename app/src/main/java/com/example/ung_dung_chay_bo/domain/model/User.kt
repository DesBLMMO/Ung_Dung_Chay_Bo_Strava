package com.example.ung_dung_chay_bo.domain.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false
)
