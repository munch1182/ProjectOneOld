package com.munch.test.project.one.player.media

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Create by munch1182 on 2021/5/31 11:41.
 */
abstract class PlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : FrameLayout(context, attrs, defAttr), IMediaController {

    /**
     * 实际的播放器实现，应该由子类设置或实现
     */
    protected abstract var player: IMediaController?

    /**
     * 给播放加入的控制视图，由[attachControllerView]设置，即可以转由外部传入自行定制
     */
    protected var view: IMediaControllerView? = null
    protected abstract var setting: IMediaSetting

    open fun attachControllerView(view: IMediaControllerView) {
        this.view = view
        this.view?.attachPlayer(this, setting)
        this.view?.setControlListener(this)
    }

    override fun start(timeout: Long) {
        player?.start()
    }

    override fun pause() {
        player?.pause()
    }

    override fun getDurationLong() = player?.getDurationLong() ?: 0L

    override fun getCurrentPositionLong(): Long = player?.getCurrentPositionLong() ?: 0L

    override fun seekToLong(pos: Long) {
        player?.seekToLong(pos)
    }

    override fun isPlaying(): Boolean = player?.isPlaying ?: false

    override fun release() {
        player?.release()
        player = null
    }

    override fun stop() {
        player?.stop()
    }

    override fun onSettingChange(setting: IMediaSetting) {
        player?.onSettingChange(setting)
    }

    override fun getBufferPercentage(): Int {
        return 0
    }
}