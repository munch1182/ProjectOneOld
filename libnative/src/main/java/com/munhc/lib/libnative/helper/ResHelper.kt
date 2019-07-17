package com.munhc.lib.libnative.helper

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue

/**
 * Created by Munch on 2019/7/16 10:46
 */
object ResHelper {

    fun getSystemDrawable(context: Context, resId: Int): Drawable? {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(resId, value, true)) {
            val attributes = context.theme.obtainStyledAttributes(intArrayOf(resId))
            val drawable = attributes.getDrawable(0)
            attributes.recycle()
            drawable
        } else null
    }
}