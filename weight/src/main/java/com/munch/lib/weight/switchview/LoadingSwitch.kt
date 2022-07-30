package com.munch.lib.weight.switchview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.core.view.children
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.extend.layoutCenter
import com.munch.lib.extend.paddingHorizontal
import com.munch.lib.extend.paddingVertical
import com.munch.lib.weight.IColorView
import kotlin.math.max

class LoadingSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), IContext, IColorView, Checkable {

    private val switch = Switch(context, attrs, defStyleAttr, defStyleRes)

    init {
        addView(switch)
    }

    private var preChecked = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxW = 0
        var maxH = 0
        children.forEach {
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            maxW = max(maxW, it.measuredWidth)
            maxH = max(maxH, it.measuredHeight)
            it.visibility = View.INVISIBLE
        }
        setMeasuredDimension(maxW + paddingHorizontal(), maxH + paddingVertical())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach { it.layoutCenter(r - l, b - t) }
    }

    override fun setColor(color: Int) {
        switch.setColor(color)
    }

    override fun setChecked(checked: Boolean) {
        preChecked = checked
        preCheckUpdate()
    }

    private fun preCheckUpdate() {
        if (isChecked == preChecked) {
            return
        }
        // TODO:  
    }

    override fun isChecked(): Boolean {
        return switch.isChecked
    }

    override fun toggle() {
        isChecked = !isChecked
    }
}