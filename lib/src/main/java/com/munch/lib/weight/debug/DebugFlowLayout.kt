package com.munch.lib.weight.debug

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.munch.lib.helper.array.RectArrayHelper
import com.munch.lib.helper.array.SpecialArrayHelper
import com.munch.lib.weight.Gravity
import com.munch.lib.weight.ViewUpdate

/**
 *
 * @see com.munch.lib.weight.FlowLayout
 *
 *  # 2021 12 03 增加group
 *
 * Create by munch1182 on 2021/8/14 14:40.
 */
class DebugFlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef), ViewUpdate<DebugFlowLayout> {

    init {
        setWillNotDraw(false)
    }

    //行间隔
    var itemLinesSpace = 8

    //子view之间的间隔
    var itemSpace = 8

    var gravityFlags = Gravity.START or Gravity.BOTTOM

    /**
     * 该行子view的最大数量，如果为-1时，则无特别限制
     */
    var maxCountInLine = -1

    private val layoutHelper = LayoutHelper()

    /**
     * 对子view进行分组，同一组的view按照样式分布
     */
    var group: Array<Int>? = null

    override fun set(set: DebugFlowLayout.() -> Unit) {
        super.set(set)
        layoutHelper.gravityFlags = gravityFlags
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
        var allRowsUsedHeight = paddingTop
        //上一行的行高度+本行已计算的高度
        var rowsUsedHeight = allRowsUsedHeight

        //当前组的序号
        var currentGroupIndex = 0
        //当前组的剩余数量
        var currentGroupCount = group?.getOrNull(currentGroupIndex) ?: childCount
        currentGroupCount++

        children.forEachIndexed { index, child ->
            //当前组的数量已经分配完
            if (currentGroupCount <= 0) {
                currentGroupIndex++
                currentGroupCount = group?.getOrNull(currentGroupIndex) ?: childCount
            }
            currentGroupCount--

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
            val countIsMax =
                if (maxCountInLine == -1) true else layoutHelper.lineInfo.lineViewCount < maxCountInLine
            if (index == 0 || (leftWidth >= viewWidth + space && countIsMax && currentGroupCount > 0)) {
                rowsUsedWidth += viewWidth + space
                //如果这个view更高，则更新高度
                if (allRowsUsedHeight + viewHeight > rowsUsedHeight) {
                    rowsUsedHeight = allRowsUsedHeight + viewHeight
                }

                layoutHelper.lineInfo.lineViewCount++
                //本行剩余宽度不够，需要换行
            } else {
                //先更新此行数据
                layoutHelper.lineInfo.apply {
                    lineCenterY = allRowsUsedHeight + (rowsUsedHeight - allRowsUsedHeight) / 2
                    lineSpaceLeft = leftWidth
                    lineTop = allRowsUsedHeight
                    lineBottom = rowsUsedHeight
                }
                layoutHelper.updateLineInfo()
                if (rowsUsedMaxWidth < rowsUsedWidth + paddingRight) {
                    rowsUsedMaxWidth = rowsUsedWidth + paddingRight
                }
                //然后换行
                rowsUsedWidth = paddingLeft + viewWidth
                allRowsUsedHeight = rowsUsedHeight + itemLinesSpace
                rowsUsedHeight = allRowsUsedHeight + viewHeight

                layoutHelper.lineInfo.apply {
                    lineNum++
                    lineViewCount = 1
                }
            }

            //3. 记录每个view的位置:每个位置挨着存放位置，更改布局方式只需要对空余位置进行偏移即可
            val l = rowsUsedWidth - viewWidth + marginL
            val t = allRowsUsedHeight + marginT
            val r = rowsUsedWidth + marginR
            val b = allRowsUsedHeight + viewHeight + marginB
            layoutHelper.updateViewRect(l, t, r, b)
        }
        layoutHelper.lineInfo.apply {
            //是否只有一行
            val oneLine = layoutHelper.lineInfo.lineNum == 0
            //补足数据
            if (oneLine) {
                layoutHelper.lineInfo.lineCenterY = rowsUsedHeight / 2
            } else {
                layoutHelper.lineInfo.lineCenterY =
                    allRowsUsedHeight + (rowsUsedHeight - allRowsUsedHeight) / 2
            }
            lineSpaceLeft = rowsMaxWidth - rowsUsedWidth - paddingRight
            lineTop = allRowsUsedHeight
            lineBottom = rowsUsedHeight
        }
        layoutHelper.updateLineInfo()

        val height =
            if (hMode == MeasureSpec.EXACTLY) heightSize else (rowsUsedHeight + paddingBottom)
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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val count = layoutHelper.lineInfoArrayHelper.getCount()
        val left = paddingLeft.toFloat()
        val right = width.toFloat() - paddingRight.toFloat()
        repeat(count) {
            layoutHelper.startLayout(it).apply {
                canvas.drawRect(left, lineTop.toFloat(), right, lineBottom.toFloat(), paint)
                canvas.drawLine(left, lineCenterY.toFloat(), right, lineCenterY.toFloat(), paint)
            }
        }
    }

