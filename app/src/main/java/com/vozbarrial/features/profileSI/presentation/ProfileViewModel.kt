package com.vozbarrial.features.profileSI.presentation

import androidx.lifecycle.ViewModel
import com.vozbarrial.domain.Reporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfileData(
        page: String,
        name: String,
        profilePhoto: String?,
        points: Int,
        reports: List<Reporte>,
        filters: List<String>
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                currentPage = page,
                name = name,
                profilePhoto = profilePhoto,
                points = points,
                reports = reports,
                availableFilters = filters,
                activityFilter = if (currentState.activityFilter.isEmpty() && filters.isNotEmpty()) filters.first() else currentState.activityFilter
            )
        }
    }

    fun onActivityFilterSelected(filter: String) {
        _uiState.update { it.copy(activityFilter = filter) }
    }

    fun onOpenNameEditor() {
        _uiState.update { it.copy(showNameEditor = true, nameDraft = it.name) }
    }

    fun onNameDraftChange(newDraft: String) {
        _uiState.update { it.copy(nameDraft = newDraft) }
    }

    fun onDismissNameEditor() {
        _uiState.update { it.copy(showNameEditor = false) }
    }

    fun onSaveName(onSaveCallback: (String) -> Unit) {
        val trimmed = _uiState.value.nameDraft.trim()
        if (trimmed.isNotBlank()) {
            onSaveCallback(trimmed)
        }
        onDismissNameEditor()
    }

    fun onOpenEditReport(report: Reporte) {
        _uiState.update {
            it.copy(
                reportToEdit = report,
                reportToEditTitle = report.title,
                reportToEditDescription = report.description
            )
        }
    }

    fun onEditTitleChange(title: String, maxLength: Int) {
        _uiState.update { it.copy(reportToEditTitle = title.take(maxLength)) }
    }

    fun onEditDescriptionChange(description: String, maxLength: Int) {
        _uiState.update { it.copy(reportToEditDescription = description.take(maxLength)) }
    }

    fun onDismissEditReport() {
        _uiState.update { it.copy(reportToEdit = null) }
    }

    fun onConfirmEditReport(
        onEditReportCallback: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
        successMessage: String,
        errorMessageFallback: String
    ) {
        val report = _uiState.value.reportToEdit ?: return
        val title = _uiState.value.reportToEditTitle
        val description = _uiState.value.reportToEditDescription

        onEditReportCallback(report.id, title, description) { success, error ->
            _uiState.update {
                it.copy(
                    operationMessage = if (success) successMessage else error ?: errorMessageFallback,
                    reportToEdit = if (success) null else it.reportToEdit
                )
            }
        }
    }

    fun onOpenDeleteReport(report: Reporte) {
        _uiState.update { it.copy(reportToDelete = report) }
    }

    fun onDismissDeleteReport() {
        _uiState.update { it.copy(reportToDelete = null) }
    }

    fun onConfirmDeleteReport(
        onDeleteReportCallback: (String, (Boolean, String?) -> Unit) -> Unit,
        successMessage: String,
        errorMessageFallback: String
    ) {
        val report = _uiState.value.reportToDelete ?: return

        onDeleteReportCallback(report.id) { success, error ->
            _uiState.update {
                it.copy(
                    operationMessage = if (success) successMessage else error ?: errorMessageFallback,
                    reportToDelete = if (success) null else it.reportToDelete
                )
            }
        }
    }

    fun onShowDeleteAccountDialog(show: Boolean) {
        _uiState.update { it.copy(showAccountDelete = show) }
    }

    fun onDismissOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }
}