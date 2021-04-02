package com.munch.pre.lib.helper

import com.munch.pre.lib.base.BaseApp

/**
 * Create by munch1182 on 2021/4/2 15:49.
 */
open class BaseFastApp : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        AppStatusHelper.register(this)
    }
}