package com.munch.lib.weight.calendar

import android.content.Context
import android.graphics.*
import com.munch.lib.extend.*
import java.util.*

/**
 * Create by munch1182 on 2022/4/25 16:13.
 */

data class Measure(val w: Int, val h: Int)
interface OnItemDraw {
    fun onItemMeasure(): Measure
    fun onItemDraw(c: Canvas, rect: RectF, calendar: Calendar)
}

interface OnMonthDraw : OnItemDraw
interface OnDayDraw : OnItemDraw
interface OnWeekDraw : OnItemDraw

class MonthDrawer(context: Context) : OnMonthDraw {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0078FF")
        textSize = context.sp2Px(20f)
    }
    private val bond = Rect()
    private val test by testPaint()

    override fun onItemMeasure(): Measure {
        paint.measureTextBounds("2020年12月", bond)
        return Measure(bond.width(), bond.height() * 2)
    }

    override fun onItemDraw(c: Canvas, rect: RectF, calendar: Calendar) {
        c.drawRect(rect, test)
        val m = "${calendar.getYear()}年${calendar.getMonth()}月"
        c.drawTextInYCenter(m, rect.left + 32f, rect.centerY(), paint)
    }
}

class WeekDrawer(context: Context) : OnWeekDraw {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3C3C4399")
        textSize = context.sp2Px(13f)
    }
    private val bond = Rect()
    private val test by testPaint()

    override fun onItemMeasure(): Measure {
        paint.measureTextBounds("周日", bond)
        return Measure(bond.width(), bond.height() * 2)
    }

    override fun onItemDraw(c: Canvas, rect: RectF, calendar: Calendar) {
        c.drawRect(rect, test)
        val m = when (calendar.getWeekToday()) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> return
        }
        c.drawTextInCenter(m, rect.centerX(), rect.centerY(), paint)
    }
}

class DayDrawer(context: Context) : OnDayDraw {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0078FF")
        textSize = context.sp2Px(18f)
    }
    private val bond = Rect()
    private val test by testPaint()

    override fun onItemMeasure(): Measure {
        paint.measureTextBounds("31", bond)
        return Measure(bond.width(), bond.height() * 4)
    }

    override fun onItemDraw(c: Canvas, rect: RectF, calendar: Calendar) {
        val cX = rect.centerX()

        c.drawRect(rect, test)
        paint.color = Color.parseColor("#0078FF")
        c.drawTextInCenter(
            calendar.getDay().toString(),
            cX, rect.top + rect.height() / 4, paint
        )
        paint.color = Color.parseColor("#C5C5C7")
        c.drawCircle(cX, rect.top + rect.height() * 3f / 4f, 8f, paint)
    }
}