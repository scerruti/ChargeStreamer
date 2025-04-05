package com.otabi.chargestreamer

import android.annotation.SuppressLint
import android.os.Bundle
import android.graphics.Color
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var mediaSessionHandler: MediaSessionHandler  // Add MediaSessionHandler reference
    private var customViewContainer: FrameLayout? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Reference the existing WebView from the layout
        webView = findViewById(R.id.webview)

        webView.settings.apply {
            loadWithOverviewMode = true
            useWideViewPort = true // Ensure content scales to fit
        }

        // Set up WebView with a custom WebChromeClient
        webView.webChromeClient = object : WebChromeClient() {

        }

        // Initialize the MediaSessionHandler
        mediaSessionHandler = MediaSessionHandler(this, webView)

        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true

            setSupportZoom(true)   // Enable zoom functionality
            builtInZoomControls = true // Add zoom controls
            displayZoomControls = false // Hide the native zoom controls UI
        }

        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val combinedInsets = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.ime() or
                        WindowInsetsCompat.Type.captionBar() or
                        WindowInsetsCompat.Type.systemGestures())
            Log.d("InsetsDebug", "Insets -> Left: ${combinedInsets.left}, Top: ${combinedInsets.top}, Right: ${combinedInsets.right}, Bottom: ${combinedInsets.bottom}")
            v.setPadding(
                combinedInsets.left,
                combinedInsets.top,
                maxOf(50, combinedInsets.right),
                combinedInsets.bottom
            )
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
            // Handle media events or additional browser features
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                customViewCallback = callback

                // Create a FrameLayout to hold the custom view
                customViewContainer = FrameLayout(this@MainActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.BLACK) // Set a black background
                    addView(view) // Add the video view
                }

                // Replace the activity's content view with the custom view
                setContentView(customViewContainer)
            }

            override fun onHideCustomView() {
                // Exit full-screen mode and restore the original layout
                customViewContainer?.removeAllViews()
                customViewContainer = null
                customViewCallback = null
                setContentView(R.layout.activity_main)
            }
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

