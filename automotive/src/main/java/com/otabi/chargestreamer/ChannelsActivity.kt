package com.otabi.chargestreamer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChannelsActivity : AppCompatActivity() {
    private lateinit var channelsManager: ChannelsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        // Initialize ChannelsManager
        channelsManager = ChannelsManager(applicationContext.cacheDir, assets)

        // Start the loading process
        lifecycleScope.launch {
            val channelsMap = loadChannelsAndIcons()
        }

        splashScreen.setKeepOnScreenCondition { false }

        setContentView(R.layout.activity_channels)

        // Initialize ChannelsManager

        // Load channels from the manager
        val channels = channelsManager.loadChannels().values.toList()

        val recyclerView = findViewById<RecyclerView>(R.id.channelsRecyclerView)
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("url", channel.url)
            startActivity(intent)
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