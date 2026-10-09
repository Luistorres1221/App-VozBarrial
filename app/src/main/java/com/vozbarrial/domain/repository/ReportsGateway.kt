package com.vozbarrial.domain.repository

import android.net.Uri
import com.vozbarrial.domain.Reporte

interface ReportsGateway {
    fun observeReports(onChange: (List<Reporte>) -> Unit)
    fun stopObserving()
    fun publish(report: Reporte, photoUri: Uri, onResult: (Result<Unit>) -> Unit)
    fun update(reportId: String, title: String, description: String, onResult: (Result<Unit>) -> Unit)
    fun delete(reportId: String, onResult: (Result<Unit>) -> Unit)
}
