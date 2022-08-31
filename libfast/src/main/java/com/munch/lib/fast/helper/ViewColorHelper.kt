package com.munch.lib.fast.helper

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.forEach
import com.munch.lib.OnUpdate
import com.munch.lib.extend.color
import com.munch.lib.extend.getAttrArrayFromTheme
import com.munch.lib.extend.isLight
import com.munch.lib.extend.randomColor
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.weight.IColorView
import com.munch.lib.weight.ITextView
import kotlin.collections.set

/**
 * Create by munch1182 on 2022/6/20 16:24.
 */
object ViewColorHelper {

    private const val PARAMS_KEY = "color"
    private var color: Int? = null
    private var onUpdate: OnUpdate? = null

    fun setColor(@ColorInt color: Int?): ViewColorHelper {
        this.color = color
        return this
    }

    @ColorInt
    fun getColor(activity: BaseFastActivity): Int? {
        return activity.activityParams[PARAMS_KEY] as? Int
    }

    @ColorInt
    fun getColor(): Int? {
        return color
    }

    fun onUpdate(onUpdate: OnUpdate): ViewColorHelper {
        this.onUpdate = onUpdate
        return this
    }

    fun updateColor(activity: BaseFastActivity) {
        if (this.color == null) {
            this.color = randomColor()
        }
        val color = activity.activityParams[PARAMS_KEY] as? Int
        if (color == this.color) {
            return
        }
        activity.activityParams[PARAMS_KEY] = ViewColorHelper.color
        fitStatusColor(activity)
        val view = activity.findViewById<FrameLayout>(Window.ID_ANDROID_CONTENT)
        fitTextColor(view)
        onUpdate?.invoke()
    }

    private fun fitStatusColor(activity: BaseFastActivity) {
        val c = color ?: return
        val needBlack = c.isLight()
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
            fitTextColor(customView, false)
        }
        activity.bar.colorStatusBar(c).setTextColorBlack(needBlack)
    }

    fun fitTextColor(view: View?, fitTextColor: Boolean = false) {
        view ?: return
        val c = color ?: return
        if (view is IColorView) {
            view.setColor(c)
        } else if (view is ViewGroup) {
            view.forEach { fitTextColor(it, fitTextColor) }
        } else {
            val textColor = if (c.isLight()) Color.BLACK else Color.WHITE
            if (view is Button) {
                view.backgroundTintList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(c))
                view.setTextColor(textColor)
            } else if (fitTextColor) {
                if (view is TextView) view.setTextColor(textColor)
                if (view is ITextView) view.setTextColor(textColor)
            }
        }
    }
}