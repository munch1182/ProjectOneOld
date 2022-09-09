package com.munch.lib.android.extend

val Any.threadName: String
    get() = Thread.currentThread().name

val Any.threadId: Long
    get() = Thread.currentThread().id