package com.munch.project.test

import com.munch.lib.BaseApp
import com.munch.lib.helper.LogLog
import com.munch.project.test.switch.SwitchHelper

/**
 * Create munch1182 on 2020/12/7 14:44.
 */
class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        SwitchHelper.INSTANCE.registerApp(this)
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            e.printStackTrace()
            LogLog.log(e.message)
        }
    }
}