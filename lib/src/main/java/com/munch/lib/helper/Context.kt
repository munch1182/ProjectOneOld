package com.munch.lib.helper

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
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
    val a = obtainStyledAttributes(intArrayOf(android.R.attr.homeAsUpIndicator))
    val result = a.getDrawable(0)
    a.recycle()
    return result
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

/**
 * 虽然方法已废弃，但仍会返回自己的服务
 */
@Suppress("DEPRECATION")
fun Context.isServiceRunning(service: Class<out Service>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
    val services = manager.getRunningServices(Int.MAX_VALUE)
    services.forEach {
        if (it.service.className == service.name) {
            return true
        }
    }
    return false
}

/**
 * 即使声明不同进程的服务也会被stop
 */
@Suppress("DEPRECATION")
fun Context.stopAllService(): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
    val services = manager.getRunningServices(Int.MAX_VALUE)
    services.forEach {
        stopService(Intent(this, Class.forName(it.service.className)))
    }
    return false
}