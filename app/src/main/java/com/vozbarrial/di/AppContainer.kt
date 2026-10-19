package com.vozbarrial.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vozbarrial.data.repository.ProfileRepository
import com.vozbarrial.data.repository.ReportsRepository
import com.vozbarrial.data.repository.StoreRepository
import com.vozbarrial.features.store.StoreViewModelFactory

class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    val profileRepository by lazy { ProfileRepository(auth, firestore) }
    val reportsRepository by lazy { ReportsRepository(auth, firestore) }
    val storeRepository by lazy { StoreRepository(auth, firestore) }

    val storeViewModelFactory by lazy { StoreViewModelFactory(storeRepository) }
}
