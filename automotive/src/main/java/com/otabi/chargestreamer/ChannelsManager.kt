package com.otabi.chargestreamer

import android.content.res.AssetManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

class ChannelsManager(cacheDir: File, assetManager: AssetManager) {
    private val fileName = "channels.properties"
    private val propertiesFile = File(cacheDir, "assets$fileName")
    private val iconHandler = IconHandler(cacheDir)
    private val channelsManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val iconListeners: MutableMap<String, IconLoadedListener> = ConcurrentHashMap()

    companion object {
        var isFileInitialized = false
    }

    init {
        // Only copy the file once
        if (!isFileInitialized && !propertiesFile.exists()) {
            // Copy asset to properties file
            assetManager.open(fileName).use { inputStream ->
                propertiesFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Load properties from the file
            val properties = Properties().apply {
                propertiesFile.inputStream().use { load(it) }
            }

            // Copy icon assets to cache
            iconHandler.copyIconAssets(cacheDir, assetManager, properties)

            // Mark file initialization as complete
            isFileInitialized = true
        }
    }

    // Load channels into a LinkedHashMap to preserve order
    fun loadChannels(iconLoadedListener: IconLoadedListener? = null): LinkedHashMap<String, Channel> {
        val channelsMap: LinkedHashMap<String, Channel>

        if (propertiesFile.exists()) {
            val properties = Properties().apply {
                propertiesFile.inputStream().use { load(it) }
            }

            // Parse the properties and maintain numerical order
            val channelsList = mutableListOf<Pair<Int, Channel>>()

            for ((key, value) in properties) {
                // Extract numerical prefix and clean the name
                val prefix = (key as String).substringBefore("_").toIntOrNull() ?: Int.MAX_VALUE
                val name = key.replace(Regex("^\\d+_"), "").replace("_", " ")

                // Use the value directly as the site URL
                val url = value as String

                // Add to the channels list
                channelsList.add(Pair(prefix, Channel(name, url, iconUrl = null.toString())))
            }


            // Sort by numerical prefix and convert to LinkedHashMap
            channelsMap = channelsList
                .sortedBy { it.first }
                .associateTo(LinkedHashMap()) { it.second.name to it.second }

            // Parallelize icon fetching
            channelsMap.forEach { (name, channel) ->
                iconLoadedListener?.let {
                    iconListeners[name] = it
                }
                channelsManagerScope.launch {
                    try {
                        val icon = iconHandler.getIcon(name, channel.url)
                        if (icon != null) {
                            channel.iconBitmap = icon
                            iconListeners[name]?.onIconLoaded(name, icon)
                        } else {
                            iconListeners[name]?.onIconFailed(name)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "com.otabi.chargestreamer.ChannelsManager",
                            "Failed to fetch icon for $name",
                            e
                        )
                    }
                }
            }
        } else {
            channelsMap = LinkedHashMap()
        }


        return channelsMap
    }

    // Save channels back to the file
    private fun saveChannels(channels: LinkedHashMap<String, Channel>) {
        val properties = Properties()
        channels.forEach { (name, channel) ->
            val key = name.replace(" ", "_") // Replace spaces with underscores
            properties[key] = "${channel.iconUrl},${channel.iconUrl}"
        }
        properties.store(propertiesFile.outputStream(), "Channels Configuration")
    }

    // Add a channel
    @Suppress("UNUSED")
    fun addChannel(channel: Channel) {
        val channels = loadChannels()
        channels[channel.name] = channel
        saveChannels(channels)
    }

    // Delete a channel by name
    @Suppress("UNUSED")
    fun deleteChannel(channelName: String) {
        val channels = loadChannels()
        channels.remove(channelName)
        saveChannels(channels)
    }
}