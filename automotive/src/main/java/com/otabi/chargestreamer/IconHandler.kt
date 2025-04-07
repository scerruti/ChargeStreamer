package com.otabi.chargestreamer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class IconHandler(private val cacheDir: File) {
    // Main function: Fetch and cache the icon dynamically
    suspend fun getIcon(channelName: String, siteUrl: String): Bitmap? {
        // Check cache first
        val cachedIcon = getCachedIcon(channelName)
        if (cachedIcon != null) return cachedIcon

        // Dynamically fetch the icon URL
        val iconUrl = fetchIconUrl(URL(siteUrl).let { "${it.protocol}://${it.host}${it.path}" })
            ?: return null

        // Fetch and cache the icon
        return fetchAndCacheIcon(channelName, iconUrl)
    }

    // Fetch the best icon URL dynamically
    private fun fetchIconUrl(siteUrl: String): String? {
        try {
            val html = downloadHtml(siteUrl)
            val document: Document = Jsoup.parse(html)

            // Focus on the <head> section
            val head = document.head()

            // Step 1: Check for apple-touch-icon
            head.select("link[rel=apple-touch-icon], link[rel=apple-touch-icon-precomposed]").first()?.let {
                return buildAbsoluteUrl(siteUrl, it.attr("href"))
            }

            // Step 2: Check for <link rel="icon"> or <link rel="shortcut icon">
            head.select("link[rel=icon], link[rel=shortcut icon]")
                .filter { element -> element.hasAttr("sizes") } // Explicit lambda name for clarity
                .maxByOrNull { element ->
                    val sizeParts = element.attr("sizes").split("x").mapNotNull { it.toIntOrNull() }
                    if (sizeParts.size == 2) sizeParts[0] * sizeParts[1] else 0 // Calculate pixel area
                }?.let { largestElement ->
                    return buildAbsoluteUrl(siteUrl, largestElement.attr("href"))
                }

            // Step 3: Check manifest.json for high-res icons
            val manifestUrl = buildAbsoluteUrl(siteUrl, "/manifest.json")
            val manifestContent = downloadManifest(manifestUrl)
            manifestContent?.let {
                val gson = com.google.gson.Gson()
                val manifest = gson.fromJson(it, Manifest::class.java)
                manifest.icons?.filter { icon -> icon.sizes != null }
                    ?.maxByOrNull { icon ->
                        val sizes = icon.sizes!!.split("x").mapNotNull { size -> size.toIntOrNull() }
                        if (sizes.size == 2) sizes[0] * sizes[1] else 0 // Calculate pixel area
                    }?.let { largestIcon ->
                        return buildAbsoluteUrl(siteUrl, largestIcon.src)
                    }
            }

            // Step 4: Fallback to favicon.ico
            return "$siteUrl/favicon.ico"
        } catch (e: Exception) {
            Log.e("com.otabi.chargestreamer.IconHandler", "Failed to fetch icon URL", e)
            return null
        }
    }

    // Check for a cached icon
    private fun getCachedIcon(channelName: String): Bitmap? {
        val cacheFile = File(cacheDir, "${channelName}_icon.png")
        return if (cacheFile.exists()) {
            BitmapFactory.decodeFile(cacheFile.absolutePath)
        } else {
            null
        }
    }

    // Fetch the icon and cache it
    private suspend fun fetchAndCacheIcon(channelName: String, iconUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(iconUrl)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                cacheIcon(channelName, bitmap)
                bitmap
            } catch (e: Exception) {
                Log.e(
                    "com.otabi.chargestreamer.IconHandler",
                    "Failed to fetch and cache icon: $iconUrl",
                    e
                )
                null
            }
        }
    }

    // Save the icon to cache
    private fun cacheIcon(channelName: String, bitmap: Bitmap) {
        val cacheFile = File(cacheDir, "${channelName}_icon.png")
        cacheFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }

    // Download HTML content
    private fun downloadHtml(url: String): String {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string() ?: ""
                } else {
                    Log.e(
                        "com.otabi.chargestreamer.IconHandler",
                        "Failed to fetch HTML: ${response.code}"
                    )
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e("com.otabi.chargestreamer.IconHandler", "Error while downloading HTML", e)
            ""
        }
    }

    // Download manifest.json content
    private fun downloadManifest(url: String): String? {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.e(
                        "com.otabi.chargestreamer.IconHandler",
                        "Failed to fetch manifest: ${response.code}"
                    )
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("com.otabi.chargestreamer.IconHandler", "Error while downloading manifest", e)
            null
        }
    }

    // Helper to build absolute URLs from relative paths
    private fun buildAbsoluteUrl(baseUrl: String, relativeUrl: String): String {
        return if (relativeUrl.startsWith("http")) {
            relativeUrl // Already absolute
        } else {
            URL(URL(baseUrl), relativeUrl).toString() // Combine base URL and relative path
        }
    }
}

data class Manifest(
    val icons: List<Icon>? = null
)

data class Icon(
    val src: String,
    val sizes: String? = null
)
