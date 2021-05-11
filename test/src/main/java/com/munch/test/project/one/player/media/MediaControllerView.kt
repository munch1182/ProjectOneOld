package com.munch.test.project.one.player.media

/**
 * Create by munch1182 on 2021/5/11 14:37.
 */
abstract class MediaControllerView : IMediaControllerView {

    private var videoView: VideoView? = null
    private var listener: IMediaController? = null
    override fun attachVideoView(videoView: VideoView, setting: MediaSetting) {
        this.videoView = videoView
        attachVideo(videoView, setting)
    }

    abstract fun attachVideo(videoView: VideoView, setting: MediaSetting)

    override fun setControlListener(listener: IMediaController) {
        this.listener = listener
    }

    override fun start(timeout: Long) {
        listener?.start(timeout)
    }

    abstract fun onStart(timeout: Long)

    override fun release() {
        videoView = null
        listener = null
    }

    override fun pause() {
        listener?.pause()
    }

    abstract fun onPause()

    override fun stop() {
        listener?.stop()
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