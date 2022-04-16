package com.munch.lib.fast

import android.app.Application
import com.munch.lib.AppHelper
import com.munch.lib.helper.ActivityHelper

/**
 * Create by munch1182 on 2022/4/15 21:20.
 */
class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppHelper.init(this)
        ActivityHelper.register(this)
    }
}