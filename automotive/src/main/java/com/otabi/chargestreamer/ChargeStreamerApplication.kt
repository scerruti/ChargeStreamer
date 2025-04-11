package com.otabi.chargestreamer

import android.app.Application
import kotlinx.coroutines.*
import org.json.JSONObject

class ChargeStreamerApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var mediaConfig: Map<String, MediaControlConfig>

    override fun onCreate() {
        super.onCreate()

        // Use the application scope for background tasks
        applicationScope.launch {
            mediaConfig = loadMediaConfig()
        }
    }

    private suspend fun loadMediaConfig(): Map<String, MediaControlConfig> {
        val configMap = mutableMapOf<String, MediaControlConfig>()

        withContext(Dispatchers.IO) {
            val inputStream = assets.open("media_config.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            jsonObject.keys().forEach { key ->
                    val siteConfig = jsonObject.getJSONObject(key)
                configMap[key] = MediaControlConfig(
                        playCommand = siteConfig.optString("playCommand"),
                        pauseCommand = siteConfig.optString("pauseCommand"),
                        nextVideoCommand = siteConfig.optString("nextVideoCommand"),
                        prevVideoCommand = siteConfig.optString("prevVideoCommand"),
                        forwardCommand = siteConfig.optString("forwardCommand"),
                        reverseCommand = siteConfig.optString("reverseCommand")
                )
            }
        }

        return configMap
    }
}

