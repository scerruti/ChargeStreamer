package com.otabi.chargestreamer

data class MediaControlConfig(
    val playCommand: String? = null,
    val pauseCommand: String? = null,
    val nextVideoCommand: String? = null,
    val prevVideoCommand: String? = null,
    val forwardCommand: String? = null,
    val reverseCommand: String? = null
)
