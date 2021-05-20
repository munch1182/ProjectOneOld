package com.munch.test.project.one.player.media

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import com.munch.pre.lib.extend.setParams
import com.munch.pre.lib.log.Logger
import com.munch.pre.lib.log.log
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText
import java.io.File
import java.util.*

/**
 * Create by munch1182 on 2021/5/11 13:53.
 */
class VideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : FrameLayout(context, attrs, defAttr), IMediaController {

    private var controller: IMediaControllerView? = null
    private var setting: MediaSetting? = null
    private val log = Logger().apply {
        tag = "video-view"
        noStack = true
    }
    private var targetUri: Uri? = null
    private var header: Map<String, String>? = null

    private val holder by lazy {
        SurfaceView(context, attrs).apply {
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    log.log("surfaceCreated")
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    log.log("surfaceChanged")
                    prepare()
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    log.log("surfaceDestroyed")
                }
            })
        }
    }
    private var mRotate = 0
    private var player: IMediaPlayer? = null

    init {
        //黑色背景
        setBackgroundColor(Color.BLACK)
        addView(holder)
    }

    fun attachControllerView(
        controller: IMediaControllerView,
        setting: MediaSetting? = null
    ): VideoView {
        this.controller = controller
        this.setting = setting
        log.log("attachControllerView")
        controller.attachVideoView(this, getSettingOrDef())
        controller.setControlListener(this)
        return this
    }

    fun setData(uri: Uri, map: Map<String, String>? = null) {
        log.log("setData:$uri,$map")
        targetUri = uri
        header = map
    }

    private fun prepare() {
        log.log("prepare: player:${player},uri.scheme:${targetUri?.scheme}")
        targetUri ?: return
        val player = newMediaPlayer().apply {
            setOnPreparedListener(playerListener)
            setOnVideoSizeChangedListener(playerListener)
            setOnCompletionListener(playerListener)
            setOnErrorListener(playerListener)
            setOnInfoListener(playerListener)
            setOnSeekCompleteListener(playerListener)
            setOnTimedTextListener(playerListener)
        }
        bindSurfaceHolder(player)
        val scheme = targetUri?.scheme ?: return
        player.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (TextUtils.isEmpty(scheme) || scheme.toLowerCase(Locale.getDefault()) == "file")
            ) {
                setDataSource(FileMediaDataSource(File(targetUri.toString())))
            } else {
                setDataSource(context, targetUri, header)
            }
            setScreenOnWhilePlaying(true)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            prepareAsync()
        }
        log.log("prepareAsync")
        this.controller?.startPrepare()
        requestLayout()
        invalidate()
    }

    private fun bindSurfaceHolder(player: IMediaPlayer) {
        player.setDisplay(holder.holder)
        holder.rotation = rotation
        log.log("bindSurfaceHolder")
    }

    private fun getSettingOrDef(): MediaSetting {
        if (setting == null) {
            setting = MediaSetting()
        }
        return setting!!
    }

    override fun settingChange(setting: MediaSetting) {
        this.setting = setting
        controller?.settingChange(setting)
    }

    private fun newMediaPlayer(): IMediaPlayer {
        if (player != null) {
            release()
        }
        val setting = getSettingOrDef()
        player = when (setting.playerType) {
            PlayerType.PLAYER_IJK -> newIjkPlayer(setting)
            PlayerType.PLAYER_ANDROID -> AndroidMediaPlayer()
            else -> throw IllegalStateException("unSupport type")
        }
        return player!!
    }

    private fun newIjkPlayer(setting: MediaSetting): IjkMediaPlayer {
        return IjkMediaPlayer().apply {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
            //硬解码
            val option = IjkMediaPlayer.OPT_CATEGORY_PLAYER
            if (setting.decodeType == DecodeType.DECODE_HARD) {
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
            setOption(option, "framedrop", 1)
            setOption(option, "start-on-prepared", 0)
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0)
            setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
        }
    }

    private val playerListener = object : PlayerListener {
        override fun onPrepared(player: IMediaPlayer?) {
            super.onPrepared(player)
            log.log("onPrepared")
            controller?.onPrepared()
            if (getSettingOrDef().autoPlay) {
                player?.start()
                controller?.start()
            }
            log.log(player?.trackInfo)
        }

        override fun onCompletion(player: IMediaPlayer?) {
            super.onCompletion(player)
            log.log("onCompletion")
            controller?.onComplete()
        }

        override fun onInfo(player: IMediaPlayer?, what: Int, extra: Int): Boolean {
            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> log.log("onInfo:MEDIA_INFO_VIDEO_TRACK_LAGGING:$extra")
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> log.log("onInfo:MEDIA_INFO_VIDEO_RENDERING_START:$extra")
                IMediaPlayer.MEDIA_INFO_BUFFERING_START -> log.log("onInfo:MEDIA_INFO_BUFFERING_START:$extra")
                IMediaPlayer.MEDIA_INFO_BUFFERING_END -> log.log("onInfo:MEDIA_INFO_BUFFERING_END: $extra")
                IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> log.log("onInfo:MEDIA_INFO_NETWORK_BANDWIDTH: $extra")
                IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING -> log.log("onInfo:MEDIA_INFO_BAD_INTERLEAVING:$extra")
                IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> log.log("onInfo:MEDIA_INFO_NOT_SEEKABLE:$extra")
                IMediaPlayer.MEDIA_INFO_METADATA_UPDATE -> log.log("onInfo:MEDIA_INFO_METADATA_UPDATE:$extra")
                IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE -> log.log("onInfo:MEDIA_INFO_UNSUPPORTED_SUBTITLE:$extra")
                IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT -> log.log("onInfo:MEDIA_INFO_SUBTITLE_TIMED_OUT:$extra")
                IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> {
                    mRotate = extra
                    log.log("onInfo:MEDIA_INFO_VIDEO_ROTATION_CHANGED: rotation=$extra")
                }
                IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START -> log.log("onInfo:MEDIA_INFO_AUDIO_RENDERING_START:$extra")
                IMediaPlayer.MEDIA_INFO_VIDEO_SEEK_RENDERING_START -> log.log("onInfo:MEDIA_INFO_VIDEO_SEEK_RENDERING_START:$extra")
                IMediaPlayer.MEDIA_INFO_AUDIO_SEEK_RENDERING_START -> log.log("onInfo:MEDIA_INFO_AUDIO_SEEK_RENDERING_START:$extra")
                IMediaPlayer.MEDIA_INFO_AUDIO_DECODED_START -> log.log("onInfo:MEDIA_INFO_AUDIO_DECODED_START:$extra")
                IMediaPlayer.MEDIA_INFO_VIDEO_DECODED_START -> log.log("onInfo:MEDIA_INFO_VIDEO_DECODED_START:$extra")
                IMediaPlayer.MEDIA_INFO_OPEN_INPUT -> log.log("onInfo:MEDIA_INFO_OPEN_INPUT:$extra")
                IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO -> log.log("onInfo:MEDIA_INFO_FIND_STREAM_INFO:$extra")
                IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN -> log.log("onInfo:MEDIA_INFO_COMPONENT_OPEN:$extra")
                else -> log.log("ijk onInfo:what=$what,extra=$extra")

            }
            return super.onInfo(player, what, extra)
        }

        override fun onError(player: IMediaPlayer?, what: Int, extra: Int): Boolean {
            when (what) {
                IjkMediaPlayer.MEDIA_ERROR_IO -> log.log("onError:MEDIA_ERROR_IO:$extra")
                IjkMediaPlayer.MEDIA_ERROR_SERVER_DIED -> log.log("onError:MEDIA_ERROR_SERVER_DIED:$extra")
                IjkMediaPlayer.MEDIA_ERROR_MALFORMED -> log.log("onError:MEDIA_ERROR_MALFORMED:$extra")
                IjkMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> log.log("onError:MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:$extra")
                IjkMediaPlayer.MEDIA_ERROR_TIMED_OUT -> log.log("onError:MEDIA_ERROR_TIMED_OUT:$extra")
                IjkMediaPlayer.MEDIA_ERROR_UNSUPPORTED -> log.log("onError:MEDIA_ERROR_UNSUPPORTED:$extra")
                IjkMediaPlayer.MEDIA_ERROR_UNKNOWN -> log.log("onError:MEDIA_ERROR_UNKNOWN:$extra")
                else -> log.log("ijk onError:what=$what,extra=$extra")
            }
            return super.onError(player, what, extra)
        }

        override fun onVideoSizeChanged(
            player: IMediaPlayer?,
            w: Int,
            h: Int,
            sar_num: Int,
            sar_den: Int
        ) {
            super.onVideoSizeChanged(player, w, h, sar_num, sar_den)
            log.log("ijk onVideoSizeChanged: $w*$h")
        }

        override fun onTimedText(player: IMediaPlayer?, text: IjkTimedText?) {
            log.log("ijk onTimedText: $text")
        }
    }

    //<editor-fold desc="proxy">
    override fun start(timeout: Long) {
        log("player onStart")
        player?.start()
    }

    override fun stop() {
        player?.stop()
    }

    override fun release() {
        player?.apply {
            stop()
            release()
            log.log("ijk release")
        }
        player = null
    }

    override fun pause() {
        player?.pause()
    }

    override fun getDuration(): Int {
        return player?.duration?.toInt() ?: throw IllegalStateException("cannot get value")
    }

    override fun getCurrentPosition(): Int {
        return player?.currentPosition?.toInt() ?: throw IllegalStateException("cannot get value")
    }

    override fun seekTo(pos: Int) {
        player?.seekTo(pos.toLong())
    }

    override fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    override fun getBufferPercentage(): Int {
        throw UnsupportedOperationException()
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        throw UnsupportedOperationException()
    }
    //</editor-fold>

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            //未考虑竖屏的情形
            else -> (width / getSettingOrDef().aspectRatio).toInt()
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        log.log("onSizeChanged")
        holder.post {
            holder.setParams {
                width = w
                height = h
            }
        }
        log.log("attachControllerView")
        controller?.onVideoViewSizeChanged(this, w, h)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
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
}