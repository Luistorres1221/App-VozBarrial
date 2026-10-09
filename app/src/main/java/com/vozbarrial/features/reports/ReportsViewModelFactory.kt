package com.vozbarrial.features.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vozbarrial.domain.repository.ReportsGateway

class ReportsViewModelFactory(private val repository: ReportsGateway) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReportsViewModel::class.java))
        return ReportsViewModel(repository) as T
    }
}
