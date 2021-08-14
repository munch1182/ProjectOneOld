package com.munch.lib.weight

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import com.munch.lib.R
import com.munch.lib.base.ViewHelper

/**
 * Create by munch1182 on 2021/8/12 17:47.
 */

class CornerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CornerLayout).apply {
            val color = getColor(R.styleable.CornerLayout_android_color, Color.TRANSPARENT)
            val tl = getDimension(R.styleable.CornerLayout_tlRadius, 0f)
            val tr = getDimension(R.styleable.CornerLayout_trRadius, 0f)
            val bl = getDimension(R.styleable.CornerLayout_blRadius, 0f)
            val br = getDimension(R.styleable.CornerLayout_brRadius, 0f)
            val strokeColor = getColor(R.styleable.CornerLayout_strokeColor, Color.TRANSPARENT)
            val strokeWidth = getDimensionPixelSize(R.styleable.CornerLayout_strokeWidth, 0)
            val drawable =
                ViewHelper.newCornerDrawable(color, tl, tr, bl, br, strokeWidth, strokeColor)
            background = drawable
        }.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 1) {
            val child = getChildAt(0)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(
                child.measuredWidth + paddingLeft + paddingRight,
                child.measuredHeight + paddingTop + paddingBottom
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 1) {
            getChildAt(0).layout(
                l + paddingLeft,
                t + paddingTop,
                r - paddingRight,
                b - paddingBottom
            )
        }
    }


}

