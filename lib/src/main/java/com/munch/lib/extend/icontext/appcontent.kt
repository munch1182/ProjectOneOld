@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend.icontext

import androidx.annotation.ColorRes
import com.munch.lib.AppHelper
import com.munch.lib.extend.getLanguage
import com.munch.lib.extend.getLocale
import com.munch.lib.extend.getLocales

/**
 * Create by munch1182 on 2022/4/15 20:53.
 */
//依赖于theme的不能使用AppHelper.app
inline fun context(): IContext = AppHelper

inline fun getColorCompat(@ColorRes color: Int) = context().getColorCompat(color)
inline fun getStatusBarHeight() = context().getStatusBarHeight()
inline fun getNavigationBarHeight() = context().getNavigationBarHeight()

inline fun isPermissionGranted(permission: String) =
    context().isPermissionGranted(permission)

inline fun dp2Px(dp: Float) = context().dp2Px(dp)
inline fun sp2Px(sp: Float) = context().sp2Px(sp)

inline fun getSelectableItemBackground() = context().getSelectableItemBackground()

inline fun putStr2Clip(content: CharSequence) = context().putStr2Clip(content)

inline fun getNameVersion() = context().getNameVersion()

inline fun isScreenOn() = context().isScreenOn()

inline fun getScreenSize(full: Boolean = false) = context().getScreenSize(full)

inline fun getLocales() = context().getLocales()
inline fun getLocale() = context().getLocale()
inline fun getLanguage() = context().getLanguage()