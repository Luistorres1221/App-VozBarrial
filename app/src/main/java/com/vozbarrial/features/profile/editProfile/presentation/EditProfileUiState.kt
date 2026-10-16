package com.vozbarrial.features.profile.editProfile.presentation

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val photoUri: String? = null,
    val error: String? = null,
    val isInitialized: Boolean = false
)
