package com.vozbarrial.domain

data class Reporte(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val place: String = "",
    val category: String = "",
    val authorEmail: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
