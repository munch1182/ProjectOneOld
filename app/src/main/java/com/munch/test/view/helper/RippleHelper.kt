package com.munch.test.view.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Create by munch on 2020/10/16 9:16
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object RippleHelper {

    private const val ID_ATTR = android.R.attr.selectableItemBackgroundBorderless
    private const val ID_MASK = android.R.id.mask
    private const val ID_COLOR = android.R.attr.colorControlHighlight

    fun newRippleBg(context: Context): Drawable? {
        val typeArray = context.obtainStyledAttributes(intArrayOf(ID_ATTR))
        val result = typeArray.getDrawable(0)
        typeArray.recycle()
        return result
    }

    private fun getDefColor(context: Context): Int {
        val typeArray = context.obtainStyledAttributes(intArrayOf(ID_COLOR))
        val result = typeArray.getColor(0, Color.TRANSPARENT)
        typeArray.recycle()
        return result
    }

    fun setRipple(
        view: View,
        rippleColor: ColorStateList? = ColorStateList.valueOf(getDefColor(view.context))
    ) {
        view.background = newRippleBg(view.context).apply {
            if (this is RippleDrawable) {
                setColor(rippleColor)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setRipple(
        view: View,
        bgDrawable: Drawable? = null,
        rippleColor: ColorStateList? = ColorStateList.valueOf(getDefColor(view.context)),
        maskDrawable: Drawable? = null,
        rippleRadius: Int = RippleDrawable.RADIUS_AUTO
    ) {
        view.background = RippleDrawable(
            rippleColor ?: ColorStateList.valueOf(getDefColor(view.context)),
            bgDrawable,
            maskDrawable
        ).apply {
            radius = rippleRadius
        }
    }
}