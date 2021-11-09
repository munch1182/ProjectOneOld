package com.munch.project.one

import android.app.Application
import android.content.Context
import com.munch.lib.app.AppForegroundHelper
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.watcher.MeasureHelper
import com.munch.lib.fast.watcher.Watcher
import com.munch.lib.helper.data.MMKVHelper
import com.munch.lib.log.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

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

    /**
     * 此方法只在冷启动时调用
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MeasureHelper.start(KEY_MEASURE_LAUNCH)
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        /*FastAppHelper.init(this)*/
        AppHelper.init(app)
        AppForegroundHelper.register(this)
        MMKVHelper.init(app)
        thread {
            Thread.setDefaultUncaughtExceptionHandler { _, e -> log(e) }
            Watcher.watchMainLoop()
            //去触发初始化，大概会快个20-40ms
            CoroutineScope(Dispatchers.Unconfined).launch {}
        }
    }
}