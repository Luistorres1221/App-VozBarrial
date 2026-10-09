package com.vozbarrial.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordVisible: Boolean = false,
    val acceptedTerms: Boolean = true,
    val receiveAlerts: Boolean = true,
    val error: String? = null,
    val showTerms: Boolean = false,
    val loading: Boolean = false,
)

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun reset() { _uiState.value = RegisterUiState() }

    fun updateName(value: String) = update { copy(name = value, error = null) }
    fun updateEmail(value: String) = update { copy(email = value, error = null) }
    fun updatePhone(value: String) = update { copy(phone = value, error = null) }
    fun updatePassword(value: String) = update { copy(password = value, error = null) }
    fun updateConfirmation(value: String) = update { copy(confirmation = value, error = null) }
    fun togglePasswordVisibility() = update { copy(passwordVisible = !passwordVisible) }
    fun setTermsAccepted(value: Boolean) = update { copy(acceptedTerms = value, error = null) }
    fun setAlertsEnabled(value: Boolean) = update { copy(receiveAlerts = value) }
    fun showTerms(value: Boolean) = update { copy(showTerms = value) }

    fun submit(
        createAccount: (String, String, String, String, (Boolean, String?) -> Unit) -> Unit,
        onSuccess: () -> Unit,
    ) {
        val form = _uiState.value
        val cleanEmail = form.email.trim()
        val validationError = when {
            form.name.trim().length < 2 -> "Escribe tu nombre completo."
            !cleanEmail.matches(EMAIL_PATTERN) -> "Escribe un correo electrónico válido."
            form.password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            !form.password.any { it.isDigit() || !it.isLetterOrDigit() } -> "Incluye al menos un número o un símbolo en la contraseña."
            form.password != form.confirmation -> "Las contraseñas no coinciden."
            !form.acceptedTerms -> "Debes aceptar las normas de convivencia para crear tu cuenta."
            else -> null
        }
        if (validationError != null) {
            _uiState.value = form.copy(error = validationError)
            return
        }
        if (form.loading) return
        _uiState.value = form.copy(error = null, loading = true)
        createAccount(form.name.trim(), cleanEmail, form.phone.trim(), form.password) { success, error ->
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = if (success) null else error ?: "Ya existe una cuenta con ese correo o hubo un error.",
            )
            if (success) {
                _uiState.value = RegisterUiState()
                onSuccess()
            }
        }
    }

    private inline fun update(transform: RegisterUiState.() -> RegisterUiState) {
        _uiState.value = _uiState.value.transform()
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
