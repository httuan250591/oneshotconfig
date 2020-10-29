package com.onshortconfig.smartconfig.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.onshortconfig.smartconfig.R
import com.onshortconfig.smartconfig.core.HMBindingFragment
import com.onshortconfig.smartconfig.core.HMViewModelSource
import com.onshortconfig.smartconfig.databinding.FragmentOneShotBinding
import com.onshortconfig.smartconfig.helper.UDPReqThread
import com.onshortconfig.smartconfig.helper.UdpHelper
import com.winnermicro.smartconfig.ConfigType
import com.winnermicro.smartconfig.IOneShotConfig
import com.winnermicro.smartconfig.SmartConfigFactory
import kotlinx.android.synthetic.main.fragment_one_shot.*
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket


class OneShotFragment : HMBindingFragment<FragmentOneShotBinding>(R.layout.fragment_one_shot),
        HMViewModelSource<OneShotViewModel> {

    private val TYPE_NO_PASSWD = 0x11
    private val TYPE_WEP = 0x12
    private val TYPE_WPA = 0x13

    private val MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1


    private var results = arrayListOf<ScanResult>()
    private var size = 0
    private var isStart = false
    private var ssid: String? = ""
    private var psw: String? = ""
    private var oneshotConfig: IOneShotConfig? = null
    private var isThreadDisable = false
    private val lstMac = arrayListOf<String>()
    private var udphelper: UdpHelper? = null
    private var uDPReqThread: UDPReqThread? = null
    private var factory: SmartConfigFactory? = null
    private var lock: WifiManager.MulticastLock? = null
    private var wifiManager: WifiManager? = null
    private var ITEM_KEY = "key"

    override val viewModel: OneShotViewModel by lazy {
        ViewModelProvider(this)[OneShotViewModel::class.java]
    }

    override fun getActionReceiverList(): List<String>? {
        return arrayListOf(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    }

    override fun getDataFromBroadcastReceiver(action: String, intent: Intent) {
        when (action) {
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                results = wifiManager?.scanResults as ArrayList<ScanResult>
                Timber.i("SCAN_RESULTS_AVAILABLE_ACTION %s", Gson().toJson(results))
            }
        }
    }

    override fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?) {
        super.onContentViewCreated(parentView, saveInstanceState)
        viewModel.setActivity(requireActivity())
        factory = SmartConfigFactory()
        oneshotConfig = factory?.createOneShotConfig(ConfigType.UDP)
        initUI()
    }

    private fun initUI() {

        wifiManager =
                requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wifiManager?.createMulticastLock("UDPwifi")

        udphelper = UdpHelper(object : UdpHelper.IUdpHelper {
            @SuppressLint("BinaryOperationInTimber")
            override fun onHandleListen(
                    datagramSocket: DatagramSocket,
                    datagramPacket: DatagramPacket
            ) {
                Timber.d("UDP Demo")
                while (!isThreadDisable) {
                    lock?.acquire()
                    try {
                        datagramSocket.receive(datagramPacket)
                        var strMsg = ""
                        val count = datagramPacket.length
                        for (i in 0 until count) {
                            strMsg += String.format("%02x", datagramPacket.data[i])
                        }
                        strMsg = strMsg.toUpperCase() + ";" + datagramPacket.address
                                .hostAddress.toString()
                        if (!lstMac.contains(strMsg)) {
                            lstMac.add(strMsg)
                            requireActivity().runOnUiThread {
                                notifyPost
                            }
                        }
                        Timber.d(
                                "UDP Demo %s", datagramPacket.address
                                .hostAddress.toString()
                                + ":" + strMsg
                        )
                    } catch (ex: Exception) {
                        Timber.d("Exception UDP  %s", ex.message)
                    }
                    lock?.release()
                }
            }

            override fun onFinally() {
                if (!isThreadDisable) requireActivity().runOnUiThread { confPost }
            }

        })

        uDPReqThread = UDPReqThread(requireActivity(), object : UDPReqThread.IUDPReqThread {
            override fun onWifiEnable() {
                Timber.d("uDPReqThread onWifiEnable")

                if (wifiManager?.isWifiEnabled!! || viewModel.isWifiApEnabled()) {
                    val timeout = 60 //miao
                    requireActivity().runOnUiThread {
                        confPre
                    }
                    oneshotConfig?.start(ssid, psw, timeout, requireContext())
                }
            }

            override fun onFinally() {
                Timber.d("uDPReqThread onFinally")
                oneshotConfig?.stop()
                requireActivity().runOnUiThread { confPost }
            }
        })

        btnConf.setOnClickListener {
            scanWifiList()
//            onStartConfig()
        }
    }

    private fun scanWifiList() {
        wifiManager?.startScan()
        try {
            size -= 1
            while (size >= 0) {
                val item = HashMap<String, String>()
                item[ITEM_KEY] = results[size].SSID + "  " + results[size].capabilities
                size--
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun onStartConfig() {
        Timber.i("btnConf.setOnClickListener")
        if (btnConf.text == getText(R.string.btn_stop_conf)) {
            stopConfig()
            return
        }
        Timber.i("btnConf.setOnClickListener start config")
        btnConf.isEnabled = false
        ssid = editSSID.text.toString()
        psw = editPsw.text.toString()
        lstMac.clear()
        isStart = true
        isThreadDisable = false
        setEditable(false)
        Thread(udphelper).start()
        Timber.i("start udphelper")
        Thread(uDPReqThread).start()
        Timber.i("start uDPReqThread")
        txtTotal.text = String.format("%d connected.", lstMac.size)
    }


    private val notifyPost = Runnable {
        //Todo handle show list
        txtTotal.text = (String.format("%d connected.", lstMac.size))
    }

    private val confPost = Runnable {
        isStart = false
        isThreadDisable = true
        setEditable(true)
        //Todo handle show list
        btnConf.text = getText(R.string.btn_conf)
        btnConf.isEnabled = true
    }

    private fun setEditable(value: Boolean) {
        editSSID.isCursorVisible = value
        editSSID.isFocusable = value
        editSSID.isFocusableInTouchMode = value
        editPsw.isCursorVisible = value
        editPsw.isFocusable = value
        editPsw.isFocusableInTouchMode = value
        if (value) {
            editPsw.requestFocus()
            editSSID.requestFocus()
        } else {
            editPsw.clearFocus()
            editSSID.clearFocus()
        }
    }

    private fun stopConfig() {
        isThreadDisable = true
        if (isStart) {
            isStart = false
            btnConf.isEnabled = false
        }
        oneshotConfig!!.stop()
    }

    private val confPre = Runnable {
        btnConf.text = getText(R.string.btn_stop_conf)
        btnConf.isEnabled = true
    }

}