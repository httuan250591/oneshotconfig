package com.onshortconfig.smartconfig.helper

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import androidx.fragment.app.FragmentActivity

@SuppressLint("WifiManagerLeak")
class UDPReqThread(
    activity: FragmentActivity, iUDPReqThread: IUDPReqThread
) : Runnable {
    var activity: FragmentActivity? = null
    var iUDPReqThread: IUDPReqThread? = null

    init {
        this.activity = activity
        this.iUDPReqThread = iUDPReqThread
    }

    override fun run() {
        var wifiManager: WifiManager? = null
        try {
            wifiManager = activity?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            if (wifiManager!!.isWifiEnabled) {
                iUDPReqThread?.onWifiEnable()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            iUDPReqThread?.onFinally()
        }
    }

    interface IUDPReqThread {
        fun onWifiEnable()
        fun onFinally()
    }
}