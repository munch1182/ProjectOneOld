package com.munch.test.project.one.player.media

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.IntDef
import com.munch.pre.lib.log.Logger
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText

/**
 * Create by munch1182 on 2021/5/31 11:58.
 */
class VideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : PlayerView(context, attrs, defAttr) {

    override var player: IMediaController? = null
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

    private fun initPlayer(setting: IMediaSetting) {
        if (setting !is VideoSetting) {
            throw UnsupportedOperationException("must be VideoSetting")
        }
        //不能通过更改设置直接更改播放器类型
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