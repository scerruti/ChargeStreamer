package com.otabi.chargestreamer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel, onChannelClick)
    }

    override fun getItemCount(): Int = channels.size

    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelIcon = itemView.findViewById<ImageView>(R.id.channelIcon)
        private val channelName = itemView.findViewById<TextView>(R.id.channelName)

        fun bind(channel: Channel, onChannelClicked: (Channel) -> Unit) {
            channelName.text = channel.name

            // Display the preloaded iconBitmap if available
            channel.iconBitmap?.let {
                channelIcon.setImageBitmap(it)
                channelIcon.contentDescription = "Icon for ${channel.name}"
            } ?: run {
                // Otherwise, use Glide to fetch and load the icon dynamically
                Glide.with(itemView.context)
                    .load(channel.iconUrl) // Using iconUrl directly
                    .placeholder(R.drawable.missing_icon) // Placeholder while loading
                    .error(R.drawable.missing_icon) // Fallback for errors
                    .into(channelIcon)

                channelIcon.contentDescription = "Loading icon for ${channel.name}"
            }

            // Attach the click listener to the item
            itemView.setOnClickListener { onChannelClicked(channel) }
        }

    }
}
