@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.fragment.app.Fragment
import com.munch.lib.android.AppHelper

//<editor-fold desc="convert">
/**
 * dp转为px
 *
 *  实际与[Context.dp2Px]计算的值是一致的, 只是有些情况下不能使用这个, 比如xml预览时
 */
inline fun Number.dp2Px(): Float {
    return (this.toFloat() * AppHelper.resources.displayMetrics.density + 0.5f * if (this.toInt() >= 0) 1f else -1f)
}

inline fun Context.dp2Px(dp: Float): Float {
    return (dp * resources.displayMetrics.density + 0.5f * if (dp.toInt() >= 0) 1f else -1f)
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


inline fun Context.sp2Px(sp: Float): Float {
    return sp * resources.displayMetrics.scaledDensity + 0.5f
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
inline val Activity.statusBarHeight: Int
    get() = ViewCompat.getRootWindowInsets(contentView)?.getInsets(Type.statusBars())?.top ?: 0
inline val Fragment.statusBarHeight: Int
    get() = requireActivity().statusBarHeight


/**
 * 获取状态栏高度
 */
inline val statusBarHeightFromId: Int
    get() = "status_bar_height".asId2GetDimen() ?: 0

/**
 * 获取导航栏高度
 */
inline val navigationBarHeight: Int
    get() = "navigation_bar_height".asId2GetDimen() ?: 0

/**
 * 获取导航栏高度
 */
inline val Activity.navigationBarHeight: Int
    get() = ViewCompat.getRootWindowInsets(contentView)?.getInsets(Type.navigationBars())?.bottom
        ?: 0
inline val Fragment.navigationBarHeight: Int
    get() = requireActivity().navigationBarHeight

/**
 * 从主题中获取属性值
 */
fun <T> Context.getAttrArrayFromTheme(attrId: Int, get: TypedArray.() -> T): T {
    val typedArray = obtainStyledAttributes(intArrayOf(attrId))
    val value = get.invoke(typedArray)
    typedArray.recycle()
    return value
}

/**
 * 获取android.R.attr.colorPrimary当前被设置的颜色值
 */
@ColorInt
fun Context.getColorPrimary(@ColorInt defValue: Int = Color.WHITE): Int {
    return getAttrArrayFromTheme(android.R.attr.colorPrimary) { getColor(0, defValue) }
}

/**
 * 获取android.R.attr.selectableItemBackground的Drawable对象
 */
fun Context.getSelectableItemBackground(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackground) { getDrawable(0) }
}

/**
 * 获取android.R.attr.selectableItemBackgroundBorderless的Drawable对象
 */
fun Context.getSelectableItemBackgroundBorderless(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackgroundBorderless) { getDrawable(0) }
}
//</editor-fold>

/**
 *  在Activity中弹出TOAST
 */
inline fun toast(any: Any?) {
    any?.let { impInMain { Toast.makeText(AppHelper, it.toString(), Toast.LENGTH_SHORT).show() } }
}

//<editor-fold desc="ime">
inline fun View.hideIme() {
    val im = AppHelper.getSystemService(Context.INPUT_METHOD_SERVICE).to<InputMethodManager>()
    im.hideSoftInputFromWindow(windowToken, 0)
}

inline fun Activity.hideIme() =
    WindowCompat.getInsetsController(window, window.decorView).hide(Type.ime())

inline fun Fragment.hideIme() = requireActivity().hideIme()

inline fun View.showIme() {
    val im = AppHelper.getSystemService(Context.INPUT_METHOD_SERVICE).to<InputMethodManager>()
    im.showSoftInput(this, 0)
}

inline fun Activity.showIme() =
    WindowCompat.getInsetsController(window, window.decorView).show(Type.ime())

inline fun Fragment.showIme() = requireActivity().showIme()
//</editor-fold>

/**
 * 有些手机有剪切板权限
 */
fun copy2Clip(text: String): Boolean {
    val cm = AppHelper.getSystemService(Context.CLIPBOARD_SERVICE)
        ?.toOrNull<ClipboardManager>()
        ?: return false
    val clip = ClipData.newPlainText("text", text)
    cm.setPrimaryClip(clip)
    return true
}

/**
 * 有些手机需要有焦点才能获取
 */
fun getClip(): List<String>? {
    val cm = AppHelper.getSystemService(Context.CLIPBOARD_SERVICE)
        ?.toOrNull<ClipboardManager>()
        ?: return null
    if (!cm.hasPrimaryClip()) {
        return null
    }
    return catch {
        val clips = cm.primaryClip ?: return@catch null
        val count = clips.itemCount
        val list = ArrayList<String>(count)
        for (i in 0 until count) {
            list.add(clips.getItemAt(i).coerceToHtmlText(AppHelper))
        }
        return@catch list
    }
}