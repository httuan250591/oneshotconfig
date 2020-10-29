package com.onshortconfig.smartconfig.core

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

abstract class HMActivity : AppCompatActivity(), HMControllerBehaviour,
    HMControllerDelegate,
    HMViewModel.OnRequestListener {

    @LayoutRes
    override fun getLayoutResId(savedInstanceState: Bundle?): Int? = null

    @MenuRes
    override fun getMenuResId(): Int? = null

    @StringRes
    override fun getTitleResId(): Int? = null

    override fun getTitleString(): String? = null

    override fun getToolbar(): androidx.appcompat.widget.Toolbar? = null

    override fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?) = Unit

    open fun getActionReceiverList(): List<String>? = null

    open fun getDataFromBroadcastReceiver(action: String, intent: Intent) = Unit

    open fun getFullScreen(): Boolean = false

    private val intentNetworkFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    private val intentFilter = IntentFilter()

    private val networkReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            (this@HMActivity as? HMViewModelSource<*>)?.let {
                val conn = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo: NetworkInfo? = conn?.activeNetworkInfo
                val isNetwork = (networkInfo?.type == ConnectivityManager.TYPE_WIFI || networkInfo != null)
                this@HMActivity.viewModel.isNetworkLogin.postValue(isNetwork)
                this@HMActivity.viewModel.isNetwork.set(isNetwork)
                this@HMActivity.viewModel.isNetworkBusy.postValue(isNetwork)
            }
        }
    }

    private val actionReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (getActionReceiverList().isNullOrEmpty()) return

            intent?.let {
                if (getActionReceiverList()!!.contains(it.action)) {
                    it.action?.let { it1 -> getDataFromBroadcastReceiver(it1, it) }
                }
            }
        }
    }

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val onBackPressedSubject: PublishSubject<Boolean> = PublishSubject.create()

    val alertHelper: HMAlertHelper by lazy {
        HMAlertHelper(this@HMActivity)
    }

    open fun isRequiredUserBackButton(): Boolean = true

    final override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // inflate layout resource if it is available
        this@HMActivity.getLayoutResId(savedInstanceState)?.let { layoutResId ->
            this@HMActivity.setContentView(layoutResId)

            val parentView = this@HMActivity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            this@HMActivity.onContentViewCreated(parentView, savedInstanceState)
        }

        // bind lifecycle
        (this@HMActivity as? HMViewModelSource<*>)?.let {
            this@HMActivity.viewModel.bind(this@HMActivity.lifecycle)
            this@HMActivity.viewModel.setRequestCallback(this@HMActivity)
        }

        // set title for activity using "titleResId" or "titleString"
        this@HMActivity.getTitleResId()?.let { titleResId ->
            this@HMActivity.setTitle(titleResId)
        }

        this@HMActivity.getTitleString()?.let { title ->
            this@HMActivity.title = title
        }

        this@HMActivity.setSupportActionBar(getToolbar())


        // register the broadcast receiver with network filter
        this@HMActivity.registerReceiver(this@HMActivity.networkReceiver, this@HMActivity.intentNetworkFilter)

        this@HMActivity.getActionReceiverList()?.let { actionList ->
            actionList.forEach { action ->
                intentFilter.addAction(action)
            }

            this@HMActivity.registerReceiver(this@HMActivity.actionReceiver, this@HMActivity.intentFilter)
        }

        // handle use press back button
        this@HMActivity.compositeDisposable.add(onBackPressedSubject
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { Toast.makeText(this@HMActivity, "Press back again to exit!", Toast.LENGTH_LONG).show() }
            .timeInterval(TimeUnit.MILLISECONDS)
            .skip(1)
            .subscribe { super.onBackPressed() }
        )

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this@HMActivity.getMenuResId()?.let { menuResId ->
            this@HMActivity.menuInflater.inflate(menuResId, menu)
        }
        MenuCompat.setGroupDividerEnabled(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.dispatchTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            val currentView = currentFocus
            currentView?.let {
                if (currentView is EditText) {
                    val outRect = Rect()
                    currentView.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.roundToInt(), event.rawY.roundToInt())) {
                        currentView.clearFocus()
                        val imm = getSystemService (Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0)
                        if (getFullScreen()) {
                            initFullScreen()
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && getFullScreen()) {
            initFullScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (getFullScreen()) {
            initFullScreen()
        }
    }

    private fun initFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onBackPressed() {
        if (!isRequiredUserBackButton()) {
            super.onBackPressed()
            return
        }

        this@HMActivity.onBackPressedSubject.onNext(true)
    }

    override fun onDestroy() {
        Timber.i("${this@HMActivity::class.java.simpleName} onDestroy")
        this@HMActivity.compositeDisposable.clear()
        this@HMActivity.networkReceiver.let {
            this@HMActivity.unregisterReceiver(it)
        }

        this@HMActivity.getActionReceiverList()?.let {
            this@HMActivity.unregisterReceiver(this@HMActivity.actionReceiver)
        }
        super.onDestroy()
    }

    override fun onWillRequest(request: HMRequestObserver<*, *>) {
        Timber.i("${this@HMActivity::class.java}", "onWillRequest")
        // todo anything before request
    }

    override fun onRequestResponseSuccess(request: HMRequestObserver<*, *>, responseData: Any?) {
        Timber.i("${this@HMActivity::class.java}", "onRequestResponseSuccess")
    }

    override fun onRequestResponseError(request: HMRequestObserver<*, *>, error: HMRequestException) {
        Timber.e("${this@HMActivity::class.java}", "onRequestResponseError")
    }

    override fun onRequestDidFinished(request: HMRequestObserver<*, *>) {
        // todo anything after request
        Timber.i("${this@HMActivity::class.java}", "onRequestDidFinished")
    }

    override fun onRequestDidFinishedAll() {
        Timber.i("${this@HMActivity::class.java}", "onRequestDidFinishedAll")
    }

}