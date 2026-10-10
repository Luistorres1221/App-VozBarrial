package com.vozbarrial.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.vozbarrial.core.validation.InputValidator
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
    fun showRecovery(value: Boolean) = update { copy(showRecovery = value) }
    fun showDialog(value: String) = update { copy(dialogMessage = value) }

    fun submitLogin(
        signIn: (String, String, (Boolean) -> Unit) -> Unit,
        onAuthenticated: (String) -> Unit,
    ) {
        val form = _uiState.value
        val cleanEmail = form.email.trim()
        val error = when {
            !InputValidator.isValidEmail(cleanEmail) -> "Escribe un correo electrónico válido."
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

    private inline fun update(transform: AuthFormUiState.() -> AuthFormUiState) {
        _uiState.value = _uiState.value.transform()
    }
}
