package com.onshortconfig.smartconfig.core

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.onshortconfig.smartconfig.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class HMFragment : Fragment(), HMControllerBehaviour,
    HMControllerDelegate, HMViewModel.OnRequestListener {

    override fun getLayoutResId(savedInstanceState: Bundle?): Int? = null

    override fun getTitleResId(): Int? = null

    override fun getMenuResId(): Int? = null

    override fun getTitleString(): String? = null

    override fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?) = Unit

    override fun getToolbar(): Toolbar? = null

    open fun getActionReceiverList(): List<String>? = null

    open fun getDataFromBroadcastReceiver(action: String, intent: Intent) = Unit

    open fun executeOnBackPressed() = Unit

    private val intentNetworkFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
    private val intentFilter = IntentFilter()
    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val onBackPressedSubject: PublishSubject<Boolean> = PublishSubject.create()
    protected fun onExecuteFinishActivity() {
        onBackPressedSubject.onNext(true)
    }

    private val networkReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            (this@HMFragment as? HMViewModelSource<*>).let { hmViewModelSource ->
                val conn =
                    context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val networkInfo: NetworkInfo? = conn?.activeNetworkInfo

                val isNetwork =
                    networkInfo?.type == ConnectivityManager.TYPE_WIFI || networkInfo != null
                hmViewModelSource?.viewModel?.isNetwork?.set(isNetwork)
                hmViewModelSource?.viewModel?.isNetworkLogin?.postValue(isNetwork)
                hmViewModelSource?.viewModel?.isNetworkBusy?.postValue(isNetwork)
            }
        }
    }

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (getActionReceiverList().isNullOrEmpty()) return

            intent?.action?.let {
                if (getActionReceiverList()!!.contains(it)) {
                    getDataFromBroadcastReceiver(it, intent)
                }
            }
        }
    }

    protected lateinit var alertHelper: HMAlertHelper

    companion object {
        inline fun <reified T : HMFragment> create(
            fragmentClass: Class<T>,
            bundle: Bundle? = null
        ): T {
            val instanceFragment = fragmentClass.newInstance()
            instanceFragment.arguments = bundle ?: Bundle()
            return instanceFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                executeOnBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("${this@HMFragment::class.java.simpleName}   ----------- onCreate")
        (this@HMFragment as? HMViewModelSource<*>)?.let { hmViewModelSource ->
            hmViewModelSource.viewModel.bind(this@HMFragment.lifecycle)
            hmViewModelSource.viewModel.setRequestCallback(this@HMFragment)
        }

        // handle use press back button
        this.compositeDisposable.add(onBackPressedSubject
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnNext {
                Toast.makeText(context, "Press back again to exit!", Toast.LENGTH_LONG).show()
            }
            .timeInterval(TimeUnit.MILLISECONDS)
            .skip(1)
            .filter {
                it.time() < 2000
            }
            .subscribe {
                requireActivity().finish()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onCreateView")
        this@HMFragment.getLayoutResId(savedInstanceState)?.let { layoutResId ->
            return inflater.inflate(layoutResId, container, false)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onViewCreated")
        this@HMFragment.alertHelper = HMAlertHelper(view.context)
        (this@HMFragment as? HMViewModelSource<*>)?.let { hmViewModelSource ->
            hmViewModelSource.viewModel.isNetworkBusy = MutableLiveData()
        }

        this@HMFragment.onContentViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onActivityCreated")
        this@HMFragment.getTitleResId()?.let {
            this@HMFragment.activity?.setTitle(it)
        }

        this@HMFragment.setHasOptionsMenu(this@HMFragment.getMenuResId() != null)
        this@HMFragment.activity?.invalidateOptionsMenu()
    }

    override fun onResume() {
        super.onResume()
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onResume")
        this@HMFragment.activity?.registerReceiver(
            this@HMFragment.networkReceiver,
            this@HMFragment.intentNetworkFilter
        )
        this@HMFragment.getActionReceiverList()?.let { actionList ->
            actionList.forEach { action ->
                intentFilter.addAction(action)
            }

            this@HMFragment.activity?.registerReceiver(
                this@HMFragment.actionReceiver,
                this@HMFragment.intentFilter
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onPause")
        (this@HMFragment as? HMViewModelSource<*>)?.let { hmViewModelSource ->
            hmViewModelSource.viewModel.isNetworkBusy = MutableLiveData()
        }

        this@HMFragment.networkReceiver?.let {
            this@HMFragment.activity?.unregisterReceiver(it)
        }

        this@HMFragment.getActionReceiverList()?.let {
            this@HMFragment.activity?.unregisterReceiver(this@HMFragment.actionReceiver)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this@HMFragment.getMenuResId()?.let { menuResId ->
            inflater.inflate(menuResId, menu)
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onStart")
    }

    override fun onStop() {
        super.onStop()
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("${this@HMFragment::class.java.simpleName}  ----------- onDestroy")
        compositeDisposable.clear()
    }

    override fun onWillRequest(request: HMRequestObserver<*, *>) {
        Timber.i("${this@HMFragment::class.java}", "onWillRequest")
        // todo anything before request
    }

    override fun onRequestResponseSuccess(request: HMRequestObserver<*, *>, responseData: Any?) {
        Timber.i("${this@HMFragment::class.java}", "onRequestResponseSuccess")
    }

    override fun onRequestResponseError(
        request: HMRequestObserver<*, *>,
        error: HMRequestException
    ) {
        Timber.e("${this@HMFragment::class.java}", "onRequestResponseError")
    }

    override fun onRequestDidFinished(request: HMRequestObserver<*, *>) {
        // todo anything after request
        Timber.i("${this@HMFragment::class.java}", "onRequestDidFinished")
    }

    override fun onRequestDidFinishedAll() {
        Timber.i("${this@HMFragment::class.java}", "onRequestDidFinishedAll")
    }

    // Handle UI status
    protected fun render(viewState: UIViewState) {

        // Loading
        //showProgress(viewState.isLoading)

        // Unauthorized
//        if (viewState.isUnAuthorized) {
//            logout()
//        }

        // Error message
        viewState.isError?.let {
            showError(it)
        }

        // Timeout
//        if (viewState.isTimeout)
//            showError(getString(R.string.connection_timed_out))
    }

    private fun showError(message: String) {
        alertHelper.toast(message)
   }

    private fun executeLogout() {
        requireActivity().finish()
    }
}
