package com.munch.test.project.one.player.video

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.IntDef
import androidx.core.view.contains
import com.munch.pre.lib.log.Logger
import com.munch.test.project.one.player.media.IMediaController
import com.munch.test.project.one.player.media.IMediaSetting
import com.munch.test.project.one.player.media.MediaState
import com.munch.test.project.one.player.media.PlayerView
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.FileDescriptor

/**
 * Create by munch1182 on 2021/5/31 11:58.
 */
class VideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : PlayerView(context, attrs, defAttr) {

    companion object {
        val ALL_ASPECT_RATIO = intArrayOf(
            IRenderView.AR_ASPECT_FIT_PARENT,
            IRenderView.AR_ASPECT_FILL_PARENT,
            IRenderView.AR_ASPECT_WRAP_CONTENT,
            /*IRenderView.AR_MATCH_PARENT,*/
            IRenderView.AR_16_9_FIT_PARENT,
            IRenderView.AR_4_3_FIT_PARENT
        )
    }

    override var player: IMediaController? = null
    private var renderView: IRenderView? = null
    override var setting: IMediaSetting = VideoSetting()
        set(value) {
            if (field != value) {
                field = value
                onSettingChange(value)
            }
        }
    private val log = Logger().apply {
        tag = "video-view"
        noStack = true
    }
    private var state = MediaState.STATE_IDEL
    private var currentAspectRatio = ALL_ASPECT_RATIO[0]
    private val playerImp: IPlayer?
        get() = player as IPlayer?

    init {
        setBackgroundColor(Color.BLACK)
    }

    fun initView(setting: IMediaSetting = VideoSetting()) {
        if (setting !is VideoSetting) {
            throw UnsupportedOperationException("must be VideoSetting")
        }
        initPlayer(setting)
        initView()
        setViewByPlayer()
        addView()
    }

    private fun addView() {
        if (renderView != null && contains(renderView!!.getView())) {
            return
        }
        addView(
            renderView!!.getView().apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }
        )
    }

    private fun setViewByPlayer() {
        if (playerImp != null && renderView != null) {
            playerImp!!.setHolder(renderView!!.getHolder())
            renderView!!.bindPlayer(playerImp!!)
            renderView!!.setAspectRatio(currentAspectRatio)
        }
    }

    private fun initView() {
        if (renderView == null) {
            renderView = TextureRenderView(context)
        }
    }

    private fun initPlayer(setting: VideoSetting) {
        if (player == null) {
            player = when (setting.playerType) {
                PlayerType.PLAYER_IJK -> IJKPlayer(setting)
                PlayerType.PLAYER_ANDROID -> AndroidPlayer(setting)
                else -> null
            }?.setListener(playerListener)
        }
    }

    fun setSetting(setting: VideoSetting): VideoView {
        this.setting = setting
        return this
    }

    private val playerListener = object : PlayerListener {
        override fun onPrepared(player: IMediaPlayer?) {
            super.onPrepared(player)
            log.log("onPrepared")

            log.log(player?.trackInfo)
        }

        override fun onCompletion(player: IMediaPlayer?) {
            super.onCompletion(player)
            log.log("onCompletion")
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

    fun setDataSource(source: IMediaDataSource) {
        playerImp?.setDataSource(source)
    }

    fun setDataSource(uri: Uri) {
        playerImp?.setDataSource(context, uri)
    }

    fun setDataSource(uri: Uri?, map: Map<String?, String?>?) {
        playerImp?.setDataSource(context, uri, map)
    }

    fun setDataSource(descriptor: FileDescriptor?) {
        playerImp?.setDataSource(descriptor)
    }

    fun setDataSource(path: String?) {
        playerImp?.setDataSource(path)
    }

    data class VideoSetting(
        //是否能够后台播放
        var enablePlayBackground: Boolean = false,
        @PlayerType
        var playerType: Int = PlayerType.PLAYER_IJK,
        var viewType: Int = 1,
        //解码类型
        @DecodeType
        var decodeType: Int = DecodeType.DECODE_HARD,
        var enableAutoRotate: Boolean = true,
        var handleResolutionChange: Boolean = true,
        var useSLES: Boolean = false,
        var enableNoView: Boolean = true,
        var lastDir: String? = null,
        var autoPlay: Boolean = false,
        //保持进度条
        var keepProgress: Boolean = false,
        var aspectRatio: Float = 16f / 9f
    ) : IMediaSetting

    @IntDef(DecodeType.DECODE_HARD, DecodeType.DECODE_SOFT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class DecodeType {

        companion object {

            const val DECODE_HARD = 0
            const val DECODE_SOFT = 1
        }
    }

    @IntDef(PlayerType.PLAYER_IJK, PlayerType.PLAYER_ANDROID)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PlayerType {

        companion object {

            const val PLAYER_IJK = 0
            const val PLAYER_ANDROID = 1
        }
    }
}