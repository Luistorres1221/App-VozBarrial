package com.vozbarrial.features.profile.profile.presentation

import com.vozbarrial.domain.model.Report

data class ProfileUiState(
    val currentPage: String = "",
    val name: String = "",
    val phone: String = "",
    val profilePhoto: String? = null,
    val points: Int = 0,
    val monedas: Int = 0,
    val nivel: String = "novato",
    val reports: List<Report> = emptyList(),
    val activityFilter: String = "Todas",
    val availableFilters: List<String> = emptyList(),
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

    val totalVotes: Int
        get() = reports.sumOf { it.votes }

    val effectivenessPercentage: Int
        get() = if (reports.isNotEmpty()) (verifiedCount * 100) / reports.size else 0

    val filteredActivityReports: List<Report>
        get() = sortedReports.filter { report ->
            when (activityFilter) {
                "Por verificar" -> report.status.equals("POR_VERIFICAR", true)
                "Verificado" -> report.status.equals("VERIFICADO", true)
                "Resuelto" -> report.status.equals("RESUELTO", true)
                "Rechazado" -> report.status.equals("RECHAZADO", true)
                else -> true
            }
        }
}
