package com.munch.test.app

import android.app.Application
import com.munch.lib.libnative.helper.AppHelper

/**
 * Created by Munch on 2019/8/24 11:02
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppHelper.init(this)
    }
}