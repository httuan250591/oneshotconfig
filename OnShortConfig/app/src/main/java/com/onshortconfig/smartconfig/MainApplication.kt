package com.onshortconfig.smartconfig

import android.Manifest
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.multidex.MultiDexApplication
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication : MultiDexApplication(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_NETWORK_STATE
        )
    }

}