package com.munch.lib.weight.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.getDayInWeekIndex
import com.munch.lib.extend.getWeekToday
import com.munch.lib.log.log
import java.util.*
import kotlin.math.max

/**
 * Create by munch1182 on 2022/4/25 16:05.
 */
class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0,
) : View(context, attrs, styleDef) {

    //当前时间
    private val calendar = Calendar.getInstance().apply {
        //firstDayOfWeek = Calendar.TUESDAY
    }

    //选中的时间，只代表点中的时间
    private val calendarSelected = Calendar.getInstance()

    //drawer
    private val month = MonthDrawer(context)
    private val day = DayDrawer(context)
    private val week = WeekDrawer(context)

    //rect, 绘制的区域
    private val rectMonth = RectF()
    private val rectWeek = RectF()
    private val rectDay = RectF()

    private val measureDay = day.onItemMeasure()
    private val measureWeek = week.onItemMeasure()
    private val measureMonth = month.onItemMeasure()

    private val rectBuff = RectF()
    private val calendarBuff = Calendar.getInstance().apply {
        firstDayOfWeek = calendar.firstDayOfWeek
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = max(
            max(measureDay.w, measureWeek.w) * 7 + paddingLeft + paddingRight,
            MeasureSpec.getSize(widthMeasureSpec)
        )
        //todo 高度设置或者计算
        val height =
            measureMonth.h + measureWeek.h * 3 + measureDay.h * 12 + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val itemWidth = (w - paddingLeft - paddingRight) / 7

        //todo 结构分发
        //此处已经确定好位置的分布
        //第一个月份的位置
        rectMonth.left = paddingLeft.toFloat()
        rectMonth.top = paddingTop.toFloat()
        rectMonth.right = w - paddingRight.toFloat()
        rectMonth.bottom = rectMonth.top + measureMonth.h
        //第一个周的位置
        rectWeek.left = rectMonth.left
        rectWeek.top = paddingTop.toFloat()
        rectWeek.right = rectWeek.left + itemWidth
        rectWeek.bottom = rectWeek.top + measureWeek.h
        //第一个日的位置， 从0开始
        rectDay.left = rectWeek.left
        rectDay.top = paddingTop.toFloat()
        rectDay.right = rectDay.left + itemWidth
        rectDay.bottom = rectDay.top + measureDay.h

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return


        var height = paddingTop.toFloat()

        val viewHeight = getHeight()
        while (height <= viewHeight) {
            //month
            height += drawMonth(canvas, height)
            //week
            height += drawWeeks(canvas, height)
            //day
            height += drawDays(canvas, height)

            calendar.add(Calendar.MONTH, 1)
        }

    }

    private fun drawDays(canvas: Canvas, height: Float): Float {
        rectBuff.set(rectDay)
        rectBuff.top += height
        rectBuff.bottom += height
        val width = rectWeek.width()

        calendarBuff.time = calendar.time
        calendarBuff.set(Calendar.DAY_OF_MONTH, 1)
        val days = calendarBuff.getMaximum(Calendar.DAY_OF_MONTH)

        val lineHeight = rectDay.height()
        var h = height

        //修改月份前的位置
        val index = calendarBuff.getDayInWeekIndex()
        repeat(index) {
            rectBuff.left += width
            rectBuff.right += width
        }

        repeat(days) {
            if (rectBuff.right > getWidth()) {
                rectBuff.left = rectDay.left
                rectBuff.right = rectDay.right
                rectBuff.top += lineHeight
                rectBuff.bottom += lineHeight

                h += lineHeight
            }

            calendarBuff.set(Calendar.DAY_OF_MONTH, it + 1)
            day.onItemDraw(canvas, rectBuff, calendarBuff)

            rectBuff.left += width
            rectBuff.right += width
        }
        return h
    }

    private fun drawWeeks(canvas: Canvas, height: Float): Float {
        rectBuff.set(rectWeek)
        rectBuff.top += height
        rectBuff.bottom += height

        val width = rectWeek.width()

        calendarBuff.time = calendar.time
        calendarBuff.set(Calendar.DAY_OF_WEEK, calendarBuff.firstDayOfWeek)
        repeat(7) {
            week.onItemDraw(canvas, rectBuff, calendarBuff)

            rectBuff.left += width
            rectBuff.right += width
            calendarBuff.add(Calendar.DAY_OF_MONTH, 1)
        }

        return rectWeek.height()
    }

    private fun drawMonth(canvas: Canvas, height: Float): Float {
        rectBuff.set(rectMonth)
        rectBuff.top += height
        rectBuff.bottom += height

        calendarBuff.time = calendar.time
        month.onItemDraw(canvas, rectBuff, calendarBuff)

        return rectMonth.height()
    }
}