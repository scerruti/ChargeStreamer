package com.otabi.chargestreamer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebView
import org.json.JSONObject
import java.net.URI

class MediaSessionHandler(context: Context, private val webView: WebView) {
    companion object {
        private val TAG: String = MediaSessionHandler::class.java.simpleName
    }

    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "MediaSessionHandler")
    private var cachedConfig: Map<String, MediaControlConfig> = emptyMap() // Config loaded from JSON

    init {
        // Initialize playback state
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
                        PlaybackStateCompat.ACTION_REWIND
            )
            .setState(
                PlaybackStateCompat.STATE_STOPPED, // Initial state: stopped
                0L,                                // Playback position: start (0 ms)
                1.0f                              // Playback speed: normal (1x)
            )
            .build()

        mediaSession.setPlaybackState(playbackState)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent?): Boolean {
                Log.d(TAG, "Media button event received.")

                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonIntent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION") // Suppresses the warning for deprecated API
                    mediaButtonIntent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                if (keyEvent != null) {
                    Log.d(TAG, "KeyEvent detected: ${keyEvent.keyCode}")
                }
                return super.onMediaButtonEvent(mediaButtonIntent)
            }

            override fun onPlay() {
                Log.d(TAG, "Play button pressed")

                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                executeCommand(MediaAction.PLAY)
            }

            override fun onPause() {
                Log.d(TAG, "Pause button pressed")

                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                executeCommand(MediaAction.PAUSE)
            }

            override fun onSkipToNext() {
                executeCommand(MediaAction.NEXT)
            }

            override fun onSkipToPrevious() {
                executeCommand(MediaAction.PREVIOUS)
            }

            override fun onFastForward() {
                executeCommand(MediaAction.FORWARD)
            }

            override fun onRewind() {
                executeCommand(MediaAction.REVERSE)
            }
        })

        mediaSession.isActive = true
        Log.d(TAG, "MediaSession is active and listening for media keys")

        // Load configurations from the JSON file
        loadMediaConfig(context)
    }

    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
                        PlaybackStateCompat.ACTION_REWIND
            )
            .setState(state, 0L, 1.0f)
            .build()

        mediaSession.setPlaybackState(playbackState)
        Log.d(TAG, "Playback state updated to: $state")
    }

    private fun executeCommand(action: MediaAction) {
        Log.d(TAG, "MediaAction: ${action.name}")

        val currentUrl = webView.url ?: return
        val config = getMediaConfigForUrl(currentUrl)
        val command = when (action) {
            MediaAction.PLAY -> config.playCommand
            MediaAction.PAUSE -> config.pauseCommand
            MediaAction.NEXT -> config.nextVideoCommand
            MediaAction.PREVIOUS -> config.prevVideoCommand
            MediaAction.FORWARD -> config.forwardCommand
            MediaAction.REVERSE -> config.reverseCommand
        }

        Log.d(TAG, "Command to be executed: $command")

        command?.let {
            webView.evaluateJavascript(it) { result ->
                Log.d(TAG, "Command execution result: $result")
            }
        }
    }

    private fun getMediaConfigForUrl(url: String): MediaControlConfig {
        val serverUrl = URI(url).host ?: return MediaControlConfig()
        return cachedConfig[serverUrl] ?: MediaControlConfig()
    }

    private fun loadMediaConfig(context: Context) {
        try {
            val inputStream = context.assets.open("media_config.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val configMap = mutableMapOf<String, MediaControlConfig>()
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
            cachedConfig = configMap
            Log.d(TAG, "Media configurations loaded: $cachedConfig")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load media configurations", e)
        }
    }

    fun cleanup() {
        mediaSession.release()
    }
}
