package com.vozbarrial.domain.repository

import com.vozbarrial.domain.Usuario

interface ProfileGateway {
    fun hasActiveSession(): Boolean
    fun loadProfile(onResult: (Result<Usuario?>) -> Unit)
    fun updateProfile(name: String, phone: String, photoUrl: String?, onResult: (Result<Usuario>) -> Unit)
    fun deleteAccount(onResult: (Result<Unit>) -> Unit)
    fun signOut()
}
