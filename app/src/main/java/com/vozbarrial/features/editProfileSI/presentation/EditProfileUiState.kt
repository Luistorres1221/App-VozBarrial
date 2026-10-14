package com.vozbarrial.features.profileSI.presentation

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val photoUri: String? = null,
    val error: String? = null,
    val isInitialized: Boolean = false
)