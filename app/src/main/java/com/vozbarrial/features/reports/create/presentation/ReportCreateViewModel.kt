package com.vozbarrial.features.reports.create.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vozbarrial.data.ai.CategorySuggester
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SUGGESTION_DEBOUNCE_MS = 1_500L
private const val MIN_DESCRIPTION_CHARS = 20

class ReportCreateViewModel(
    private val suggester: CategorySuggester = CategorySuggester(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportCreateUiState())
    val uiState: StateFlow<ReportCreateUiState> = _uiState.asStateFlow()

    private var suggestionJob: Job? = null
    private var lastAnalyzedText: String? = null

    fun reset() {
        suggestionJob?.cancel()
        lastAnalyzedText = null
        _uiState.value = ReportCreateUiState()
    }

    fun applySuggestion() {
        val suggestion = _uiState.value.suggestedCategory ?: return
        selectCategory(suggestion)
    }

    private fun scheduleSuggestion() {
        suggestionJob?.cancel()
        val title = _uiState.value.title.trim()
        val description = _uiState.value.description.trim()
        if (title.isEmpty() || description.length < MIN_DESCRIPTION_CHARS) {
            _uiState.update { it.copy(suggestedCategory = null, suggesting = false) }
            return
        }
        val key = "$title|$description"
        if (key == lastAnalyzedText) return
        suggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MS)
            _uiState.update { it.copy(suggesting = true) }
            try {
                val result = suggester.suggest(title, description)
                if (result != null) lastAnalyzedText = key
                _uiState.update { it.copy(suggestedCategory = result) }
            } finally {
                _uiState.update { it.copy(suggesting = false) }
            }
        }
    }

    fun selectCategory(value: String) {
        _uiState.update { it.copy(category = value) }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value.take(60), error = null) }
        scheduleSuggestion()
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value.take(280), error = null) }
        scheduleSuggestion()
    }

    fun addPhoto(uri: String?) {
        if (uri.isNullOrBlank()) return
        val current = _uiState.value.photoUris
        if (current.size < 3) {
            _uiState.update { it.copy(photoUris = current + uri, error = null) }
        }
    }

    fun removePhoto(index: Int) {
        _uiState.update { it.copy(photoUris = it.photoUris.filterIndexed { i, _ -> i != index }, error = null) }
    }

    fun updateGpsLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                gpsActive = true,
                locationSelected = true,
                locationMessage = "Ubicación GPS actualizada",
            )
        }
    }

    fun denyLocationPermission() {
        _uiState.update { it.copy(locationMessage = "Permite el acceso a la ubicación para usar el GPS.") }
    }

    fun confirmMapLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                gpsActive = false,
                locationSelected = true,
                locationMessage = "Punto seleccionado en el mapa",
            )
        }
    }

    fun publish(
        submit: (String, String, String, Double, Double, List<Uri>, (Boolean, String?) -> Unit) -> Unit,
    ) {
        val form = _uiState.value
        val validationError = when {
            form.title.isBlank() -> "Escribe un título para el reporte."
            form.description.isBlank() -> "Describe brevemente lo sucedido."
            !form.locationSelected -> "Usa el GPS o elige un punto en el mapa."
            form.photoUris.isEmpty() -> "Adjunta al menos una fotografía como evidencia (máximo 3)."
            form.publishing -> return
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        _uiState.update { it.copy(publishing = true, error = null) }
        submit(
            form.title.trim(),
            form.description.trim(),
            form.category,
            form.latitude,
            form.longitude,
            form.photoUris.map { Uri.parse(it) },
        ) { success, error ->
            if (success) reset()
            else _uiState.update {
                it.copy(
                    publishing = false,
                    error = error ?: "No se pudo publicar el reporte.",
                )
            }
        }
    }
}
