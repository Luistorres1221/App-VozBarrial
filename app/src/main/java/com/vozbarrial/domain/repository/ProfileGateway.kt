package com.vozbarrial.domain.repository

import android.content.Context
import com.vozbarrial.domain.model.Usurious

interface ProfileGateway {
    fun hasActiveSession(): Boolean
    fun loadProfile(onResult: (Result<Usurious?>) -> Unit)
    fun updateProfile(context: Context, name: String, photoUrl: String?, onResult: (Result<Usurious>) -> Unit)
    fun deleteAccount(onResult: (Result<Unit>) -> Unit)
    fun signOut()
}
