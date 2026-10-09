package com.vozbarrial.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vozbarrial.domain.repository.AuthGateway

class AuthViewModelFactory(private val repository: AuthGateway) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java)) { "Unsupported ViewModel: ${modelClass.name}" }
        return AuthViewModel(repository) as T
    }
}
