package com.vozbarrial.features.reports.presentation

data class ReportCreateUiState(
    val category: String = "Seguridad",
    val title: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(),
    val latitude: Double = 4.60971,
    val longitude: Double = -74.08175,
    val gpsActive: Boolean = false,
    val locationSelected: Boolean = false,
    val locationMessage: String = "",
    val publishing: Boolean = false,
    val error: String? = null,
)
