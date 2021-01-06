package com.munch.lib.common

import android.app.Application
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2021/1/6 18:08.
 */
object RouterHelper {

    object Test {

        const val MAIN = "/test/main"
    }

    object TestSimple {

        const val MAIN = "/testsimple/main"
    }

    fun init(app: Application) {
        if (BaseApp.debugMode()) {
            ARouter.openDebug()
            ARouter.openLog()
        }
        ARouter.init(app)
    }
}

fun start2Component(target: String) {
    ARouter.getInstance().build(target).navigation()
}

fun start2Component(target: String, func: (Postcard) -> Unit) {
    ARouter.getInstance().build(target).apply { func.invoke(this) }.navigation()
}
