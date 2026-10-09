package com.vozbarrial.features.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vozbarrial.data.reports.ReportsRepository

class ReportsViewModelFactory(private val repository: ReportsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReportsViewModel::class.java))
        return ReportsViewModel(repository) as T
    }
}
