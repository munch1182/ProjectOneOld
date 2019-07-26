package com.munhc.lib.libnative.helper

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

    companion object {

        @JvmStatic
        fun getInstance() = Singleton.INSTANCE

        @JvmStatic
        fun getContext() = getInstance().getApp()
    }

    private class Singleton {

        companion object {
            internal val INSTANCE = AppHelper()
        }
    }

}