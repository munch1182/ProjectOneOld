package com.munch.lib.base

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt

/**
 * Create by munch1182 on 2021/8/6 17:20.
 */

fun Context.startActivity(target: Class<*>, bundle: Bundle? = null) =
    startActivity(Intent(this, target).apply {
        val extras = bundle ?: return@apply
        putExtras(extras)
    })

fun Context.dp2Px(dp: Float): Float {
    return (dp * resources.displayMetrics.density + 0.5f * if (dp >= 0) 1f else -1f)
}

fun Context.sp2Px(sp: Float): Float {
    return sp * resources.displayMetrics.scaledDensity + 0.5f
}

fun Context.getAttrFromTheme(attrId: Int): TypedValue {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrId, typedValue, true)
    return typedValue
}

@ColorInt
fun Context.getColorPrimary(): Int {
    return getAttrArrayFromTheme(android.R.attr.colorPrimary) {
        getColor(0, Color.WHITE)
    }
}

fun Context.getSelectableItemBackground(): Drawable? {
    return getAttrArrayFromTheme(android.R.attr.selectableItemBackground) {
        getDrawable(0)
    }
}

fun <T> Context.getAttrArrayFromTheme(attrId: Int, get: TypedArray.() -> T): T {
    val typedArray = theme.obtainStyledAttributes(intArrayOf(attrId))
    val value = get.invoke(typedArray)
    typedArray.recycle()
    return value
}

fun Context.getStrArray(@ArrayRes arrayId: Int): Array<out String>? {
    return try {
        resources.getStringArray(arrayId)
    } catch (e: Resources.NotFoundException) {
        null
    }
}

fun Context.getIntArray(@ArrayRes arrayId: Int): IntArray? {
    return try {
        resources.getIntArray(arrayId)
    } catch (e: Resources.NotFoundException) {
        null
    }
}
