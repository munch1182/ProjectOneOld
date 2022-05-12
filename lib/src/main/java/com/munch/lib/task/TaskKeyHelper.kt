package com.munch.lib.task

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by munch1182 on 2022/4/13 20:14.
 */
object TaskKeyHelper {

    private val ai = AtomicInteger()

    val curr: Int
        get() = ai.getAndIncrement()
}