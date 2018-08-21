package com.example.android.uamp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.android.uamp.media.IPlayer
import com.example.android.uamp.media.Player
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.File

class Main2Activity : AppCompatActivity() {
    private val log: TextView by lazy { findViewById<TextView>(R.id.logs) }
    private lateinit var counter: CountDownTimer
    private val player by lazy {
        Player.init(application)
        Player.instance
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        player.liveDataPlayerState.observe(this, Observer {
            log.appendLine("State", it)
        })
        player.liveDataPlayNow.observe(this, Observer {
            log.appendLine("PlayNow", it)
        })
        player.liveDataPlayList.observe(this, Observer {
            log.appendLine("PlayList", it.joinToString(separator = "\n\n", prefix = "\n"))
        })
        counter = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
                log.appendLine(
                    "progress",
                    "${player.currentPosition}/${player.trackDuration}"
                )
            }

        }
        counter.start()
        if (player.playList?.isNotEmpty() == true) {

        } else {
            AsyncTask.THREAD_POOL_EXECUTOR.execute {
                val files = arrayOf(
                    "track1.mp3", "art144.jpg",
                    "track2.mp3", "art144.jpg"
                )
                    .map { fileName ->
                        val file = File(cacheDir, fileName)
                        if (!file.exists()) {
                            assets.open(fileName).copyTo(file.outputStream())
                        }
                        Uri.fromFile(file)

                    }.chunked(2)
                    .toMutableList()
                    .also {
                        it.add(it[1].toMutableList().also {
                            it[0] =
                                    Uri.parse("https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3")
                        })
                    }
                player.playList = files.mapIndexed { index, pair ->
                    val uri1 = pair[0]
                    val uri2 = pair[1]
                    IPlayer.Item(
                        "track$index",
                        uri1.toString(),
                        "Title$index",
                        "Artist$index",
                        "Album$index",
                        uri2.toString(),
                        BitmapFactory.decodeFile(uri2.path)
                    )
                }
                Log.v("rom", "player.playList: ${player.playList}")
            }
        }
        toggle.setOnClickListener { player.togglePlayPause() }
        prev.setOnClickListener { player.prev() }
        next.setOnClickListener { player.next() }
        stop.setOnClickListener { player.stop() }
        revert.setOnClickListener { player.playList = player.playList?.reversed() }
    }

    override fun onDestroy() {
        counter.cancel()
        super.onDestroy()
    }

    private fun TextView.appendLine(key: String? = null, value: Any? = null) {
        val runnable = Runnable {
            val line = (key?.plus(": ") ?: "").plus(value.toString())
            append("\n$line")
            scrollView.scrollBy(0, 100)
        }
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            runnable.run()
        } else {
            post(runnable)
        }
    }
}


