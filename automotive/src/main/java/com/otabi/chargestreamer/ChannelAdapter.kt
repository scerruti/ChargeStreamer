package com.otabi.chargestreamer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChannelsAdapter(
    private var channels: List<Channel>,
    private val icons: MutableMap<String, Bitmap>,
    private val onChannelClick: (Channel) -> Unit // Click listener
) : RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder>() {

    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.channelName)
        val iconImageView: ImageView = itemView.findViewById(R.id.channelIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel, parent, false) // Replace with your item layout
        return ChannelViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.nameTextView.text = channel.name

        // Use the dynamically loaded icon if available, otherwise use the default icon
        val icon = icons[channel.name] ?: channel.iconBitmap // channel.iconBitmap will be the default
        if (icon != null) {
            holder.iconImageView.setImageBitmap(icon)
        } else {
            // Handle no default or missing icon
            holder.iconImageView.setImageResource(R.drawable.missing_icon)
        }

        // Set the click listener on the item view
        holder.itemView.setOnClickListener {
            onChannelClick(channel) // Call the provided listener
        }
    }

    override fun getItemCount() = channels.size

    fun updateIcon(channelName: String, icon: Bitmap) {
        val channelIndex = getChannelIndex(channelName)
        if (channelIndex != -1) {
            icons[channelName] = icon
            notifyItemChanged(channelIndex)
        }
    }

    fun getChannelIndex(channelName: String): Int {
        return channels.indexOfFirst { it.name == channelName }
    }
}