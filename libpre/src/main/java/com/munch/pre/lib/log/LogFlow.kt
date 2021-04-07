package com.munch.pre.lib.log

import com.munch.pre.lib.UNCOMPLETED
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by munch1182 on 2021/4/2 14:39.
 */
@UNCOMPLETED
class LogFlow(private val tag: String) {
    private var index = AtomicInteger(0)

    fun start() {
        LogLog.setTag(tag).methodOffset(1).log(("=============$tag start=============="))
    }

    fun end() {
        LogLog.setTag(tag).methodOffset(1).log(("=============$tag start=============="))
    }

    fun flow(any: Any) {
        LogLog.setTag(tag).methodOffset(1).log(("flow ${index.getAndIncrement()}: $any"))
    }
}