package com.munch.project.test

import com.munch.lib.common.CommonApp
import com.munch.project.test.switch.SwitchHelper

/**
 * Create munch1182 on 2020/12/7 14:44.
 */
class TestApp : CommonApp() {

    override fun onCreate() {
        super.onCreate()
        SwitchHelper.INSTANCE.registerApp(this)
    }
}