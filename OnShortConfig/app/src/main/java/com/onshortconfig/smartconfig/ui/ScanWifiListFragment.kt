package com.onshortconfig.smartconfig.ui

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.onshortconfig.smartconfig.R
import com.onshortconfig.smartconfig.core.HMBindingFragment
import com.onshortconfig.smartconfig.core.HMViewModelSource
import com.onshortconfig.smartconfig.databinding.FragmentScanWifiListBinding
import com.onshortconfig.smartconfig.entity.WifiModel
import kotlinx.android.synthetic.main.fragment_scan_wifi_list.*

class ScanWifiListFragment : HMBindingFragment<FragmentScanWifiListBinding>(R.layout.fragment_scan_wifi_list),
        HMViewModelSource<CusDataViewModel> {

    private var wifiManager: WifiManager? = null
    private var wifiModelList = arrayListOf<WifiModel>()
    override fun getActionReceiverList(): List<String>? {
        return arrayListOf(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    }

    override fun getDataFromBroadcastReceiver(action: String, intent: Intent) {
        when (action) {
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                val wifiList = wifiManager?.scanResults
                for (scanResult in wifiList!!) {
                    wifiModelList.add(WifiModel(scanResult.SSID, scanResult.capabilities))
                }
                Toast.makeText(context, Gson().toJson(wifiModelList), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override val viewModel: CusDataViewModel by lazy {
        ViewModelProvider(this)[CusDataViewModel::class.java]
    }

    override fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?) {
        super.onContentViewCreated(parentView, saveInstanceState)
        wifiManager = requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        initUI()
    }

    private fun initUI() {
        btnScan.setOnClickListener {
            wifiManager!!.startScan()
        }
    }

}