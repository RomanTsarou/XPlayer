package com.example.android.uamp.media

import android.support.v4.media.MediaMetadataCompat

interface IPlayer {
    enum class State { PLAY, PAUSE, STOP, PREPARING, ERROR }

    val state: State
    val currentMedia: MediaMetadataCompat?
    var playList: List<MediaMetadataCompat>?
    fun play()
    fun start(mediaId: String)
    fun pause()
    fun stop()
    fun next()
    fun prev()
    fun tooglePlayPause()
    fun seekTo(millis: Long)

}