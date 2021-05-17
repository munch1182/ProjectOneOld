package com.munch.project.launcher.base

import android.os.Process
import com.munch.pre.lib.BuildConfig
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.AppStatusHelper
import com.munch.pre.lib.helper.measure.SimpleMeasureTime
import com.munch.pre.lib.log.Logger
import com.munch.pre.lib.watcher.Watcher
import com.munch.project.launcher.test.TestTask
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2021/5/8 10:53.
 */
class LauncherApp : BaseApp() {

    companion object {

        val appLog = Logger().apply {
            tag = "Launcher-p1"
            noStack = true
            enable = BuildConfig.DEBUG
        }

        val measureHelper = SimpleMeasureTime()

        fun getInstance() = BaseApp.getInstance() as LauncherApp

        private const val TAG_SHOW_APP = "App -> MainActivity"

        fun startMeasureLaunch() {
            if (debug()) {
                measureHelper.start(TAG_SHOW_APP, 0L)
            }
        }

        fun stopMeasureLaunch() {
            if (debug()) {
                measureHelper.stop(TAG_SHOW_APP)
                measureHelper.remove(TAG_SHOW_APP)
            }
        }
    }

    var appUpdate: (() -> Unit)? = null
        set(value) {
            field = value
            appItemTask.update = value
        }
    private val appItemTask by lazy { AppItemTask() }

    override fun onCreate() {
        super.onCreate()
        startMeasureLaunch()
        if (debug()) {
            measureHelper.measure("app init") { initNeed() }
        } else {
            initNeed()
        }
    }

    private fun initNeed() {
        DataHelper.init()
        AppStatusHelper.register(this)
        Watcher().watchMainLoop().strictMode().startFpsMonitor()
        //因为Executor中使用了协程且此时会进行初始化，放在子线程进行可以减少主线程执行时间(大概10ms)
        thread {
            Executor()
                .add(DelayInitTask())
                .add(appItemTask)
                .add(TestTask())
                .execute()
        }
    }

    override fun handleUncaught() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            appLog.log(e)
            e.printStackTrace()
            AppHelper.resetApp2Activity(this)
            Process.killProcess(Process.myPid())
        }
    }
}