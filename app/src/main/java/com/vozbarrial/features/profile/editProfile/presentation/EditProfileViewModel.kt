package com.vozbarrial.features.profile.editProfile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import com.vozbarrial.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun initialize(name: String, photo: String?) {
        _uiState.update {
            it.copy(
                name = if (it.name.isBlank()) name else it.name,
                photoUri = photo,
                isInitialized = true
            )
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
        context: Context,
        onSuccess: () -> Unit,
        invalidNameErrorMessage: String
    ) {
        val state = _uiState.value
        val cleanName = state.name.trim()

        if (cleanName.isBlank()) {
            showError(invalidNameErrorMessage)
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        profileRepository.updateProfile(context, cleanName, state.photoUri) { result ->
            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                onSuccess()
            } else {
                showError(result.exceptionOrNull()?.localizedMessage ?: "No se pudo actualizar el perfil.")
            }
        }
    }
}
