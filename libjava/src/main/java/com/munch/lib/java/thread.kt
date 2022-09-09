package com.munch.lib.java

/**
 * 获取当前线程名
 */
inline val threadName: String
    get() = Thread.currentThread().name

/**
 * 获取当前线程ID
 */
inline val threadId: Long
    get() = Thread.currentThread().id