package com.vozbarrial.features.auth.passwordRecovery.presentation

data class RecoveryUiState(
    val email: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val succeeded: Boolean = false,
)
