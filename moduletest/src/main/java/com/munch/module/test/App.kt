package com.munch.module.test

import android.app.Application
import com.munch.lib.libnative.helper.AppHelper
import com.squareup.leakcanary.LeakCanary

/**
 * Created by Munch on 2019/7/13 14:49
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
        AppHelper.getInstance().init(this)
    }
}