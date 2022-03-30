package com.munch.lib.android

import android.app.Application

/**
 * 此类作为context的存储类，赋值后可供本库中的其它类和函数提供context，而无需再传参
 *
 * Create by munch1182 on 2022/3/30 19:15.
 */
object AppHelper {

    private var application: Application? = null

    val appNullable: Application?
        get() = application

    val app: Application
        get() = application ?: throw NullPointerException("must call init")

    fun init(application: Application) {
        this.application = application
    }
}