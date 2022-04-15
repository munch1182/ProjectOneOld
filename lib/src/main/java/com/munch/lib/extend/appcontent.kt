@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import com.munch.lib.AppHelper

/**
 * Create by munch1182 on 2022/4/15 20:53.
 */
inline fun getCommonContext(): Context = AppHelper.app


inline fun getColorCompat(@ColorRes color: Int) = getCommonContext().getColorCompat(color)
inline fun getStatusBarHeight() = getCommonContext().getStatusBarHeight()
inline fun getNavigationBarHeight() = getCommonContext().getNavigationBarHeight()

inline fun getDimenById(name: String) = getCommonContext().getDimenById(name)

inline fun isPermissionGranted(permission: String) =
    getCommonContext().isPermissionGranted(permission)

inline fun dp2Px(dp: Float) = getCommonContext().dp2Px(dp)
inline fun sp2Px(sp: Float) = getCommonContext().sp2Px(sp)

inline fun getAttrFromTheme(attrId: Int) = getCommonContext().getAttrFromTheme(attrId)

inline fun getColorPrimary() = getCommonContext().getColorPrimary()

inline fun getSelectableItemBackground() = getCommonContext().getSelectableItemBackground()

inline fun <T> getAttrArrayFromTheme(attrId: Int, noinline get: TypedArray.() -> T) =
    getCommonContext().getAttrArrayFromTheme(attrId, get)

/**
 * 使用string-array数组时，使用此方法获取数组
 */
inline fun getStrArray(@ArrayRes arrayId: Int) = getCommonContext().getStrArray(arrayId)

/**
 * 使用integer-array数组时，使用此方法获取数组
 */
inline fun getIntArray(@ArrayRes arrayId: Int) = getCommonContext().getIntArray(arrayId)

/**
 * 使用array数组且item为资源id时，使用此方法获取id数组
 */
inline fun getIdsArray(@ArrayRes arrayId: Int) = getCommonContext().getIdsArray(arrayId)

inline fun putStr2Clip(content: String) = getCommonContext().putStr2Clip(content)

inline fun getNameVersion() = getCommonContext().getNameVersion()

inline fun isScreenOn() = getCommonContext().isScreenOn()