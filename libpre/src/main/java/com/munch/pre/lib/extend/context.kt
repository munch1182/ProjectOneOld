package com.munch.pre.lib.extend

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * Create by munch1182 on 2021/3/31 11:33.
 */
inline fun <reified T> Context.getService(name: String): T? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        this.getSystemService(T::class.java)
    } else {
        this.getSystemService(name) as? T
    }
}

fun Context.getAttrFromTheme(attrId: Int): TypedValue {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrId, typedValue, true)
    return typedValue
}

fun Context.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun Context.startActivity(target: Class<out Activity>) {
    startActivity(Intent(this, target))
}

fun Context.startActivity(target: Class<out Activity>, bundle: Bundle) {
    startActivity(Intent(this, target).apply {
        putExtras(bundle)
    })
}