package com.munch.project.one

import android.app.Application
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.log.log

/**
 * Create by munch1182 on 2022/4/15 21:20.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ActivityHelper.register(this)
        ActivityHelper.isForegroundLiveData.observeForever {
            log(it)
        }
    }
}