package com.munch.lib.libnative.helper

import android.app.Application
import android.content.pm.ApplicationInfo

/**
 * Created by Munch on 2019/7/26 8:52
 */
class AppHelper private constructor() {

    private lateinit var app: Application

    fun initApp(app: Application) {
        this.app = app
    }

    fun getApp() = app

    fun debug(): Boolean {
        return try {
            val info = app.applicationInfo
            info.flags.and(ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }

    companion object {

        @JvmStatic
        fun init(app: Application) = getInstance().initApp(app)

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