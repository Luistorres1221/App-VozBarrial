package com.vozbarrial.features.reports.presentation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.vozbarrial.data.repository.ReportsRepository
import com.vozbarrial.domain.model.Reporte
import com.vozbarrial.domain.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class ReportsViewModel : ViewModel() {
    private val repository = ReportsRepository()
    val reports = mutableStateListOf<Reporte>()

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
        photo: Uri,
        profile: Usuario?,
        context: Context,
        onComplete: (Boolean, String?) -> Unit,
    ) {
        if (profile == null) return onComplete(false, "Inicia sesión para publicar un reporte.")

        val reportId = UUID.randomUUID().toString()
        val report = Reporte(
            id = reportId,
            title = title.trim(),
            description = description.trim(),
            place = "Lat: ${"%.5f".format(Locale.US, latitude)}, Lon: ${"%.5f".format(Locale.US, longitude)}",
            category = category,
            authorEmail = profile.email,
            authorUid = profile.uid,
            imageUrl = null,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
        )

        // 1. Publish immediately so user returns to map instantly (0 wait time)
        repository.publish(report) { result ->
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }

        // 2. Upload image to Cloudinary in background thread
        CoroutineScope(Dispatchers.IO).launch {
            val imageUrl = repository.uploadToCloudinary(context, photo)
            if (!imageUrl.isNullOrBlank()) {
                FirebaseFirestore.getInstance()
                    .collection("reportes")
                    .document(reportId)
                    .update("imageUrl", imageUrl)
            }
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
