package com.munch.project.launcher.base

import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.AppStatusHelper
import com.munch.pre.lib.log.Logger
import com.munch.pre.lib.watcher.Watcher
import kotlin.system.measureTimeMillis

/**
 * Create by munch1182 on 2021/5/8 10:53.
 */
class LauncherApp : BaseApp() {

    companion object {

        val appLog = Logger().apply {
            tag = "Launcher-p1"
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (debug()) {
            appLog.log("app init: ${measureTimeMillis { initNeed() }} ms")
        } else {
            initNeed()
        }
    }

    private fun initNeed() {
        AppStatusHelper.register(this)
        DataHelper.init()
        Watcher().watchMainLoop().strictMode()
    }
}