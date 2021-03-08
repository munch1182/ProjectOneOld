package com.munch.lib.test.view


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef

/**
 * 没有写宽高间距因为可以用内部View的margin和padding实现
 * 需要简单的FlowLayout可以直接用{@see com.google.android.material.internal.FlowLayout}
 * 此类相比实现了{@see com.munch.test.view.weight.FlowLayout.Gravity}
 *
 * Create by munch on 2020/9/4 13:32
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef) {

    private var layoutHelper = LayoutHelper()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        layoutHelper.reset()

        var child: View?
        //可用宽度
        val rowsMaxWidth = widthSize - paddingLeft - paddingRight
        //已用宽度
        var rowsUsedWidth = paddingLeft + paddingRight
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
            var leftMargin = 0
            var rightMargin = 0
            var topMargin = 0
            var bottomMargin = 0
            //需要重写generateLayoutParams
            if (child.layoutParams is MarginLayoutParams) {
                val params = child.layoutParams as MarginLayoutParams
                leftMargin = params.leftMargin
                rightMargin = params.rightMargin
                topMargin = params.topMargin
                bottomMargin = params.bottomMargin
            }
            childWidth += leftMargin + rightMargin
            childHeight += topMargin + bottomMargin

            leftWidth = rowsMaxWidth - rowsUsedWidth
            //不换行
            //i==0时走上面的判断，否则如果第一个宽度过大会显示错位
            if (i == 0 || leftWidth >= childWidth) {
                rowsUsedWidth += childWidth
                //某些单个控件更高则更新高度
                if (lastRowHeight + childHeight > columnHeight) {
                    columnHeight = lastRowHeight + childHeight
                }
                if (i == 0) {
                    layoutHelper.lineNum = 0
                    layoutHelper.lineViewCount = 1
                } else {
                    layoutHelper.lineViewCount++
                }
                //换行
            } else {
                layoutHelper.lineCenterY = lastRowHeight + (columnHeight - lastRowHeight) / 2
                layoutHelper.spaceLeft = leftWidth
                layoutHelper.updateLines()
                //下一行
                rowsUsedWidth = paddingLeft + childWidth + paddingRight
                lastRowHeight = columnHeight
                columnHeight += childHeight

                layoutHelper.lineNum++
                layoutHelper.lineViewCount = 1
            }

            layoutHelper.l = rowsUsedWidth - childWidth + leftMargin
            layoutHelper.t = lastRowHeight + topMargin
            layoutHelper.r = rowsUsedWidth - rightMargin
            layoutHelper.b = lastRowHeight + childHeight - bottomMargin
            layoutHelper.updateRect()
        }
        //是否只有一行
        val oneLine = layoutHelper.lineArray.size == 0

        var width = if (!oneLine) widthSize else rowsUsedWidth
        var height = columnHeight + paddingBottom

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        }
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        }

        //补足数据
        if (oneLine) {
            layoutHelper.lineCenterY = heightSize / 2
        } else {
            layoutHelper.lineCenterY = lastRowHeight + (columnHeight - lastRowHeight) / 2
        }
        layoutHelper.spaceLeft = rowsMaxWidth - rowsUsedWidth
        layoutHelper.updateLines()

        setMeasuredDimension(width, height)
    }

    override fun onLayout(change: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutHelper.startLayout(0)
        var lineCount = layoutHelper.lineViewCount
        var lineIndex = 0
        for (i in 0..childCount) {
            //获取下一行数据
            if (i >= lineCount) {
                layoutHelper.startLayout(layoutHelper.lineNum + 1)
                lineCount += layoutHelper.lineViewCount
                lineIndex = 0
            }
            layoutHelper.layout(getChildAt(i), i, lineIndex)
            lineIndex++
        }
    }

    fun setGravity(@Gravity gravity: Int) {
        layoutHelper.gravity = gravity
        requestLayout()
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

        /**
         * 每一个子view的位置信息，4个为一组，以l、t、r、b顺序储存
         * @see LayoutHelper.updateRect
         */
        var rectArray = ArrayList<Int>()

        /**
         * 每一行信息，4个数据一组
         * @see LayoutHelper.updateLines
         */
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
                else -> view.layout(l, t, r, b)
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