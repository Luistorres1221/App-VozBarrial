package com.vozbarrial.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vozbarrial.data.auth.AuthRepository
import com.vozbarrial.data.profile.ProfileRepository
import com.vozbarrial.data.reports.ReportsRepository
import com.vozbarrial.data.store.StoreRepository
import com.vozbarrial.features.auth.presentation.viewmodel.AuthViewModelFactory
import com.vozbarrial.features.profile.ProfileViewModelFactory
import com.vozbarrial.features.reports.ReportsViewModelFactory
import com.vozbarrial.features.store.StoreViewModelFactory

/** Composition root: Firebase clients and repositories are created once and shared. */
class AppContainer {
    private val auth by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FirebaseAuth.getInstance() }
    private val firestore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FirebaseFirestore.getInstance() }
    private val storage by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FirebaseStorage.getInstance() }

    val authRepository by lazy { AuthRepository(auth, firestore) }
    val profileRepository by lazy { ProfileRepository(auth, firestore, storage) }
    val reportsRepository by lazy { ReportsRepository(auth, firestore, storage) }
    val storeRepository by lazy { StoreRepository(auth, firestore) }

    val authViewModelFactory by lazy { AuthViewModelFactory(authRepository) }
    val profileViewModelFactory by lazy { ProfileViewModelFactory(profileRepository) }
    val reportsViewModelFactory by lazy { ReportsViewModelFactory(reportsRepository) }
    val storeViewModelFactory by lazy { StoreViewModelFactory(storeRepository) }
}
