package com.munch.test.project.one.player.media

import androidx.annotation.IntDef

/**
 * Create by munch1182 on 2021/5/11 14:52.
 */
interface IMediaSetting

data class MediaMate(val title: String, val total: Long, val from: String)

/**
 * Create by munch1182 on 2021/5/31 11:06.
 */
@IntDef(
    MediaState.STATE_IDEL,
    MediaState.STATE_INITIALIZED,
    MediaState.STATE_PREPARING,
    MediaState.STATE_PREPARED,
    MediaState.STATE_STARTED,
    MediaState.STATE_PAUSED,
    MediaState.STATE_COMPLETED,
    MediaState.STATE_STOPPED,
    MediaState.STATE_ERROR,
    MediaState.STATE_RELEASE
)
@Retention(AnnotationRetention.SOURCE)
annotation class MediaState {

    companion object {
        /**
         * 初始状态/空状态
         */
        const val STATE_IDEL = 0

        /**
         * 初始化成功
         */
        const val STATE_INITIALIZED = 1

        /**
         * 准备中
         */
        const val STATE_PREPARING = 2

        /**
         * 准备工作完成
         */
        const val STATE_PREPARED = 3

        /**
         * 播放中
         */
        const val STATE_STARTED = 4

        /**
         * 播放暂停
         */
        const val STATE_PAUSED = 5

        /**
         * 播放完成
         */
        const val STATE_COMPLETED = 6

        /**
         * 播放结束
         */
        const val STATE_STOPPED = 7

        const val STATE_ERROR = 8

        const val STATE_RELEASE = 9
    }
}