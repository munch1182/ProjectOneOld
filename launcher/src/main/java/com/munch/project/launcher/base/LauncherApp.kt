package com.munch.project.launcher.base

import android.os.Process
import com.munch.pre.lib.BuildConfig
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.AppStatusHelper
import com.munch.pre.lib.helper.measure.MeasureTimeHelper
import com.munch.pre.lib.helper.measure.SimpleMeasureTime
import com.munch.pre.lib.log.Logger
import com.munch.pre.lib.watcher.Watcher
import com.munch.project.launcher.item.AppItemTask

/**
 * Create by munch1182 on 2021/5/8 10:53.
 */
class LauncherApp : BaseApp() {

    companion object {

        val appLog = Logger().apply {
            tag = "Launcher-p1"
            enable = BuildConfig.DEBUG
        }

        val measureHelper = SimpleMeasureTime()

        fun getInstance() = BaseApp.getInstance() as LauncherApp
    }

    override fun onCreate() {
        super.onCreate()
        if (debug()) {
            measureHelper.measure("app init") { initNeed() }
        } else {
            initNeed()
        }
    }

    private fun initNeed() {
        AppStatusHelper.register(this)
        DataHelper.init()
        Watcher().watchMainLoop().strictMode()
        Executor().add(AppItemTask()).execute()
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