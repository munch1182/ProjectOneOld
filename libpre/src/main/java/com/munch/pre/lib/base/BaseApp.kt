package com.munch.pre.lib.base

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Process
import com.munch.pre.lib.BuildConfig
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.log.log

/**
 * Create by munch1182 on 2021/3/30 16:19.
 */
open class BaseApp : Application() {

    companion object {

        fun debug() = BuildConfig.DEBUG

        private lateinit var instance: BaseApp

        fun getInstance() = instance

    }

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            log(e)
            e.printStackTrace()
            AppHelper.resetApp2Activity(this)
            Process.killProcess(Process.myPid())
        }
    }

    fun getMainHandler(): Handler {
        return handler
    }
}