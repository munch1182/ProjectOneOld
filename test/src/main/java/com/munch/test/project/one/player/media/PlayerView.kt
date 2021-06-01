package com.munch.test.project.one.player.media

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * Create by munch1182 on 2021/5/31 11:41.
 */
abstract class PlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : FrameLayout(context, attrs, defAttr), IMediaController {

    protected var player: IMediaPlayer? = null
    protected var view: IMediaControllerView? = null
    protected open var setting = MediaSetting()
        set(value) {
            if (field != value) {
                field = value
                onSettingChange(field)
            }
        }
    protected open var state = MediaState.STATE_IDEL

    protected open fun initPlayer() {

    }

    fun attachView(view: IMediaControllerView) {
        this.view = view
        this.view?.attachView(this, setting)
        this.view?.setControlListener(this)
    }

    override fun start(timeout: Long) {
        player?.start()
    }

    override fun pause() {
        player?.pause()
    }

    override fun getDurationLong() = player?.duration ?: 0L

    override fun getCurrentPositionLong(): Long = player?.currentPosition ?: 0L

    override fun seekToLong(pos: Long) {
        player?.seekTo(pos)
    }

    override fun isPlaying(): Boolean = player?.isPlaying ?: false

    override fun release() {
        player?.release()
    }

    override fun stop() {
        player?.stop()
    }

    override fun onSettingChange(setting: MediaSetting) {
    }

    override fun getBufferPercentage(): Int {
        return 0
    }
}