package com.onshortconfig.smartconfig.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import timber.log.Timber

interface HMLifeCycleBehaviour : LifecycleObserver {

    var lifecycle: Lifecycle?

    fun bind(lifecycle: Lifecycle) {
        if (this@HMLifeCycleBehaviour.lifecycle != null) {
            return
        }

        this@HMLifeCycleBehaviour.lifecycle = lifecycle
        this@HMLifeCycleBehaviour.lifecycle?.addObserver(this@HMLifeCycleBehaviour)
    }

    fun unbind() {
        this@HMLifeCycleBehaviour.lifecycle?.removeObserver(this@HMLifeCycleBehaviour)
    }

    fun lifeCycleOnCreate() {
        Timber.i("${this@HMLifeCycleBehaviour::class.java.simpleName} onCreate")
    }

    fun lifeCycleOnStart() {
        Timber.i("${this@HMLifeCycleBehaviour::class.java.simpleName} onStart")
    }

    fun lifeCycleOnStop() {
        Timber.i("${this@HMLifeCycleBehaviour::class.java.simpleName} onStop")
    }

    fun lifeCycleOnPause() {
        Timber.i("${this@HMLifeCycleBehaviour::class.java.simpleName} onPause")
    }

    fun lifeCycleOnResume() {
        Timber.i("${this@HMLifeCycleBehaviour::class.java.simpleName} onResume")
    }

    fun lifeCycleOnDestroy() {
        Timber.i("${this@HMLifeCycleBehaviour::class.java.simpleName} onDestroy")
    }
}