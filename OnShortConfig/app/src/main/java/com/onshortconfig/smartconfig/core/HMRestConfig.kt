package com.onshortconfig.smartconfig.core

import android.content.Context
import androidx.annotation.RawRes

data class HMRestConfig(
    var context: Context? = null,
    var baseURL: String = "",
    var authorizationToken: String? = null,
    var authorizationTokenPrefix: String = "Bearer",
    @RawRes var certificateResId: Int? = null,
    var exceptionParsers: List<HMRestParseException> = listOf(),
    var additionalHeaders: Map<String, String> = emptyMap()
) {

    fun updateContext(context: Context): HMRestConfig {
        this@HMRestConfig.context = context
        return this@HMRestConfig
    }

    fun updateAPIBaseURL(baseURL: String): HMRestConfig {
        this@HMRestConfig.baseURL = baseURL
        return this@HMRestConfig
    }

    fun updateAuthorizationToken(authorizationToken: String?): HMRestConfig {
        this@HMRestConfig.authorizationToken = authorizationToken
        return this@HMRestConfig
    }

    fun updateAuthorizationTokenPrefix(authorizationTokenPrefix: String = "Bearer"): HMRestConfig {
        this@HMRestConfig.authorizationTokenPrefix = authorizationTokenPrefix
        return this@HMRestConfig
    }

    fun updateCertificateRawResId(certificateResId: Int): HMRestConfig {
        this@HMRestConfig.certificateResId = certificateResId
        return this@HMRestConfig
    }

    fun updateAdditionalHeaders(additionalHeaders: Map<String, String>): HMRestConfig {
        this@HMRestConfig.additionalHeaders = additionalHeaders
        return this@HMRestConfig
    }

    fun updateExceptionParsers(exceptionParsers: List<HMRestParseException>): HMRestConfig {
        this@HMRestConfig.exceptionParsers = exceptionParsers
        return this@HMRestConfig
    }
}