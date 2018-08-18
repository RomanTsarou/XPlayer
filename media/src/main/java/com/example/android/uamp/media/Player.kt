package com.example.android.uamp.media

import android.app.Application
import android.support.v4.media.MediaMetadataCompat
import com.example.android.uamp.media.IPlayer.State

class Player private constructor(val app: Application) : IPlayer {


    private var _state = State.PREPARING
    private var _playList: List<MediaMetadataCompat>? = null
    override val state: State get() = _state

    override val currentMedia: MediaMetadataCompat?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override var playList: List<MediaMetadataCompat>?
        get() = _playList
        set(value) {
            _playList = value
        }

    override fun play() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start(mediaId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun next() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prev() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tooglePlayPause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seekTo(millis: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private lateinit var context: Application
        fun init(app: Application) {
            if (!::context.isInitialized) {
                context = app
            }
        }

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Player(context) }
    }
}