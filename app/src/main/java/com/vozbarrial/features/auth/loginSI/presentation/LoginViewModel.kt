package com.vozbarrial.features.auth.loginSI.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = update { copy(email = value, message = "") }
    fun updatePassword(value: String) = update { copy(password = value, message = "") }
    fun togglePasswordVisibility() = update { copy(passwordVisible = !passwordVisible) }
    fun setLoading(value: Boolean) = update { copy(loading = value) }

    fun showMessage(value: String, isSuccess: Boolean = false) = update {
        copy(message = value, success = isSuccess)
    }

    fun clearMessage() = update { copy(message = "", success = false) }
    fun showRecovery(value: Boolean) = update { copy(showRecovery = value) }
    fun showDialog(value: String) = update { copy(dialogMessage = value) }

    fun submitLogin(
        signIn: (String, String, (Boolean) -> Unit) -> Unit,
        onAuthenticated: (String) -> Unit,
    ) {
        val form = _uiState.value
        val cleanEmail = form.email.trim()
        val error = when {
            !cleanEmail.matches(EMAIL_PATTERN) -> "Escribe un correo electrónico válido."
            form.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            form.loading -> return
            else -> null
        }
        if (error != null) {
            _uiState.value = form.copy(message = error, success = false)
            return
        }
        _uiState.value = form.copy(loading = true, message = "", success = false)
        signIn(cleanEmail, form.password) { authenticated ->
            _uiState.value = _uiState.value.copy(
                loading = false,
                password = if (authenticated) "" else _uiState.value.password,
                message = if (authenticated) "" else "El correo o la contraseña no son correctos.",
                success = false,
            )
            if (authenticated) onAuthenticated(cleanEmail)
        }
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    private inline fun update(transform: LoginUiState.() -> LoginUiState) {
        _uiState.value = _uiState.value.transform()
    }
}
