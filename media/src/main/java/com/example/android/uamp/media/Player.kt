package com.example.android.uamp.media

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import com.example.android.uamp.media.IPlayer.State
import com.example.android.uamp.media.extensions.*
import com.example.android.uamp.media.library.AbstractMusicSource
import com.example.android.uamp.media.library.STATE_INITIALIZED
import com.example.android.uamp.media.library.STATE_INITIALIZING

class Player private constructor(val app: Application) : IPlayer {
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback()
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(app, MusicService::class.java),
        mediaBrowserConnectionCallback, null
    )
        .apply { connect() }
    private lateinit var mediaController: MediaControllerCompat
    private val _stateLiveData = MutableLiveData<State>().apply { postValue(State.PREPARING) }
    val stateLiveData: LiveData<State> get() = _stateLiveData
    private var _playList: List<Item>? = null
    internal val mediaSource = MusicSource0()
    override val state: State get() = _stateLiveData.value ?: State.PREPARING

    override val currentMedia: MediaMetadataCompat?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override var playList: List<Item>?
        get() = _playList
        set(value) {
            _playList = value
            mediaSource.catalog = (value ?: emptyList()).mapIndexed { index, item ->
                item.toMediaMetadata(index.toLong(), value!!.size.toLong())
            }
        }

    override fun play() {
        controls {
            if (state == State.STOP) {
                val mediaId = _playList?.getOrNull(0)?.id
                if (mediaId != null) {
                    start(mediaId)
                } else {
                    Log.w("Player", "Empty playlist")
                }
            } else {
                play()
            }
        }
    }

    override fun pause() {
        controls {
            pause()
        }
    }

    override fun start(mediaId: String) {
        controls {
            playFromMediaId(mediaId, null)
        }
    }

    override fun stop() {
        controls {
            stop()
        }
    }

    override fun next() {
        controls {
            skipToNext()
        }
    }

    override fun prev() {
        controls {
            skipToPrevious()
        }
    }

    override fun togglePlayPause() {
        when (state) {
            State.PLAY -> pause()
            State.PAUSE, State.STOP -> play()
        }
    }

    override fun seekTo(millis: Long) {
        controls {
            seekTo(millis)
        }
    }

    private fun controls(body: MediaControllerCompat.TransportControls.() -> Unit) {
        if (::mediaController.isInitialized) {
            body(mediaController.transportControls)
        } else {
            Log.w("Player", "mediaController not Initialized")
        }
    }

    private inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully
         * completed.
         */
        override fun onConnected() {
            // Get a MediaController for the MediaSession.
            mediaController = MediaControllerCompat(app, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            _stateLiveData.postValue(State.STOP)
        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        override fun onConnectionSuspended() {
            _stateLiveData.postValue(State.ERROR)
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        override fun onConnectionFailed() {
            _stateLiveData.postValue(State.ERROR)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Log.i("rom", "onPlaybackStateChanged: $state")
            val s = when (state.state) {
                PlaybackStateCompat.STATE_ERROR -> State.ERROR
                PlaybackStateCompat.STATE_PAUSED -> State.PAUSE
                PlaybackStateCompat.STATE_PLAYING -> State.PLAY
                PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> State.STOP
                else -> null
            }
            s?.takeIf { it != _stateLiveData.value }?.also {
                _stateLiveData.postValue(it)
            }

//            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.i("rom", "onMetadataChanged: ${metadata?.duration}")
//            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {

            Log.i("rom", "onQueueChanged: $queue")
        }

        /**
         * Normally if a [MediaBrowserServiceCompat] drops its connection the callback comes via
         * [MediaControllerCompat.Callback] (here). But since other connection status events
         * are sent to [MediaBrowserCompat.ConnectionCallback], we catch the disconnect here and
         * send it on to the other callback.
         */
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    companion object {
        private lateinit var context: Application
        fun init(app: Application) {
//            if (!::context.isInitialized) {
            context = app
//            }
        }

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Player(context) }
    }

    internal inner class MusicSource0 : AbstractMusicSource() {
        internal var catalog: List<MediaMetadataCompat> = emptyList()
            set(value) {
                field = value
                state = STATE_INITIALIZED
            }

        init {
            state = STATE_INITIALIZING
        }
//
//            UpdateCatalogTask(Glide.with(context)) { mediaItems ->
//                catalog = mediaItems
//                state = STATE_INITIALIZED
//            }.execute(source)
//        }

        override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()
    }

    class Item(
        val id: String,
        val title: String,
        val artist: String,
        val mediaUri: String,
        val imageUri: String,
        val album: String,
        val image: Bitmap?
    )
}

private fun Player.Item.toMediaMetadata(
    trackNumber: Long,
    totalTrackCount: Long
): MediaMetadataCompat {
    return MediaMetadataCompat.Builder().also {
        it.id = id
        it.title = title
        it.artist = artist
        it.album = album
//        it.duration = durationMs
//        it.genre = jsonMusic.genre
        it.mediaUri = mediaUri
        it.albumArtUri = imageUri
        it.trackNumber = trackNumber
        it.trackCount = totalTrackCount
        it.flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

        // To make things easier for *displaying* these, set the display properties as well.
        it.displayTitle = title
        it.displaySubtitle = artist
        it.displayDescription = album
        it.displayIconUri = imageUri

        it.albumArt = image

        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
        // MediaMetadataCompat object. This is needed to send accurate metadata to the
        // media session during updates.
        it.downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
    }.build()
}
