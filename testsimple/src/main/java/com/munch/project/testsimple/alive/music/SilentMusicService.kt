package com.munch.project.testsimple.alive.music

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.SystemClock
import com.munch.lib.log
import com.munch.project.testsimple.R
import com.munch.project.testsimple.alive.ScreenReceiverHelper
import com.munch.project.testsimple.alive.TestDataHelper
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2020/12/15 10:42.
 */
class SilentMusicService : Service() {

    companion object {

        fun start(context: Context) {
            context.startService(Intent(context, SilentMusicService::class.java))
        }

        fun register(context: Context) {
            start(
                context
            )
        }
    }

    private var player: MediaPlayer? = null

    private val pool by lazy {
        ThreadPoolExecutor(
            2, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue<Runnable>()
        )
    }

    override fun onCreate() {
        super.onCreate()
        ScreenReceiverHelper(this).addScreenStateListener(object :
            ScreenReceiverHelper.ScreenStateListener {
            override fun onScreenOn(context: Context?) {
                stopPlay()
            }

            override fun onScreenOff(context: Context?) {
                playMusic()
            }

            override fun onUserPresent(context: Context?) {
            }
        }).register()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun playMusic() {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.silent)
        }
        if (player?.isPlaying == true) {
            return
        }
        pool.execute {
            log("SilentMusicService play")
            player?.run {
                setVolume(0f, 0f)
                /*setOnCompletionListener {
                    log(it.isPlaying)
                    it.start()
                    log(it.isPlaying)
                }*/
                isLooping = true
                start()
                while (true) {
                    SystemClock.sleep(1000)
                    log("isPlaying：$isPlaying")
                }
            }
            TestDataHelper.timerSilentMusic(this, pool)
        }
    }

    fun stopPlay() {
        log("SilentMusicService stop")
        player?.stop()
        player?.release()
        player = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}