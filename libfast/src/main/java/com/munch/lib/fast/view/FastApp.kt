package com.munch.lib.fast.view

import android.app.Application
import com.munch.lib.fast.view.base.ActivityHelper

class FastApp : Application() {

    override fun onCreate() {
        super.onCreate()
        //永远持有一个ActivityHelper触发监听
        ActivityHelper.register()
    }
}