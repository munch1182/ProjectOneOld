package com.munch.lib.android

import android.app.Application
import android.view.ContextThemeWrapper
import com.munch.lib.android.extend.ScopeContext
import kotlinx.coroutines.*
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

    internal fun init(context: Application) {
        attachBaseContext(context)
    }

    override val coroutineContext: CoroutineContext = appJob + appJobName + Dispatchers.Default
}