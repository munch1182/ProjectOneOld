package com.munch.lib.common

import android.app.Activity
import android.app.Application
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2021/1/6 18:08.
 */
object RouterHelper {

    object App {
        const val MAIN = "/app/main"

        const val KEY_RESTART = "key_restart"
    }

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

class LostNavigationCallback(private val onLost: ((postcard: Postcard?) -> Unit)? = null) :
    NavigationCallback {

    override fun onFound(postcard: Postcard?) {
    }

    override fun onLost(postcard: Postcard?) {
        onLost?.invoke(postcard)
    }

    override fun onArrival(postcard: Postcard?) {
    }

    override fun onInterrupt(postcard: Postcard?) {
    }

}

fun Activity.start2ComponentCallbackLost(
    target: String,
    osLost: (postcard: Postcard?) -> Unit
) {
    ARouter.getInstance().build(target).navigation(this, LostNavigationCallback(osLost))
}

fun Activity.start2ComponentCallbackLost(
    target: String,
    func: (Postcard) -> Unit,
    osLost: (postcard: Postcard?) -> Unit
) {
    ARouter.getInstance().build(target).apply { func.invoke(this) }
        .navigation(this, LostNavigationCallback(osLost))
}

fun start2Component(target: String) {
    ARouter.getInstance().build(target).navigation()
}

fun Activity.start2Component4Result(target: String, requestCode: Int) {
    ARouter.getInstance().build(target).navigation(this, requestCode)
}

inline fun start2Component(target: String, func: (Postcard) -> Unit) {
    ARouter.getInstance().build(target).apply { func.invoke(this) }.navigation()
}
