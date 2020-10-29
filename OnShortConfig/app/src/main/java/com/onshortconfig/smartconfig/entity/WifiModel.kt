package com.onshortconfig.smartconfig.entity

class WifiModel(ssid: String, capabilities: String) {
    var ssid: String = ""
    var capabilities: String = ""

    init {
        this.ssid = ssid
        this.capabilities = capabilities
    }
}