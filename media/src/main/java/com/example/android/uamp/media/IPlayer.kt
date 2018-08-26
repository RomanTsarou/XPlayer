package com.example.android.uamp.media

import android.graphics.Bitmap
import androidx.lifecycle.LiveData

interface IPlayer {
    enum class State { PLAY, PAUSE, STOP, PREPARING, ERROR }

    val liveDataPlayerState: LiveData<State>
    val liveDataPlayNow: LiveData<Track>
    val liveDataPlayList: LiveData<List<Track>>
    val trackDuration: Long
    val currentPosition: Long
    var playList: List<Track>?
    fun play()
    fun start(mediaId: String)
    fun pause()
    fun stop()
    fun next()
    fun prev()
    fun togglePlayPause()
    fun seekTo(millis: Long)

    data class Track @JvmOverloads constructor(
        val id: String,
        val mediaUri: String,
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val imageUri: String? = null,
        val albumArt: Bitmap? = null
    )
}