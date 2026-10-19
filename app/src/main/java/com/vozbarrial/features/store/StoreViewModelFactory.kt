package com.vozbarrial.features.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vozbarrial.domain.repository.StoreGateway

class StoreViewModelFactory(private val repository: StoreGateway) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(StoreViewModel::class.java))
        return StoreViewModel(repository) as T
    }
}
