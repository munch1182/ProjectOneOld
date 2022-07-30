package com.munch.lib.weight.shape

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.weight.*

class Color @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ContainerLayout(context, attrs, defStyleAttr), FunctionalView, ITextView, IDrawableView,
    IContext {

    private var state = intArrayOf()
    private var color = -1
    private var textColor: ColorDrawable? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.Color).apply {

            color = getColor(R.styleable.Color_android_color, -1)
            textColor = getDrawable(R.styleable.Color_android_textColor) as? ColorDrawable

            val state = mutableListOf<Int>()
            var have = this.getBoolean(R.styleable.Color_android_state_pressed, false)
            if (have) state.add(android.R.attr.state_pressed)
            have = this.getBoolean(R.styleable.Color_android_state_active, false)
            if (have) state.add(android.R.attr.state_active)
            have = this.getBoolean(R.styleable.Color_android_state_accelerated, false)
            if (have) state.add(android.R.attr.state_accelerated)
            have = this.getBoolean(R.styleable.Color_android_state_activated, false)
            if (have) state.add(android.R.attr.state_activated)
            have = this.getBoolean(R.styleable.Color_android_state_checkable, false)
            if (have) state.add(android.R.attr.state_checkable)
            have = this.getBoolean(R.styleable.Color_android_state_checked, false)
            if (have) state.add(android.R.attr.state_checked)
            have = this.getBoolean(R.styleable.Color_android_state_drag_can_accept, false)
            if (have) state.add(android.R.attr.state_drag_can_accept)
            have = this.getBoolean(R.styleable.Color_android_state_drag_hovered, false)
            if (have) state.add(android.R.attr.state_drag_hovered)
            have = this.getBoolean(R.styleable.Color_android_state_enabled, false)
            if (have) state.add(android.R.attr.state_enabled)
            have = this.getBoolean(R.styleable.Color_android_state_first, false)
            if (have) state.add(android.R.attr.state_first)
            have = this.getBoolean(R.styleable.Color_android_state_focused, false)
            if (have) state.add(android.R.attr.state_focused)
            have = this.getBoolean(R.styleable.Color_android_state_hovered, false)
            if (have) state.add(android.R.attr.state_hovered)
            have = this.getBoolean(R.styleable.Color_android_state_last, false)
            if (have) state.add(android.R.attr.state_last)
            have = this.getBoolean(R.styleable.Color_android_state_middle, false)
            if (have) state.add(android.R.attr.state_middle)
            have = this.getBoolean(R.styleable.Color_android_state_selected, false)
            if (have) state.add(android.R.attr.state_selected)
            have = this.getBoolean(R.styleable.Color_android_state_single, false)
            if (have) state.add(android.R.attr.state_single)
            have = this.getBoolean(R.styleable.Color_android_state_window_focused, false)
            if (have) state.add(android.R.attr.state_window_focused)

            this@Color.state = state.toIntArray()
        }.recycle()
    }

    override fun setTextColor(color: ColorStateList) {
        this.textColor = TextColorDrawable().apply { setTintList(color) }
    }

    override fun setTextColor(color: Int) {
        this.textColor = TextColorDrawable(color)
    }

    override fun updateDrawable(): ColorDrawable? {
        return if (color != -1) {
            ColorDrawable(color)
        } else if (textColor != null) {
            TextColorDrawable(textColor!!.color)
        } else {
            null
        }?.apply { this.state = this@Color.state }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isVisible) updateView()
    }

    private fun updateView() {
        val v = getView() ?: return
        if (color != -1) {
            v.setBackgroundColor(color)
        }
        if (v is TextView && textColor != null) {
            textColor?.color?.let { v.setTextColor(it) }
        }
    }

    override fun judgeCanBeTarget(target: View?) = target != null && target !is FunctionalView

    class TextColorDrawable() : ColorDrawable() {

        constructor(@ColorInt color: Int) : this() {
            setColor(color)
        }
    }
}