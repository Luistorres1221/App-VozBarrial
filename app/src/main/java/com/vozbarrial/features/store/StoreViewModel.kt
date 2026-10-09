package com.vozbarrial.features.store

import androidx.lifecycle.ViewModel
import com.vozbarrial.data.store.StoreRepository
import com.vozbarrial.domain.Usuario

class StoreViewModel(private val repository: StoreRepository) : ViewModel() {
    fun equip(frame: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) =
        repository.equip(frame) { result -> onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage) }

    fun purchase(frame: String, cost: Int, onProfileUpdated: (Usuario) -> Unit, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) =
        repository.purchase(frame, cost) { result ->
            result.onSuccess(onProfileUpdated)
            onComplete(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
        }
}
