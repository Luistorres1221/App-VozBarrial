package com.vozbarrial.domain.model

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val place: String = "",
    val category: String = "",
    val authorEmail: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorNivel: String = Nivel.NOVATO.value,
    val imageUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = ReportStatus.POR_VERIFICAR.value,
    val votes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
