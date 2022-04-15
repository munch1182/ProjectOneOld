@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.content.res.TypedArray
import androidx.annotation.ArrayRes
import com.munch.lib.AppHelper

/**
 * Create by munch1182 on 2022/4/15 20:53.
 */

inline fun getStatusBarHeight() = AppHelper.app.getStatusBarHeight()
inline fun getNavigationBarHeight() = AppHelper.app.getNavigationBarHeight()

inline fun getDimenById(name: String) = AppHelper.app.getDimenById(name)

inline fun isPermissionGranted(permission: String) = AppHelper.app.isPermissionGranted(permission)

inline fun dp2Px(dp: Float) = AppHelper.app.dp2Px(dp)
inline fun sp2Px(sp: Float) = AppHelper.app.sp2Px(sp)

inline fun getAttrFromTheme(attrId: Int) = AppHelper.app.getAttrFromTheme(attrId)

inline fun getColorPrimary() = AppHelper.app.getColorPrimary()

inline fun getSelectableItemBackground() = AppHelper.app.getSelectableItemBackground()

inline fun <T> getAttrArrayFromTheme(attrId: Int, noinline get: TypedArray.() -> T) =
    AppHelper.app.getAttrArrayFromTheme(attrId, get)

/**
 * 使用string-array数组时，使用此方法获取数组
 */
inline fun getStrArray(@ArrayRes arrayId: Int) = AppHelper.app.getStrArray(arrayId)

/**
 * 使用integer-array数组时，使用此方法获取数组
 */
inline fun getIntArray(@ArrayRes arrayId: Int) = AppHelper.app.getIntArray(arrayId)

/**
 * 使用array数组且item为资源id时，使用此方法获取id数组
 */
inline fun getIdsArray(@ArrayRes arrayId: Int) = AppHelper.app.getIdsArray(arrayId)

inline fun putStr2Clip(content: String) = AppHelper.app.putStr2Clip(content)

inline fun getNameVersion() = AppHelper.app.getNameVersion()

inline fun isScreenOn() = AppHelper.app.isScreenOn()