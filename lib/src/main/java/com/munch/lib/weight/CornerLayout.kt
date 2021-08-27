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
            val radius = getDimension(R.styleable.CornerLayout_radius, 0f)
            val tl = getDimension(R.styleable.CornerLayout_tlRadius, radius)
            val tr = getDimension(R.styleable.CornerLayout_trRadius, radius)
            val bl = getDimension(R.styleable.CornerLayout_blRadius, radius)
            val br = getDimension(R.styleable.CornerLayout_brRadius, radius)
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
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)

            val childViewWidth = MeasureSpec.makeMeasureSpec(
                width - paddingLeft - paddingRight, MeasureSpec.EXACTLY
            )
            val childViewHeight = MeasureSpec.makeMeasureSpec(
                height - paddingTop - paddingBottom, MeasureSpec.EXACTLY
            )
            measureChild(child, childViewWidth, childViewHeight)

            val w = child.measuredWidth + paddingLeft + paddingBottom
            val h = child.measuredHeight + paddingTop + paddingBottom
            if (w > width) {
                setMeasuredDimension(width, h)
            } else {
                setMeasuredDimension(w, h)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 1) {
            getChildAt(0).layout(
                paddingLeft,
                paddingTop,
                width - paddingRight,
                height - paddingBottom
            )
        }
    }


}

