package com.vozbarrial.domain

data class Reporte(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val place: String = "",
    val category: String = "",
    val authorEmail: String = "",
    val authorUid: String = "",
    val imageUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "ACTIVO",
    val timestamp: Long = System.currentTimeMillis()
)
