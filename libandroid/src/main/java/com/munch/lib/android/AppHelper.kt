package com.munch.lib.android

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import com.munch.lib.android.extend.ScopeContext
import com.munch.lib.android.helper.InfoHelper
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 1. 提供给所有可以使用Application上下文的地方一个App
 *    lib中也会直接调用AppHelper而省去Context的参数需求
 * - 已经使用AppInitializer注册, 因此无需注册
 *
 * 2. 提供一个App范围的CoroutineScope
 */
object AppHelper : ContextThemeWrapper(), ScopeContext {

    private val appJob = SupervisorJob()
    private val appJobName = CoroutineName("App")
    private lateinit var application: Application

    internal fun init(context: Application) {
        attachBaseContext(context)
        registerConfigurationChanged(context)
        application = context
    }

    override val coroutineContext: CoroutineContext = appJob + appJobName + Dispatchers.Default

    fun to() = application

    /**
     * 注册[Application.onConfigurationChanged]的回调
     */
    private fun registerConfigurationChanged(context: Application) {
        context.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(p0: Configuration) {
                InfoHelper.updateWhenChange()
            }

            override fun onLowMemory() {
            }
        })
    }
}