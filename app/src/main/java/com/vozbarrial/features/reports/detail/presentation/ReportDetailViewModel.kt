package com.vozbarrial.features.reports.detail.presentation

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.vozbarrial.data.repository.ReportsRepository
import com.vozbarrial.domain.model.Report
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = ReportsRepository()
    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    fun setReport(report: Report) {
        _uiState.update { it.copy(report = report, supportCount = report.votes) }
        observeContributions(report.id)
        checkUserSupport(report.id)
    }

    private fun checkUserSupport(reportId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (reportId.isBlank()) return

        firestore.collection("reportes")
            .document(reportId)
            .collection("supports")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val supported = doc.exists()
                _uiState.update { it.copy(isSupported = supported) }
            }
    }

    private fun observeContributions(reportId: String) {
        listenerRegistration?.remove()
        if (reportId.isBlank()) return

        listenerRegistration = firestore.collection("reportes")
            .document(reportId)
            .collection("aportes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val list = snapshot.documents.mapNotNull { doc ->
                    val contrib = doc.toObject(Contribution::class.java)
                    contrib?.copy(id = doc.id)
                }
                _uiState.update { it.copy(contributions = list) }
            }
    }

    fun onNewContributionChanged(text: String) {
        _uiState.update { it.copy(newContributionText = text) }
    }

    fun addContributionPhoto(uriString: String?) {
        if (uriString.isNullOrBlank()) return
        val current = _uiState.value.contributionPhotoUris
        if (current.size < 2) {
            _uiState.update { it.copy(contributionPhotoUris = current + uriString) }
        }
    }

    fun removeContributionPhoto(index: Int) {
        _uiState.update { it.copy(contributionPhotoUris = it.contributionPhotoUris.filterIndexed { i, _ -> i != index }) }
    }

    fun addContribution(context: Context, authorName: String) {
        val currentText = _uiState.value.newContributionText.trim()
        val photoUris = _uiState.value.contributionPhotoUris
        val reportId = _uiState.value.report.id
        if ((currentText.isBlank() && photoUris.isEmpty()) || reportId.isBlank()) return

        // Clear input immediately for instant feedback
        _uiState.update {
            it.copy(
                newContributionText = "",
                contributionPhotoUris = emptyList(),
                isUploading = true
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uploadedUrls = mutableListOf<String>()
                for (uriStr in photoUris) {
                    val url = repository.uploadToCloudinary(context, uriStr.toUri())
                    if (!url.isNullOrBlank()) {
                        uploadedUrls.add(url)
                    }
                }

                val newContrib = Contribution(
                    id = "",
                    authorName = authorName.ifBlank { "Vecino" },
                    authorRole = "Colaborador",
                    timeAgo = "Hace un momento",
                    content = currentText,
                    isOfficial = false,
                    imageUrls = uploadedUrls,
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("reportes")
                    .document(reportId)
                    .collection("aportes")
                    .add(newContrib)

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isUploading = false) }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isUploading = false) }
                }
            }
        }
    }

    fun toggleSupport() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val currentReport = _uiState.value.report
        val reportId = currentReport.id
        if (reportId.isBlank() || uid == null) return

        if (_uiState.value.isSupported) {
            return // User already supported this report once
        }

        val supportRef = firestore.collection("reportes")
            .document(reportId)
            .collection("supports")
            .document(uid)

        supportRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val newVotes = currentReport.votes + 1
                supportRef.set(mapOf("timestamp" to System.currentTimeMillis()))
                firestore.collection("reportes")
                    .document(reportId)
                    .update("votes", newVotes)

                _uiState.update {
                    it.copy(
                        isSupported = true,
                        report = it.report.copy(votes = newVotes),
                        supportCount = newVotes
                    )
                }
            } else {
                _uiState.update { it.copy(isSupported = true) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
