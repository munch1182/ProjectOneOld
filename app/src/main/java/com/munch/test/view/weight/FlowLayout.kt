package com.munch.test.view.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef

/**
 * 没有写宽高间距因为可以用内部View的margin和padding实现
 * Create by munch on 2020/9/4 13:32
 */
class FlowLayout(context: Context, attrs: AttributeSet?, styleDef: Int) :
    ViewGroup(context, attrs, styleDef) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var helper = LayoutHelper()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        helper.reset()

        var child: View?
        //可用宽度
        val rowsMaxWidth = widthSize - paddingLeft - paddingRight
        //已用宽度
        var rowsUsedWidth = paddingLeft
        //已有高度
        var columnHeight = paddingTop
        var lastRowHeight = paddingTop
        var leftWidth: Int
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

            if (i == 0) {
                helper.lineNum = 0
                helper.lineViewCount = 1
            }
            leftWidth = rowsMaxWidth - rowsUsedWidth
            //不换行
            if (leftWidth >= childWidth) {
                rowsUsedWidth += childWidth
                //某些单个控件更高则更新高度
                if (lastRowHeight + childHeight > columnHeight) {
                    columnHeight = lastRowHeight + childHeight
                }
                if (i != 0) {
                    helper.lineViewCount++
                }
                //换行
            } else {
                helper.lineCenterY = lastRowHeight + (columnHeight - lastRowHeight) / 2
                helper.spaceLeft = leftWidth
                helper.updateLines()
                //下一行
                rowsUsedWidth = paddingLeft + childWidth
                lastRowHeight = columnHeight
                columnHeight += childHeight

                helper.lineNum++
                helper.lineViewCount = 1
            }

            helper.l = rowsUsedWidth - childWidth + params.leftMargin
            helper.t = lastRowHeight + params.topMargin
            helper.r = rowsUsedWidth - params.rightMargin
            helper.b = lastRowHeight + childHeight - params.bottomMargin
            helper.updateRect()
        }
        //是否只有一行
        val oneLine = helper.lineArray.size == 0

        var width = if (!oneLine) widthSize else rowsUsedWidth
        var height = columnHeight + paddingBottom

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        }
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        }

        if (oneLine) {
            helper.lineCenterY = heightSize / 2
            helper.spaceLeft = 0
            helper.updateLines()
            //补足最后一行的数据
        } else {
            helper.lineCenterY = lastRowHeight + (columnHeight - lastRowHeight) / 2
            helper.spaceLeft = rowsMaxWidth - rowsUsedWidth
            helper.updateLines()
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(change: Boolean, l: Int, t: Int, r: Int, b: Int) {
        helper.startLayout(0)
        var lineCount = helper.lineViewCount
        var lineIndex = 0
        for (i in 0..childCount) {
            //获取下一行数据
            if (i >= lineCount) {
                helper.startLayout(helper.lineNum + 1)
                lineCount += helper.lineViewCount
                lineIndex = 0
            }
            helper.layout(getChildAt(i), i, lineIndex)
            lineIndex++
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    fun setGravity(@Gravity gravity: Int) {
        helper.gravity = gravity
        requestLayout()
    }

    @IntDef(START, END, CENTER, CENTER_VERTICAL, CENTER_HORIZONTAL)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Gravity

    private class LayoutHelper {
        var lineNum = 0
        var lineViewCount = 0
        var lineCenterY = 0

        //剩下的空间
        var spaceLeft = 0

        @Gravity
        var gravity = START

        var rectArray = ArrayList<Int>()
        var lineArray = ArrayList<Int>()

        var l = 0
        var t = 0
        var r = 0
        var b = 0

        fun reset() {
            rectArray.clear()
            lineArray.clear()
            lineNum = 0
            lineViewCount = 0
            lineCenterY = 0
            spaceLeft = 0
        }

        fun updateLines() {
            lineArray.add(lineNum)
            lineArray.add(lineViewCount)
            lineArray.add(lineCenterY)
            lineArray.add(spaceLeft)
        }

        fun updateRect() {
            rectArray.add(l)
            rectArray.add(t)
            rectArray.add(r)
            rectArray.add(b)
        }

        fun layout(view: View?, index: Int, lineIndex: Int) {
            view ?: return
            val l = rectArray[index * 4]
            val t = rectArray[index * 4 + 1]
            val r = rectArray[index * 4 + 2]
            val b = rectArray[index * 4 + 3]
            when (gravity) {
                START -> view.layout(l, t, r, b)
                END -> view.layout(l + spaceLeft, t, r + spaceLeft, b)
                CENTER_HORIZONTAL -> {
                    //给末尾留下空隙,所以+1
                    val left = spaceLeft / (lineViewCount + 1)
                    view.layout(l + left * (lineIndex + 1), t, r + left * (lineIndex + 1), b)
                }
                CENTER_VERTICAL -> {
                    view.layout(
                        l,
                        lineCenterY - view.measuredHeight / 2,
                        r,
                        lineCenterY + view.measuredHeight / 2
                    )
                }
                CENTER -> {
                    val left = spaceLeft / (lineViewCount + 1)
                    view.layout(
                        l + left * (lineIndex + 1),
                        lineCenterY - view.measuredHeight / 2,
                        r + left * (lineIndex + 1),
                        lineCenterY + view.measuredHeight / 2
                    )
                }
                END_CENTER_VERTICAL -> {
                    view.layout(
                        l + spaceLeft,
                        lineCenterY - view.measuredHeight / 2,
                        r + spaceLeft,
                        lineCenterY + view.measuredHeight / 2
                    )
                }
            }
        }

        fun startLayout(index: Int) {
            if (index * 4 >= lineArray.size) {
                return
            }
            lineNum = lineArray[index * 4]
            lineViewCount = lineArray[index * 4 + 1]
            lineCenterY = lineArray[index * 4 + 2]
            spaceLeft = lineArray[index * 4 + 3]

            if (spaceLeft < 0) {
                spaceLeft = 0
            }
        }

        override fun toString(): String {
            return "lineNum:$lineNum,lineViewCount:$lineViewCount,lineCenterY:$lineCenterY,spaceLeft:$spaceLeft,gravity:$gravity"
        }
    }

    companion object {
        const val START = 0
        const val END = 1
        const val CENTER = 2
        const val CENTER_VERTICAL = 3
        const val CENTER_HORIZONTAL = 4
        const val END_CENTER_VERTICAL = 5
    }
}