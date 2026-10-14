package com.vozbarrial.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vozbarrial.data.profile.ProfileRepository
import com.vozbarrial.data.reports.ReportsRepository
import com.vozbarrial.data.store.StoreRepository
import com.vozbarrial.features.reports.ReportsViewModelFactory
import com.vozbarrial.features.store.StoreViewModelFactory

class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    val profileRepository by lazy { ProfileRepository(auth, firestore, storage) }
    val reportsRepository by lazy { ReportsRepository(auth, firestore, storage) }
    val storeRepository by lazy { StoreRepository(auth, firestore) }

    val reportsViewModelFactory by lazy { ReportsViewModelFactory(reportsRepository) }
    val storeViewModelFactory by lazy { StoreViewModelFactory(storeRepository) }
}
