package com.munch.lib.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.DrawableCompat.setTint

/**
 * Create by munch1182 on 2020/12/7 14:33.
 */
fun Context.startActivity(clazz: Class<out Activity>, bundle: Bundle? = null) {
    startActivity(
        Intent(this, clazz).apply {
            if (bundle != null) {
                putExtras(bundle)
            }
        })
}

fun Context.getBackIcon(): Drawable? {
    /*return if (Build.VERSION.SDK_INT >= 18) {
        val a = obtainStyledAttributes(
            null, intArrayOf(android.R.attr.homeAsUpIndicator),
            android.R.attr.actionBarSplitStyle, 0
        )
        val result = a.getDrawable(0)
        a.recycle()
        result
    } else {*/
    val a = obtainStyledAttributes(intArrayOf(android.R.attr.homeAsUpIndicator))
    val result = a.getDrawable(0)
    a.recycle()
    return result
    /*}*/
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.getBackIconWhite(): Drawable? {
    return getBackIcon()?.apply {
        setTint(this, Color.WHITE)
    }
}

fun Context.getListDivider(): Drawable? {
    val a = obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
    val divider = a.getDrawable(0)
    a.recycle()
    return divider
}

fun Context.dp2Px(dpVal: Float): Float {
    return dpVal * this.resources.displayMetrics.scaledDensity + 0.5f
}

fun Context.px2Dp(pxVal: Float): Float {
    return pxVal / this.resources.displayMetrics.scaledDensity + 0.5f
}