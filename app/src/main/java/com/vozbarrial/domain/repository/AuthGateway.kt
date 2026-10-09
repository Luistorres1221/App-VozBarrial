package com.vozbarrial.domain.repository

import com.vozbarrial.domain.auth.AuthOperationResult

/** Domain contract for authentication. Firebase is only one possible implementation. */
interface AuthGateway {
    fun signIn(email: String, password: String, onResult: (Result<String>) -> Unit)
    fun createAccount(name: String, email: String, phone: String, password: String, onResult: (Result<String>) -> Unit)
    fun signOut()
    fun sendPasswordResetEmail(email: String, onResult: (AuthOperationResult<Unit>) -> Unit)
}
