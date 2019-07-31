package com.munhc.lib.libnative.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat

/**
 * Created by Munch on 2019/7/16 10:46
 */
object ResHelper {

    private const val NAME_STATUS_BAR_HEIGHT = "status_bar_height"
    private const val NAME_NAVIGATION_BAR_HEIGHT = "navigation_bar_height"

    @JvmStatic
    @JvmOverloads
    fun getDrawable(context: Context = AppHelper.getContext(), @DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    @JvmOverloads
    @JvmStatic
    fun getDimen(context: Context = AppHelper.getContext(), @DimenRes resId: Int): Float {
        return context.resources.getDimension(resId)
    }

    @JvmOverloads
    @JvmStatic
    fun getSystemDrawable(context: Context = AppHelper.getContext(), @DrawableRes resId: Int): Drawable? {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(resId, value, true)) {
            val attributes = context.theme.obtainStyledAttributes(intArrayOf(resId))
            val drawable = attributes.getDrawable(0)
            attributes.recycle()
            drawable
        } else null
    }

    @JvmOverloads
    @JvmStatic
    fun getString(context: Context = AppHelper.getContext(), @StringRes resId: Int): String {
        return context.getString(resId)
    }

    @JvmOverloads
    @JvmStatic
    @ColorInt
    fun getColor(context: Context = AppHelper.getContext(), @ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    @JvmStatic
    @ColorInt
    fun getColor(resId: String): Int {
        return Color.parseColor(resId)
    }

    @JvmOverloads
    @JvmStatic
    fun dp2Px(context: Context = AppHelper.getContext(), dpVal: Float): Float {
        return dpVal * context.resources.displayMetrics.scaledDensity + 0.5f
    }

    @JvmOverloads
    @JvmStatic
    fun px2Dp(context: Context = AppHelper.getContext(), pxVal: Float): Float {
        return pxVal / context.resources.displayMetrics.scaledDensity + 0.5f
    }

    @JvmStatic
    fun str2Color(colorStr: String): Int {
        return Color.parseColor(colorStr)
    }

    @JvmOverloads
    @JvmStatic
    fun getStringArray(context: Context = AppHelper.getContext(), @ArrayRes resId: Int): Array<String> {
        return ArrayResHelper.getStringArray(context, resId)
    }

    @JvmOverloads
    @JvmStatic
    fun getResIdArray(context: Context = AppHelper.getContext(), @ArrayRes resId: Int): IntArray {
        return ArrayResHelper.getResIdArray(context, resId)
    }

    @JvmOverloads
    @JvmStatic
    fun getIntArray(context: Context = AppHelper.getContext(), @ArrayRes resId: Int): IntArray {
        return ArrayResHelper.getIntArray(context, resId)
    }

    @JvmOverloads
    @JvmStatic
    fun getStatusBarHeight(context: Context = AppHelper.getContext()): Int {
        val resources = context.resources
        val id = resources.getIdentifier(NAME_STATUS_BAR_HEIGHT, "dimen", "android")
        return resources.getDimensionPixelSize(id)
    }

    /**
     * 若无，则返回0
     */
    @JvmOverloads
    @JvmStatic
    fun getNavigationBarHeight(context: Context = AppHelper.getContext()): Int {
        val res = context.resources
        val resourceId = res.getIdentifier(NAME_NAVIGATION_BAR_HEIGHT, "dimen", "android")
        return if (resourceId != 0) {
            res.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 返回 -1 则未获取到
     *
     * @see android.content.res.Resources.Theme.resolveAttribute
     */
    @JvmOverloads
    @JvmStatic
    fun getActionBarSize(context: Context = AppHelper.getContext()): Int {
        val typedValue = TypedValue()
        val actionBarSize: Int
        actionBarSize = if (context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
        } else {
            -1
        }
        return actionBarSize
    }

    private object ArrayResHelper {

        fun getStringArray(context: Context = AppHelper.getContext(), @ArrayRes arrayResId: Int): Array<String> {
            return context.resources.getStringArray(arrayResId)
        }

        fun getResIdArray(context: Context = AppHelper.getContext(), @ArrayRes arrayResId: Int): IntArray {
            val typedArray = context.resources.obtainTypedArray(arrayResId)
            val array = IntArray(typedArray.length()) {
                typedArray.getResourceId(it, 0)
            }
            typedArray.recycle()
            return array
        }

        fun getIntArray(context: Context = AppHelper.getContext(), @ArrayRes arrayResId: Int): IntArray {
            return context.resources.getIntArray(arrayResId)
        }
    }
}