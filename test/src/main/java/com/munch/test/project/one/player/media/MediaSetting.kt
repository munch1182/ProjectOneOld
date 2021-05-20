package com.munch.test.project.one.player.media

import androidx.annotation.IntDef

/**
 * Create by munch1182 on 2021/5/11 14:52.
 */
data class MediaSetting(
    //是否能够后台播放
    var enablePlayBackground: Boolean = false,
    //播放器类型
    @PlayerType
    var playerType: Int = PlayerType.PLAYER_IJK,
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
)

@IntDef(PlayerType.PLAYER_IJK, PlayerType.PLAYER_ANDROID)
@Retention(AnnotationRetention.SOURCE)
annotation class PlayerType {

    companion object {

        const val PLAYER_IJK = 0
        const val PLAYER_ANDROID = 1
    }
}

@IntDef(DecodeType.DECODE_HARD, DecodeType.DECODE_SOFT)
@Retention(AnnotationRetention.SOURCE)
annotation class DecodeType {

    companion object {

        const val DECODE_HARD = 0
        const val DECODE_SOFT = 1
    }
}