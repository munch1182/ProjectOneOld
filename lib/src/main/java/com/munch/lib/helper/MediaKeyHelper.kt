package com.munch.lib.helper

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent

/**
 * 播放/暂停/上一首/下一首/音量+-
 *
 * Create by munch1182 on 2021/8/19 9:20.
 */
class MediaKeyHelper(private val am: AudioManager?) {

    constructor(context: Context) : this(context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager?)

    fun playOrPause() {
        if (am != null) {
            if (am.isMusicActive) pause() else play()
        }
    }

    fun play() = controlKey(KeyEvent.KEYCODE_MEDIA_PLAY)

    fun pause() = controlKey(KeyEvent.KEYCODE_MEDIA_PAUSE)

    fun playPrevious() = controlKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    fun playNext() = controlKey(KeyEvent.KEYCODE_MEDIA_NEXT)

    fun volumeDown() = controlVolume(AudioManager.ADJUST_LOWER)

    fun volumeUp() = controlVolume(AudioManager.ADJUST_RAISE)

    fun getMusicVolume() = am?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: -1

    fun controlKey(keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        val event = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        try {
            am?.dispatchMediaKeyEvent(event)
            am?.dispatchMediaKeyEvent(KeyEvent.changeAction(event, KeyEvent.ACTION_UP))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun controlVolume(direction: Int) {
        am?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

}