package com.vozbarrial.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds transient sign-in form state across recompositions and configuration changes. */
data class AuthFormUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val message: String = "",
    val success: Boolean = false,
    val dialogMessage: String = "",
    val registering: Boolean = false,
    val showRecovery: Boolean = false,
)

class AuthFormViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthFormUiState())
    val uiState: StateFlow<AuthFormUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = update { copy(email = value, message = "") }
    fun updatePassword(value: String) = update { copy(password = value, message = "") }
    fun togglePasswordVisibility() = update { copy(passwordVisible = !passwordVisible) }
    fun setLoading(value: Boolean) = update { copy(loading = value) }
    fun showMessage(value: String, isSuccess: Boolean = false) = update { copy(message = value, success = isSuccess) }
    fun clearMessage() = update { copy(message = "", success = false) }
    fun toggleRegister() = update { copy(registering = !registering, message = "", success = false) }
    fun showRecovery(value: Boolean) = update { copy(showRecovery = value) }
    fun showDialog(value: String) = update { copy(dialogMessage = value) }

    private inline fun update(transform: AuthFormUiState.() -> AuthFormUiState) {
        _uiState.value = _uiState.value.transform()
    }
}
