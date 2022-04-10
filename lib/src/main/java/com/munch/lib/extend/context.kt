@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.content.Context
import android.content.res.Resources
import androidx.core.content.PermissionChecker
import com.munch.lib.AppHelper

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

inline fun Context.isPermissionGranted(permission: String) =
    PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED

inline fun isPermissionGranted(permission: String) = AppHelper.app.isPermissionGranted(permission)