package com.example.android.uamp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.android.uamp.media.IPlayer
import com.example.android.uamp.media.Player
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.File

class Main2Activity : AppCompatActivity() {
    private val log: TextView by lazy { findViewById<TextView>(R.id.logs) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Player.init(application)
        Player.instance.liveDataPlayerState.observe(this, Observer {
            log.appendLine("State", it)
        })
        Player.instance.liveDataPlayNow.observe(this, Observer {
            log.appendLine("PlayNow", it)
        })
        object : CountDownTimer(50000, 1000) {
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
                log.appendLine(
                    "progress",
                    "${Player.instance.currentPosition}/${Player.instance.trackDuration}"
                )
            }

        }.start()
        if (Player.instance.playList?.isNotEmpty() == true) {

        } else {
            AsyncTask.THREAD_POOL_EXECUTOR.execute {
                val files = arrayOf("track1.mp3", "art144.jpg")
                    .map { fileName ->
                        val file = File(cacheDir, fileName)
                        if (!file.exists()) {
                            assets.open(fileName).copyTo(file.outputStream())
                        }
                        Uri.fromFile(file)
                    }
                Player.instance.playList = listOf(
                    IPlayer.Item(
                        "track1",
                        files[0].toString(),
                        "Title1",
                        "Artist1",
                        "Album1",
                        files[1].toString(),
                        BitmapFactory.decodeFile(files[1].path)
                    )
                )
            }
        }
        toggle.setOnClickListener { Player.instance.togglePlayPause() }
    }
}

private fun TextView.appendLine(key: String? = null, value: Any? = null) {
    val runnable = Runnable {
        val line = (key?.plus(": ") ?: "").plus(value.toString())
        append("\n$line")
    }
    if (Looper.getMainLooper().thread == Thread.currentThread()) {
        runnable.run()
    } else {
        post(runnable)
    }
}
