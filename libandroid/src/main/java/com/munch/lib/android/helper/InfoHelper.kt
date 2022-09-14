package com.munch.lib.android.helper

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.*
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.os.ConfigurationCompat
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.extend.navigationBarHeight
import com.munch.lib.android.extend.statusBarHeight
import com.munch.lib.android.extend.to
import java.util.*

object InfoHelper {

    //系统定制商: meizu
    val brand: String = BRAND

    //手机型号: 16X
    val model: String = MODEL

    //版本: 10
    val phoneAndroidVersion: String = VERSION.RELEASE

    //版本号: 29
    val phoneAndroidVersionCode: String = VERSION.SDK_INT.toString()

    //显示的版本: Flyme 9.21.11.18 bate
    val phomeVersion: String = DISPLAY

    //产品型号: meizu_16X_CN
    val product: String = PRODUCT

    //硬件类型: qcom
    val hardware: String = HARDWARE

    //开发代号: REL
    val condeName by lazy { VERSION.CODENAME }

    //rom制造商: Meizu
    val rom by lazy { MANUFACTURER }

    //支持的abi
    val abis by lazy { SUPPORTED_ABIS }

    private val versionInfo by lazy {
        val info = AppHelper.packageManager.getPackageInfo(
            AppHelper.packageName,
            PackageManager.GET_META_DATA
        )
        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            info.longVersionCode to info.versionName
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong() to info.versionName
        }
    }

    val appVersionCode: Long
        get() = versionInfo.first
    val appVersionName: String
        get() = versionInfo.second

    private val screenInfo: Size by lazy {
        AppHelper.getSystemService(Context.WINDOW_SERVICE)
            .to<WindowManager>().let { wm ->
                if (VERSION.SDK_INT >= VERSION_CODES.R) {
                    wm.currentWindowMetrics.bounds.let {
                        Size(it.width(), it.height())
                    }
                } else {
                    DisplayMetrics().let {
                        @Suppress("DEPRECATION")
                        wm.defaultDisplay.getRealMetrics(it)
                        Size(it.widthPixels, it.heightPixels)
                    }
                }
            }
    }

    private val windowInfo: Size by lazy {
        AppHelper.getSystemService(Context.WINDOW_SERVICE)
            .to<WindowManager>().let { wm ->
                if (VERSION.SDK_INT >= VERSION_CODES.R) {
                    wm.currentWindowMetrics.let {
                        val mask =
                            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
                        val insets = it.windowInsets.getInsetsIgnoringVisibility(mask)
                        val bd = it.bounds
                        Size(
                            (bd.width() - insets.right - insets.left),
                            (bd.height() - insets.top - insets.bottom)
                        )
                    }
                } else {
                    DisplayMetrics().let {
                        @Suppress("DEPRECATION")
                        wm.defaultDisplay.getMetrics(it)
                        Size(it.widthPixels, it.heightPixels)
                    }
                }
            }
    }

    val screenWidth: Int
        get() = screenInfo.width

    val screenHeight: Int
        get() = screenInfo.height

    val screenRealWidth: Int
        get() = windowInfo.width

    val screenRealHeight
        get() = windowInfo.height

    val density: Float
        get() = AppHelper.resources.displayMetrics.density
    val scaledDensity: Float
        get() = AppHelper.resources.displayMetrics.scaledDensity

    private var loc: Locale =
        ConfigurationCompat.getLocales(AppHelper.resources.configuration).get(0)
            ?: Locale.getDefault()

    // 获取当前的location
    val location: Locale
        get() = loc

    val language: String
        get() = loc.language

    val phoneDesc by lazy { "$brand  $model  $phomeVersion  android$phoneAndroidVersion  version$phoneAndroidVersionCode  ${abis.joinToString()}" }

    val windowDesc by lazy { "${screenWidth}x${screenHeight} status($statusBarHeight) nav($navigationBarHeight) density(${density}) scaledDensity($scaledDensity)" }

    val appDesc by lazy { "$appVersionName $appVersionCode" }

    /**
     * 当某些情况下(如配置更改)需要更新缓存时, 调用此方法
     */
    internal fun updateWhenChange() {
        loc = ConfigurationCompat.getLocales(AppHelper.resources.configuration).get(0)
            ?: Locale.getDefault()
    }
}