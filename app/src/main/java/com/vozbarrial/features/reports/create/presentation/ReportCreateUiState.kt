package com.vozbarrial.features.reports.create.presentation

import com.vozbarrial.domain.model.DEFAULT_MAP_CENTER

data class ReportCreateUiState(
    val category: String = "Seguridad",
    val title: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(),
    val latitude: Double = DEFAULT_MAP_CENTER.latitude,
    val longitude: Double = DEFAULT_MAP_CENTER.longitude,
    val gpsActive: Boolean = false,
    val locationSelected: Boolean = false,
    val locationMessage: String = "",
    val publishing: Boolean = false,
    val error: String? = null,
)
