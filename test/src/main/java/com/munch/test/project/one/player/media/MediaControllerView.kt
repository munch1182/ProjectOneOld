package com.munch.test.project.one.player.media

/**
 * 不带on前缀的方法是调用方法，应该回调给[IMediaController]并调用对应的on方法
 * Create by munch1182 on 2021/5/11 14:37.
 */
abstract class MediaControllerView : IMediaControllerView {

    private var player: IMediaController? = null
    private var listener: IMediaController? = null
    override fun attachPlayer(player: IMediaController, setting: IMediaSetting) {
        this.player = player
    }

    override fun setControlListener(listener: IMediaController) {
        this.listener = listener
    }

    override fun start(timeout: Long) {
        listener?.start(timeout)
        onStart(timeout)
    }

    abstract fun onStart(timeout: Long)

    override fun release() {
        player = null
        listener = null
    }

    override fun pause() {
        listener?.pause()
        onPause()
    }

    abstract fun onPause()

    override fun stop() {
        listener?.stop()
        onStop()
    }

    abstract fun onStop()

    override fun getDuration(): Int = player?.duration ?: 0

    override fun getCurrentPosition() = player?.currentPosition ?: 0

    override fun seekTo(pos: Int) {
        listener?.seekTo(pos)
    }

    override fun isPlaying() = player?.isPlaying ?: false

    override fun getBufferPercentage() = player?.bufferPercentage ?: 0

    override fun canPause(): Boolean = player?.canPause() ?: false

    override fun canSeekBackward(): Boolean = player?.canSeekBackward() ?: false

    override fun canSeekForward(): Boolean = player?.canSeekForward() ?: false

    override fun getAudioSessionId(): Int = player?.audioSessionId ?: -1
}