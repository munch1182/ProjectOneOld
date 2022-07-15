@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlin.reflect.KClass

/**
 * Created by munch1182 on 2022/4/3 17:50.
 */

inline fun Context.startActivity(target: KClass<out Activity>) =
    startActivity(Intent(this, target.java))

inline fun Context.startActivity(target: KClass<out Activity>, bundle: Bundle) =
    startActivity(Intent(this, target.java).apply {
        putExtras(bundle)
    })

inline fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.getDimenById(name: String): Int? {
    val id = resources.getIdentifier(name, "dimen", "android")
    return catch { resources.getDimensionPixelSize(id) }
}

inline fun Context.getStatusBarHeight() = getDimenById("status_bar_height")
inline fun Context.getNavigationBarHeight() = getDimenById("navigation_bar_height")

inline fun Context.getScreenSize(full: Boolean = false) =
    (getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.getScreenSize(full)

inline fun Context.isPermissionGranted(permission: String) =
    PermissionChecker.checkSelfPermission(
        this,
        permission
    ) == PermissionChecker.PERMISSION_GRANTED

fun Context.dp2Px(dp: Float): Float {
    return (dp * resources.displayMetrics.density + 0.5f * if (dp >= 0) 1f else -1f)
}

fun Context.sp2Px(sp: Float): Float {
    return sp * resources.displayMetrics.scaledDensity + 0.5f
}

fun Context.getAttrFromTheme(attrId: Int, theme: Resources.Theme = this.theme): TypedValue? {
    val typedValue = TypedValue()
    if (!theme.resolveAttribute(attrId, typedValue, true)) {
        return null
    }
    return typedValue
}

@ColorInt
fun Context.getColorPrimary(): Int {
    return getAttrArrayFromTheme(android.R.attr.colorPrimary) { getColor(0, Color.WHITE) }
}

fun Context.getSelectableItemBackground(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackground) { getDrawable(0) }
}

fun Context.getSelectableItemBackgroundBorderless(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackgroundBorderless) { getDrawable(0) }
}

fun <T> Context.getAttrArrayFromTheme(attrId: Int, get: TypedArray.() -> T): T {
    val typedArray = theme.obtainStyledAttributes(intArrayOf(attrId))
    val value = get.invoke(typedArray)
    typedArray.recycle()
    return value
}

/**
 * 使用string-array数组时，使用此方法获取数组
 */
inline fun Context.getStrArray(@ArrayRes arrayId: Int): Array<out String>? {
    return catch { resources.getStringArray(arrayId) }
}

/**
 * 使用integer-array数组时，使用此方法获取数组
 */
inline fun Context.getIntArray(@ArrayRes arrayId: Int): IntArray? {
    return catch { resources.getIntArray(arrayId) }
}

/**
 * 使用array数组且item为资源id时，使用此方法获取id数组
 */
fun Context.getIdsArray(@ArrayRes arrayId: Int): IntArray {
    val ota = resources.obtainTypedArray(arrayId)
    val size = ota.length()
    val array = IntArray(size) { ota.getResourceId(it, 0) }
    ota.recycle()
    return array
}

fun Context.putStr2Clip(content: CharSequence): Boolean {
    val cm =
        getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
    val mClipData = ClipData.newPlainText(content, content)
    cm.setPrimaryClip(mClipData)
    return true
}

fun Context.shareText(content: CharSequence) {
    startActivity(Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

fun Context.shareUri(uri: Uri) {
    startActivity(Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = "*/*"
    })
}

fun Context.getNameVersion(): Pair<String, Long> {
    return packageManager.getPackageInfo(
        packageName,
        PackageManager.GET_CONFIGURATIONS
    ).let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            it.versionName to it.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            it.versionName to it.versionCode.toLong()
        }
    }
}

fun Context.isScreenOn(): Boolean? {
    return (getSystemService(Context.POWER_SERVICE) as? PowerManager?)?.isInteractive
}