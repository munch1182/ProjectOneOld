package com.munch.lib.android.weight

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import com.munch.lib.android.R
import com.munch.lib.android.extend.dp2Px
import com.munch.lib.android.extend.paddingHorizontal
import com.munch.lib.android.extend.paddingVertical
import com.munch.lib.android.helper.array.SpecialArrayHelper
import kotlin.math.max
import kotlin.math.min

/**
 * 子view的容器, 横向排列, 当剩余宽度不够时, 自动换行排列
 *
 * [Sign]可作为换行标准的view
 *
 * 不支持内容滑动
 * 子view不支持margin
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef), ViewUpdate<FlowLayout> {

    private val layoutInfo = LineInfoArrayHelper()
    private val curr = LineInfo()

    var itemSpace = 0 // view之间的间隔
    var lineSpace = 0 // 行之间的间隔
    var maxCountInLine = 0 // 一行允许排列的最大子view个数, 超过这个数将自动换行, 如果为0则不限

    /**
     * 子view的布局位置
     *
     * 注意: 如果使用[Gravity.CENTER_HORIZONTAL], 则会使得[itemSpace]无效,
     * 而此时如果[getClipToPadding]为false, 子view会整体上水平居中
     */
    var gravity: Int = Gravity.TOP or Gravity.START

    init {
        val dp8 = context.dp2Px(8f).toInt()
        context.obtainStyledAttributes(attrs, R.styleable.FlowLayout).apply {
            lineSpace = getDimensionPixelOffset(R.styleable.FlowLayout_flow_lineSpace, dp8)
            itemSpace = getDimensionPixelOffset(R.styleable.FlowLayout_flow_itemSpace, dp8)
            maxCountInLine = getInt(R.styleable.FlowLayout_flow_maxCountInLine, 0)
            gravity = getInt(R.styleable.FlowLayout_android_gravity, Gravity.NO_GRAVITY)
        }.recycle()
    }

    override fun update(update: FlowLayout.() -> Unit) {
        update.invoke(this)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = MeasureSpec.getSize(widthMeasureSpec)
        val maxW = w - paddingHorizontal

        var usedW = 0
        var usedH = 0

        var currTop = 0
        var spaceLeft = maxW // 重设剩余宽度
        curr.nextLine()

        children.forEach {

            val isSign = it is Sign
            if (it.isGone && !isSign) return@forEach

            if (!isSign) {
                measureChild(it, widthMeasureSpec, heightMeasureSpec)
            }

            val childW = if (isSign) 0 else it.measuredWidth
            val childH = if (isSign) 0 else it.measuredHeight

            if (it is Sign // 如果这个view仅是换行标记
                || (maxCountInLine > 0 && curr.viewCount >= maxCountInLine) // 超过限定的数量
                || ((childW + itemSpace) > spaceLeft  // 剩余宽度不够(如果不是第一个, 则其实际占用需要加上itemSpace)
                        && curr.viewCount > 0) // 且不是行第一个view(如果是此行第一个, 不管实际宽度都必须放下)
            ) { // 则需要换行
                // 记录已使用宽度(不直接计算剩余宽度因为此时计算已经最大宽度,但setMeasuredDimension的是使用宽度)
                curr.widthUsed = min(maxW - spaceLeft, maxW)
                layoutInfo.put(curr) // 保存行数据
                usedW = max(usedW, maxW - spaceLeft)
                usedH = max(usedH, curr.bottom) // 计算使用宽高

                currTop = curr.bottom + lineSpace // 更新行Top位置
                curr.nextLine()
                spaceLeft = maxW // 重设最大宽度
            }
            if (it is Sign) {
                return@forEach
            }

            spaceLeft -= (childW + (if (curr.viewCount == 0) 0 else itemSpace))
            curr.viewCount += 1
            curr.top = currTop
            curr.bottom = max(curr.bottom, curr.top + childH) // 最高的view的高度即行的高度
        }

        curr.widthUsed = min(maxW - spaceLeft, maxW)
        layoutInfo.put(curr) // 保存最后一行的行数据
        usedW = max(usedW, maxW - spaceLeft)
        usedH = max(usedH, curr.bottom) // 计算使用宽高

        val modeW = MeasureSpec.getMode(widthMeasureSpec)
        val finW = if (modeW == MeasureSpec.EXACTLY) {
            w
        } else {
            usedW + paddingHorizontal
        }
        val modeH = MeasureSpec.getMode(heightMeasureSpec)
        val finH = if (modeH == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            usedH + paddingVertical
        }

        setMeasuredDimension(finW, finH)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        var level = 0
        var widthLeft: Int
        curr.nextLine() // 清除数据
        val maxW = width

        var left = 0
        var top = paddingTop
        var right = 0
        var bottom = 0

        var viewSpace = 0
        var count = 0


        children.forEach {

            if (it.isGone) return@forEach

            val w = it.measuredWidth
            val h = it.measuredHeight


            if (count <= 0) { // 如果本行无剩余view, 或者是第一个

                layoutInfo.get(level++, curr) // 读取下一行信息
                count = curr.viewCount

                // 测量时依据的是最大宽度, 但设置宽度时为使用宽度, 因此要重新计算剩余宽度
                widthLeft = maxW - paddingHorizontal - curr.widthUsed

                // TODO:  111
            } else {
                left = right + viewSpace // 下一个view的left为上一个view的right+space
            }

            right = left + w + viewSpace
            bottom = top + h

            it.layout(left, top, right, bottom)
            count--
        }
    }

    private class LineInfoArrayHelper : SpecialArrayHelper<Int>(4) {

        fun get(level: Int, lineInfo: LineInfo) = lineInfo.apply {
            viewCount = getVal(level, 0)
            widthUsed = getVal(level, 1)
            top = getVal(level, 2)
            bottom = getVal(level, 3)
        }

        fun put(lineInfo: LineInfo): LineInfoArrayHelper {
            add(lineInfo.viewCount, lineInfo.widthUsed, lineInfo.top, lineInfo.bottom)
            return this
        }
    }

    private class LineInfo {

        /**
         * 本行view的个数
         */
        var viewCount = 0

        /**
         * 本行使用的宽度(padding已计算在占用中)
         */
        var widthUsed = 0

        /**
         * 此行的top
         */
        var top = 0

        /**
         * 此行的bottom
         */
        var bottom = 0

        val lineHeight: Int
            get() = bottom - top

        fun nextLine() {
            viewCount = 0
            widthUsed = 0
            top = 0
            bottom = 0
        }

        override fun toString(): String {
            return "LineInfo(viewCount=$viewCount, widthUsed=$widthUsed, top=$top, bottom=$bottom)"
        }
    }
}