package com.munch.test.project.one.base

import com.munch.lib.fast.base.FastApp
import com.munch.pre.lib.log.log
import com.munch.pre.lib.watcher.Watcher
import com.munch.test.project.one.switch.SwitchHelper
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2021/3/31 13:59.
 */
class TestApp : FastApp() {

    var btInited = false

    companion object {

        fun get() = getInstance() as TestApp

    }

    override fun onCreate() {
        super.onCreate()
        measureHelper.measure("app init") {
            DataHelper.init()
            SwitchHelper.INSTANCE.registerApp(this)
            Watcher().watchMainLoop().strictMode().startFpsMonitor()
            thread {
                try {
                    IjkMediaPlayer.loadLibrariesOnce(null)
                    IjkMediaPlayer.native_profileBegin("libijkplayer.so")
                } catch (e: Exception) {
                    log(e)
                }
            }
        }
    }
}