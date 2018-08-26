# XPlayer
forked from [googlesamples/android-UniversalMusicPlayer](https://github.com/googlesamples/android-UniversalMusicPlayer)

[![Release](https://jitpack.io/v/RomanTsarou/XPlayer.svg)](https://jitpack.io/#RomanTsarou/XPlayer)


#### Android music player library for [androidx](https://developer.android.com/topic/libraries/support-library/androidx-overview) projects, with simlpe API:
```
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
```
#### Sample use(Kotlin):
```
Player.init(context)
...
Player.playList = myPlayList
...
Player.play()
```
License
-------

Copyright 2018 Roman Tsarou

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
