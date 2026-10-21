package com.vozbarrial.domain.repository

import com.vozbarrial.domain.model.Report

interface ReportsGateway {
    fun observeReports(onChange: (List<Report>) -> Unit)
    fun stopObserving()
    fun observeReport(reportId: String, onChange: (Report?) -> Unit)
    fun stopObservingReport()
    fun publish(report: Report, onResult: (Result<Unit>) -> Unit)
    fun update(reportId: String, title: String, description: String, onResult: (Result<Unit>) -> Unit)
    fun delete(reportId: String, onResult: (Result<Unit>) -> Unit)
}
