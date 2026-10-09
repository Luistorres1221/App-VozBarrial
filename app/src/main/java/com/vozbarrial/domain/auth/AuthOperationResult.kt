package com.vozbarrial.domain.auth

sealed interface AuthOperationResult<out T> {
    data class Success<T>(val value: T) : AuthOperationResult<T>
    data class Failure(val error: AuthError) : AuthOperationResult<Nothing>
}

sealed interface AuthError {
    data object NetworkUnavailable : AuthError
    data object AccountNotFound : AuthError
    data class ProviderFailure(val code: String?, val message: String?) : AuthError
}
