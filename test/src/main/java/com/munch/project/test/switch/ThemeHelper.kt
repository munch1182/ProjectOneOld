package com.munch.project.test.switch

import android.app.Activity
import android.content.Context
import com.munch.lib.helper.getAttrFromTheme
import com.munch.lib.helper.getColorCompat
import com.munch.project.test.R

/**
 * Create by munch1182 on 2020/12/30 17:09.
 */
object ThemeHelper {

    const val THEME_DEF = 0
    const val THEME_MODE_1 = 1

    fun setTheme(activity: Activity) {
        activity.setTheme(getTheme())
    }

    fun getTheme(): Int {
        return when (SwitchHelper.INSTANCE.getThemeMode()) {
            THEME_MODE_1 -> {
                R.style.AppTheme_P1_Theme_exp_1
            }
            else -> {
                R.style.AppTheme_P1
            }
        }
    }

    fun attr2Resource(context: Context, attrId: Int): Int {
        return context.getAttrFromTheme(attrId).resourceId
    }
}

fun Context.attr2Resource(attrId: Int) = ThemeHelper.attr2Resource(this, attrId)

fun Context.attr2Color(attrId: Int) = getColorCompat(attr2Resource(attrId))