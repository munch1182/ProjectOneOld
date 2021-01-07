package com.munch.lib.common

import android.app.Activity
import android.app.Application
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2021/1/6 18:08.
 */
object RouterHelper {

    object Test {

        const val MAIN = "/test/main"

        const val PROVIDE_THEME = "/test/theme_provide"
    }

    object TestSimple {

        const val MAIN = "/testsimple/main"
    }

    fun inject(any: Any) {
        ARouter.getInstance().inject(any)
    }

    fun init(app: Application) {
        if (BaseApp.debugMode()) {
            ARouter.openDebug()
            ARouter.openLog()
        }
        ARouter.init(app)
    }

    fun <T : IProvider> getService(service: Class<T>): T? {
        return ARouter.getInstance().navigation(service)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IProvider> getService(name: String): T? {
        return ARouter.getInstance().build(name).navigation() as? T?
    }
}

fun start2Component(target: String) {
    ARouter.getInstance().build(target).navigation()
}

fun start2Component4Result(context: Activity, target: String, requestCode: Int) {
    ARouter.getInstance().build(target).navigation(context, requestCode)
}

fun start2Component(target: String, func: (Postcard) -> Unit) {
    ARouter.getInstance().build(target).apply { func.invoke(this) }.navigation()
}
