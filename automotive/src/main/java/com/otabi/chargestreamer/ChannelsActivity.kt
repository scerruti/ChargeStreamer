package com.otabi.chargestreamer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.datatransport.BuildConfig
import com.otabi.chargestreamer.databinding.ActivityChannelsBinding

class ChannelsActivity : AppCompatActivity(), IconLoadedListener {
	@Suppress("PrivatePropertyName")
	private val TAG = this::class.java.simpleName


    private lateinit var binding: ActivityChannelsBinding
    private lateinit var channelsManager: ChannelsManager
    private lateinit var channelsAdapter: ChannelsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            // Look for unclosed resources
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog() // Logs the issue
                    .penaltyDeath() // Crashes the app to highlight the issue (optional)
                    .build()
            )
        }

        // Use GridLayoutManager for 6 columns
        binding.channelsRecyclerView.layoutManager = GridLayoutManager(this, 6)

        // Add ItemDecoration for consistent spacing
        binding.channelsRecyclerView.addItemDecoration(SpacingItemDecoration(16))

        // Initialize ChannelsManager
        channelsManager = ChannelsManager(cacheDir, assets)

        // Load channels and provide the listener
        val channels = channelsManager.loadChannels(this)

        // Initialize the adapter with the channels, an empty icon map, and the click listener
        channelsAdapter = ChannelsAdapter(channels.values.toList(), mutableMapOf()) { channel ->
            // Handle channel click here
            onChannelClicked(channel)
        }
        binding.channelsRecyclerView.adapter = channelsAdapter
    }

    override fun onIconLoaded(channelName: String, icon: Bitmap) {
        runOnUiThread {
            // Update the channel's icon in the adapter
            channelsAdapter.updateIcon(channelName, icon)
            Log.d(TAG, "Icon loaded for $channelName")

        }
    }

    override fun onIconFailed(channelName: String) {
        runOnUiThread {
            // Optionally, handle the failure (e.g., display a default icon)
            channelsAdapter.notifyItemChanged(channelsAdapter.getChannelIndex(channelName))
            Log.w(TAG, "Failed to load icon for $channelName")

        }
    }

    private fun onChannelClicked(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("CHANNEL_NAME", channel.name)
        intent.putExtra("CHANNEL_URL", channel.url)
        startActivity(intent)
    }
}

class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(spacing, spacing, spacing, spacing)
    }
}