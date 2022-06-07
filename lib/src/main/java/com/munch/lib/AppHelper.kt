package com.munch.lib

import android.app.Application
import android.content.Context
import com.munch.lib.extend.icontext.IContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 此类作为context的存储类，赋值后可供本库中的其它类和函数提供context，而无需再传参
 *
 * 同时作为app的全局scope
 *
 * Create by munch1182 on 2022/3/30 19:15.
 */
object AppHelper : CoroutineScope, IContext {

    private var application: Application? = null
    private var job = SupervisorJob().apply { cancel() }

    val appNullable: Application?
        get() = application

    val app: Application
        get() = application ?: throw NullPointerException("must call init")

    fun init(application: Application) {
        AppHelper.application = application
        job = SupervisorJob()
    }

    override val ctx: Context
        get() = app

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job
}