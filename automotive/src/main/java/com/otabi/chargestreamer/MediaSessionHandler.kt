package com.otabi.chargestreamer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

class MediaSessionHandler(
    context: Context,
    private val webView: WebView
) {
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "MediaSessionHandler")

    // Hardcoded fallback configurations
    private val hardcodedConfig = mapOf(
        "m.youtube.com" to MediaControlConfig(
            playCommand = "document.querySelector('video').play()",
            pauseCommand = "document.querySelector('video').pause()",
            nextVideoCommand = "document.querySelector('button[aria-label=\"Next video\"]')?.click()",
            prevVideoCommand = "document.querySelector('button[aria-label=\"Previous video\"]')?.click()",
            forwardCommand = "document.querySelector('button[aria-label=\"Fast forward 10 seconds\"]')?.click()",
            reverseCommand = "document.querySelector('button[aria-label=\"Rewind 10 seconds\"]')?.click()",
        )
    )

    private var cachedConfig: Map<String, MediaControlConfig> = emptyMap()

    init {
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
                Log.d("MediaSessionHandler", "Media button event received.")
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonIntent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION") // Suppresses the warning for deprecated API
                    mediaButtonIntent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                if (keyEvent != null) {
                    Log.d("MediaSessionHandler", "KeyEvent detected: ${keyEvent.keyCode}")
                }
                return super.onMediaButtonEvent(mediaButtonIntent)
            }

            override fun onPlay() {
                Log.d("MediaSessionHandler", "Play button pressed")
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                executeCommand(MediaAction.PLAY)
            }

            override fun onPause() {
                Log.d("MediaSessionHandler", "Pause button pressed")
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
        Log.d("MediaSessionHandler", "MediaSession is active and listening for media keys")

    }

    // The updatePlaybackState helper function
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
        Log.d("PlaybackState", "Playback state updated to: $state")
    }

    private fun executeCommand(action: MediaAction) {
        Log.d("MediaControl", "MediaAction: ${action.name}")

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

        Log.d("MediaControl", "Command to be executed: $command")

        command?.let {
            webView.evaluateJavascript(it) { result ->
                Log.d("MediaControl", "Play command result: $result")
            }
//            webView.evaluateJavascript(it, null)
        }
    }

    private fun getMediaConfigForUrl(url: String): MediaControlConfig {

        val serverUrl = URI(url).host
        return cachedConfig[serverUrl] ?: hardcodedConfig[serverUrl] ?: MediaControlConfig()
    }

    suspend fun fetchDynamicConfig() {
        // Simulate a web call to fetch dynamic configurations (e.g., from an API)
        val dynamicConfig = withContext(Dispatchers.IO) {
            // Replace with your actual web call logic
            mapOf(
                "vimeo.com" to MediaControlConfig(
                    playCommand = "document.querySelector('video').play()",
                    pauseCommand = "document.querySelector('video').pause()",
                    forwardCommand = "document.querySelector('video').currentTime += 10",
                    reverseCommand = "document.querySelector('video').currentTime -= 10"
                )
            )
        }

        cachedConfig = dynamicConfig
    }

    fun cleanup() {
        mediaSession.release()
    }
}
