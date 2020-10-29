package com.onshortconfig.smartconfig.core

import android.content.Context
import com.onshortconfig.smartconfig.R

class HMRequestException: HMJsonObject {
    var statusCode: Int = -1
    var httpBody: String? = null
    var type: HMExceptionType = HMExceptionType.UNKNOWN
    var originalException: Throwable? = null

    fun hasUnAuthorization(): Boolean {
        return arrayOf(401, 403).contains(this@HMRequestException.statusCode)
    }

    fun hasBadRequest(): Boolean {
        return arrayOf(400).contains(this@HMRequestException.statusCode)
    }

    fun getErrorMessage(context: Context, messageDefault: String = context.getString(R.string.hm_error_api_unknown)): String {

        return when (this@HMRequestException.type) {
            HMExceptionType.SERVER -> {
                // parse error response from backend and get actual message
                // otherwise (parsing error, no message,...) will return unknown error from local resource
                HMRestApi.config.exceptionParsers.asSequence().mapNotNull { parser ->
                    parser.getErrorMessage(this@HMRequestException.httpBody ?: "{}")
                }.firstOrNull() ?: messageDefault
            }
            HMExceptionType.NETWORK_TIMEOUT -> context.getString(R.string.hm_error_api_network_timeout)
            HMExceptionType.NO_INTERNET -> context.getString(R.string.hm_error_api_no_internet)
            else -> messageDefault
        }
    }

    enum class HMExceptionType {
        SERVER,
        NO_INTERNET,
        NETWORK_TIMEOUT,
        UNKNOWN,
    }
}