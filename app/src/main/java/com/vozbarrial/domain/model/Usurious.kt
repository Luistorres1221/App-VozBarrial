package com.vozbarrial.domain.model

data class Usurious(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val equippedFrame: String = "",
    val points: Int = 0,
    val monedas: Int = 0,
    val nivel: String = Nivel.NOVATO.value,
    val ownedFrames: List<String> = emptyList(),
    val role: UserRole = UserRole.USUARIO,
    val createdAt: Long = System.currentTimeMillis()
)
