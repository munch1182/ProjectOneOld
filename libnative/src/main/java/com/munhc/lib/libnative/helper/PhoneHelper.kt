package com.munhc.lib.libnative.helper

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager

/**
 * 缓存手机固定数据
 * Created by Munch on 2019/7/26 9:29
 */
class PhoneHelper private constructor() {

    companion object {
        @JvmStatic
        fun getInstance() = Singleton.INSTANCE

        @JvmStatic
        fun getStatusBarHeight() = getInstance().getStatusBar()

        @JvmStatic
        fun getScreenWidth() = getInstance().getScreenWidth()

        @JvmStatic
        fun getScreenHeight() = getInstance().getScreenHeight()

        @JvmStatic
        fun getActionBarHeight() = getInstance().getActionBar()

        private const val NOT_GET_VAL: Int = -1
    }

    private fun getActionBar(): Int {
        if (isUnGet(actionBarSize)) {
            actionBarSize = ResHelper.getActionBarSize(AppHelper.getContext())
        }
        return actionBarSize
    }

    private object Singleton {
        val INSTANCE = PhoneHelper()
    }

    private var statusBarViewHeight: Int = NOT_GET_VAL
    private var point: Point? = null
    private var actionBarSize: Int = NOT_GET_VAL

    fun getScreenWidth(context: Context = AppHelper.getContext()): Int = getScreenPoint(context).x
    fun getScreenHeight(context: Context = AppHelper.getContext()): Int = getScreenPoint(context).y

    fun getScreenPoint(context: Context = AppHelper.getContext()): Point {
        if (null == point) {
            point = Point()
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                wm.defaultDisplay.getRealSize(point)
            } else {
                wm.defaultDisplay.getSize(point)
            }
        }
        return point!!
    }

    fun getStatusBar(): Int {
        if (isUnGet(statusBarViewHeight)) {
            statusBarViewHeight = ResHelper.getStatusBarHeight(AppHelper.getContext())
        }
        return statusBarViewHeight
    }

    private fun isUnGet(value: Int): Boolean = value == NOT_GET_VAL

}