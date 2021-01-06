package com.munch.project.app

import com.munch.lib.common.CommonApp
import com.munch.lib.common.RouterHelper
import com.munch.project.test.switch.SwitchHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by munch1182 on 2021/1/6 18:11.
 */
@HiltAndroidApp
class App : CommonApp() {

    override fun onCreate() {
        super.onCreate()
        RouterHelper.init(this)
        SwitchHelper.INSTANCE.registerApp(this)
    }
}