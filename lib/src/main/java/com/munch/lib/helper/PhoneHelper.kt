package com.munch.lib.helper

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.WindowManager
import com.munch.lib.app.AppHelper

/**
 * 获取手机相关的参数的方法
 *
 * Create by munch1182 on 2021/8/5 15:01.
 */
object PhoneHelper {

    /**
     * 获取品牌
     */
    fun getBrand(): String? = Build.BRAND

    /**
     * 获取产品名
     */
    fun getModel(): String? = Build.MODEL

    /**
     * 获取产品全名
     */
    fun getProduct(): String? = Build.PRODUCT

    /**
     * 获取SDK版本，如27
     */
    fun getSDKVersion() = Build.VERSION.SDK_INT

    /**
     * 获取android版本名,如8.1
     */
    fun getSDKRelease() = Build.VERSION.RELEASE

    /**
     * 支持的架构
     */
    fun getAbis(): Array<String> = Build.SUPPORTED_ABIS

    /**
     * 获取内存信息
     */
    fun getMemoryInfo(context: Context = AppHelper.app): ActivityManager.MemoryInfo? {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return null
        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        return info
    }

    fun getRom(): StatFs {
        return StatFs(Environment.getDataDirectory().path)
    }

    /**
     * 获取手机屏幕密度和dpi
     */
    fun getDensity(context: Context = AppHelper.app): Pair<Float, Int> {
        val dm = context.resources.displayMetrics
        return dm.density to dm.densityDpi
    }

    /**
     * 获取分辨率
     */
    @Suppress("DEPRECATION")
    fun getScreenSize(context: Context = AppHelper.app): Size? {
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealSize(point)
        } else {
            val manager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                ?: return null
            manager.defaultDisplay.getRealSize(point)
        }
        return Size(point.x, point.y)
    }

    /**
     * 获取可见区域，一般是有底部虚拟导航栏的屏幕的高度减去导航栏高度
     */
    @Suppress("DEPRECATION")
    fun getScreenWidthHeight(context: Context = AppHelper.app): Size? {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            ?: return null
        val width: Int
        val height: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = manager.currentWindowMetrics.bounds
            width = bounds.width()
            height = bounds.height()
        } else {
            val metrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(metrics)
            width = metrics.widthPixels
            height = metrics.heightPixels
        }
        return Size(width, height)
    }

    /**
     * 获取状态栏高度(px)，获取失败则为null
     */
    fun getStatusBarHeight(context: Context = AppHelper.app) =
        getResById(context, "status_bar_height")

    /**
     * 获取底部导航栏高度(px)，获取失败则为null
     */
    fun getNavigationBarHeight(context: Context = AppHelper.app) =
        getResById(context, "navigation_bar_height")

    /**
     * 获取导航栏高度(px)，获取失败则为null
     */
    fun getActionBarSize(context: Context = AppHelper.app): Int? {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(
            typedValue.data, context.resources.displayMetrics
        ).takeIf { it > -1 }
    }

    private fun getResById(context: Context, name: String): Int? {
        val resources = context.resources
        val id = resources.getIdentifier(name, "dimen", "android")
        return try {
            resources.getDimensionPixelSize(id)
        } catch (e: Resources.NotFoundException) {
            null
        }
    }
}