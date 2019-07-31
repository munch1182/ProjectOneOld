package com.munhc.lib.libnative.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable

/**
 * Created by Munch on 2019/7/26 9:21
 */
object SystemResHelper {

    @JvmStatic
    fun getMenuIcon(context: Context) = DrawerArrowDrawable(context)

    @JvmStatic
    fun getMenuIconWhite(context: Context) = getMenuIcon(context).apply {
        this.color = Color.WHITE
    }

    @JvmStatic
    fun getBackIcon(context: Context): Drawable? {
        return if (Build.VERSION.SDK_INT >= 18) {
            val a = context.obtainStyledAttributes(
                null, intArrayOf(android.R.attr.homeAsUpIndicator),
                android.R.attr.actionBarSplitStyle, 0
            )
            val result = a.getDrawable(0)
            a.recycle()
            result
        } else {
            val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.homeAsUpIndicator))
            val result = a.getDrawable(0)
            a.recycle()
            result
        }
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getBackIconWhite(context: Context): Drawable? = getBackIcon(context)?.apply {
        setTint(Color.WHITE)
    }

    @JvmStatic
    fun getListDivider(context: Context): Drawable? {
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        val divider = a.getDrawable(0)
        a.recycle()
        return divider
    }
}