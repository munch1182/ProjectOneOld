package com.munch.lib.fast.view

import android.app.Application
import com.munch.lib.android.helper.ThreadHelper
import com.munch.lib.fast.view.base.ActivityHelper

open class FastApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ActivityHelper.register()
        ThreadHelper.setCaught(Uncaught()).caughtThreadException()
    }
}