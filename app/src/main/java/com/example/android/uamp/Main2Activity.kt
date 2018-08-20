package com.example.android.uamp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.android.uamp.media.Player
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.File

class Main2Activity : AppCompatActivity() {
    private val log: TextView by lazy { findViewById<TextView>(R.id.logs) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Player.init(application)
        Player.instance.stateLiveData.observe(this, Observer {
            log.appendLine("State", it)
        })
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
                    Player.Item(
                        "track1",
                        "Title1",
                        "Artist1",
                        files[0].toString(),
                        files[1].toString(),
                        "Album1",
                        BitmapFactory.decodeFile(files[1].path)
                    )
                )
            }
        }
        toggle.setOnClickListener { Player.instance.togglePlayPause() }
    }
}

private fun TextView.appendLine(key: String? = null, value: Any? = null) {
    val line = (key?.plus(": ") ?: "").plus(value.toString())
    append("\n$line")
}
