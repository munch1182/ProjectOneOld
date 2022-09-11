@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.annotation.ColorInt
import com.munch.lib.android.AppHelper

//<editor-fold desc="convert">
/**
 * dp转为px
 */
inline fun Number.dp2Px(): Float {
    return (this.toFloat() * AppHelper.resources.displayMetrics.density + 0.5f * if (this.toInt() >= 0) 1f else -1f)
}

/**
 * dp转为px并转为int值
 */
inline fun Number.dp2Px2Int() = dp2Px().toInt()

/**
 * sp转为px
 */
inline fun Number.sp2Px(): Float {
    return this.toFloat() * AppHelper.resources.displayMetrics.scaledDensity + 0.5f
}

/**
 * sp转为px并转为int值
 */
inline fun Number.sp2Px2Int() = sp2Px().toInt()
//</editor-fold>

//<editor-fold desc="res">
/**
 * 获取android包下资源值
 */
fun String.asId2GetDimen(): Int? {
    val id = AppHelper.resources.getIdentifier(this, "dimen", "android")
    return catch { AppHelper.resources.getDimensionPixelSize(id) }
}

/**
 * 获取状态栏高度
 */
inline val statusBarHeight: Int
    get() = "status_bar_height".asId2GetDimen() ?: 0

/**
 * 获取导航栏高度
 */
inline val navigationBarHeight: Int
    get() = "navigation_bar_height".asId2GetDimen() ?: 0

/**
 * 从主题中获取属性值
 */
fun <T> getAttrArrayFromTheme(attrId: Int, get: TypedArray.() -> T): T {
    val typedArray = AppHelper.obtainStyledAttributes(intArrayOf(attrId))
    val value = get.invoke(typedArray)
    typedArray.recycle()
    return value
}

/**
 * 获取android.R.attr.colorPrimary当前被设置的颜色值
 */
@ColorInt
fun getColorPrimary(@ColorInt defValue: Int = Color.WHITE): Int {
    return getAttrArrayFromTheme(android.R.attr.colorPrimary) { getColor(0, defValue) }
}

/**
 * 获取android.R.attr.selectableItemBackground的Drawable对象
 */
fun getSelectableItemBackground(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackground) { getDrawable(0) }
}

/**
 * 获取android.R.attr.selectableItemBackgroundBorderless的Drawable对象
 */
fun getSelectableItemBackgroundBorderless(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackgroundBorderless) { getDrawable(0) }
}
//</editor-fold>

/**
 *  在Activity中弹出TOAST
 */
inline fun toast(any: Any?) {
    any?.let { runInMain { Toast.makeText(AppHelper, it.toString(), Toast.LENGTH_SHORT).show() } }
}