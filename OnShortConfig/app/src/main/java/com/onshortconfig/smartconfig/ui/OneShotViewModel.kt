package com.onshortconfig.smartconfig.ui

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import com.onshortconfig.smartconfig.core.HMViewModel

class OneShotViewModel : HMViewModel() {

    private var activity: Activity? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    fun getWifiSSID(): String? {
        val ssid = "unknown id"
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            val mWifiManager = (activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            val info = mWifiManager.connectionInfo
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                info.ssid
            } else {
                info.ssid.replace("\"", "")
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            val connManager = (activity?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            val networkInfo = connManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                if (networkInfo.extraInfo != null) {
                    return networkInfo.extraInfo.replace("\"", "")
                }
            }
        }
        return ssid
    }

    private fun getWifiApConfiguration(): WifiConfiguration? {
        val wifiManager =
            activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method =
                wifiManager.javaClass.getMethod("getWifiApConfiguration")
            method.invoke(wifiManager) as WifiConfiguration
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun isWifiApEnabled(): Boolean {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED
    }

    private fun getWifiApState(): WIFI_AP_STATE? {
        var tmp: Int
        val wifiManager =
            activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method = wifiManager.javaClass.getMethod("getWifiApState")
            tmp = method.invoke(wifiManager) as Int
            if (tmp > 10) tmp -= 10
            WIFI_AP_STATE::class.java.enumConstants!![tmp]
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            WIFI_AP_STATE.WIFI_AP_STATE_FAILED
        }
    }

    enum class WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING, WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
    }
}