package com.munch.pre.lib.calender

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.pre.lib.calender.MonthHelper.Companion.getHelper
import com.munch.pre.lib.helper.RectArrayHelper
import java.util.*

/**
 * Create by munch1182 on 2021/5/6 16:12.
 */
class MonthView @JvmOverloads constructor(
    context: Context,
    month: Month,
    var config: CalendarConfig?,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        Month.now(),
        CalendarConfig(),
        attrs
    )

    constructor(context: Context) : this(context, null)

    internal val helper = month.getHelper(config?.firstDayOfWeek ?: Calendar.MONDAY)

    private var widthUnit = 0f
    private var heightUnit = 0f
    private val rectArray = RectArrayHelper()
    private lateinit var parameter: DayParameter
    private val rect = RectF()
    private var clickDay: Day? = null
    private var onDaySelectListener: OnDaySelectListener? = null
    var selectHelper: DaySelectHelper? = config?.daySelect

    fun updateMonth(month: Month) {
        helper.change(month)
        requestLayout()
        invalidate()
    }

    fun getMonth() = helper.month

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val config = config ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val wh = config.wh
        val border8 = wh.borderSize * 8f

        widthUnit = if (wh.width != -1) {
            wh.width.toFloat()
        } else {
            val w = (widthSize - paddingLeft - paddingRight - border8) / 7f
            when {
                wh.minWidth != -1 && w < wh.minWidth.toFloat() -> wh.minWidth.toFloat()
                wh.maxWidth != -1 && w > wh.maxWidth.toFloat() -> wh.maxWidth.toFloat()
                else -> w
            }
        }
        val width = widthUnit * 7 + border8 + paddingLeft + paddingRight

        val weeks = if (!config.height.fixHeight) helper.getWeeks() else 6
        val borderHeight = (weeks + 1) * wh.borderSize
        heightUnit = if (wh.height != -1) {
            wh.height.toFloat()
        } else {
            var h = (heightSize - paddingTop - paddingBottom - borderHeight) / weeks.toFloat()
            if (h <= 0f) {
                h = widthUnit
            }
            when {
                wh.minHeight != -1 && h < wh.minHeight.toFloat() -> wh.minWidth.toFloat()
                wh.maxHeight != -1 && h > wh.maxHeight.toFloat() -> wh.maxWidth.toFloat()
                else -> h
            }
        }
        val height = heightUnit * weeks + borderHeight + paddingTop + paddingBottom
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val config = config ?: return super.onSizeChanged(w, h, oldw, oldh)

        rectArray.clear()
        for (i in 0 until 6 * 7) {
            val weekIndex = i / 7
            val week = i % 7
            val left = week * widthUnit + config.wh.borderSize * (week + 1) + paddingStart
            val top = weekIndex * heightUnit + config.wh.borderSize * (weekIndex + 1) + paddingTop
            rectArray.addArray(
                left,
                top,
                (left + widthUnit),
                (top + heightUnit)
            )
        }
        parameter = DayParameter(helper.getIndexDay(0), rect, this, 0)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val config = config ?: return
        val draw = config.drawConfig ?: return
        parameter.daySelect = selectHelper
        draw.onDrawStart(canvas, helper.month, this)
        val count = if (config.height.fixHeight) {
            6 * 7
        } else {
            helper.getWeeks() * 7
        }
        for (i in 0 until count) {
            parameter.day = helper.getIndexDay(i)
            parameter.index = i
            rect.set(
                rectArray.getLeft(i),
                rectArray.getTop(i),
                rectArray.getRight(i),
                rectArray.getBottom(i)
            )
            parameter.rect = rect
            //因为每一次调用所以共用一个对象
            draw.onDrawDay(canvas, parameter)
        }
        draw.onDrawOver(canvas, helper.month, this)
    }

    fun onDaySelect(listener: OnDaySelectListener): MonthView {
        this.onDaySelectListener = listener
        return this
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                clickDay = getDayByTouch(event) ?: return false
                if (selectHelper?.onDown(clickDay!!) == true) {
                    invalidate()
                } else {
                    performClick()
                }
                onDaySelectListener?.onDaySelect(clickDay!!)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val day = getDayByTouch(event) ?: return false
                if (clickDay != day) {
                    clickDay = day
                    if (selectHelper?.onMove(day) == true) {
                        invalidate()
                    }
                    onDaySelectListener?.onDaySelect(clickDay!!)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (selectHelper?.onUp() == true) {
                    invalidate()
                }
                onDaySelectListener?.onDaySelectEnd()
                clickDay = null
                return false
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getDayByTouch(event: MotionEvent): Day? {
        if (rectArray.getArray().isEmpty()) {
            return null
        }
        val x = event.x
        val y = event.y
        var row = -1
        //前7个数据确定列数
        for (i in 0 until 7) {
            if (rectArray.getRight(i) > x) {
                row = i
                break
            }
        }
        if (row == -1) {
            return null
        }
        var line = -1
        for (i in 0 until helper.getWeeks()) {
            if (rectArray.getBottom(i * 7) > y) {
                line = i
                break
            }
        }
        if (line == -1) {
            return null
        }
        return helper.getIndexDay(line * 7 + row)
    }

    data class DayParameter(
        //当前要绘制的天数
        var day: Day,
        //当前天数的可绘制范围
        var rect: RectF,
        //当前月分的对象
        var view: MonthView,
        //当前绘制天数在整个天数中的序列(包括非本月的补充天数)
        var index: Int,
        //处理选中的天数
        var daySelect: DaySelectHelper? = null
    )

}

abstract class DaySelectHelper {

    open fun onDown(day: Day) = false

    open fun onMove(day: Day) = false

    open fun onUp() = false

    class DayClickHelper : DaySelectHelper(), DayClick {

        private var clickDay: Day? = null
        private var upDay: Day? = null

        override fun onDown(day: Day): Boolean {
            super.onDown(day)
            clickDay = day
            return false
        }

        override fun onMove(day: Day): Boolean {
            super.onMove(day)
            clickDay = day
            return false
        }

        override fun onUp(): Boolean {
            super.onUp()
            upDay = clickDay
            return true
        }

        override fun getClickedDay(): Day? {
            return upDay
        }
    }

    class DayRangeSelectHelper : DaySelectHelper(), DayRangSelect {

        private var firstDay: Day? = null
        private var lastDay: Day? = null
        private var rangeDay = mutableListOf<Day>()
        override fun onDown(day: Day): Boolean {
            super.onDown(day)
            if (lastDay != null) {
                clear()
            }
            if (firstDay == null) {
                firstDay = day
                return true
            } else if (lastDay == null) {
                lastDay = day
                return true
            }
            return false
        }

        private fun clear() {
            firstDay = null
            lastDay = null
            rangeDay.clear()
        }

        override fun onMove(day: Day): Boolean {
            super.onMove(day)
            lastDay = day
            return true
        }

        override fun onUp(): Boolean {
            super.onUp()
            firstDay ?: return false
            lastDay ?: return false
            rangeDay.clear()
            if (firstDay!! > lastDay!!) {
                val temp = firstDay
                firstDay = lastDay
                lastDay = temp
            }
            val days = lastDay!! - firstDay!!
            for (i in 1..days) {
                rangeDay.add(firstDay!! + i)
            }
            return true
        }

        override fun getFirstClickedDay(): Day? = firstDay

        override fun getLastClickedDay(): Day? = lastDay

        override fun getRangeDay(): MutableList<Day> = rangeDay
    }
}

interface DaySelect

interface DayClick : DaySelect {

    fun getClickedDay(): Day?
}

interface DayRangSelect : DaySelect {

    fun getFirstClickedDay(): Day?

    fun getLastClickedDay(): Day?

    fun getRangeDay(): MutableList<Day>
}

interface OnDaySelectListener {

    fun onDaySelect(day: Day)

    fun onDaySelectEnd()
}