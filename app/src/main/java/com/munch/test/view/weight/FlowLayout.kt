package com.munch.test.view.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Create by munch on 2020/9/4 13:32
 */
class FlowLayout(context: Context, attrs: AttributeSet?, styleDef: Int) :
    ViewGroup(context, attrs, styleDef) {

    var verticalSpace = 30f
    var horizontalSpace = 15f

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        var height = 0
        var child: View
        for (i in 0..childCount) {
            child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

        }
    }

    override fun onLayout(change: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }
}