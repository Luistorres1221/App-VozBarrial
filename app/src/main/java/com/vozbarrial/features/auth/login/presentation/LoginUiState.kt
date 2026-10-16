package com.vozbarrial.features.auth.login.presentation

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val message: String = "",
    val success: Boolean = false,
    val dialogMessage: String = "",
    val showRecovery: Boolean = false,
)
