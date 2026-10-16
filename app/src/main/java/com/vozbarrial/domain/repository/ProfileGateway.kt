package com.vozbarrial.domain.repository

import com.vozbarrial.domain.model.Usurious

interface ProfileGateway {
    fun hasActiveSession(): Boolean
    fun loadProfile(onResult: (Result<Usurious?>) -> Unit)
    fun updateProfile(name: String, phone: String, photoUrl: String?, onResult: (Result<Usurious>) -> Unit)
    fun deleteAccount(onResult: (Result<Unit>) -> Unit)
    fun signOut()
}
