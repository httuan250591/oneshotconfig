package com.onshortconfig.smartconfig.core

import android.content.Context
import androidx.annotation.RawRes
import java.io.BufferedInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

object HMSocketFactory {

    fun getSocketFactory(context: Context, @RawRes certificateResId: Int): SSLSocketFactory? {
        try {
            val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
            val bufferInputStream = BufferedInputStream(context.resources.openRawResource(certificateResId))
            val certificate: X509Certificate = bufferInputStream.use { it ->
                certificateFactory.generateCertificate(it) as X509Certificate
            }

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType).apply {
                load(null, null)
                setCertificateEntry("ca", certificate)
            }

            val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                init(keyStore)
            }

            val sslContext: SSLContext = SSLContext.getInstance("TLS").apply {
                init(null, tmf.trustManagers, null)
            }

            return sslContext.socketFactory
        } catch (e: Exception) {
            return null
        }
    }
}