package com.munch.lib.fast.view

import android.app.Application
import com.munch.lib.android.AppHelper
import com.munch.lib.android.helper.ThreadHelper
import com.munch.lib.android.log.Logger
import com.munch.lib.fast.view.base.ActivityHelper
import com.munch.lib.fast.view.record.RecordHelper
import kotlinx.coroutines.launch

open class FastApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ActivityHelper.register()
        ThreadHelper.setCaught(Uncaught()).caughtThreadException()
        Logger.setLogListener { tag, content ->
            if (tag == "loglog") return@setLogListener // 不记录默认日志
            AppHelper.launch { RecordHelper.log(tag, content) }
        }
    }
}