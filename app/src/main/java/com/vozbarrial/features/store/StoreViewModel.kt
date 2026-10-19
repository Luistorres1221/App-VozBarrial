package com.vozbarrial.features.store

import androidx.lifecycle.ViewModel
import com.vozbarrial.domain.model.Usurious
import com.vozbarrial.domain.repository.StoreGateway
import com.vozbarrial.data.repository.StoreRepository

class StoreViewModel(private val repository: StoreGateway = StoreRepository()) : ViewModel() {
    fun equip(frame: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) =
        repository.equip(frame) { result -> onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage) }

    fun purchase(frame: String, cost: Int, onProfileUpdated: (Usurious) -> Unit, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) =
        repository.purchase(frame, cost) { result ->
            result.onSuccess(onProfileUpdated)
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }
}
