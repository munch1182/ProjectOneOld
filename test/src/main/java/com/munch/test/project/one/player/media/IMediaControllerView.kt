package com.munch.test.project.one.player.media

/**
 * Create by munch1182 on 2021/5/11 14:14.
 */
interface IMediaControllerView : IMediaController {

    /**
     * 关联操作视图与实现
     */
    fun attachPlayer(player: IMediaController, setting: IMediaSetting)

    /**
     * 将视图层的操作回调给操作层
     */
    fun setControlListener(listener: IMediaController)

    fun startPrepare()

    fun showInfo(info: MediaMate)

    fun onPrepared()

    fun onComplete()

    fun onVideoViewSizeChanged(videoView: IMediaController, w: Int, h: Int)
}