package com.maxivpn.app

data class VpnConfig(
    val name: String,
    val uri: String,
    val xrayJson: String? = null
)
