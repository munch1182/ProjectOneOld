package com.munch.test.project.one.player.media

import android.widget.MediaController

/**
 * Create by munch1182 on 2021/5/11 14:14.
 */
interface IMediaController : MediaController.MediaPlayerControl {

    override fun start() = start(10 * 1000L)

    fun start(timeout: Long)

    fun release()

    fun stop()

    fun settingChange(setting: MediaSetting)

    fun toggle() {
        if (isPlaying) pause() else start()
    }
}