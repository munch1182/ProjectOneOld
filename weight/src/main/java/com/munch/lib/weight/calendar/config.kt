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
    fun onItemDraw(c: Canvas, item: OnItemDesc, calendar: Calendar)
}

interface OnMonthDraw : OnItemDraw
interface OnWeekDraw : OnItemDraw
interface OnDayDraw : OnItemDraw

data class OnItemDesc(
    //该行的绘制范围，超出该范围的绘制仍然会有效
    var rectF: RectF,
    //该item是否是该行起始
    var isLineStart: Boolean = false,
    //该item是否改行结束
    var isLineEnd: Boolean = false,
    //该item是否选中
    var isSelected: Boolean = false,
    //该item是否有上一个相邻的选中(跨行也算相邻)
    var hasLastSelected: Boolean = false,
    //该item是否有下一个相邻的选中(跨行也算相邻)
    var hasNextSelected: Boolean = false
) {
    fun reset() {
        isLineStart = false
        isLineEnd = false
        isSelected = false
        hasLastSelected = false
        hasNextSelected = false
    }
}

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


    override fun onItemDraw(c: Canvas, item: OnItemDesc, calendar: Calendar) {
        val rect = item.rectF
        //c.drawRect(rect, test)
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
        return Measure(bond.width(), bond.width() * 2)
    }

    override fun onItemDraw(c: Canvas, item: OnItemDesc, calendar: Calendar) {
        val rect = item.rectF
        //c.drawRect(rect, test)
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
        c.drawTextInCenter(m, rect.centerX(), rect.top + rect.height() / 4f, paint)
    }
}

class DayDrawer(context: Context) : OnDayDraw {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0078FF")
        textSize = context.sp2Px(20f)
    }
    private val bond = Rect()
    private val rectBuf = RectF()
    private val test by testPaint()

    override fun onItemMeasure(): Measure {
        paint.measureTextBounds("31", bond)
        return Measure(bond.width() + 8, (bond.width() + 8) * 2)
    }

    override fun onItemDraw(c: Canvas, item: OnItemDesc, calendar: Calendar) {
        val rect = item.rectF
        if (item.isSelected) {
            paint.color = Color.parseColor("#A4CFFF")
            rectBuf.set(rect)
            rectBuf.bottom = rect.top + rect.height() / 2f + 8

            if (!item.hasLastSelected || item.isLineStart) {
                rectBuf.left += rect.width() / 4f
            }
            if (!item.hasNextSelected || item.isLineEnd) {
                rectBuf.right -= rect.width() / 4f
            }
            c.drawRect(rectBuf, paint)
            if (!item.hasNextSelected) {
                val right = rectBuf.right
                rectBuf.left = right - 10f
                rectBuf.right = right + 8f
            }
            if (!item.hasLastSelected) {
                val left = rectBuf.left
                rectBuf.right = left + 10f
                rectBuf.left = left - 8f
            }

            c.drawRoundRect(rectBuf, 16f, 16f, paint)
        }

        val cX = rect.centerX()
        //c.drawRect(rect, test)
        if (item.isSelected) {
            paint.color = Color.parseColor("#0078FF")
        } else {
            paint.color = Color.parseColor("#C5C5C7")
        }
        c.drawTextInCenter(
            calendar.getDay().toString(),
            cX, rect.top + rect.height() / 4f, paint
        )
        paint.color = Color.parseColor("#C5C5C7")
        c.drawCircle(cX, rect.top + rect.height() * 3f / 4f, 8f, paint)
    }


}