    private class LayoutHelper {
        val rectArrayHelper = RectArrayHelper()
        val lineInfoArrayHelper = LineArrayHelper()
        val lineInfo = LineInfo()

        var gravityFlags = Gravity.START or Gravity.BOTTOM

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
                lineTop = lineInfoArrayHelper.getLineTop(line)
                lineBottom = lineInfoArrayHelper.getLineBottom(line)
            }
        }

        /**
         * 根据[gravityFlags]来布局
         */
        fun layout(view: View?, index: Int, lineIndex: Int, info: LineInfo) {
            view ?: return
            var l = rectArrayHelper.getLeft(index)
            var t = rectArrayHelper.getTop(index)
            var r = rectArrayHelper.getRight(index)
            var b = rectArrayHelper.getBottom(index)

            when {
                Gravity.hasFlag(gravityFlags, Gravity.CENTER_HORIZONTAL) -> {
                    //给末尾留下空隙,所以+1
                    val left = info.lineSpaceLeft / (info.lineViewCount + 1)
                    val i = left * (lineIndex + 1)
                    l += i
                    r += i
                }
                Gravity.hasFlag(gravityFlags, Gravity.START) -> {
                    //no op
                }
                Gravity.hasFlag(gravityFlags, Gravity.END) -> {
                    l += info.lineSpaceLeft
                    r += info.lineSpaceLeft
                }
            }

            when {
                Gravity.hasFlag(gravityFlags, Gravity.CENTER_VERTICAL) -> {
                    val cy = info.lineCenterY
                    val halfHeight = view.measuredHeight / 2
                    t = cy - halfHeight
                    b = cy + halfHeight
                }
                Gravity.hasFlag(gravityFlags, Gravity.TOP) -> {
                    //no op
                }
                Gravity.hasFlag(gravityFlags, Gravity.BOTTOM) -> {
                    t = info.lineBottom - view.measuredHeight
                    b = info.lineBottom
                }
            }
            view.layout(l, t, r, b)
        }
    }

    private class LineInfo {
        fun reset() {
            lineNum = 0
            lineViewCount = 0
            lineCenterY = 0
            lineSpaceLeft = 0
            lineTop = 0
            lineBottom = 0
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

        /**
         * 该行顶部位置
         */
        var lineTop = 0

        /**
         * 该行底部位置
         */
        var lineBottom = 0
    }

    private class LineArrayHelper : SpecialArrayHelper(6) {
        fun getLineNum(line: Int) = getVal(line, 0)
        fun getViewCount(line: Int) = getVal(line, 1)
        fun getCenterY(line: Int) = getVal(line, 2)
        fun getSpaceLeft(line: Int) = getVal(line, 3)
        fun getLineTop(line: Int) = getVal(line, 4)
        fun getLineBottom(line: Int) = getVal(line, 5)

        fun add(lineInfo: LineInfo) {
            add(
                lineInfo.lineNum,
                lineInfo.lineViewCount,
                lineInfo.lineCenterY,
                lineInfo.lineSpaceLeft,
                lineInfo.lineTop,
                lineInfo.lineBottom,
            )
        }
    }
}