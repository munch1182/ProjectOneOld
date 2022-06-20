package com.munch.lib.fast.helper

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.forEach
import com.munch.lib.extend.color
import com.munch.lib.extend.getAttrArrayFromTheme
import com.munch.lib.fast.base.BaseFastActivity

/**
 * Create by munch1182 on 2022/6/20 16:24.
 */
object ViewColorHelper {

    private const val PARAMS_KEY = "color"
    private var color: Int? = null

    fun setColor(@ColorInt color: Int?) {
        this.color = color
    }

    fun updateColor(activity: BaseFastActivity) {
        if (this.color == null) {
            this.color = generaColor()
        }
        val color = activity.activityParams[PARAMS_KEY] as? Int
        if (color == this.color) {
            return
        }
        fitStatusColor(activity)
        val view = activity.findViewById<FrameLayout>(android.R.id.content)
        fitViewColor(view)
        activity.activityParams[PARAMS_KEY] = ViewColorHelper.color
    }

    private fun fitStatusColor(activity: BaseFastActivity) {
        val c = color ?: return
        val luminance = ColorUtils.calculateLuminance(c)
        val needBlack = luminance > 0.5
        val textColor = if (needBlack) Color.BLACK else Color.WHITE
        activity.supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(c))
            title = title?.color(textColor)
            val home = activity.getAttrArrayFromTheme(android.R.attr.homeAsUpIndicator) {
                getDrawable(0)?.apply { setTint(textColor) }
            }
            setHomeAsUpIndicator(home)
            setDisplayShowTitleEnabled(true)
            setDisplayShowCustomEnabled(true)
            fitViewColor(customView, true)
        }
        activity.bar.colorStatusBar(c).setTextColorBlack(needBlack)
    }

    private fun fitViewColor(view: View?, fitTextColor: Boolean = false) {
        view ?: return
        val c = color ?: return
        if (view is ViewGroup) {
            view.forEach { fitViewColor(it) }
        } else {
            val luminance = ColorUtils.calculateLuminance(c)
            val needBlack = luminance > 0.5
            val textColor = if (needBlack) Color.BLACK else Color.WHITE
            if (view is Button) {
                view.backgroundTintList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(c))
                view.setTextColor(textColor)
            }
            if (fitTextColor && view is TextView) {
                view.setTextColor(textColor)
            }
        }
    }

    @ColorInt
    private fun generaColor(): Int {
        val r = java.util.Random()
        return Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255))
    }
}