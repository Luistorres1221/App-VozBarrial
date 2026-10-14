package com.vozbarrial.features.auth.passwordRecoverySI.presentation

data class RecoveryUiState(
    val email: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val succeeded: Boolean = false,
)
