package com.munch.test.lib.pre.base

import com.munch.pre.lib.helper.BaseFastApp
import com.munch.test.lib.pre.switch.SwitchHelper

/**
 * Create by munch1182 on 2021/3/31 13:59.
 */
class TestApp : BaseFastApp() {

    override fun onCreate() {
        super.onCreate()
        DataHelper.init()
        SwitchHelper.INSTANCE.registerApp(this)
    }
}