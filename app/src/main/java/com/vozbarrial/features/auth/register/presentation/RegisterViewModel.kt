package com.vozbarrial.features.auth.register.presentation

import androidx.lifecycle.ViewModel
import com.vozbarrial.core.validation.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun reset() { _uiState.value = RegisterUiState() }

    fun updateName(value: String) = update { copy(name = value, error = null) }
    fun updateEmail(value: String) = update { copy(email = value, error = null) }
    fun updatePassword(value: String) = update { copy(password = value, error = null) }
    fun updateConfirmation(value: String) = update { copy(confirmation = value, error = null) }
    fun togglePasswordVisibility() = update { copy(passwordVisible = !passwordVisible) }
    fun setTermsAccepted(value: Boolean) = update { copy(acceptedTerms = value, error = null) }
    fun showTerms(value: Boolean) = update { copy(showTerms = value) }

    fun submit(
        createAccount: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
        onSuccess: () -> Unit,
        errNameEmpty: String,
        errEmailInvalid: String,
        errPwdLength: String,
        errPwdSymbol: String,
        errPwdMismatch: String,
        errTermsRequired: String,
        errGeneric: String
    ) {
        val form = _uiState.value
        val cleanEmail = form.email.trim()
        val validationError = when {
            form.name.trim().length < 2 -> errNameEmpty
            !InputValidator.isValidEmail(cleanEmail) -> errEmailInvalid
            form.password.length < 8 -> errPwdLength
            !form.password.any { it.isDigit() || !it.isLetterOrDigit() } -> errPwdSymbol
            form.password != form.confirmation -> errPwdMismatch
            !form.acceptedTerms -> errTermsRequired
            else -> null
        }
        if (validationError != null) {
            _uiState.value = form.copy(error = validationError)
            return
        }
        if (form.loading) return
        _uiState.value = form.copy(error = null, loading = true)
        createAccount(form.name.trim(), cleanEmail, form.password) { success, error ->
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = if (success) null else error ?: errGeneric,
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

}
