package com.vozbarrial.domain.repository

import com.vozbarrial.domain.Usuario

interface StoreGateway {
    fun equip(frame: String, onResult: (Result<Unit>) -> Unit)
    fun purchase(frame: String, cost: Int, onResult: (Result<Usuario>) -> Unit)
}
