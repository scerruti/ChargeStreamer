//package com.otabi.chargestreamer
//
//
//import android.app.Notification
//import android.content.Intent
//import android.os.Bundle
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.MediaDescriptionCompat
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import androidx.annotation.NonNull
//import androidx.core.app.ServiceCompat
//import androidx.core.content.ContextCompat
//import androidx.leanback.media.PlayerAdapter
//import androidx.media.MediaBrowserServiceCompat
//import com.google.android.gms.cast.framework.MediaNotificationManager
//
//class VideoService : MediaBrowserServiceCompat() {
//    private var mSession: MediaSessionCompat? = null
//    private var mPlayback: PlayerAdapter? = null
//    private var mMediaNotificationManager: MediaNotificationManager? = null
//    private var mCallback: MediaSessionCallback? = null
//    private var mServiceInStartedState = false
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Create a new MediaSession.
//        mSession = MediaSessionCompat(this, "VideoService")
//        mCallback = MediaSessionCallback()
//        mSession!!.setCallback(mCallback)
//        mSession!!.setFlags(
//            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
//                    MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
//                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//        )
//        sessionToken = mSession!!.sessionToken
//
////        mMediaNotificationManager = MediaNotificationManager(this)
//
////        mPlayback = MediaPlayerAdapter(this, MediaPlayerListener())
//        Log.d(TAG, "onCreate: VideoService creating MediaSession, and MediaNotificationManager"))

//    }
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        stopSelf()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mMediaNotificationManager.onDestroy()
////        mPlayback.stop()
//        mSession!!.release()
//        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released"))

//    }
//
//    override fun onGetRoot(
//        clientPackageName: String,
//        clientUid: Int,
//        rootHints: Bundle?
//    ): BrowserRoot? {
//        return BrowserRoot(/*VideoLibrary.getRoot()*/null.toString(), null)
//    }
//
//    override fun onLoadChildren(
//        parentId: String,
//        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
//    ) {
//        result.sendResult(/*VideoLibrary.getMediaItems()*/null)
//    }
//
//    // MediaSession Callback: Transport Controls -> MediaPlayerAdapter
//    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
//        private val mPlaylist: MutableList<MediaSessionCompat.QueueItem> = ArrayList()
//        private var mQueueIndex = -1
//        private var mPreparedMedia: MediaMetadataCompat? = null
//
//        override fun onAddQueueItem(description: MediaDescriptionCompat) {
//            mPlaylist.add(
//                MediaSessionCompat.QueueItem(
//                    description,
//                    description.hashCode().toLong()
//                )
//            )
//            mQueueIndex = if (mQueueIndex == -1) 0 else mQueueIndex
//            mSession!!.setQueue(mPlaylist)
//        }
//
//        override fun onRemoveQueueItem(description: MediaDescriptionCompat) {
//            mPlaylist.remove(
//                MediaSessionCompat.QueueItem(
//                    description,
//                    description.hashCode().toLong()
//                )
//            )
//            mQueueIndex = if (mPlaylist.isEmpty()) -1 else mQueueIndex
//            mSession!!.setQueue(mPlaylist)
//        }
//
//        override fun onPrepare() {
//            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
//                // Nothing to play.
//                return
//            }
//
//            val mediaId = mPlaylist[mQueueIndex].description.mediaId
////            mPreparedMedia = VideoLibrary.getMetadata(this@VideoService, mediaId)
//            mSession!!.setMetadata(mPreparedMedia)
//
//            if (!mSession!!.isActive) {
//                mSession!!.isActive = true
//            }
//        }
//
//        override fun onPlay() {
//            if (!isReadyToPlay) {
//                // Nothing to play.
//                return
//            }
//
//            if (mPreparedMedia == null) {
//                onPrepare()
//            }
//
////            mPlayback.playFromMedia(mPreparedMedia)
//            Log.d(TAG, "onPlayFromMediaId: MediaSession active"))

//        }
//
//        override fun onPause() {
//            mPlayback?.pause()
//        }
//
//        override fun onStop() {
////            mPlayback.stop()
//            mSession!!.isActive = false
//        }
//
//        override fun onSkipToNext() {
//            mQueueIndex = (++mQueueIndex % mPlaylist.size)
//            mPreparedMedia = null
//            onPlay()
//        }
//
//        override fun onSkipToPrevious() {
//            mQueueIndex = if (mQueueIndex > 0) mQueueIndex - 1 else mPlaylist.size - 1
//            mPreparedMedia = null
//            onPlay()
//        }
//
//        override fun onSeekTo(pos: Long) {
//            mPlayback.seekTo(pos)
//        }
//
//        private val isReadyToPlay: Boolean
//            get() = (!mPlaylist.isEmpty())
//    }
//
//    // MediaPlayerAdapter Callback: MediaPlayerAdapter state -> MusicService.
//    inner class MediaPlayerListener internal constructor() : PlaybackInfoListener() {
//        private val mServiceManager: ServiceManager
//
//        init {
//            mServiceManager = ServiceManager()
//        }
//
//        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
//            // Report the state to the MediaSession.
//            mSession!!.setPlaybackState(state)
//
//            // Manage the started state of this service.
//            when (state.state) {
//                PlaybackStateCompat.STATE_PLAYING -> mServiceManager.moveServiceToStartedState(state)
//                PlaybackStateCompat.STATE_PAUSED -> mServiceManager.updateNotificationForPause(state)
//                PlaybackStateCompat.STATE_STOPPED -> mServiceManager.moveServiceOutOfStartedState(
//                    state
//                )
//            }
//        }
//
//        internal inner class ServiceManager {
//            internal fun moveServiceToStartedState(state: PlaybackStateCompat) {
//                val notification: Notification =
//                    mMediaNotificationManager.getNotification(
//                        mPlayback.getCurrentMedia(), state, sessionToken
//                    )
//
//                if (!mServiceInStartedState) {
//                    ContextCompat.startForegroundService(
//                        this@MusicService,
//                        Intent(this@MusicService, MusicService::class.java)
//                    )
//                    mServiceInStartedState = true
//                }
//
//                ServiceCompat.startForeground(
//                    MediaNotificationManager.NOTIFICATION_ID,
//                    notification
//                )
//            }
//
//            fun updateNotificationForPause(state: PlaybackStateCompat) {
//                ServiceCompat.stopForeground(false)
//                val notification: Notification =
//                    mMediaNotificationManager.getNotification(
//                        mPlayback.getCurrentMedia(), state, sessionToken
//                    )
//                mMediaNotificationManager.getNotificationManager()
//                    .notify(MediaNotificationManager.NOTIFICATION_ID, notification)
//            }
//
//            fun moveServiceOutOfStartedState(state: PlaybackStateCompat) {
//                ServiceCompat.stopForeground(true)
//                stopSelf()
//                mServiceInStartedState = false
//            }
//        }
//    }
//
//    companion object {
//        private val TAG: String = MusicService::class.java.simpleName
//    }
//}