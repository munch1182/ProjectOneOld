package com.munch.lib.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2020/12/18 10:25.
 */
object PhoneHelper {

    fun getBrand(): String? = Build.BRAND

    fun getVersion() = Build.VERSION.SDK_INT

    /**
     * 分辨率
     */
    @Suppress("DEPRECATION")
    fun getScreenWidthHeightReal(context: Context = BaseApp.getInstance()): Pair<Int, Int>? {
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealSize(point)
        } else {
            val manager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return null
            manager.defaultDisplay.getRealSize(point)
        }
        return Pair(point.x, point.y)
    }

    /**
     * 可见区域，一般是高度的分辨率减去navigation高度
     */
    @Suppress("DEPRECATION")
    fun getScreenWidthHeight(context: Context = BaseApp.getInstance()): Pair<Int, Int>? {
        val manager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return null
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
        return Pair(width, height)
    }

    fun getStatusBarHeight(context: Context = BaseApp.getInstance()): Int? {
        return getResById(context, "status_bar_height")
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

    fun getNavigationBarHeight(context: Context = BaseApp.getInstance()): Int? {
        return getResById(context, "navigation_bar_height")
    }

}