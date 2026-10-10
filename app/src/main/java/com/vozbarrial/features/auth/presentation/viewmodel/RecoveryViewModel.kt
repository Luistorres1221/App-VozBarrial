package com.vozbarrial.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.vozbarrial.core.validation.InputValidator
import com.vozbarrial.domain.auth.AuthError
import com.vozbarrial.domain.auth.AuthOperationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RecoveryUiState(
    val email: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val succeeded: Boolean = false,
)

class RecoveryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) { _uiState.value = _uiState.value.copy(email = value, message = "") }

    fun requestReset(request: (String, (AuthOperationResult<Unit>) -> Unit) -> Unit) {
        val normalizedEmail = _uiState.value.email.trim()
        if (!InputValidator.isValidEmail(normalizedEmail)) {
            _uiState.value = _uiState.value.copy(message = "Escribe un correo válido.")
            return
        }
        _uiState.value = _uiState.value.copy(message = "", sending = true)
        request(normalizedEmail) { result ->
            when (result) {
                is AuthOperationResult.Success -> _uiState.value = _uiState.value.copy(sending = false, succeeded = true)
                is AuthOperationResult.Failure -> when (result.error) {
                    AuthError.AccountNotFound -> _uiState.value = _uiState.value.copy(sending = false, succeeded = true)
                    AuthError.NetworkUnavailable -> _uiState.value = _uiState.value.copy(sending = false, message = "No hay conexión. Verifica tu conexión e inténtalo de nuevo.")
                    else -> _uiState.value = _uiState.value.copy(sending = false, message = "No se pudo completar la solicitud. Inténtalo de nuevo.")
                }
            }
        }
    }
}
