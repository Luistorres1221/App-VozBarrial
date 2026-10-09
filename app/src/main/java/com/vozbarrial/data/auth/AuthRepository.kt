package com.vozbarrial.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.FirebaseNetworkException
import com.vozbarrial.domain.auth.AuthError
import com.vozbarrial.domain.auth.AuthOperationResult

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    fun sendPasswordResetEmail(
        email: String,
        onResult: (AuthOperationResult<Unit>) -> Unit,
    ) {
        firebaseAuth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AuthOperationResult.Success(Unit))
                } else {
                    onResult(AuthOperationResult.Failure(mapError(task.exception)))
                }
            }
    }

    private fun mapError(exception: Exception?): AuthError {
        if (exception is FirebaseNetworkException) return AuthError.NetworkUnavailable

        val authException = exception as? FirebaseAuthException
        return when (authException?.errorCode) {
            "ERROR_NETWORK_REQUEST_FAILED" -> AuthError.NetworkUnavailable
            "ERROR_USER_NOT_FOUND", "ERROR_EMAIL_NOT_FOUND", "ERROR_INVALID_USER" -> AuthError.AccountNotFound
            else -> AuthError.ProviderFailure(
                code = authException?.errorCode,
                message = exception?.localizedMessage,
            )
        }
    }
}
