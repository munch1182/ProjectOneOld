@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.content.Context
import android.content.res.Resources
import com.munch.lib.android.AppHelper

/**
 * Created by munch1182 on 2022/4/3 17:50.
 */
fun Context.getDimenById(name: String): Int? {
    val id = resources.getIdentifier(name, "dimen", "android")
    return try {
        resources.getDimensionPixelSize(id)
    } catch (e: Resources.NotFoundException) {
        null
    }
}

inline fun Context.getStatusBarHeight() = getDimenById("status_bar_height")
inline fun Context.getNavigationBarHeight() = getDimenById("navigation_bar_height")

inline fun getStatusBarHeight() = AppHelper.app.getStatusBarHeight()
inline fun getNavigationBarHeight() = AppHelper.app.getNavigationBarHeight()