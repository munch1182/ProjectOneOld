@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import com.munch.lib.BaseApp

/**
 * 此类的数据可以缓存
 *
 * 返回值自行需要判断是否为-1
 *
 * Create by munch1182 on 2020/12/18 10:25.
 */
object PhoneHelper {

    private var screenWidthHeightReal: Size? = null
    private var screenWidthHeight: Size? = null
    private var statusBarHeight = -1
    private var navigationBarHeight = -1
    private var actionBarSize = -1

    fun getBrand(): String? = Build.BRAND

    fun getVersion() = Build.VERSION.SDK_INT

    /**
     * 分辨率
     */
    @Suppress("DEPRECATION")
    fun getScreenWidthHeightReal(context: Context = BaseApp.getInstance()): Size? {
        if (screenWidthHeightReal != null) {
            return screenWidthHeightReal
        }
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealSize(point)
        } else {
            val manager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return null
            manager.defaultDisplay.getRealSize(point)
        }
        screenWidthHeightReal = Size(point.x, point.y)
        return screenWidthHeightReal
    }

    /**
     * 可见区域，一般是高度减去navigation高度
     */
    @Suppress("DEPRECATION")
    fun getScreenWidthHeight(context: Context = BaseApp.getInstance()): Size? {
        if (screenWidthHeight != null) {
            return screenWidthHeight
        }
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
        screenWidthHeight = Size(width, height)
        return screenWidthHeight
    }

    fun getStatusBarHeight(context: Context = BaseApp.getInstance()): Int {
        if (statusBarHeight == -1) {
            statusBarHeight = getResById(context, "status_bar_height") ?: -1
        }
        return statusBarHeight
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

    fun getNavigationBarHeight(context: Context = BaseApp.getInstance()): Int {
        if (navigationBarHeight == -1) {
            navigationBarHeight = getResById(context, "navigation_bar_height") ?: -1
        }
        return navigationBarHeight
    }

    fun getActionBarSize(context: Context = BaseApp.getInstance()): Int {
        if (actionBarSize == -1) {
            actionBarSize = context.getActionBarSize()
        }
        return actionBarSize
    }

}