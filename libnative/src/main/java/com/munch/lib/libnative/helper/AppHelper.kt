package com.munch.lib.libnative.helper

import android.app.Application

/**
 * Created by Munch on 2019/7/26 8:52
 */
class AppHelper private constructor() {

    private lateinit var app: Application

    fun init(app: Application) {
        this.app = app
    }

    fun getApp() = app

    fun debug() = true

    companion object {

        @JvmStatic
        fun getInstance() = Singleton.INSTANCE

        @JvmStatic
        fun getContext() = getInstance().getApp()

        @JvmStatic
        fun isDebug() = getInstance().debug()
    }

    private class Singleton {

        companion object {
            internal val INSTANCE = AppHelper()
        }
    }

}