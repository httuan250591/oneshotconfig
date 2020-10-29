package com.onshortconfig.smartconfig.core

import androidx.annotation.StringRes
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList

open class HMRequestObserver<T, R : Observable<T>>(private val requestObservable: R) : DisposableObserver<T>() {

    val id = UUID.randomUUID().toString()

    @StringRes
    var requestId: Int? = null

    private val onBeforeRequestListeners: ArrayList<((request: HMRequestObserver<T, R>) -> Unit)> = ArrayList()
    private val onSuccessRequestListeners: ArrayList<((request: HMRequestObserver<T, R>, responseData: T) -> Unit)> =
        ArrayList()
    private val onErrorRequestListeners: ArrayList<((request: HMRequestObserver<T, R>, exception: HMRequestException) -> Unit)> =
        ArrayList()
    private val onAfterRequestListeners: ArrayList<((request: HMRequestObserver<T, R>) -> Unit)> = ArrayList()

    fun setRequestId(@StringRes requestId: Int?): HMRequestObserver<T, R> {
        this@HMRequestObserver.requestId = requestId
        return this@HMRequestObserver
    }

    fun success(block: (request: HMRequestObserver<T, R>, responseData: T) -> Unit): HMRequestObserver<T, R> {
        this@HMRequestObserver.onSuccessRequestListeners.add(block)
        return this@HMRequestObserver
    }

    fun error(block: (request: HMRequestObserver<T, R>, exception: HMRequestException) -> Unit): HMRequestObserver<T, R> {
        this@HMRequestObserver.onErrorRequestListeners.add(block)
        return this@HMRequestObserver
    }

    fun doBeforeRequest(block: (request: HMRequestObserver<T, R>) -> Unit): HMRequestObserver<T, R> {
        this@HMRequestObserver.onBeforeRequestListeners.add(block)
        return this@HMRequestObserver
    }

    fun doAfterResponse(block: (request: HMRequestObserver<T, R>) -> Unit): HMRequestObserver<T, R> {
        this@HMRequestObserver.onAfterRequestListeners.add(block)
        return this@HMRequestObserver
    }

    fun submit(
        subscribeThread: Scheduler = Schedulers.io(),
        observableThread: Scheduler = AndroidSchedulers.mainThread()
    ): HMRequestObserver<T, R> {
        this@HMRequestObserver.requestObservable
            .subscribeOn(subscribeThread)
            .observeOn(observableThread)
            .subscribeWith(this@HMRequestObserver)
        return this@HMRequestObserver
    }

    final override fun onStart() {
        super.onStart()
        this@HMRequestObserver.onBeforeRequestListeners.forEach { it.invoke(this@HMRequestObserver) }
    }


    final override fun onComplete() {
        Timber.i("onComplete")
    }

    final override fun onNext(t: T) {
        Timber.i("onNext")
        this@HMRequestObserver.onSuccessRequestListeners.forEach { it.invoke(this@HMRequestObserver, t) }
        this@HMRequestObserver.onAfterRequestListeners.forEach { it.invoke(this@HMRequestObserver) }
    }

    final override fun onError(throwable: Throwable) {
        Timber.i("onError: $throwable")
        val exception = HMRequestException()
        exception.originalException = throwable

        when (throwable) {
            is HttpException -> {
                exception.type = HMRequestException.HMExceptionType.SERVER
                exception.httpBody = throwable.response()?.errorBody()?.string()
                exception.statusCode = throwable.response()?.code()!!
            }
            is SocketTimeoutException -> {
                exception.type = HMRequestException.HMExceptionType.NETWORK_TIMEOUT
            }
            is IOException -> {
                exception.type = HMRequestException.HMExceptionType.NO_INTERNET
            }
            else -> {
                exception.type = HMRequestException.HMExceptionType.UNKNOWN
            }
        }

        this@HMRequestObserver.onErrorRequestListeners.forEach { it.invoke(this@HMRequestObserver, exception) }
    }
}