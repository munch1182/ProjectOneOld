package com.munch.common.base.log

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy


/**
 * Created by Munch on 2018/12/8.
 */
object Loglog {

    private const val isDebug = true
    private const val Tag = "p1"

    fun log(msg: Any?) {
        if (isDebug) {
            Logger.addLogAdapter(AndroidLogAdapter(PrettyFormatStrategy.newBuilder().tag("233333333333333333333333333333").build()))
            Logger.d(msg?.toString())
        }
    }
}