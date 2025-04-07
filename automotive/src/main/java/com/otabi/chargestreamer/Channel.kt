package com.otabi.chargestreamer

import android.graphics.Bitmap

data class Channel(
    val name: String,
    val url: String,
    val iconUrl: String,
    var iconBitmap: Bitmap? = null // New property for the fetched icon
)
