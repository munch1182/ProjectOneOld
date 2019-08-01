package com.munch.lib.libnative.excetion

/**
 * Created by Munch on 2019/7/26 14:28
 */
open class BException(private val canHandle: Boolean, message: String?, cause: Throwable?) :
    RuntimeException(message, cause) {

    /**
     * @param canHandle 是否可以被处理，即即使出现此种异常，也可自行处理或者不处理而不抛出异常
     */
    constructor(canHandle: Boolean, message: String?) : this(canHandle, message, null)

    fun canHandle() = canHandle
}