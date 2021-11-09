package com.munch.lib.log

import android.content.Context
import android.os.PowerManager
import android.util.ArrayMap
import com.munch.lib.BuildConfig
import com.munch.lib.app.AppForegroundHelper
import com.munch.lib.app.AppHelper
import com.munch.lib.base.getNameVersion
import com.munch.lib.helper.net.NetHelper

/**
 * Create by munch1182 on 2021/10/28 15:26.
 */
object AppRuntimeEnvHelper {

    val env by lazy {
        ArrayMap<String, () -> String>().apply {
            val pw = (AppHelper.app.getSystemService(Context.POWER_SERVICE) as? PowerManager)
            put("包名") { AppHelper.app.packageName }
            put("版本") { AppHelper.app.getNameVersion().let { "${it.first}(${it.second})" } }
            put("是否是正式版") { (!BuildConfig.DEBUG).toString() }
            put("是否在前台") { AppForegroundHelper.isInForeground.toString() }
            put("当前网络") { NetHelper.getInstance().currentNet?.toString() ?: "unknown" }
            put("是否亮屏") { pw?.isInteractive.toString() }
            put("IDLE模式") { pw?.isDeviceIdleMode.toString() }
            put("省电模式") { pw?.isPowerSaveMode.toString() }
        }
    }

}