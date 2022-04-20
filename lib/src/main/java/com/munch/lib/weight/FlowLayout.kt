package com.munch.lib.weight

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.munch.lib.R
import com.munch.lib.Testable
import com.munch.lib.extend.OnViewUpdate
import com.munch.lib.extend.dp2Px
import com.munch.lib.extend.drawRectLine
import com.munch.lib.extend.testPaint
import com.munch.lib.helper.array.RectArrayHelper
import com.munch.lib.helper.array.SpecialIntArrayHelper

/**
 *
 * 1. measure时进行分行，并将子view的位置紧挨存放
 * 2. layout时根据gravity来均分间隔
 *
 * Create by munch1182 on 2022/3/8 16:40.
 */
@Testable
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef), OnViewUpdate<FlowLayout> {

    companion object {

        const val STYLE_SPREAD = 0
        const val STYLE_PACKED = 1
    }

    private val dp8 by lazy { context.dp2Px(8f).toInt() }

    //行间隔
    var lineSpace = 0

    //子view之间的间隔
    var itemSpace = 0

    /**
     * 注意，使用的时[Gravity]，而不是系统的Gravity
     */
    var gravity: Int = Gravity.START or Gravity.TOP

    /**
     * 每行子view的最大数量，如果为0时，则无特别限制
     */
    var maxCountInLine = 0

    var style = STYLE_SPREAD

    private var test = false
    private val testPaint by testPaint()

    private val layoutHelper = LayoutHelper()

    private val config = Config(this)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FlowLayout).apply {
            test = getBoolean(R.styleable.FlowLayout_test, false)
            lineSpace = getDimensionPixelOffset(R.styleable.FlowLayout_flow_lineSpace, dp8 / 2)
            itemSpace = getDimensionPixelOffset(R.styleable.FlowLayout_flow_itemSpace, dp8)
            maxCountInLine = getInt(R.styleable.FlowLayout_flow_maxCountInLine, 0)
            gravity = getInt(R.styleable.FlowLayout_gravity, Gravity.START or Gravity.TOP)
            style = getInt(R.styleable.FlowLayout_flow_style, STYLE_SPREAD)
        }.recycle()
        setWillNotDraw(!test)
        layoutHelper.updateConfig(config.updateFrom(this))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        layoutHelper.updateConfig(config.updateFrom(this))

        layoutHelper.updateMaxWidth(widthSize - paddingLeft - paddingRight)
        if (layoutHelper.needMeasure()) {
            layoutHelper.startMeasure()
            children.forEach { child ->
                when {
                    child is Space -> layoutHelper.wrapLine()
                    child.visibility == View.GONE -> {}
                    else -> {
                        //1. 测量child
                        measureChild(child, widthMeasureSpec, heightMeasureSpec)
                        layoutHelper.measure(child)
                    }
                }
            }
            layoutHelper.stopMeasure()
        }

        val height =
            if (hMode == MeasureSpec.EXACTLY) heightSize
            else (layoutHelper.usedMaxHeight + paddingTop + paddingBottom)
        val width =
            if (wMode == MeasureSpec.EXACTLY) widthSize else (layoutHelper.usedMaxWidth)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val views = children.filter { it !is Space && it.visibility != View.GONE }.toList()
        layoutHelper.layout(this, views, r - l)
    }

    private data class Config(
        var lineSpace: Int = 8,
        var itemSpace: Int = 8,
        @Gravity
        var gravity: Int = Gravity.START,
        var style: Int = STYLE_SPREAD,
        var maxCountInLine: Int = 0
    ) {

        constructor(flow: FlowLayout) : this(
            flow.lineSpace,
            flow.itemSpace,
            flow.gravity,
            flow.style,
            flow.maxCountInLine
        )

        fun shouldMeasure(c: Config): Boolean {
            return c.itemSpace != itemSpace || c.lineSpace != itemSpace || c.maxCountInLine != maxCountInLine
        }

        fun updateFrom(flow: FlowLayout): Config {
            lineSpace = flow.lineSpace
            itemSpace = flow.itemSpace
            gravity = flow.gravity
            style = flow.style
            maxCountInLine = flow.maxCountInLine
            return this
        }
    }

    private class LayoutHelper {

        private var currView = ViewWrapper()
        private var rectHelper = RectArrayHelper()
        private var nextViewWrapFlag = false

        private val lineArray = LineArrayHelper()
        private val currLine = LineInfo()
        var currConfig = Config()
            private set

        //用于记录当前已经使用的高度和宽度
        var usedMaxHeight: Int = 0
        var usedMaxWidth: Int = 0

        //用于记录每一行已经添加的宽度
        //并不是最后实际要占用的宽度，还需要加上currItemSpace才是
        var currLineWidthUsed = 0

        //测量时能够使用的最大宽度
        var maxWidthCanUse = 0

        //该行itemSpace需要占用的宽度
        val currItemSpace: Int
            //除去第一个view，其余view都需要itemSpace
            get() = (currLine.lineViewCount - 1) * currConfig.itemSpace


        fun startMeasure() {
            currView.reset()
            rectHelper.clear()
            nextViewWrapFlag = false

            lineArray.clear()
            currLine.reset()
            currLine.lineSpaceLeft = maxWidthCanUse
            currLineWidthUsed = 0

            usedMaxHeight = 0
            usedMaxWidth = 0

        }

        private fun newLine() {
            endLine()
            //在转到新行
            val lastLineNum = currLine.lineNum
            val lastLineBottom = currLine.lineBottom

            currLine.reset()
            currLine.lineSpaceLeft = maxWidthCanUse

            currLine.lineNum = lastLineNum + 1
            currLine.lineTop = lastLineBottom
            //第一行外，每一行顶部加上lineSpace
            if (currLine.lineNum > 0) {
                currLine.lineTop += currConfig.lineSpace
            }

            currLineWidthUsed = 0
        }

        private fun endLine() {
            //先保存此行数据
            lineArray.add(currLine)
            //更新记录的数据
            if (usedMaxHeight < currLine.lineBottom) {
                usedMaxHeight = currLine.lineBottom
            }
            val nowWidth = currLineWidthUsed + currItemSpace
            if (usedMaxWidth < nowWidth) {
                usedMaxWidth = nowWidth
            }
        }

        private fun addView(view: ViewWrapper, rectHelper: RectArrayHelper) {
            currLine.lineViewCount++

            //记录位置的时候不记录itemSpace，直接挨着放
            //但是判断宽度的时候实际上计算了itemSpace(这样可以方便center的计算)
            val l = currLineWidthUsed
            val t = currLine.lineTop
            val r = l + view.childNeedWidth
            val b = t + view.childNeedHeight
            rectHelper.add(l, t, r, b)
            currLine.lineSpaceLeft -= view.childNeedWidth
            if (currLine.lineBottom < b) {
                currLine.lineBottom = b
            }

            currLineWidthUsed = r
        }

        fun updateConfig(config: Config) {
            currConfig = config
        }

        fun updateMaxWidth(width: Int) {
            maxWidthCanUse = width
        }

        fun needMeasure(): Boolean {
            return true
        }


        fun wrapLine() {
            nextViewWrapFlag = true
        }

        fun measure(child: View) {
            currView.update(child)
            //2. 判断是否换行
            val needWrap = needWrapLine(currView)
            //3. 处理换行和不换行
            //3.1 处理换行
            if (needWrap) {
                //转到新行
                newLine()
            }
            //记录每个view的位置:每个位置挨着存放位置，更改布局方式只需要对空余位置进行偏移即可
            addView(currView, rectHelper)
        }

        fun stopMeasure() {
            endLine()
        }

        fun layout(view: FlowLayout, views: List<View>, width: Int) {
            var line = setLineFromArray(0)
            var layoutViewCount = 0
            while (line != null) {
                //因为currLine.lineSpaceLeft是根据测量是可用的最大宽度计算的
                //而此处的宽度可能与当时的最大宽度不同
                currLine.lineSpaceLeft -= maxWidthCanUse - width
                if (currLine.lineSpaceLeft < 0) {
                    currLine.lineSpaceLeft = 0
                }
                //去掉itemSpace后剩余的空间
                val lineLeft = currLine.lineSpaceLeft - currItemSpace
                //均分剩余空间
                val spaceAvgSpread = lineLeft / (line.lineViewCount + 1)
                val spaceAvgPacked = lineLeft / 2
                val gravity = currConfig.gravity
                repeat(line.lineViewCount) {
                    val viewIndex = layoutViewCount + it
                    val l = rectHelper.getLeft(viewIndex)
                    val t = rectHelper.getTop(viewIndex)
                    val r = rectHelper.getRight(viewIndex)
                    val b = rectHelper.getBottom(viewIndex)
                    var xOffset = it * currConfig.itemSpace + view.paddingLeft
                    var yOffset = view.paddingTop
                    when {
                        Gravity.hasFlag(gravity, Gravity.CENTER_HORIZONTAL) -> {
                            xOffset += if (currConfig.style == STYLE_SPREAD) {
                                spaceAvgSpread * (it + 1)
                            } else {
                                spaceAvgPacked
                            }
                        }
                        Gravity.hasFlag(gravity, Gravity.START) -> {
                        }
                        Gravity.hasFlag(gravity, Gravity.END) -> {
                            xOffset += lineLeft
                        }
                    }
                    when {
                        Gravity.hasFlag(gravity, Gravity.CENTER_VERTICAL) -> {
                            yOffset = (currLine.lineBottom - b) / 2
                        }
                        Gravity.hasFlag(gravity, Gravity.TOP) -> {
                        }
                        Gravity.hasFlag(gravity, Gravity.BOTTOM) -> {
                            yOffset = currLine.lineBottom - b
                        }
                    }
                    views[viewIndex].layout(l + xOffset, t + yOffset, r + xOffset, b + yOffset)
                }

                layoutViewCount += line.lineViewCount
                line = setLineFromArray(line.lineNum + 1)
            }
        }

        fun setLineFromArray(index: Int): LineInfo? {
            if (lineArray.getCount() <= index) {
                return null
            }
            return currLine.apply {
                lineNum = lineArray.getLineNum(index)
                lineViewCount = lineArray.getViewCount(index)
                lineSpaceLeft = lineArray.getSpaceLeft(index)
                lineTop = lineArray.getLineTop(index)
                lineBottom = lineArray.getLineBottom(index)
            }
        }


        /**
         * 判断该child是否需要换行
         */
        private fun needWrapLine(child: ViewWrapper): Boolean {
            //2. 判断是否能够放入此行
            //2.1判断是否有换行占位view
            if (nextViewWrapFlag) {
                nextViewWrapFlag = false
                //如果有换行标志，则强制换行
                return true
            }
            //2.2 如果这是该行第一个，则直接放入此行
            if (currLine.lineViewCount == 0) {
                return false
            }
            //2.3 判断剩余空间是否足够
            //2.3.1 判断是否还有剩余宽度
            if (currLine.lineSpaceLeft <= 0) {
                return true
            }
            //2.3.2，如果不是第一个，则判断是否剩余有childNeedWidth+itemSpace的宽度
            //因为currentLine.lineSpaceLeft没有记录itemSpace，所以此处要计算
            //因为itemSpace代表的时view左侧的space，所以必须要有
            val hasWidth =
                child.childNeedWidth + currConfig.itemSpace <= maxWidthCanUse - currLineWidthUsed - currItemSpace
            if (!hasWidth) {
                return true
            }
            //2.3.3 判断是否设置了每行最大个数且是否超过此个数
            val isOverCount =
                currConfig.maxCountInLine > 0 && currLine.lineViewCount >= currConfig.maxCountInLine
            if (isOverCount) {
                return true
            }
            return false
        }

    }

    private class ViewWrapper {
        private var viewWidth = 0
        private var viewHeight = 0

        private var marginL = 0
        private var marginR = 0
        private var marginT = 0
        private var marginB = 0

        var view: View? = null

        fun update(child: View) {
            view = child
            viewWidth = child.measuredWidth
            viewHeight = child.measuredHeight
            val lp = child.layoutParams
            if (lp is MarginLayoutParams) {
                marginL = lp.leftMargin
                marginT = lp.topMargin
                marginR = lp.rightMargin
                marginB = lp.bottomMargin
            }
        }

        fun reset() {
            viewWidth = 0
            viewHeight = 0
            marginL = 0
            marginR = 0
            marginT = 0
            marginB = 0
        }

        val childNeedWidth: Int
            get() = viewWidth + marginL + marginR

        val childNeedHeight: Int
            get() = viewHeight + marginR + marginB
    }

    private class LineArrayHelper : SpecialIntArrayHelper(5) {
        fun getLineNum(line: Int) = getVal(line, 0)
        fun getViewCount(line: Int) = getVal(line, 1)
        fun getSpaceLeft(line: Int) = getVal(line, 2)
        fun getLineTop(line: Int) = getVal(line, 3)
        fun getLineBottom(line: Int) = getVal(line, 4)

        fun add(lineInfo: LineInfo) {
            add(
                lineInfo.lineNum,
                lineInfo.lineViewCount,
                lineInfo.lineSpaceLeft,
                lineInfo.lineTop,
                lineInfo.lineBottom,
            )
        }
    }

    private data class LineInfo(
        //行号
        var lineNum: Int = 0,
        //该行view的数量
        var lineViewCount: Int = 0,
        //该行剩余的宽度
        var lineSpaceLeft: Int = 0,
        //该行顶部位置
        var lineTop: Int = 0,
        //该行底部位置
        var lineBottom: Int = 0
    ) {

        fun reset() {
            lineNum = 0
            lineViewCount = 0
            lineSpaceLeft = 0
            lineTop = 0
            lineBottom = 0
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (test) {
            var line = layoutHelper.setLineFromArray(0)
            var viewCount = 0
            while (line != null) {
                canvas.drawRectLine(
                    0f,
                    line.lineTop.toFloat(),
                    (layoutHelper.maxWidthCanUse - line.lineSpaceLeft + layoutHelper.currItemSpace).toFloat(),
                    line.lineBottom.toFloat(),
                    testPaint
                )
                viewCount += line.lineViewCount

                line = layoutHelper.setLineFromArray(line.lineNum + 1)
            }
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

    override fun updateView(update: FlowLayout.() -> Unit) {
        update.invoke(this)
        requestLayout()
    }
}