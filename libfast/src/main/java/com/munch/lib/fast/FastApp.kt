package com.munch.lib.fast

import android.app.ActivityManager
import android.app.Application
import android.os.StatFs
import com.munch.lib.app.AppHelper
import com.munch.lib.helper.PhoneHelper
import com.munch.lib.log.log

/**
 * 快速接入lib，可以直接使用或者继承[FastApp]，也可以使用[FastAppHelper.init]
 *
 * Create by munch1182 on 2021/8/5 15:41.
 */
open class FastApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FastAppHelper.init(this)
    }
}

object FastAppHelper {

    fun init(app: Application) {
        AppHelper.init(app)
        Thread.setDefaultUncaughtExceptionHandler { _, e -> log(e) }
    }

    fun collectPhoneInfo(): LinkedHashMap<String, String?> {
        val map = LinkedHashMap<String, String?>()
        PhoneHelper.apply {
            val memory = getMemoryInfo()
            val rom = getRom()
            val context = AppHelper.app
            val screenSize = getScreenSize(context)
            val realSize = "${screenSize?.width}/${screenSize?.height}"
            val screenWidthHeight = getScreenWidthHeight(context)
            val size = "${screenWidthHeight?.width}/${screenWidthHeight?.height}"
            map["brand"] = getBrand()
            map["model"] = getModel()
            map["product"] = getProduct()
            map["sdk version"] = getSDKVersion().toString()
            map["sdk release"] = getSDKRelease().toString()
            map["support abis"] = getAbis().joinToString()
            map["real_size"] = realSize
            map["size"] = size
            map["density"] = getDensity(context).toString()
            map["status bar height"] = "${getStatusBarHeight(context)}"
            map["action bar height"] = "${getActionBarSize(context)}"
            map["navigation bar height"] = "${getNavigationBarHeight(context)}"
            map["ram"] = memory?.formatString()
            map["rom"] = rom.formatString()
        }
        return map
    }

    private fun StatFs.formatString(): String {
        return "Rom:${availableBytes.toDouble()}/${totalBytes.toDouble()}"
    }

    private fun ActivityManager.MemoryInfo.formatString(): String {
        return "Ram:${availMem.toDouble()}/${totalMem.toDouble()},isLow:${this.lowMemory}"
    }
}