package com.vozbarrial.domain.model

enum class ReportStatus(val value: String, val label: String) {
    POR_VERIFICAR("POR_VERIFICAR", "Por verificar"),
    VERIFICADO("VERIFICADO", "Verificado"),
    RESUELTO("RESUELTO", "Resuelto"),
    RECHAZADO("RECHAZADO", "Rechazado"),
    ELIMINADO("ELIMINADO", "Eliminado");

    companion object {
        fun fromString(value: String?): ReportStatus {
            if (value.isNullOrBlank()) return POR_VERIFICAR
            return entries.find { it.value.equals(value, true) || it.label.equals(value, true) }
                ?: POR_VERIFICAR
        }
    }
}
