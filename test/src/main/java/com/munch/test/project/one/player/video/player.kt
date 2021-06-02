package com.munch.test.project.one.player.video

import android.content.Context
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import com.munch.test.project.one.player.media.IMediaController
import com.munch.test.project.one.player.media.IMediaSetting
import tv.danmaku.ijk.media.player.*
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.FileDescriptor

/**
 * Create by munch1182 on 2021/6/2 11:26.
 */
interface IPlayer : IMediaController {
    fun setDataSource(source: IMediaDataSource)

    fun setDataSource(context: Context, uri: Uri)

    fun setDataSource(context: Context?, uri: Uri?, map: Map<String?, String?>?)

    fun setDataSource(descriptor: FileDescriptor?)

    fun setDataSource(path: String?)

    fun setSurface(surface: Surface)

    fun setHolder(holder: SurfaceHolder?)

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int
}

abstract class AbstractPlayer(protected var setting: VideoView.VideoSetting) : IPlayer {

    protected abstract val player: AbstractMediaPlayer

    override fun start(timeout: Long) {
        player.start()
    }

    override fun pause() {
        player.pause()
    }

    override fun getDurationLong(): Long = player.duration

    override fun getCurrentPositionLong(): Long = player.currentPosition

    override fun seekToLong(pos: Long) {
        player.seekTo(pos)
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun release() {
        player.release()
    }

    override fun stop() {
        player.stop()
    }

    override fun getBufferPercentage(): Int = 0

    override fun setDataSource(source: IMediaDataSource) {
        player.setDataSource(source)
    }

    override fun setDataSource(context: Context, uri: Uri) {
        player.setDataSource(context, uri)
    }

    override fun setDataSource(context: Context?, uri: Uri?, map: Map<String?, String?>?) {
        player.setDataSource(context, uri, map)
    }

    override fun setDataSource(descriptor: FileDescriptor?) {
        player.setDataSource(descriptor)
    }

    override fun setDataSource(path: String?) {
        player.dataSource = path
    }

    override fun setSurface(surface: Surface) {
        player.setSurface(surface)
    }

    override fun setHolder(holder: SurfaceHolder?) {
        player.setDisplay(holder)
    }

    open fun setListener(playerListener: PlayerListener): AbstractPlayer {
        player.apply {
            setOnPreparedListener(playerListener)
            setOnVideoSizeChangedListener(playerListener)
            setOnCompletionListener(playerListener)
            setOnErrorListener(playerListener)
            setOnInfoListener(playerListener)
            setOnSeekCompleteListener(playerListener)
            setOnTimedTextListener(playerListener)
        }
        return this
    }

    override fun getVideoHeight(): Int = player.videoHeight

    override fun getVideoWidth(): Int = player.videoWidth
}

class IJKPlayer(setting: VideoView.VideoSetting) : AbstractPlayer(setting) {
    private val ijkPlayer = IjkMediaPlayer().apply {
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
        setIjkBySetting(this, setting)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0)
        setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
    }

    private fun setIjkBySetting(player: IjkMediaPlayer, setting: VideoView.VideoSetting) {
        player.apply {
            //硬解码
            val option = IjkMediaPlayer.OPT_CATEGORY_PLAYER
            if (setting.decodeType == VideoView.DecodeType.DECODE_HARD) {
                setOption(option, "mediacodec", 1)
                setOption(
                    option,
                    "mediacodec-auto-rotate",
                    if (setting.enableAutoRotate) 1 else 0
                )
                setOption(
                    option, "mediacodec-handle-resolution-change",
                    if (setting.handleResolutionChange) 1 else 0
                )
            } else {
                setOption(option, "mediacodec", 0)
            }
            setOption(option, "opensles", if (setting.useSLES) 1 else 0)
        }
    }

    override val player: AbstractMediaPlayer = ijkPlayer

    override fun onSettingChange(setting: IMediaSetting) {
        if (setting is VideoView.VideoSetting) {
            setIjkBySetting(ijkPlayer, setting)
        }
    }
}

class AndroidPlayer(setting: VideoView.VideoSetting) : AbstractPlayer(setting) {

    private val androidPlayer = AndroidMediaPlayer()
    override val player: AbstractMediaPlayer = androidPlayer

    override fun onSettingChange(setting: IMediaSetting) {
    }
}

interface PlayerListener : IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener,
    IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnBufferingUpdateListener,
    IMediaPlayer.OnErrorListener, IMediaPlayer.OnVideoSizeChangedListener,
    IMediaPlayer.OnCompletionListener, IMediaPlayer.OnTimedTextListener {
    override fun onPrepared(player: IMediaPlayer?) {
    }

    override fun onInfo(player: IMediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onSeekComplete(player: IMediaPlayer?) {
    }

    override fun onBufferingUpdate(player: IMediaPlayer?, p1: Int) {
    }

    override fun onError(player: IMediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onVideoSizeChanged(
        player: IMediaPlayer?,
        w: Int,
        h: Int,
        sar_num: Int,
        sar_den: Int
    ) {

    }

    override fun onCompletion(player: IMediaPlayer?) {

    }

    override fun onTimedText(player: IMediaPlayer?, text: IjkTimedText?) {
    }
}