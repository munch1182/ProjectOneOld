package com.munch.project.test

import android.app.Application
import com.munch.lib.helper.LogLog

/**
 * Create munch1182 on 2020/12/7 14:44.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            e.printStackTrace()
            LogLog.log(e.message)
        }
    }
}