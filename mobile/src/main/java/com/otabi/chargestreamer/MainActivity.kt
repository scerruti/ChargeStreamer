package com.otabi.chargestreamer

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Reference the existing WebView from the layout
        val webView: WebView = findViewById(R.id.webview)

        // Configure WebView settings
        val webSettings: WebSettings = webView.settings
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

        WebView.setWebContentsDebuggingEnabled(true)
        webView.loadUrl("https://www.youtube.com")
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()  // Clean up resources
    }
}