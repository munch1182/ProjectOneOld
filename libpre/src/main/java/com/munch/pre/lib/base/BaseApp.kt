package com.munch.pre.lib.base

import android.app.Application
import android.os.Process
import com.munch.pre.lib.BuildConfig
import com.munch.pre.lib.extend.log
import com.munch.pre.lib.helper.AppHelper

/**
 * Create by munch1182 on 2021/3/30 16:19.
 */
open class BaseApp : Application() {

    companion object {

        fun debug() = BuildConfig.DEBUG

        private lateinit var instance: BaseApp

        fun getInstance() = instance

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            log(e)
            AppHelper.resetApp2Activity(this)
            Process.killProcess(Process.myPid())
        }
    }
}