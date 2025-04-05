package com.otabi.chargestreamer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide

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
//            Glide.with(itemView.context)
//                .load(channel.icon)
//                .placeholder(R.drawable.placeholder_icon)
//                .into(channelIcon)
            itemView.setOnClickListener { onChannelClicked(channel) }
        }
    }
}
