package com.otabi.chargestreamer

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var mediaSessionHandler: MediaSessionHandler  // Add MediaSessionHandler reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Reference the existing WebView from the layout
        webView = findViewById(R.id.webview)

        // Initialize the MediaSessionHandler
        mediaSessionHandler = MediaSessionHandler(this, webView)

        // Configure WebView settings
        val webSettings: WebSettings = webView.settings
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the WebViewClient to keep navigation within the WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false // Let WebView handle the URL
            }
        }

        // Set the WebChromeClient to manage media playback and other browser-like features
        webView.webChromeClient = object : WebChromeClient() {
            // Handle media events or additional browser features if needed
        }

//        WebView.setWebContentsDebuggingEnabled(true)
        webView.loadUrl("https://www.youtube.com")

        // Optional: Fetch dynamic configurations
        fetchMediaConfigs()
    }

    private fun fetchMediaConfigs() {
        // Fetch dynamic configurations asynchronously
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // Fetch dynamic configurations asynchronously
                mediaSessionHandler.fetchDynamicConfig()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionHandler.cleanup()  // Ensure MediaSession is released
        webView.destroy()  // Clean up WebView resources
    }
}
