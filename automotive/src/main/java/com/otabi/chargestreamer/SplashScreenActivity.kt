package com.otabi.chargestreamer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var channelsManager: ChannelsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ChargeStreamer_SplashScreen)
        setContentView(R.layout.activity_splash_screen)

        // Initialize ChannelsManager
        channelsManager = ChannelsManager(applicationContext.cacheDir, assets)

        // Start the loading process
        lifecycleScope.launch {
            val channelsMap = loadChannelsAndIcons()
            moveToPlayerActivity(channelsMap)
        }
    }

    private suspend fun loadChannelsAndIcons(): LinkedHashMap<String, Channel> {
        return withContext(Dispatchers.IO) {
            channelsManager.loadChannels()
        }
    }

    private fun moveToPlayerActivity(channelsMap: LinkedHashMap<String, Channel>) {
        // Pass channelsMap to the MainActivity via Intent or other mechanisms
        val intent = Intent(this, ChannelsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
