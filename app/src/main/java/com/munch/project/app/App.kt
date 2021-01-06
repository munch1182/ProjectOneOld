package com.munch.project.app

import com.munch.lib.common.CommonApp
import com.munch.lib.common.RouterHelper

/**
 * Create by munch1182 on 2021/1/6 18:11.
 */
class App : CommonApp() {

    override fun onCreate() {
        super.onCreate()
        RouterHelper.init(this)
    }
}