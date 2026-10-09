package com.vozbarrial.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.vozbarrial.data.auth.AuthRepository
import com.vozbarrial.domain.auth.AuthOperationResult

/** Coordinates authentication actions for the presentation layer. */
class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        repository.signIn(email, password) { result -> onResult(result.isSuccess) }
    }

    fun createAccount(
        name: String,
        email: String,
        phone: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        repository.createAccount(name, email, phone, password) { result ->
            onResult(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun requestPasswordReset(email: String, onResult: (AuthOperationResult<Unit>) -> Unit) {
        repository.sendPasswordResetEmail(email, onResult)
    }

    fun signOut() = repository.signOut()
}
