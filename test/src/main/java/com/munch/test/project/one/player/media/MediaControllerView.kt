package com.munch.test.project.one.player.media

/**
 * 不带on前缀的方法是调用方法，应该回调给[IMediaController]并调用对应的on方法
 * Create by munch1182 on 2021/5/11 14:37.
 */
abstract class MediaControllerView : IMediaControllerView {

    private var videoView: IMediaController? = null
    private var listener: IMediaController? = null
    override fun attachView(videoView: IMediaController, setting: MediaSetting) {
        this.videoView = videoView
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
        videoView = null
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

    override fun getDuration(): Int = videoView?.duration ?: 0

    override fun getCurrentPosition() = videoView?.currentPosition ?: 0

    override fun seekTo(pos: Int) {
        listener?.seekTo(pos)
    }

    override fun isPlaying() = videoView?.isPlaying ?: false

    override fun getBufferPercentage() = videoView?.bufferPercentage ?: 0

    override fun canPause(): Boolean = videoView?.canPause() ?: false

    override fun canSeekBackward(): Boolean = videoView?.canSeekBackward() ?: false

    override fun canSeekForward(): Boolean = videoView?.canSeekForward() ?: false

    override fun getAudioSessionId(): Int = videoView?.audioSessionId ?: -1
}