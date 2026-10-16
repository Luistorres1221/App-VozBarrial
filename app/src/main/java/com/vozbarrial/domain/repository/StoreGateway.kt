package com.vozbarrial.domain.repository

import com.vozbarrial.domain.model.Usurious

interface StoreGateway {
    fun equip(frame: String, onResult: (Result<Unit>) -> Unit)
    fun purchase(frame: String, cost: Int, onResult: (Result<Usurious>) -> Unit)
}
