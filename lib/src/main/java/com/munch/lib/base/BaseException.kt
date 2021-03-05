package com.munch.lib.base

import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2021/3/5 14:10.
 */
open class BaseException(
    //表示该异常是必须处理，false表示该异常可以被无视
    private var mustHandle: Boolean = true,
    override val message: String? = "no message",
    override val cause: Throwable? = null
) : Exception(message, cause) {

    private var canIgnore = false

    fun judge() {
        if (!canIgnore) {
            throw this
        }
        // ignore
    }

    fun needHandle(mustHandle: Boolean = true) {
        this.mustHandle = mustHandle
        canIgnore = !mustHandle
    }
}

open class DebugException(
    mustHandle: Boolean = BaseApp.debugMode(),
    override val message: String? = "no message",
    override val cause: Throwable? = null
) : BaseException(mustHandle, message, cause)