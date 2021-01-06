package com.munch.project.app

import com.munch.lib.common.CommonApp
import dagger.hilt.android.HiltAndroidApp

/**
 * 如果不用插件注册的话，当分开打包时此处会显示找不到类
 * 但这是正常的，不影响正常使用
 *
 * Create by munch1182 on 2021/1/6 18:11.
 */
@HiltAndroidApp
class App : CommonApp() {

    override fun onCreate() {
        super.onCreate()
        /*SwitchHelper.INSTANCE.registerApp(this)*/
    }
}