package com.onshortconfig.smartconfig.core

interface HMRestParseException {
    fun getErrorMessage(responseBody: String): String?
}