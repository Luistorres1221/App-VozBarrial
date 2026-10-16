package com.vozbarrial.domain.repository

import com.vozbarrial.domain.model.Reporte

interface ReportsGateway {
    fun observeReports(onChange: (List<Reporte>) -> Unit)
    fun stopObserving()
    fun publish(report: Reporte, onResult: (Result<Unit>) -> Unit)
    fun update(reportId: String, title: String, description: String, onResult: (Result<Unit>) -> Unit)
    fun delete(reportId: String, onResult: (Result<Unit>) -> Unit)
}
