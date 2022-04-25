package com.munch.lib.weight.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.getMonth
import com.munch.lib.extend.getWeekToday
import com.munch.lib.extend.getYear
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

/**
 * Create by munch1182 on 2022/4/25 16:05.
 */
class Calendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef) {


    var showStyle: Style = Style.Month(1)

    //是否绘制星期一
    var showWeek = true
    var firstOfWeek = Calendar.SUNDAY
    var onMonthDraw: OnMonthLabelDraw = object : OnMonthLabelDraw {}
    var onWeekDraw: OnWeekLabelDraw = object : OnWeekLabelDraw {}
    var onDayDraw: OnDayDraw = object : OnDayDraw {}

    private val rectDay = RectF()
    private val rectMonth = RectF()
    private val curr = Calendar.getInstance()
    private val currTemp = Calendar.getInstance()
    private val weekLabels = mutableListOf(
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.THURSDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
    )
    private val days: MutableList<Int> = ArrayList(42)

    private val rectDayTemp = RectF()
    private val rectWeekTemp = RectF()
    private val rectMonthTemp = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        rectMonth.setEmpty()
        rectDay.setEmpty()

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var minWidth = 0
        var minHeight = 0
        val month = onMonthDraw.onMeasure(showStyle)
        val week = onWeekDraw.onMeasureHeight()
        val day = onDayDraw.onMeasure(showStyle)

        rectMonth.bottom = month.height.toFloat()
        rectDay.bottom = day.height.toFloat()

        when (val style = showStyle) {
            is Style.Week -> {
                minHeight = month.height + day.height * style.count
                minWidth = day.with * 7
            }
            is Style.Month -> {
                minHeight = (month.height + day.height * 6) * style.count // 一个月最多可跨6周
                minWidth = day.with * 7
            }
            is Style.Year -> {
                minHeight = (month.height + day.height * 6) * (12 / style.monthSpanCount)
                minWidth = (day.with * 7) * style.monthSpanCount +
                        (style.monthSpace * (style.monthSpanCount - 1)) //间距
            }
            is Style.Fill -> {
                minWidth = day.with * 7
            }
        }
        if (showWeek) {
            minHeight += week
        }
        setMeasuredDimension(
            max(width, min(minWidth + paddingLeft + paddingRight, width)),
            max(height, min(minHeight + paddingTop + paddingBottom, height))
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val wr = w - paddingLeft - paddingRight
        val day = onDayDraw.onMeasure(showStyle)
        rectMonth.right = wr.toFloat()

        when (showStyle) {
            is Style.Week -> {
                rectDay.right = wr / 7f
            }
            is Style.Month -> {
                rectDay.right = wr / 7f
            }
            is Style.Year -> {
                rectDay.right = day.with.toFloat()
            }
            is Style.Fill -> {
                rectDay.right = wr / 7f
            }
        }

        val index = weekLabels.indexOf(firstOfWeek)

        if (index != 0) {
            val list = weekLabels.subList(0, index).apply { addAll(weekLabels.subList(index, 7)) }
            weekLabels.clear()
            weekLabels.addAll(list)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        curr.firstDayOfWeek = firstOfWeek

        rectMonthTemp.set(rectMonth)
        rectWeekTemp.set(rectDay)
        rectDayTemp.set(rectDay)
        when (val style = showStyle) {
            is Style.Week -> {
                drawMonthWeekLabel(canvas)
            }
            is Style.Month -> {
                drawMonthWeekLabel(canvas)
                val year = curr.getYear()
                val month = curr.getMonth()

                currTemp.set(year, month, 1)
                val indexFirst = weekLabels.indexOf(currTemp.getWeekToday())
                repeat(indexFirst) { days.add(0, -1) }

                val maxDay = curr.getMaximum(Calendar.DAY_OF_MONTH)
                repeat(maxDay) { days.add(it + 1) }

                currTemp.set(Calendar.DAY_OF_MONTH, maxDay)
                val indexEnd = weekLabels.indexOf(currTemp.getWeekToday())
                repeat(indexEnd) { days.add(-1) }
                val width = rectDay.width()
                val height = rectDay.height()

                for (i in 0..41) {
                    val d = days.getOrNull(i) ?: return
                    if (d < 0) {
                        continue
                    }
                    rectDayTemp.left = (i % 7) * width
                    rectDayTemp.right = rectDayTemp.left + width
                    rectDayTemp.top = (i / 7) * height
                    rectDayTemp.bottom = rectDayTemp.top + height
                    onDayDraw.onDraw(canvas, rectDayTemp, year, month, i)
                }
            }
            is Style.Year -> {
            }
            is Style.Fill -> {
                drawMonthWeekLabel(canvas)
            }
        }
    }

    private fun drawMonthWeekLabel(canvas: Canvas) {
        onMonthDraw.onDraw(canvas, rectMonthTemp, curr.getYear(), curr.getMonth())
        if (showWeek) {
            weekLabels.forEach { onWeekDraw.onDraw(canvas, rectWeekTemp, it) }
        }
    }
}