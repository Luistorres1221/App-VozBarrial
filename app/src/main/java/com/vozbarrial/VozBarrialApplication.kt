package com.vozbarrial

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.vozbarrial.di.AppContainer

class VozBarrialApplication : Application() {
    val appContainer: AppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppContainer() }

    override fun onCreate() {
        super.onCreate()
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
        } catch (_: Exception) {}
    }
}
