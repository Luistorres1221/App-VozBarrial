package com.vozbarrial.features.registerSI.presentation

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordVisible: Boolean = false,
    val acceptedTerms: Boolean = true,
    val receiveAlerts: Boolean = true,
    val error: String? = null,
    val showTerms: Boolean = false,
    val loading: Boolean = false,
)