package com.vozbarrial.features.profile

import androidx.lifecycle.ViewModel
import com.vozbarrial.domain.Usuario
import com.vozbarrial.domain.repository.ProfileGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(val profile: Usuario? = null, val loading: Boolean = false, val error: String? = null)

class ProfileViewModel(private val repository: ProfileGateway) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    fun hasActiveSession() = repository.hasActiveSession()

    fun loadProfile(onComplete: (Boolean) -> Unit = {}) {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        repository.loadProfile { result ->
            _uiState.value = result.fold({ ProfileUiState(profile = it) }, { ProfileUiState(error = it.localizedMessage ?: "No se pudo cargar el perfil.") })
            onComplete(result.isSuccess && result.getOrNull() != null)
        }
    }

    fun updateProfile(name: String, phone: String, photoUrl: String?, onComplete: (Boolean) -> Unit = {}) {
        repository.updateProfile(name, phone, photoUrl) { result ->
            result.onSuccess { fresh ->
                val current = _uiState.value.profile
                _uiState.value = ProfileUiState(profile = fresh.copy(
                    points = current?.points ?: fresh.points,
                    ownedFrames = current?.ownedFrames ?: fresh.ownedFrames,
                    equippedFrame = current?.equippedFrame ?: fresh.equippedFrame,
                    role = current?.role ?: fresh.role,
                    createdAt = current?.createdAt ?: fresh.createdAt,
                ))
            }
            result.onFailure { _uiState.value = _uiState.value.copy(error = it.localizedMessage ?: "No se pudo guardar el perfil.") }
            onComplete(result.isSuccess)
        }
    }

    fun acceptProfile(profile: Usuario) { _uiState.value = _uiState.value.copy(profile = profile, error = null) }
    fun acceptEquippedFrame(frame: String) {
        _uiState.value.profile?.let { _uiState.value = _uiState.value.copy(profile = it.copy(equippedFrame = frame)) }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit = {}) = repository.deleteAccount { result ->
        if (result.isSuccess) _uiState.value = ProfileUiState()
        else _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage ?: "No se pudo eliminar la cuenta.")
        onComplete(result.isSuccess)
    }

    fun signOut() { repository.signOut(); _uiState.value = ProfileUiState() }
}
