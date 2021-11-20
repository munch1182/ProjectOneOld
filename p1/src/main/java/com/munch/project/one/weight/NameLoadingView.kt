package com.munch.project.one.weight

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.munch.project.one.R

/**
 * Create by munch1182 on 2021/11/20 16:31.
 */
class NameLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_loading, this, true)
    }

    private val realView: View
        get() = getChildAt(0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(realView, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(realView.measuredWidth, realView.measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        realView.layout(0, 0, width, height)
    }
}