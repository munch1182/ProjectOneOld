package com.munch.pre.lib.calender

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.munch.pre.lib.calender.MonthHelper.Companion.getHelper

/**
 * Create by munch1182 on 2021/5/6 16:12.
 */
internal class MonthView(
    context: Context,
    month: Month,
    private val config: CalendarConfig
) : View(context) {

    constructor(context: Context) : this(context, Month.now(), CalendarConfig())

    private val helper = month.getHelper()

    fun updateMonth(month: Month) {
        helper.change(month)
        requestLayout()
    }

    private var widthUnit = 0
    private var heightUnit = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val wh = config.wh
        val border8 = wh.borderWidth * 8
        val width = when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                val w = (widthSize - border8) / 7
                widthUnit = when {
                    w < wh.minWidth -> wh.minWidth
                    w > wh.maxWidth -> wh.maxWidth
                    else -> w
                }
                widthUnit * 7
            }
            else -> {
                widthUnit = wh.width
                border8 + widthUnit * 7
            }
        }
        val weeks =
            if (config.height.fixHeight == -1) helper.getWeeks() else config.height.fixHeight
        val borderHeight = weeks + 1
        val height = when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                val w = (heightSize - borderHeight) / weeks
                heightUnit = when {
                    w < wh.minHeight -> wh.minHeight
                    w > wh.maxHeight -> wh.maxHeight
                    else -> w
                }
                heightUnit * weeks
            }
            else -> {
                heightUnit = wh.height
                borderHeight + heightUnit * weeks
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

    }
}