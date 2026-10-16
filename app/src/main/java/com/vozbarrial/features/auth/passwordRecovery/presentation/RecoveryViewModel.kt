package com.vozbarrial.features.auth.passwordRecovery.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.vozbarrial.core.result.AuthError
import com.vozbarrial.core.result.AuthOperationResult

class RecoveryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, message = "")
    }

    fun requestReset(request: (String, (AuthOperationResult<Unit>) -> Unit) -> Unit) {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Escribe tu correo electrónico.")
            return
        }
        if (_uiState.value.sending) return
        _uiState.value = _uiState.value.copy(sending = true, message = "")
        request(email) { result ->
            _uiState.value = _uiState.value.copy(sending = false)
            when (result) {
                is AuthOperationResult.Success -> {
                    _uiState.value = _uiState.value.copy(succeeded = true)
                }
                is AuthOperationResult.Failure -> when (result.error) {
                    AuthError.NetworkUnavailable -> {
                        _uiState.value = _uiState.value.copy(message = "Sin conexión a internet. Verifica tu red.")
                    }
                    AuthError.AccountNotFound -> {
                        _uiState.value = _uiState.value.copy(succeeded = true)
                    }
                    is AuthError.ProviderFailure -> {
                        _uiState.value = _uiState.value.copy(message = result.error.message ?: "No se pudo enviar el correo.")
                    }
                }
            }
        }
    }
}
