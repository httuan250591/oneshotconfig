package com.onshortconfig.smartconfig.core

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

abstract class HMViewModel : ViewModel(), HMLifeCycleBehaviour {

    interface OnRequestListener {

        fun onWillRequest(request: HMRequestObserver<*, *>)

        fun onRequestResponseSuccess(request: HMRequestObserver<*, *>, responseData: Any?)

        fun onRequestResponseError(request: HMRequestObserver<*, *>, error: HMRequestException)

        fun onRequestDidFinished(request: HMRequestObserver<*, *>)

        fun onRequestDidFinishedAll()
    }

    val isNetworkLogin: MutableLiveData<Boolean> = MutableLiveData(true)
    var isNetwork: ObservableField<Boolean> = ObservableField(false)
    var isNetworkBusy: MutableLiveData<Boolean> = MutableLiveData()

    override var lifecycle: Lifecycle? = null
    private var requestCallbacks = ArrayList<OnRequestListener>()
    private var requestQueues = ObservableArrayList<HMRequestObserver<*, *>>()
    protected var compositeDisposable = CompositeDisposable()

    fun setRequestCallback(listener: OnRequestListener) {
        this@HMViewModel.requestCallbacks.add(listener)
    }

    fun <T, R : Observable<T>> request(
        requestServiceMethod: R,
        isUniqueCallback: Boolean = true
    ): HMRequestObserver<T, R> {

        val instance = HMRequestObserver<T, R>(requestServiceMethod)

        instance.doBeforeRequest { requestObserver ->
            this@HMViewModel.requestQueues.add(requestObserver)
            this@HMViewModel.requestCallbacks.forEach { it.onWillRequest(requestObserver) }
        }

        instance.success { request, responseData ->
            this@HMViewModel.requestCallbacks.forEach {
                it.onRequestResponseSuccess(
                    request,
                    responseData
                )
            }
        }

        instance.error { request, exception ->
            this@HMViewModel.requestCallbacks.forEach {
                it.onRequestResponseError(
                    request,
                    exception
                )
            }
        }

        instance.doAfterResponse { request ->
            this@HMViewModel.requestQueues.removeAll { it.id == request.id }
            this@HMViewModel.requestCallbacks.forEach {
                it.onRequestDidFinished(request)
                if (requestQueues.isEmpty()) {
                    it.onRequestDidFinishedAll()
                }
            }
        }

        return instance
    }

    private fun getClassNameCallMethod(): String {
        val stackTraceElement = Thread.currentThread().stackTrace[3]
        return stackTraceElement.className
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    override fun lifeCycleOnCreate() {
        super.lifeCycleOnCreate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun lifeCycleOnStart() {
        super.lifeCycleOnStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun lifeCycleOnResume() {
        super.lifeCycleOnResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun lifeCycleOnPause() {
        super.lifeCycleOnPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun lifeCycleOnStop() {
        super.lifeCycleOnStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun lifeCycleOnDestroy() {
        this@HMViewModel.requestCallbacks.clear()
        this@HMViewModel.compositeDisposable.dispose()
        super.lifeCycleOnDestroy()
    }

    fun add(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    // UIViewState
//    private val _viewState: SingleLiveEvent<UIViewState> = SingleLiveEvent()
    private val _viewState: SingleLiveEvent<UIViewState> = SingleLiveEvent()
    val viewState: LiveData<UIViewState> get() = _viewState

    protected fun showLoading() {
        showLoading(true)
    }

    protected fun hideLoading() {
        showLoading(false)
    }

    protected fun showLoading(newIsLoading: Boolean) {
//        if (_viewState.value != null)
        _viewState.value = UIViewState(isLoading = newIsLoading)
    }

    protected fun showSuccess(newIsSuccess: Boolean) {
//        if (_viewState.value != null)
        _viewState.value = UIViewState(isSuccess = newIsSuccess)
    }

    protected fun showError(message: String?) {
//        if (_viewState.value != null)
        _viewState.value = UIViewState(isError = message)
    }

    fun onTimeout() {
//        if (_viewState.value != null)
        _viewState.value = UIViewState(
            isLoading = false,
            isTimeout = true
        )
    }

    open fun onComplete() {
//        if (_viewState.value != null)
        _viewState.value = UIViewState(
            isLoading = false,
            isError = null,
            isTimeout = false
        )
    }

    fun onFailure(errorMessage: String) {
        Timber.e("onFailure: $errorMessage")
//        if (_viewState.value != null)
        _viewState.value = UIViewState(
            isError = errorMessage,
            isLoading = false,
            isSuccess = false
        )
    }

    fun onUnAuthorized() {
        Timber.e("onUnAuthorized")
//        if (_viewState.value != null)
        _viewState.value = UIViewState(
            isLoading = false,
            isUnAuthorized = true
        )
    }
}