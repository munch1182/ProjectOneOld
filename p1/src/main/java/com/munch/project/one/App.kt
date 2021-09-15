package com.munch.project.one

import android.app.Application
import android.content.Context
import com.munch.lib.fast.FastAppHelper
import com.munch.lib.fast.watcher.MeasureHelper

/**
 * Create by munch1182 on 2021/9/14 14:31.
 */
class App : Application() {

    companion object {

        private lateinit var app: App

        val instance: App
            get() = app

        private const val KEY_MEASURE_LAUNCH = "app_launch"

        fun getLaunchCost() = MeasureHelper.cost(KEY_MEASURE_LAUNCH)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MeasureHelper.start(KEY_MEASURE_LAUNCH)
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        FastAppHelper.init(this)
    }
}