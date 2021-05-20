package com.munch.test.project.one.player.media

/**
 * Create by munch1182 on 2021/5/11 14:14.
 */
interface IMediaControllerView : IMediaController {

    fun attachVideoView(videoView: VideoView, setting: MediaSetting)

    fun setControlListener(listener: IMediaController)

    //尚未决定info的信息结构
    fun showInfo() {}

    fun startPrepare()

    fun onPrepared()

    fun onComplete()

    fun onVideoViewSizeChanged(videoView: VideoView, w: Int, h: Int)
}