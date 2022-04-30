@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.content.Context
import androidx.annotation.ColorRes
import com.munch.lib.AppHelper

/**
 * Create by munch1182 on 2022/4/15 20:53.
 */
//依赖于theme的不能使用AppHelper.app
inline fun getCommonContext(): Context = AppHelper.app

inline fun getColorCompat(@ColorRes color: Int) = getCommonContext().getColorCompat(color)
inline fun getStatusBarHeight() = getCommonContext().getStatusBarHeight()
inline fun getNavigationBarHeight() = getCommonContext().getNavigationBarHeight()

inline fun isPermissionGranted(permission: String) =
    getCommonContext().isPermissionGranted(permission)

inline fun dp2Px(dp: Float) = getCommonContext().dp2Px(dp)
inline fun sp2Px(sp: Float) = getCommonContext().sp2Px(sp)

inline fun getSelectableItemBackground() = getCommonContext().getSelectableItemBackground()

inline fun putStr2Clip(content: CharSequence) = getCommonContext().putStr2Clip(content)

inline fun getNameVersion() = getCommonContext().getNameVersion()

inline fun isScreenOn() = getCommonContext().isScreenOn()