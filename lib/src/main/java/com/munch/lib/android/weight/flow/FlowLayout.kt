package com.munch.lib.android.weight.flow

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.munch.lib.android.helper.array.RectArrayHelper
import com.munch.lib.android.helper.array.SpecialIntArrayHelper
import com.munch.lib.android.weight.Gravity
import com.munch.lib.android.weight.Space

/**
 * Create by munch1182 on 2022/3/8 16:40.
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef) {

    //行间隔
    var lineSpace = 8

    //子view之间的间隔
    var itemSpace = 8

    @Gravity
    var gravity: Int = Gravity.START

    /**
     * 每行子view的最大数量，如果为0时，则无特别限制
     */
    var maxCountInLine = 0

    private val layoutHelper = LayoutHelper()

    init {
        layoutHelper.updateConfig(Config(this))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (layoutHelper.needMeasure()) {
            layoutHelper.reset()

            //当前使用的最大宽度
            layoutHelper.startMeasure(widthSize)

            children.forEachIndexed { index, child ->
                when {
                    child is Space -> layoutHelper.wrapLine()
                    child.visibility == View.GONE -> {}
                    else -> {
                        //1. 测量child
                        measureChild(child, widthMeasureSpec, heightMeasureSpec)
                        layoutHelper.measure(index, child)
                    }
                }
            }
            layoutHelper.stopMeasure()
        }

        val height =
            if (hMode == MeasureSpec.EXACTLY) heightSize else (layoutHelper.getUsedMaxHeight() + paddingBottom)
        val width =
            if (wMode == MeasureSpec.EXACTLY) widthSize else layoutHelper.getUsedMaxWidth() + paddingRight
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var viewIndex = 0
        var lineIndex = 0
        var lineInfo = layoutHelper.startLayout(lineIndex)
        var lineViewCount = 0
        children.forEach { view ->
            if (view is Space || view.visibility == GONE) {
                return@forEach
            }
            layoutHelper.layout(view, viewIndex, lineInfo, lineViewCount)
            lineViewCount++
            lineInfo?.let {
                //获取下一行的信息
                if (it.lineViewCount <= lineViewCount) {
                    lineIndex++
                    lineInfo = layoutHelper.startLayout(lineIndex)
                    lineViewCount = 0
                }
            }
            viewIndex++
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

    private data class Config(
        var lineSpace: Int = 8,
        var itemSpace: Int = 8,
        @Gravity
        var gravity: Int = Gravity.START,
        var maxCountInLine: Int = 0,
        var paddingLeft: Int = 0,
        var paddingTop: Int = 0,
        var paddingRight: Int = 0,
        var paddingBottom: Int = 0,
    ) {

        constructor(flow: FlowLayout) : this(
            flow.lineSpace,
            flow.itemSpace,
            flow.gravity,
            flow.maxCountInLine,
            flow.paddingLeft,
            flow.paddingTop,
            flow.paddingRight,
            flow.paddingBottom
        )
    }

    private class LayoutHelper {
        /**
         * 可用的最大宽度
         */
        private var lineMaxWidth = 0

        /**
         * 已用的宽度
         */
        private var viewUsedWidth = 0
        private var viewUsedHeight = 0

        fun getUsedMaxWidth() = viewUsedWidth
        fun getUsedMaxHeight() = viewUsedHeight

        private val viewRect = RectArrayHelper()
        private val lineInfoArrayHelper = LineArrayHelper()
        private var nextLineFlag = false

        private val currentLineInfo = LineInfo()
        private val currentViewWrapper = MeasureViewWrapper()
        private var currentConfig: Config = Config()

        fun reset() {
            lineMaxWidth = 0
            viewRect.clear()
            currentLineInfo.reset()
            lineInfoArrayHelper.clear()
            nextLineFlag = false

            viewUsedWidth = 0
            viewUsedHeight = 0
        }

        fun startMeasure(widthSize: Int) {
            lineMaxWidth = widthSize - currentConfig.paddingLeft - currentConfig.paddingRight
            //增加首行的left+top的space
            currentLineInfo.newLine(
                lineMaxWidth - currentConfig.paddingLeft - currentConfig.itemSpace,
                currentLineInfo.lineBottom + currentConfig.lineSpace
            )
        }

        fun measure(index: Int, child: View) {
            currentViewWrapper.update(child)
            //2. 判断是否换行
            val needWrap = needWrapLine(index, child)
            //3. 处理换行和不换行
            //3.1 处理换行
            if (needWrap) {
                //先更新此行数据
                currentLineInfo.endLine()
                //记录此行数据
                lineInfoArrayHelper.add(currentLineInfo)
                //转到新行
                currentLineInfo.newLine(
                    lineMaxWidth - currentConfig.paddingLeft - currentConfig.itemSpace,
                    currentLineInfo.lineBottom + currentConfig.lineSpace
                )
                //并在新行添加view
            }
            currentLineInfo.addView(currentViewWrapper)
            //记录每个view的位置:每个位置挨着存放位置，更改布局方式只需要对空余位置进行偏移即可
            val t = currentLineInfo.lineTop
            val r = lineMaxWidth - currentLineInfo.lineSpaceLeft
            val l = r - currentViewWrapper.childNeedWidth
            val b = currentLineInfo.lineTop + currentViewWrapper.childNeedHeight

            viewRect.add(l, t, r, b)

            viewUsedWidth = viewUsedWidth.coerceAtLeast(r)
            viewUsedHeight = viewUsedHeight.coerceAtLeast(b)
        }

        /**
         * 判断该child是否需要换行
         */
        private fun needWrapLine(index: Int, child: View): Boolean {
            //2. 判断是否能够放入此行
            //2.1 第一个view单独处理，避免第一个宽度过大显示错误
            if (index == 0) {
                return false
            }
            //2.2判断是否有换行占位view
            if (nextLineFlag) {
                nextLineFlag = false
                //如果有换行标志，则强制换行
                return true
            }
            //2.3 判断是否设置了每行最大个数且是否超过此个数
            val isMaxCount =
                currentConfig.maxCountInLine > 0 && currentLineInfo.lineViewCount >= currentConfig.maxCountInLine
            if (isMaxCount) {
                return true
            }
            //2.4 判断剩余空间是否足够
            var childWidth = child.measuredWidth
            val lp = child.layoutParams
            if (lp is MarginLayoutParams) {
                childWidth += lp.leftMargin + lp.rightMargin
            }
            //判断空间时加上了itemSpace
            return childWidth + currentConfig.itemSpace > currentLineInfo.lineSpaceLeft
        }

        /**
         * 强制下一个view换行
         */
        fun wrapLine() {
            nextLineFlag = true
        }

        fun updateConfig(config: Config) {
            currentConfig = config
        }

        /**
         * 从[lineInfoArrayHelper]中还原[line]行的信息到[currentLineInfo]
         */
        fun startLayout(line: Int): LineInfo? {
            if (lineInfoArrayHelper.getCount() <= line) {
                return null
            }
            return currentLineInfo.apply {
                lineNum = lineInfoArrayHelper.getLineNum(line)
                lineViewCount = lineInfoArrayHelper.getViewCount(line)
                lineCenterY = lineInfoArrayHelper.getCenterY(line)
                lineSpaceLeft = lineInfoArrayHelper.getSpaceLeft(line)
                lineTop = lineInfoArrayHelper.getLineTop(line)
                lineBottom = lineInfoArrayHelper.getLineBottom(line)
            }
        }

        fun stopMeasure() {
            currentLineInfo.endLine()
            lineInfoArrayHelper.add(currentLineInfo)
            //增加right+bottom的space
            viewUsedWidth += currentConfig.itemSpace
            viewUsedHeight += currentConfig.lineSpace
        }

        /**
         * 根据[gravity]来布局
         *
         * @param lineIndex 该view位于此行的位置，如果line为null，则此值w无效
         **/
        fun layout(view: View?, index: Int, line: LineInfo?, lineIndex: Int) {
            view ?: return
            var l = viewRect.getLeft(index) + currentConfig.itemSpace
            var t = viewRect.getTop(index)
            var r = viewRect.getRight(index)
            var b = viewRect.getBottom(index)
            var xOffset = 0
            var yOffset = 0
            line?.let {
                val gravity = currentConfig.gravity
                when {
                    Gravity.hasFlag(gravity, Gravity.CENTER_HORIZONTAL) -> {
                        xOffset = it.lineSpaceLeft / it.lineViewCount
                    }
                    Gravity.hasFlag(gravity, Gravity.START) -> {}
                    Gravity.hasFlag(gravity, Gravity.END) -> {
                        xOffset = it.lineSpaceLeft
                    }
                }
                when {
                    Gravity.hasFlag(gravity, Gravity.CENTER_VERTICAL) -> {
                        t = it.lineCenterY - viewRect.getHeight(index) / 2
                        b = it.lineCenterY + viewRect.getHeight(index) / 2
                    }
                    Gravity.hasFlag(gravity, Gravity.TOP) -> {}
                    Gravity.hasFlag(gravity, Gravity.BOTTOM) -> {
                        yOffset = it.lineBottom - t
                    }
                }
            }
            view.layout(l + xOffset, t + yOffset, r + xOffset, b + yOffset)
        }

        /**
         * 如何判断是否是内部view的宽高变化？
         */
        fun needMeasure(): Boolean {
            return true
        }
    }

    private class MeasureViewWrapper {
        private var viewWidth = 0
        private var viewHeight = 0

        private var marginL = 0
        private var marginR = 0
        private var marginT = 0
        private var marginB = 0

        fun update(child: View) {
            viewWidth = child.measuredWidth
            viewHeight = child.measuredHeight
            val lp = child.layoutParams
            if (lp is MarginLayoutParams) {
                marginL = lp.leftMargin
                marginT = lp.topMargin
                marginR = lp.topMargin
                marginB = lp.bottomMargin
            }
        }

        val childNeedWidth: Int
            get() = viewWidth + marginL + marginR

        val childNeedHeight: Int
            get() = viewHeight + marginR + marginB

    }

    private class LineArrayHelper : SpecialIntArrayHelper(6) {
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

    private class LineInfo {
        fun reset() {
            lineNum = 0
            lineViewCount = 0
            lineCenterY = 0
            lineSpaceLeft = 0
            lineTop = 0
            lineBottom = 0
        }

        fun newLine(spaceLeft: Int, top: Int) {
            lineNum++
            lineViewCount = 0
            lineCenterY = 0
            lineSpaceLeft = spaceLeft
            lineTop = top
            lineBottom = top
        }

        fun endLine() {
            lineCenterY = lineTop + (lineBottom - lineTop) / 2
        }

        fun addView(view: MeasureViewWrapper) {
            lineViewCount++
            lineSpaceLeft -= view.childNeedWidth
            val nextBottom = lineTop + view.childNeedHeight
            if (lineBottom < nextBottom) {
                lineBottom = nextBottom
            }
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
}