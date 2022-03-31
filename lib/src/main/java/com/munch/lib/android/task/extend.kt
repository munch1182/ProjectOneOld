package com.munch.lib.android.task

import android.os.Looper

/**
 * Create by munch1182 on 2022/3/31 15:44.
 */

@Suppress("NOTHING_TO_INLINE")
inline fun Thread.isMain() = Looper.getMainLooper().thread.id == this.id