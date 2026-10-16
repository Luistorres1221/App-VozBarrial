package com.vozbarrial.features.profile.profile.presentation

import com.vozbarrial.domain.model.Report

data class ProfileUiState(
    val currentPage: String = "",
    val name: String = "",
    val phone: String = "",
    val profilePhoto: String? = null,
    val points: Int = 0,
    val reports: List<Report> = emptyList(),
    val activityFilter: String = "",
    val availableFilters: List<String> = emptyList(),

    // Estados para control de modales/diálogos
    val showNameEditor: Boolean = false,
    val nameDraft: String = "",
    val showAccountDelete: Boolean = false,
    val reportToEdit: Report? = null,
    val reportToEditTitle: String = "",
    val reportToEditDescription: String = "",
    val reportToDelete: Report? = null,
    val operationMessage: String? = null,
    val isLoading: Boolean = false
) {
    val sortedReports: List<Report>
        get() = reports.sortedByDescending { it.timestamp }

    val verifiedCount: Int
        get() = reports.count { it.status.equals("RESUELTO", true) || it.status.equals("VERIFICADO", true) }

    val filteredActivityReports: List<Report>
        get() = sortedReports.filter { report ->
            if (activityFilter.isBlank()) true
            else report.status.equals(activityFilter, ignoreCase = true)
        }
}
