package com.munch.lib.helper

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by munch1182 on 2022/5/14 21:46.
 */
interface IIntegerHelper {

    val curr: Int
}

open class IntegerHelper : IIntegerHelper {

    private val ai = AtomicInteger()

    override val curr: Int
        get() = ai.getAndIncrement()
}