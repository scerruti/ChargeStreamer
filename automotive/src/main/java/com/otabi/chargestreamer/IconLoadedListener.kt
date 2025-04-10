package com.otabi.chargestreamer

import android.graphics.Bitmap

interface IconLoadedListener {
    fun onIconLoaded(channelName: String, icon: Bitmap)
    fun onIconFailed(channelName: String)
}