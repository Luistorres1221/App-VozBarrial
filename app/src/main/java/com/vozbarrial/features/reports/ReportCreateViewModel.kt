package com.vozbarrial.features.reports

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReportCreateUiState(
    val category: String = "Seguridad",
    val title: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val latitude: Double = 4.60971,
    val longitude: Double = -74.08175,
    val gpsActive: Boolean = false,
    val locationSelected: Boolean = false,
    val locationMessage: String = "",
    val publishing: Boolean = false,
    val error: String? = null,
)

class ReportCreateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportCreateUiState())
    val uiState: StateFlow<ReportCreateUiState> = _uiState.asStateFlow()

    fun reset() { _uiState.value = ReportCreateUiState() }

    fun selectCategory(value: String) { _uiState.value = _uiState.value.copy(category = value) }
    fun updateTitle(value: String) { _uiState.value = _uiState.value.copy(title = value.take(60), error = null) }
    fun updateDescription(value: String) { _uiState.value = _uiState.value.copy(description = value.take(280), error = null) }
    fun setPhoto(uri: String?) { _uiState.value = _uiState.value.copy(photoUri = uri, error = null) }

    fun updateGpsLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            gpsActive = true,
            locationSelected = true,
            locationMessage = "Ubicación GPS actualizada",
        )
    }

    fun denyLocationPermission() {
        _uiState.value = _uiState.value.copy(locationMessage = "Permite el acceso a la ubicación para usar el GPS.")
    }

    fun confirmMapLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            gpsActive = false,
            locationSelected = true,
            locationMessage = "Punto seleccionado en el mapa",
        )
    }

    fun publish(
        submit: (String, String, String, Double, Double, Uri, (Boolean, String?) -> Unit) -> Unit,
    ) {
        val form = _uiState.value
        val validationError = when {
            form.title.isBlank() -> "Escribe un título para el reporte."
            form.description.isBlank() -> "Describe brevemente lo sucedido."
            !form.locationSelected -> "Usa el GPS o elige un punto en el mapa."
            form.photoUri.isNullOrBlank() -> "Adjunta al menos una fotografía como evidencia."
            form.publishing -> return
            else -> null
        }
        if (validationError != null) {
            _uiState.value = form.copy(error = validationError)
            return
        }
        _uiState.value = form.copy(publishing = true, error = null)
        submit(
            form.title.trim(), form.description.trim(), form.category,
            form.latitude, form.longitude, Uri.parse(form.photoUri),
        ) { success, error ->
            _uiState.value = if (success) ReportCreateUiState() else _uiState.value.copy(
                publishing = false,
                error = error ?: "No se pudo publicar el reporte.",
            )
        }
    }
}
