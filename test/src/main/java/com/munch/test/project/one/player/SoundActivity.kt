package com.munch.test.project.one.player

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.munch.test.project.one.base.BaseItemActivity

/**
 * Create by munch1182 on 2021/6/1 9:39.
 */
class SoundActivity : BaseItemActivity() {

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    override fun clickItem(pos: Int) {
        if (audioManager == null) {
            toast("无法获取AudioManager")
            return
        }
        when (pos) {
            0 -> ctrlMusic(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            1 -> ctrlMusic(KeyEvent.KEYCODE_MEDIA_NEXT)
            2 -> {
                /*if (audioManager?.isMusicActive == true) {
                    ctrlMusic(KeyEvent.KEYCODE_MEDIA_PAUSE)
                } else {
                    ctrlMusic(KeyEvent.KEYCODE_MEDIA_PLAY)
                }*/
                ctrlMusic(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            }
            3 -> ctrlVolume(AudioManager.ADJUST_RAISE)
            4 -> ctrlVolume(AudioManager.ADJUST_LOWER)
        }
    }

    private fun ctrlVolume(direction: Int) {
        audioManager?.adjustStreamVolume(
            AudioManager.STREAM_SYSTEM,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("PREVIOUS", "NEXT", "PLAY/PAUSE", "VOLUME_UP", "VOLUME_DOWN")
    }

    private fun ctrlMusic(code: Int) {
        val now = System.currentTimeMillis()
        val down = KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0)
        audioManager?.dispatchMediaKeyEvent(down)
        audioManager?.dispatchMediaKeyEvent(KeyEvent.changeAction(down, KeyEvent.ACTION_UP))
    }
}