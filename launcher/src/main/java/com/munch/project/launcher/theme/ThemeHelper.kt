package com.munch.project.launcher.theme

import android.app.Activity
import android.content.Context
import com.munch.lib.helper.getAttrFromTheme
import com.munch.lib.helper.getColorCompat
import com.munch.project.launcher.R

/**
 * todo 未完成
 * Create by munch1182 on 2021/2/23 15:10.
 */
object ThemeHelper {

    private const val THEME_DEF = 0

    fun setTheme(activity: Activity) {
        activity.setTheme(getTheme())
    }

    private fun getTheme(): Int {
        return THEME_DEF
    }

    fun attr2Resource(context: Context, attrId: Int): Int {
        return context.getAttrFromTheme(attrId).resourceId
    }

    fun getCurrentThemePageBg() = R.attr.pageBg
}

fun Context.attr2Resource(attrId: Int) = ThemeHelper.attr2Resource(this, attrId)

fun Context.attr2Color(attrId: Int) = getColorCompat(attr2Resource(attrId))

fun Context.getBgColor() = attr2Color(ThemeHelper.getCurrentThemePageBg())