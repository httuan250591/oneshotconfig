package com.onshortconfig.smartconfig.helper

import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.util.Log
import timber.log.Timber
import java.io.IOException
import java.net.*

internal class UdpHelper(iUdpHelper: IUdpHelper) : Runnable {
    private var iUdpHelper: IUdpHelper? = null
    val port = 65534
    private val datagramSocket = DatagramSocket(port)

    init {
        this.iUdpHelper = iUdpHelper
    }

    private fun startListen() {
        val message = ByteArray(100)
        try {
            datagramSocket.broadcast = true
            datagramSocket.soTimeout = 1000
            val datagramPacket = DatagramPacket(
                    message,
                    message.size
            )
            try {
                iUdpHelper?.onHandleListen(datagramSocket, datagramPacket)
            } catch (e: IOException) { //IOException
                e.printStackTrace()
            }
            datagramSocket.close()
        } catch (e: SocketException) {
            e.printStackTrace()
//            Timber.e(e)
        } finally {
            iUdpHelper?.onFinally()
        }
    }

    override fun run() {
        startListen()
    }


    interface IUdpHelper {
        fun onHandleListen(datagramSocket: DatagramSocket, datagramPacket: DatagramPacket)
        fun onFinally()
    }
}