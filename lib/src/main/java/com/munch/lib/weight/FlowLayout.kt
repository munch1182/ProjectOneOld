package com.munch.lib.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.view.children
import com.munch.lib.helper.array.RectArrayHelper
import com.munch.lib.helper.array.SpecialArrayHelper

/**
 * 用于流布局
 * 目标：
 * 1. 可以设置对齐方式(start,end,center,center_vertical,center_horizontal,end_center_vertical)
 * 2. 可以设置每行最大个数
 * 3. 可以设置间隔
 *
 * Create by munch1182 on 2021/8/10 17:33.
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef) {

    //行间隔
    var itemLinesSpace = 8

    //子view之间的间隔
    var itemSpace = 8

    private val layoutHelper = LayoutHelper()

    fun setGravity(@Gravity gravity: Int) {
        layoutHelper.gravity = gravity
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        layoutHelper.reset()
        //可用宽度
        val rowsMaxWidth = widthSize - paddingLeft - paddingRight
        //已用宽度
        var rowsUsedWidth = paddingLeft
        //所有行使用的最长宽度
        var rowsUsedMaxWidth = rowsUsedWidth
        //上一行为止的行高度
        var lastRowsHeight = paddingTop
        //上一行的行高度+本行已计算的高度
        var rowsHeight = paddingTop
        children.forEachIndexed { index, child ->
            if (child.visibility == View.GONE) {
                return@forEachIndexed
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            //1. 计算子view的宽高
            var viewWidth = child.measuredWidth
            var viewHeight = child.measuredHeight
            val lp = child.layoutParams
            var marginL = 0
            var marginT = 0
            var marginR = 0
            var marginB = 0
            //需要重写generateLayoutParams
            if (lp is MarginLayoutParams) {
                marginL = lp.leftMargin
                marginT = lp.topMargin
                marginR = lp.rightMargin
                marginB = lp.bottomMargin
            }
            viewWidth += marginL + marginR
            viewHeight += marginT + marginB
            //2. 根据新的子view的宽高，来更新行数据
            val leftWidth = rowsMaxWidth - rowsUsedWidth - paddingRight
            //如果剩余宽度足够
            //index == 0时走上面的判断，避免第一个宽度过大显示错误
            //第一个时无需考虑间隔
            val space = if (index == 0) 0 else itemSpace
            if (index == 0 || leftWidth >= viewWidth + space) {
                rowsUsedWidth += viewWidth + space
                //如果这个view更高，则更新高度
                if (lastRowsHeight + viewHeight > rowsHeight) {
                    rowsHeight = lastRowsHeight + viewHeight
                }

                layoutHelper.lineInfo.lineViewCount++
                //本行剩余宽度不够，需要换行
            } else {
                //先更新此行数据
                layoutHelper.lineInfo.apply {
                    lineCenterY = lastRowsHeight + (rowsHeight - lastRowsHeight) / 2
                    lineSpaceLeft = leftWidth
                    layoutHelper.updateLineInfo()
                }
                if (rowsUsedMaxWidth < rowsUsedWidth + paddingRight) {
                    rowsUsedMaxWidth = rowsUsedWidth + paddingRight
                }
                //然后换行
                rowsUsedWidth = paddingLeft + viewWidth
                lastRowsHeight = rowsHeight + itemLinesSpace
                rowsHeight += viewHeight

                layoutHelper.lineInfo.apply {
                    lineNum++
                    lineViewCount = 1
                }
            }

            //3. 记录每个view的位置:每个位置挨着存放位置，更改布局方式只需要对空余位置进行偏移即可
            val l = rowsUsedWidth - viewWidth + marginL
            val t = lastRowsHeight + marginT
            val r = rowsUsedWidth - marginR
            val b = lastRowsHeight + viewHeight - marginB
            layoutHelper.updateViewRect(l, t, r, b)
        }

        //是否只有一行
        val oneLine = layoutHelper.lineInfo.lineNum == 0
        //补足数据
        if (oneLine) {
            layoutHelper.lineInfo.lineCenterY = rowsHeight / 2
        } else {
            layoutHelper.lineInfo.lineCenterY = lastRowsHeight / 2 + rowsHeight / 2
        }
        layoutHelper.lineInfo.lineSpaceLeft = rowsMaxWidth - rowsUsedWidth
        layoutHelper.updateLineInfo()


        val height = if (hMode == MeasureSpec.EXACTLY) heightSize else (rowsHeight + paddingBottom)
        val width = if (wMode == MeasureSpec.EXACTLY) widthSize else rowsUsedMaxWidth
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var lineInfo = layoutHelper.startLayout(0)
        var viewCount = lineInfo.lineViewCount
        //该行中的序数
        var lineIndex = 0
        for (i in 0 until childCount) {
            if (i >= viewCount) {
                lineInfo = layoutHelper.startLayout(lineInfo.lineNum + 1)
                viewCount += lineInfo.lineViewCount
                lineIndex = 0
            }
            layoutHelper.layout(getChildAt(i), i, lineIndex, lineInfo)
            lineIndex++
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    private class LayoutHelper {
        val rectArrayHelper = RectArrayHelper()
        val lineInfoArrayHelper = LineArrayHelper()
        val lineInfo = LineInfo()

        @Gravity
        var gravity = START

        /**
         * 该行子view的最大数量，如果为-1时，则无特别限制
         */
        var maxCountInLine = -1

        fun reset() {
            rectArrayHelper.clear()
            lineInfoArrayHelper.clear()
            lineInfo.reset()
        }

        /**
         * 将[lineInfo]的数据存储到[lineInfoArrayHelper]
         */
        fun updateLineInfo() {
            lineInfoArrayHelper.add(lineInfo)
        }

        /**
         * 将位置信息存储到[rectArrayHelper]
         */
        fun updateViewRect(l: Int, t: Int, r: Int, b: Int) {
            rectArrayHelper.add(l, t, r, b)
        }

        /**
         * 从[lineInfoArrayHelper]中还原[line]行的信息到[lineInfo]
         */
        fun startLayout(line: Int): LineInfo {
            return lineInfo.apply {
                lineNum = lineInfoArrayHelper.getLineNum(line)
                lineViewCount = lineInfoArrayHelper.getViewCount(line)
                lineCenterY = lineInfoArrayHelper.getCenterY(line)
                lineSpaceLeft = lineInfoArrayHelper.getSpaceLeft(line)
            }
        }

        /**
         * 根据[gravity]来布局
         */
        fun layout(view: View?, index: Int, lineIndex: Int, info: LineInfo) {
            view ?: return
            val l = rectArrayHelper.getLeft(index)
            val t = rectArrayHelper.getTop(index)
            val r = rectArrayHelper.getRight(index)
            val b = rectArrayHelper.getBottom(index)
            when (gravity) {
                START -> view.layout(l, t, r, b)
                END -> view.layout(l + info.lineSpaceLeft, t, r + info.lineSpaceLeft, b)
                CENTER_HORIZONTAL -> {
                    //给末尾留下空隙,所以+1
                    val left = info.lineSpaceLeft / (info.lineViewCount + 1)
                    val i = left * (lineIndex + 1)
                    view.layout(l + i, t, r + i, b)
                }
                CENTER_VERTICAL -> {
                    val cy = info.lineCenterY
                    val halfHeight = view.measuredHeight / 2
                    view.layout(l, cy - halfHeight, r, cy + halfHeight)
                }
                CENTER -> {
                    val left = info.lineSpaceLeft / (info.lineViewCount + 1)
                    val i = left * (lineIndex + 1)
                    val cy = info.lineCenterY
                    val halfHeight = view.measuredHeight / 2
                    view.layout(l + i, cy - halfHeight, r + i, cy + halfHeight)
                }
                END_CENTER_VERTICAL -> {
                    val cy = info.lineCenterY
                    val halfHeight = view.measuredHeight / 2
                    val left = info.lineSpaceLeft
                    view.layout(l + left, cy - halfHeight, r + left, cy + halfHeight)
                }
                else -> view.layout(l, t, r, b)
            }
        }
    }

    private class LineInfo {
        fun reset() {
            lineNum = 0
            lineViewCount = 0
            lineCenterY = 0
            lineSpaceLeft = 0
        }

        /**
         * 行号
         */
        var lineNum = 0

        /**
         * 该行view的数量
         */
        var lineViewCount = 0

        /**
         * 该行y轴中心点
         */
        var lineCenterY = 0

        /**
         * 该行剩余的宽度
         */
        var lineSpaceLeft = 0
    }

    private class LineArrayHelper : SpecialArrayHelper(4) {
        fun getLineNum(line: Int) = getVal(line, 0)
        fun getViewCount(line: Int) = getVal(line, 1)
        fun getCenterY(line: Int) = getVal(line, 2)
        fun getSpaceLeft(line: Int) = getVal(line, 3)

        fun add(lineInfo: LineInfo) {
            add(
                lineInfo.lineNum,
                lineInfo.lineViewCount,
                lineInfo.lineCenterY,
                lineInfo.lineSpaceLeft
            )
        }
    }

    @IntDef(START, END, CENTER, CENTER_VERTICAL, CENTER_HORIZONTAL, END_CENTER_VERTICAL)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Gravity

    companion object {
        const val START = 0
        const val END = 1
        const val CENTER = 2
        const val CENTER_VERTICAL = 3
        const val CENTER_HORIZONTAL = 4
        const val END_CENTER_VERTICAL = 5
    }
}