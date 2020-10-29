package com.onshortconfig.smartconfig.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.onshortconfig.smartconfig.R
import com.onshortconfig.smartconfig.core.HMBindingFragment
import com.onshortconfig.smartconfig.core.HMViewModelSource
import com.onshortconfig.smartconfig.databinding.FragmentCusDataBinding
import com.onshortconfig.smartconfig.helper.UDPReqThread
import com.winnermicro.smartconfig.ConfigType
import com.winnermicro.smartconfig.ISmartConfig
import com.winnermicro.smartconfig.SmartConfigFactory
import kotlinx.android.synthetic.main.fragment_cus_data.*
import timber.log.Timber

class CusDataFragment : HMBindingFragment<FragmentCusDataBinding>(R.layout.fragment_cus_data),
    HMViewModelSource<CusDataViewModel> {

    private var psw: String = ""
    private var smartConfig: ISmartConfig? = null
    private var isStart = false
    private var uDPReqThread: UDPReqThread? = null

    override val viewModel: CusDataViewModel by lazy {
        ViewModelProvider(this)[CusDataViewModel::class.java]
    }

    override fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?) {
        super.onContentViewCreated(parentView, saveInstanceState)
        val factory = SmartConfigFactory()
        smartConfig = factory.createSmartConfig(ConfigType.UDP, requireActivity())
        initUI()
    }

    fun onKeyDown() {
        if (isStart) stopConfig()
    }

    private fun initUI() {
        uDPReqThread = UDPReqThread(requireActivity(), object : UDPReqThread.IUDPReqThread {
            override fun onFinally() {
                Timber.d(" IUDPReqThread onFinally ")
                smartConfig?.stopConfig()
                requireActivity().runOnUiThread {
                    confPost
                }

            }

            override fun onWifiEnable() {
                Timber.d(" IUDPReqThread onWifiEnable ")
                while (isStart) {
                    if (smartConfig?.sendData(psw) == false) {
                        break
                    }
                    Thread.sleep(10)
                }
            }
        })

        btnConf.setOnClickListener {
            if (isStart) {
                stopConfig()
                return@setOnClickListener
            }
            psw = editPsw.text.toString()
            isStart = true
            setEditable(false)
            Thread(uDPReqThread).start()
            btnConf.text = getText(R.string.btn_stop_cus_data)
            requireActivity()
        }
    }

    private fun stopConfig() {
        isStart = false
        btnConf.isEnabled = false
    }

    private fun setEditable(value: Boolean) {
        if (value) {
            editPsw.isCursorVisible = true
            editPsw.isFocusable = true
            editPsw.isFocusableInTouchMode = true
            editPsw.requestFocus()
        } else {
            editPsw.isCursorVisible = false
            editPsw.isFocusable = false
            editPsw.isFocusableInTouchMode = false
            editPsw.clearFocus()
        }
    }

    private val confPost = Runnable {
        isStart = false
        btnConf.isEnabled = true
        setEditable(true)
        btnConf.text = getText(R.string.btn_cus_data)
    }

}