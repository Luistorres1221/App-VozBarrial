package com.vozbarrial.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.vozbarrial.domain.Usuario
import com.vozbarrial.domain.auth.AuthError
import com.vozbarrial.domain.auth.AuthOperationResult

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun signIn(email: String, password: String, onResult: (Result<String>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                result.user?.uid?.let { onResult(Result.success(it)) }
                    ?: onResult(Result.failure(IllegalStateException("Firebase no devolvió un usuario.")))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun createAccount(name: String, email: String, phone: String, password: String, onResult: (Result<String>) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onResult(Result.failure(IllegalStateException("Firebase no devolvió un usuario.")))
                    return@addOnSuccessListener
                }
                val profile = Usuario(uid = user.uid, name = name.trim(), email = email.trim(), phone = phone.trim())
                firestore.collection("usuarios").document(user.uid).set(profile)
                    .addOnSuccessListener { onResult(Result.success(user.uid)) }
                    .addOnFailureListener { error ->
                        user.delete().addOnCompleteListener { onResult(Result.failure(error)) }
                    }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun signOut() = firebaseAuth.signOut()

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
