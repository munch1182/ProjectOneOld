@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend.icontext

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.annotation.*
import androidx.fragment.app.Fragment
import com.munch.lib.UnSupportException
import com.munch.lib.extend.*
import kotlin.reflect.KClass

/**
 * Created by munch1182 on 2022/4/3 17:50.
 */

/**
 * 主要用于省略写法
 */
interface IContext {

    val ctx: Context
        get() {
            return when (this) {
                is Context -> this
                is Fragment -> requireContext()
                is View -> context
                else -> throw UnSupportException()
            }
        }
}

inline fun IContext.cacheDir() = ctx.cacheDir
inline fun IContext.filesDir() = ctx.filesDir
inline fun IContext.assets() = ctx.assets
inline fun IContext.getString(@StringRes str: Int) = ctx.getString(str)
inline fun IContext.getDrawable(@DrawableRes res: Int) = ctx.theme.getDrawable(res)
inline fun IContext.startActivity(target: KClass<out Activity>) = ctx.startActivity(target)
inline fun IContext.getColorCompat(@ColorRes color: Int) = ctx.getColorCompat(color)
inline fun IContext.getDimenById(name: String): Int? = ctx.getDimenById(name)
inline fun IContext.getStatusBarHeight() = ctx.getStatusBarHeight()
inline fun IContext.getNavigationBarHeight() = ctx.getNavigationBarHeight()
inline fun IContext.isPermissionGranted(permission: String) = ctx.isPermissionGranted(permission)
inline fun IContext.dp2Px(dp: Float) = ctx.dp2Px(dp)
inline fun IContext.sp2Px(sp: Float) = ctx.sp2Px(sp)
inline fun IContext.getAttrFromTheme(attrId: Int) = ctx.getAttrFromTheme(attrId)
inline fun IContext.getSelectableItemBackground() = ctx.getSelectableItemBackground()
inline fun IContext.getStrArray(@ArrayRes arrayId: Int) = ctx.getStrArray(arrayId)
inline fun IContext.getIntArray(@ArrayRes arrayId: Int) = ctx.getIntArray(arrayId)
inline fun IContext.getIdsArray(@ArrayRes arrayId: Int) = ctx.getIdsArray(arrayId)
inline fun IContext.putStr2Clip(content: CharSequence) = ctx.putStr2Clip(content)
inline fun IContext.shareText(content: CharSequence) = ctx.shareText(content)
inline fun IContext.shareUri(uri: Uri) = ctx.shareUri(uri)
inline fun IContext.getNameVersion() = ctx.getNameVersion()
inline fun IContext.isScreenOn() = ctx.isScreenOn()
inline fun IContext.getScreenSize(full: Boolean = false) = ctx.getScreenSize(full)

@ColorInt
fun IContext.getColorPrimary(@ColorInt defValue: Int = Color.WHITE) = ctx.getColorPrimary(defValue)

inline fun <T> IContext.getAttrArrayFromTheme(attrId: Int, noinline get: TypedArray.() -> T) =
    ctx.getAttrArrayFromTheme(attrId, get)

@RequiresApi(Build.VERSION_CODES.N)
inline fun IContext.dataDir() = ctx.dataDir