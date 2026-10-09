package com.vozbarrial.data.profile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.vozbarrial.domain.Usuario

class ProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    fun hasActiveSession(): Boolean = auth.currentUser != null

    fun loadProfile(onResult: (Result<Usuario?>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.success(null))
        firestore.collection(USERS).document(user.uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(Result.success(snapshot.toObject(Usuario::class.java) ?: Usuario(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    name = user.email.orEmpty().substringBefore('@'),
                )))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun updateProfile(name: String, phone: String, photoUrl: String?, onResult: (Result<Usuario>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        fun save(url: String?) {
            val updated = mapOf("name" to name.trim(), "phone" to phone.trim(), "photoUrl" to url)
            firestore.collection(USERS).document(user.uid).set(updated, SetOptions.merge())
                .addOnSuccessListener {
                    onResult(Result.success(Usuario(uid = user.uid, name = name.trim(), email = user.email.orEmpty(), phone = phone.trim(), photoUrl = url)))
                }
                .addOnFailureListener { onResult(Result.failure(it)) }
        }
        val uri = photoUrl?.let(Uri::parse)
        if (uri != null && uri.scheme in setOf("content", "file")) {
            val ref = storage.reference.child("usuarios/${user.uid}/perfil")
            ref.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) throw (task.exception ?: IllegalStateException("No se pudo subir la foto."))
                ref.downloadUrl
            }.addOnSuccessListener { save(it.toString()) }
                .addOnFailureListener { onResult(Result.failure(it)) }
        } else save(photoUrl)
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        user.delete().addOnSuccessListener {
            firestore.collection(USERS).document(user.uid).delete()
                .addOnSuccessListener { onResult(Result.success(Unit)) }
                .addOnFailureListener { onResult(Result.failure(it)) }
        }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun signOut() = auth.signOut()

    companion object { private const val USERS = "usuarios" }
}
