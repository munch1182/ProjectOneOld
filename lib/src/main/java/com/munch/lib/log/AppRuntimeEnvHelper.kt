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
            put("packageName") { AppHelper.app.packageName }
            put("version") { AppHelper.app.getNameVersion().let { "${it.first}(${it.second})" } }
            put("release") { (!BuildConfig.DEBUG).toString() }
            put("in foreground") { AppForegroundHelper.isInForeground.toString() }
            put("net") { NetHelper.getInstance().currentNet?.toString() ?: "unknown" }
            put("screen light") { pw?.isInteractive.toString() }
            put("IDLE mode") { pw?.isDeviceIdleMode.toString() }
            put("power save mode") { pw?.isPowerSaveMode.toString() }
        }
    }

}