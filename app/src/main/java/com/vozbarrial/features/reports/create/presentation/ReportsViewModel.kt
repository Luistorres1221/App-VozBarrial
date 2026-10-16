package com.vozbarrial.features.reports.create.presentation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vozbarrial.data.repository.ReportsRepository
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.Usurious
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class ReportsViewModel : ViewModel() {
    private val repository = ReportsRepository()
    val reports = mutableStateListOf<Report>()

    init {
        repository.observeReports { list ->
            reports.clear()
            reports.addAll(list)
        }
    }

    fun startObserving() {}
    fun stopObserving() {}

    fun createReport(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double,
        photos: List<Uri>,
        profile: Usurious?,
        context: Context,
        onComplete: (Boolean, String?) -> Unit,
    ) {
        if (profile == null) return onComplete(false, "Inicia sesión para publicar un reporte.")

        val reportId = UUID.randomUUID().toString()

        val report = Report(
            id = reportId,
            title = title.trim(),
            description = description.trim(),
            place = "Lat: ${"%.5f".format(Locale.US, latitude)}, Lon: ${"%.5f".format(Locale.US, longitude)}",
            category = category,
            authorEmail = profile.email,
            authorUid = profile.uid,
            authorName = profile.name.ifBlank { profile.email.substringBefore("@") },
            authorNivel = profile.nivel,
            imageUrl = null,
            latitude = latitude,
            longitude = longitude,
            status = "POR_VERIFICAR",
            timestamp = System.currentTimeMillis(),
        )

        if (!reports.any { it.id == reportId }) {
            reports.add(0, report)
        }

        onComplete(true, null)

        viewModelScope.launch(Dispatchers.IO) {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            if (profile.uid.isNotBlank()) {
                firestore.collection("usuarios").document(profile.uid)
                    .update("monedas", com.google.firebase.firestore.FieldValue.increment(50))
            } else if (profile.email.isNotBlank()) {
                firestore.collection("usuarios").whereEqualTo("email", profile.email).get()
                    .addOnSuccessListener { docs ->
                        for (doc in docs) {
                            doc.reference.update("monedas", com.google.firebase.firestore.FieldValue.increment(50))
                        }
                    }
            }

            val uploadedUrls = mutableListOf<String>()
            for (photoUri in photos) {
                val url = repository.uploadToCloudinary(context, photoUri)
                if (!url.isNullOrBlank()) {
                    uploadedUrls.add(url)
                }
            }
            val combinedImages = if (uploadedUrls.isNotEmpty()) uploadedUrls.joinToString(",") else null
            val finalReport = if (combinedImages != null) report.copy(imageUrl = combinedImages) else report

            repository.publish(finalReport) { _ -> }
        }
    }

    fun updateReport(id: String, title: String, description: String, onComplete: (Boolean, String?) -> Unit) {
        repository.update(id, title, description) { result ->
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun deleteReport(id: String, onComplete: (Boolean, String?) -> Unit) {
        repository.delete(id) { result ->
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }
    }
}
