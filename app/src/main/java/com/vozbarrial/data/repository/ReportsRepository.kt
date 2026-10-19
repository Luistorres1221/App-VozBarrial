package com.vozbarrial.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.repository.ReportsGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL

class ReportsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ReportsGateway {
    private var listener: ListenerRegistration? = null

    override fun observeReports(onChange: (List<Report>) -> Unit) {
        if (listener != null) return

        listener = firestore.collection(REPORTS).addSnapshotListener { snapshot, _ ->
            val documents = snapshot?.documents ?: return@addSnapshotListener
            onChange(documents.mapNotNull { it.toObject(Report::class.java)?.copy(id = it.id) })
        }
    }

    override fun stopObserving() { listener?.remove(); listener = null }

    suspend fun uploadToCloudinary(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            var scale = 1
            while (options.outWidth / scale / 2 >= 800 && options.outHeight / scale / 2 >= 800) {
                scale *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
            val compressedBytes = outputStream.toByteArray()

            val boundary = "Boundary-${System.currentTimeMillis()}"
            val url = URL("https://api.cloudinary.com/v1_1/ibiejjya/image/upload")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            val connOutputStream = connection.outputStream
            val writer = PrintWriter(OutputStreamWriter(connOutputStream, "UTF-8"), true)

            // Add upload_preset
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            writer.append("voz_barrial\r\n")

            // Add file
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"evidence.jpg\"\r\n")
            writer.append("Content-Type: image/jpeg\r\n\r\n")
            writer.flush()

            connOutputStream.write(compressedBytes)
            connOutputStream.flush()

            writer.append("\r\n--$boundary--\r\n")
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                json.optString("secure_url").ifBlank { null }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun publish(report: Report, onResult: (Result<Unit>) -> Unit) {
        firestore.collection(REPORTS).document(report.id).set(report)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener {
                android.util.Log.e("ReportsRepository", "Error publishing report: ${it.message}", it)
                onResult(Result.failure(it))
            }
    }

    override fun update(reportId: String, title: String, description: String, onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        val ref = firestore.collection(REPORTS).document(reportId)
        firestore.runTransaction { tx ->
            val snapshot = tx.get(ref)
            val ownerUid = snapshot.getString("authorUid").orEmpty()
            val ownerEmail = snapshot.getString("authorEmail").orEmpty()
            require(ownerUid == user.uid || (ownerUid.isBlank() && ownerEmail.equals(user.email, true))) { "Solo puedes editar tus propios reportes." }
            tx.update(ref, mapOf("title" to title.trim(), "description" to description.trim()))
        }.addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun delete(reportId: String, onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        val ref = firestore.collection(REPORTS).document(reportId)
        firestore.runTransaction { tx ->
            val snapshot = tx.get(ref)
            val ownerUid = snapshot.getString("authorUid").orEmpty()
            val ownerEmail = snapshot.getString("authorEmail").orEmpty()
            require(ownerUid == user.uid || (ownerUid.isBlank() && ownerEmail.equals(user.email, true))) { "Solo puedes eliminar tus propios reportes." }
            tx.delete(ref)
        }.addOnSuccessListener {
            onResult(Result.success(Unit))
        }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun moderate(reportId: String, status: String, onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser
            ?: return onResult(Result.failure(IllegalStateException("Inicia sesión para moderar reportes.")))
        val ref = firestore.collection(REPORTS).document(reportId)
        val adminRef = firestore.collection("usuarios").document(user.uid)
        firestore.runTransaction { tx ->
            val adminProfile = tx.get(adminRef)
            val snapshot = tx.get(ref)
            val isAdmin = user.email.equals(ADMIN_EMAIL, ignoreCase = true) ||
                adminProfile.getString("role").equals("ADMIN", ignoreCase = true)
            require(isAdmin) { "Esta acción está reservada al administrador." }
            require(snapshot.exists()) { "El reporte ya no está disponible." }
            tx.update(ref, mapOf(
                "status" to status,
                "moderatedAt" to System.currentTimeMillis(),
                "moderatedBy" to user.uid
            ))
        }.addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun reject(
        reportId: String,
        reason: String,
        explanation: String,
        allowResubmission: Boolean,
        notifyByEmail: Boolean,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val user = auth.currentUser
            ?: return onResult(Result.failure(IllegalStateException("Inicia sesión para moderar reportes.")))
        val ref = firestore.collection(REPORTS).document(reportId)
        val adminRef = firestore.collection("usuarios").document(user.uid)
        firestore.runTransaction { tx ->
            val adminProfile = tx.get(adminRef)
            val snapshot = tx.get(ref)
            val isAdmin = user.email.equals(ADMIN_EMAIL, ignoreCase = true) ||
                adminProfile.getString("role").equals("ADMIN", ignoreCase = true)
            require(isAdmin) { "Esta acción está reservada al administrador." }
            require(snapshot.exists()) { "El reporte ya no está disponible." }
            tx.update(ref, mapOf(
                "status" to com.vozbarrial.domain.model.ReportStatus.RECHAZADO.value,
                "rejectionReason" to reason.trim(),
                "rejectionExplanation" to explanation.trim(),
                "allowResubmission" to allowResubmission,
                "notifyByEmail" to notifyByEmail,
                "moderatedAt" to System.currentTimeMillis(),
                "moderatedBy" to user.uid
            ))
        }.addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    companion object {
        private const val REPORTS = "reportes"
        private const val ADMIN_EMAIL = "vozbarrial@gmail.com"
    }
}
