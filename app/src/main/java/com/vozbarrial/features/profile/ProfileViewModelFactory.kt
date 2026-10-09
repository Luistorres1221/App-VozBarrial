package com.vozbarrial.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vozbarrial.domain.repository.ProfileGateway

class ProfileViewModelFactory(private val repository: ProfileGateway) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
        return ProfileViewModel(repository) as T
    }
}
