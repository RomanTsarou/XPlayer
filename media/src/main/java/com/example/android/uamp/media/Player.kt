package com.example.android.uamp.media

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
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

class Player private constructor(private val app: Application) : IPlayer {
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback()
    private val mediaBrowser = MediaBrowserCompat(
        application,
        ComponentName(app, MusicService::class.java),
        mediaBrowserConnectionCallback, null
    )
        .apply { connect() }
    private var mediaController: MediaControllerCompat? = null
    private val _liveDataPlayerState = MutableLiveData<State>().apply { postValue(State.PREPARING) }
    override val liveDataPlayerState: LiveData<State> get() = _liveDataPlayerState
    //    internal val mediaSource = MusicSource0()
    private val state: State get() = _liveDataPlayerState.value ?: State.PREPARING
    private val _liveDataPlayNow = MutableLiveData<IPlayer.Item>()
    override val liveDataPlayNow: LiveData<IPlayer.Item> get() = _liveDataPlayNow
    private val _liveDataPlayList = MutableLiveData<List<IPlayer.Item>>()
    override val liveDataPlayList: LiveData<List<IPlayer.Item>> get() = _liveDataPlayList
    override val trackDuration: Long get() = mediaController?.metadata?.duration ?: -1L
    override val currentPosition: Long get() = mediaController?.playbackState?.position ?: -1L

    internal var mediaMetadataList: List<MediaMetadataCompat>? = null
    override var playList: List<IPlayer.Item>? = null
        set(value) {
            field = value
            if (value?.contains(_liveDataPlayNow.value) == true) {
            } else {
                _liveDataPlayNow.postValue(value?.getOrNull(0))
            }
            mediaMetadataList = value?.map { it.toMediaMetadata() }
            _liveDataPlayList.postValue(value)

            controls {
                val playNowId = _liveDataPlayNow.value?.id
                if (playNowId != null) {
                    val extra = if (state == State.STOP) null else Bundle().apply {
                        putBoolean("needSeekTo", true)
                    }
                    when (state) {
                        State.PLAY -> {
                            playFromMediaId(playNowId, extra)
                        }
                        else -> {
                            prepareFromMediaId(playNowId, extra)
                        }
                    }
                } else {
                    stop()
                }
            }
        }

    override fun play() {
        controls {
            if (state == State.STOP) {
                val mediaId = playList?.getOrNull(0)?.id
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
        mediaController?.also {
            body(it.transportControls)
        } ?: Log.w("Player", "mediaController not Initialized")
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


            _liveDataPlayerState.postValue(State.STOP)
//            if (playList != null) {
//                mediaBrowser.subscribe(mediaBrowser.root, SubscriptionCallback0())
//            }
        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        override fun onConnectionSuspended() {
            _liveDataPlayerState.postValue(State.ERROR)
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        override fun onConnectionFailed() {
            _liveDataPlayerState.postValue(State.ERROR)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Log.i("rom", "onPlaybackStateChanged: ${state.position}")
            val s = when (state.state) {
                PlaybackStateCompat.STATE_ERROR -> State.ERROR
                PlaybackStateCompat.STATE_PAUSED -> State.PAUSE
                PlaybackStateCompat.STATE_PLAYING -> State.PLAY
                PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> State.STOP
                else -> null
            }
            s?.takeIf { it != _liveDataPlayerState.value }?.also {
                _liveDataPlayerState.postValue(it)
            }

//            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.i("rom", "onMetadataChanged: ${metadata?.let {
                mapOf(
                    "id" to it.id,
                    "mediaId" to it.description.mediaId,
                    "year" to it.year,
                    "date" to it.date,
                    "albumArtist" to it.albumArtist,
                    "album" to it.album,
                    "author" to it.author,
                    "compilation" to it.compilation,
                    "composer" to it.composer,
                    "displayDescription" to it.displayDescription,
                    "displaySubtitle" to it.displaySubtitle,
                    "genre" to it.genre,
                    "writer" to it.writer,
                    "duration" to it.duration,
                    "fullDescription" to it.fullDescription
                )
            }}")
            val item = _liveDataPlayList.value?.find { it.id == metadata?.description?.mediaId }
            if (item != null && item != _liveDataPlayNow.value) {
                _liveDataPlayNow.postValue(item)
            }
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
        private lateinit var application: Application
        fun init(app: Application) {
            application = app
        }

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Player(application) }
    }

    internal val mediaItems
        get() = mediaMetadataList?.map {
            MediaBrowserCompat.MediaItem(
                it.description,
                it.flag
            )
        }
}

private fun IPlayer.Item.toMediaMetadata(
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
//        it.trackNumber = trackNumber
//        it.trackCount = totalTrackCount
        it.flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

        // To make things easier for *displaying* these, set the display properties as well.
        it.displayTitle = title
        it.displaySubtitle = artist
        it.displayDescription = album
        it.displayIconUri = imageUri

        it.albumArt = albumArt

        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
        // MediaMetadataCompat object. This is needed to send accurate metadata to the
        // media session during updates.
        it.downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
    }.build()
}
