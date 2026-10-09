package com.vozbarrial.features.reports

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.vozbarrial.domain.Reporte
import com.vozbarrial.domain.Usuario
import com.vozbarrial.domain.repository.ReportsGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

data class ReportsUiState(val reports: List<Reporte> = emptyList(), val error: String? = null)

class ReportsViewModel(private val repository: ReportsGateway) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun startObserving() = repository.observeReports { reports -> _uiState.value = _uiState.value.copy(reports = reports) }

    fun stopObserving() { repository.stopObserving() }

    fun createReport(title: String, description: String, category: String, latitude: Double, longitude: Double, photo: Uri, profile: Usuario?, onComplete: (Boolean, String?) -> Unit) {
        if (profile == null) return onComplete(false, "Inicia sesión para publicar un reporte.")
        val report = Reporte(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            place = "Lat: ${"%.5f".format(Locale.US, latitude)}, Lon: ${"%.5f".format(Locale.US, longitude)}",
            category = category,
            authorEmail = profile.email,
            authorUid = profile.uid,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
        )
        repository.publish(report, photo) { result ->
            result.onFailure { _uiState.value = _uiState.value.copy(error = it.localizedMessage ?: "No se pudo publicar el reporte.") }
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun updateReport(id: String, title: String, description: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) =
        repository.update(id, title, description) { result ->
            result.onFailure { _uiState.value = _uiState.value.copy(error = it.localizedMessage ?: "No se pudo editar el reporte.") }
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }

    fun deleteReport(id: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) =
        repository.delete(id) { result ->
            result.onFailure { _uiState.value = _uiState.value.copy(error = it.localizedMessage ?: "No se pudo eliminar el reporte.") }
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }

    override fun onCleared() { repository.stopObserving(); super.onCleared() }
}
