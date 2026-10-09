package com.vozbarrial.features.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val photoUri: String? = null,
    val error: String? = null,
)

class EditProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    fun initialize(name: String, phone: String, photo: String?) {
        _uiState.value = EditProfileUiState(name = name, phone = phone, photoUri = photo)
    }

    fun updateName(value: String) { _uiState.value = _uiState.value.copy(name = value, error = null) }
    fun updatePhone(value: String) { _uiState.value = _uiState.value.copy(phone = value, error = null) }
    fun setPhoto(value: String?) { _uiState.value = _uiState.value.copy(photoUri = value, error = null) }
    fun showError(value: String) { _uiState.value = _uiState.value.copy(error = value) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun save(onSave: (String, String, String?) -> Unit) {
        val state = _uiState.value
        val cleanName = state.name.trim()
        if (cleanName.split(Regex("\\s+")).size < 2) {
            showError("Escribe tu nombre y apellido.")
            return
        }
        onSave(cleanName, state.phone.trim(), state.photoUri)
    }
}
