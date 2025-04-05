package com.otabi.chargestreamer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChannelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        val channels = listOf(
            Channel("YouTube", "https://www.youtube.com?theme=dark", "https://www.youtube.com/favicon.ico"),
            Channel("YouTube TV", "https://tv.youtube.com", "https://tv.youtube.com/favicon.ico"),
            Channel("Vimeo", "https://www.vimeo.com", "https://www.vimeo.com/favicon.ico"),
            Channel("Dailymotion", "https://www.dailymotion.com", "https://www.dailymotion.com/favicon.ico"),
            Channel("Twitch", "https://www.twitch.tv", "https://www.twitch.tv/favicon.ico")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.channelsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("url", channel.url)
            startActivity(intent)
        }
    }
}