package com.munch.test.view.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 没有写宽高间距因为可以用内部View的margin和padding实现
 * Create by munch on 2020/9/4 13:32
 */
class FlowLayout(context: Context, attrs: AttributeSet?, styleDef: Int) :
    ViewGroup(context, attrs, styleDef) {

    private val rectArray = ArrayList<Int>()

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        rectArray.clear()
        var child: View?
        //可用宽度
        val rowsMaxWidth = widthSize - paddingLeft - paddingRight
        //已用宽度
        var rowsUsedWidth = paddingLeft
        //已有高度
        var columnHeight = paddingTop
        var lastRowHeight = paddingTop
        var isWrap = false
        for (i in 0..childCount) {
            child = getChildAt(i) ?: break
            if (child.visibility == View.GONE) {
                continue
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            var childWidth = child.measuredWidth
            var childHeight = child.measuredHeight
            //需要重写generateLayoutParams(attrs: AttributeSet?)方法
            val params = child.layoutParams as MarginLayoutParams
            childWidth += params.leftMargin + params.rightMargin
            childHeight += params.topMargin + params.bottomMargin

            //不换行
            if (rowsMaxWidth - rowsUsedWidth >= childWidth) {
                rowsUsedWidth += childWidth
                //某些单个控件更高则更新高度
                if (lastRowHeight + childHeight > columnHeight) {
                    columnHeight = lastRowHeight + childHeight
                }
                //换行
            } else {
                rowsUsedWidth = paddingLeft + childWidth
                lastRowHeight = columnHeight
                columnHeight += childHeight
                isWrap = true
            }

            rectArray.add(rowsUsedWidth - childWidth + params.leftMargin)
            rectArray.add(lastRowHeight + params.topMargin)
            rectArray.add(rowsUsedWidth - params.rightMargin)
            rectArray.add(lastRowHeight + childHeight - params.bottomMargin)
        }

        var width = if (isWrap) widthSize else rowsUsedWidth
        var height = columnHeight + paddingBottom

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        }
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(change: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0..childCount) {
            getChildAt(i)?.layout(
                rectArray[i * 4],
                rectArray[i * 4 + 1],
                rectArray[i * 4 + 2],
                rectArray[i * 4 + 3]
            )
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }
}