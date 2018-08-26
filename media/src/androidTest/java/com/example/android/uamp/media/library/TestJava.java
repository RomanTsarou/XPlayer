package com.example.android.uamp.media.library;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.android.uamp.media.IPlayer;
import com.example.android.uamp.media.Player;
import com.example.android.uamp.media.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.lifecycle.Observer;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.runner.AndroidJUnit4;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class TestJava {
    @Test
    public void testJavaAvailable() {
        final Context context = InstrumentationRegistry.getTargetContext();
        final CountDownLatch downLatch = new CountDownLatch(1);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                startPlayer(context, downLatch);
            }
        });
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startPlayer(Context context, final CountDownLatch downLatch) {
        List<IPlayer.Track> playlist = Arrays.asList(
                new IPlayer.Track(
                        "id_1",
                        "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/01_-_Intro_-_The_Way_Of_Waking_Up_feat_Alan_Watts.mp3",
                        "Intro - The Way Of Waking Up (feat. Alan Watts)",
                        "The Kyoto Connection",
                        "Wake Up",
                        "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/art.jpg",
                        ((BitmapDrawable) context.getDrawable(R.drawable.player_default_art)).getBitmap()
                ),
                new IPlayer.Track(
                        "id_2",
                        "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3",
                        "Geisha",
                        "The Kyoto Connection",
                        "Wake Up"
                )
        );
        Player.init(context);
        Player.INSTANCE.getLiveDataPlayerState().observeForever(new Observer<IPlayer.State>() {
            @Override
            public void onChanged(IPlayer.State state) {
                Log.i("rom", "onChanged: " + state);
                if (state == IPlayer.State.STOP) {
                    if (Player.INSTANCE.getCurrentPosition() == 0) {
                        Player.INSTANCE.play();
                    } else {
                        downLatch.countDown();
                    }
                }
            }
        });
        Player.INSTANCE.setPlayList(playlist);
    }
}
