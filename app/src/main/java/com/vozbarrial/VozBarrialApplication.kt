package com.vozbarrial

import android.app.Application
import com.vozbarrial.di.AppContainer

class VozBarrialApplication : Application() {
    val appContainer: AppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppContainer() }
}
