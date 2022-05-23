package com.munch.lib.weight.wheelview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.drawTextInCenter
import com.munch.lib.extend.measureTextBounds
import java.lang.Integer.max

/**
 * Create by munch1182 on 2022/5/23 16:41.
 */
class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {


    private var onItem = NumberItemListener()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var textSize = 16f
    var textColor = Color.BLACK

    /**
     * item之间的间距
     */
    var itemPadding = 24f

    /**
     * 所有要显示的数量，只能是奇数
     * 3即中间一个上下各一个
     */
    var itemNumber = 3
        set(value) {
            if (value % 2 == 0) {
                throw IllegalStateException("must be odd numbers")
            }
            field = value
        }

    private val count: Int
        get() = (itemNumber - 1) / 2
    private val itemRect = Rect()

    private val center = PointF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        updatePaint()
        val max = onItem.maxWidthStr()
        paint.measureTextBounds(max, itemRect)
        val width = max(itemRect.width(), w) + paddingLeft + paddingRight
        val height = max(itemRect.height() * itemNumber, h) + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        itemRect.set(
            0,
            0,
            w - paddingLeft - paddingRight,
            (h - paddingTop - paddingBottom) / itemNumber
        )
        center.set(
            itemRect.width() / 2f + paddingLeft,
            itemRect.height() / 2f + paddingTop
        )
    }

    private fun updatePaint() {
        paint.apply {
            this.textSize = this@WheelView.textSize
            this.color = this@WheelView.textColor
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        var str = onItem.curr

        val x = center.x
        var y = center.y
        canvas.drawTextInCenter(str, x, y, paint)

        y = center.y
        repeat(count) {
            val index = it + 1
            if (!onItem.onIndexValid(index)) {
                return@repeat
            }
            str = onItem.offset(-index)
            y -= index * itemPadding
            canvas.drawTextInCenter(str, x, y, paint)
        }

        y = center.y
        repeat(count) {
            val index = it + 1
            if (!onItem.onIndexValid(index)) {
                return@repeat
            }
            str = onItem.offset(index)
            y += index * itemPadding
            canvas.drawTextInCenter(str, x, y, paint)
        }
    }

    interface OnItemListener {
        var currIndex: Int

        fun onItem(index: Int): String

        fun maxWidthStr(): String

        val curr: String
            get() = onItem(currIndex)

        fun offset(off: Int) = onItem(currIndex + off)

        fun onIndexValid(index: Int): Boolean
    }

    class NumberItemListener : OnItemListener {
        private val minIndex: Int
            get() = Int.MIN_VALUE
        private val maxIndex: Int
            get() = Int.MAX_VALUE

        override var currIndex: Int = 0

        override fun onItem(index: Int): String = index.toString()

        override fun maxWidthStr(): String = onItem(maxIndex)

        override fun onIndexValid(index: Int): Boolean {
            return currIndex in minIndex..maxIndex
        }
    }
}