package com.munch.lib.app

import android.app.Application
import com.munch.lib.app.AppHelper.app
import com.munch.lib.app.AppHelper.init

/**
 * lib用于获取和管理application的方法类
 *
 * 如果使用到此类或者依赖此类的类或者方法，则必须在之前调用[init]方法进行初始化
 *
 * @see init
 * @see app
 *
 * Create by munch1182 on 2021/8/5 13:56.
 */
object AppHelper {

    private lateinit var application: Application

    val app: Application
        get() = application

    fun init(app: Application) {
        application = app
    }

}