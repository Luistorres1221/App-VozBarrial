package com.vozbarrial.domain

import com.vozbarrial.domain.enums.FrameType
import com.vozbarrial.domain.enums.UserRole

data class Usuario(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val equippedFrame: String = FrameType.CLASICO_CIVICO.frameName,
    val points: Int = 0,
    val ownedFrames: List<String> = listOf(FrameType.CLASICO_CIVICO.frameName),
    val role: UserRole = UserRole.USUARIO,
    val createdAt: Long = System.currentTimeMillis()
)
