package com.munch.lib.weight.calendar

import android.graphics.Canvas
import android.graphics.RectF
import java.util.Calendar

/**
 * Create by munch1182 on 2022/4/25 16:13.
 */
//日期显示样式
sealed class Style {
    data class Week(
        val count: Int = 1,//显示的周数
        val fill: Boolean = true, //是否填充空白的位置
    ) : Style()

    data class Month(
        val count: Int = 1,
        val fill: Boolean = true, //是否填充空白的位置
    ) : Style()

    data class Year(
        val monthSpanCount: Int = 3, //显示一行的月份数
        val monthSpace: Int = 8, //月份之间的间隔
    ) : Style()

    object Fill : Style()
}


data class Measure(val with: Int, val height: Int)

/**
 * 用于绘制每一天
 */
interface OnDayDraw {

    fun onMeasure(style: Style): Measure {
        return Measure(0, 0)
    }

    fun onDraw(canvas: Canvas, rect: RectF, year: Int, month: Int, day: Int) {}
}

/**
 * 用于绘制月标识
 */
interface OnMonthLabelDraw {

    fun onMeasure(style: Style): Measure {
        return Measure(0, 0)
    }

    fun onDraw(canvas: Canvas, rect: RectF, year: Int, month: Int) {}
}

/**
 * 用于绘制星期标识
 */
interface OnWeekLabelDraw {

    fun onMeasureHeight(): Int = 0

    /**
     * @param dayOfWeek [Calendar.DAY_OF_WEEK]
     */
    fun onDraw(canvas: Canvas, rect: RectF, dayOfWeek: Int) {}
}

