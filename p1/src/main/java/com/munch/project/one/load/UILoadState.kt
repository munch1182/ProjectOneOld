package com.munch.project.one.load

/**
 * Create by munch1182 on 2021/9/16 11:22.
 */
sealed class UILoadState {

    /** 加载中 */
    object Loading : UILoadState()

    /** 加载成功 */
    data class Success<T>(val value: T) : UILoadState()

    /** 加载失败 */
    data class Fail(val e: Exception) : UILoadState()
}