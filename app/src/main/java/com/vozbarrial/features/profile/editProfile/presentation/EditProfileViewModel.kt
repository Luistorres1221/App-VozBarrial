package com.vozbarrial.features.profile.editProfile.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun initialize(name: String, photo: String?) {
        if (!_uiState.value.isInitialized) {
            _uiState.update {
                it.copy(
                    name = name,
                    photoUri = photo,
                    isInitialized = true
                )
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun setPhoto(value: String?) {
        _uiState.update { it.copy(photoUri = value, error = null) }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun save(
        onSave: (String, String?) -> Unit,
        invalidNameErrorMessage: String
    ) {
        val state = _uiState.value
        val cleanName = state.name.trim()

        if (cleanName.split(Regex("\\s+")).size < 2) {
            showError(invalidNameErrorMessage)
            return
        }

        onSave(cleanName, state.photoUri)
    }
}
