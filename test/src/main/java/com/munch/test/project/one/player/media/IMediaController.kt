package com.munch.test.project.one.player.media

import android.widget.MediaController

/**
 * Create by munch1182 on 2021/5/11 14:14.
 */
interface IMediaController : MediaController.MediaPlayerControl {

    override fun start() = start(10 * 1000L)
    fun start(timeout: Long)
    override fun pause()
    fun toggle() {
        if (isPlaying) pause() else start()
    }

    override fun getDuration(): Int = getDurationLong().toInt()
    fun getDurationLong(): Long
    override fun getCurrentPosition(): Int = getCurrentPositionLong().toInt()
    fun getCurrentPositionLong(): Long
    override fun seekTo(pos: Int) = seekToLong(pos.toLong())
    fun seekToLong(pos: Long)
    override fun isPlaying(): Boolean
    fun release()
    fun stop()
    fun onSettingChange(setting: IMediaSetting)

    override fun getBufferPercentage(): Int
    override fun canPause(): Boolean = true
    override fun canSeekBackward(): Boolean = true
    override fun canSeekForward(): Boolean = true
    override fun getAudioSessionId(): Int = 0
}

