package com.vozbarrial.data.store

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.vozbarrial.domain.Usuario
import com.vozbarrial.domain.repository.StoreGateway

class StoreRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : StoreGateway {
    override fun equip(frame: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        firestore.collection(USERS).document(uid).update("equippedFrame", frame)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun purchase(frame: String, cost: Int, onResult: (Result<Usuario>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        val expectedCost = when {
            frame.startsWith("Guardi") -> 800
            frame.startsWith("Eco") -> 450
            frame.startsWith("Ojo") -> 800
            frame.startsWith("Paz") -> 1200
            frame.startsWith("Escudo") -> 500
            else -> null
        }
        if (expectedCost == null || expectedCost != cost) {
            return onResult(Result.failure(IllegalArgumentException("El artículo o su precio no son válidos.")))
        }
        val ref = firestore.collection(USERS).document(uid)
        firestore.runTransaction { transaction: Transaction ->
            val snapshot = transaction.get(ref)
            val profile = snapshot.toObject(Usuario::class.java) ?: throw IllegalStateException("No se encontró el perfil ciudadano.")
            require(cost > 0) { "El costo del artículo no es válido." }
            require(frame !in profile.ownedFrames) { "Este marco ya fue adquirido." }
            require(profile.points >= cost) { "No tienes puntos suficientes." }
            profile.copy(points = profile.points - cost, ownedFrames = profile.ownedFrames + frame, equippedFrame = frame)
                .also { transaction.set(ref, it) }
        }.addOnSuccessListener { onResult(Result.success(it)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    companion object { private const val USERS = "usuarios" }
}
