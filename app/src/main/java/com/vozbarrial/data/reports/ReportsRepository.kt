package com.vozbarrial.data.reports

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.vozbarrial.domain.Reporte

class ReportsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private var listener: ListenerRegistration? = null

    fun observeReports(onChange: (List<Reporte>) -> Unit) {
        if (listener != null) return
        listener = firestore.collection(REPORTS).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            onChange(snapshot.documents.mapNotNull { it.toObject(Reporte::class.java)?.copy(id = it.id) })
        }
    }

    fun stopObserving() { listener?.remove(); listener = null }

    fun publish(report: Reporte, photoUri: Uri, onResult: (Result<Unit>) -> Unit) {
        val ref = storage.reference.child("reportes/${report.id}/evidencia")
        ref.putFile(photoUri).continueWithTask { task ->
            if (!task.isSuccessful) throw (task.exception ?: IllegalStateException("No se pudo subir la evidencia."))
            ref.downloadUrl
        }.continueWithTask { task ->
            if (!task.isSuccessful) throw (task.exception ?: IllegalStateException("No se pudo obtener la evidencia."))
            firestore.collection(REPORTS).document(report.id).set(report.copy(imageUrl = task.result.toString()))
        }.addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun update(reportId: String, title: String, description: String, onResult: (Result<Unit>) -> Unit) {
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

    fun delete(reportId: String, onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        val ref = firestore.collection(REPORTS).document(reportId)
        firestore.runTransaction { tx ->
            val snapshot = tx.get(ref)
            val ownerUid = snapshot.getString("authorUid").orEmpty()
            val ownerEmail = snapshot.getString("authorEmail").orEmpty()
            require(ownerUid == user.uid || (ownerUid.isBlank() && ownerEmail.equals(user.email, true))) { "Solo puedes eliminar tus propios reportes." }
            tx.delete(ref)
        }.addOnSuccessListener {
            storage.reference.child("reportes/$reportId/evidencia").delete()
                .addOnCompleteListener { onResult(Result.success(Unit)) }
        }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    companion object { private const val REPORTS = "reportes" }
}
