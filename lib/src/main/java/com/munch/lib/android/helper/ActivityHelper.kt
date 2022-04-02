package com.munch.lib.android.helper

import android.app.Application
import com.munch.lib.android.extend.SingletonHolder

/**
 * Create by munch1192 on 2022/4/2 17:28.
 */
class ActivityHelper private constructor(context: Application) {
    companion object : SingletonHolder<ActivityHelper, Application>({ ActivityHelper(it) })

    fun register(app: Application) {

    }
}