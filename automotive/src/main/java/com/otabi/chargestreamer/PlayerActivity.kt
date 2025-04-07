package com.otabi.chargestreamer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.otabi.chargestreamer.databinding.ActivityPlayerBinding

class PlayerActivity : ComponentActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var webView: WebView
    private lateinit var mediaSessionHandler: MediaSessionHandler
    private var customViewContainer: ConstraintLayout? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalContentView: View? = null
    private var isFullscreen = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
        originalContentView = binding.main

        binding.channelsButton.setOnClickListener {
            val intent = Intent(this, ChannelsActivity::class.java)
            startActivity(intent)
        }
        webView = binding.webView

        webView.settings.apply {
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true

        webView.webChromeClient = object : WebChromeClient() {}

        mediaSessionHandler = MediaSessionHandler(this, webView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                view?.setBackgroundColor(Color.BLACK) // Set background color on start.
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.setBackgroundColor(Color.TRANSPARENT) //reset background to be transparent
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customViewContainer != null) {
                    onHideCustomView()
                    return
                }
                customViewCallback = callback
                isFullscreen = true
                hideSystemBars(true)

                customViewContainer = ConstraintLayout(this@PlayerActivity).apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.BLACK)
                    addView(view, 0)
                    val constraintLayoutParams = ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    constraintLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    constraintLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    constraintLayoutParams.endToStart = binding.rightMarginAreaLayout.id
                    constraintLayoutParams.marginStart = 20

                    view?.layoutParams = constraintLayoutParams
                }
                setContentView(customViewContainer)
            }

            override fun onHideCustomView() {
                if (customViewContainer == null) {
                    return
                }
                isFullscreen = false
                hideSystemBars(false)
                customViewContainer?.removeAllViews()

                customViewContainer = null

                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                setContentView(originalContentView)
            }
        }
        val url: String? = intent.getStringExtra("url")

        webView.settings.javaScriptEnabled = true
        if (url != null) {
            webView.loadUrl(url)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionHandler.cleanup()
        webView.destroy()
    }

    private fun hideSystemBars(fullscreen: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                if (fullscreen) {
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            if (fullscreen) {
                originalContentView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            } else {
                originalContentView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }
}