package com.munch.pre.lib.calender

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.munch.pre.lib.calender.MonthHelper.Companion.getHelper
import com.munch.pre.lib.helper.RectArrayHelper

/**
 * Create by munch1182 on 2021/5/6 16:12.
 */
class MonthView(
    context: Context,
    attrs: AttributeSet? = null,
    month: Month,
    var config: CalendarConfig
) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        Month.now(),
        CalendarConfig()
    )

    constructor(context: Context) : this(context, null)

    private val helper = month.getHelper()

    fun updateMonth(month: Month) {
        helper.change(month)
        requestLayout()
    }

    private var widthUnit = 0f
    private var heightUnit = 0f
    private val rectArray = RectArrayHelper()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
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

        val weeks =
            if (config.height.fixHeight == -1) helper.getWeeks() else config.height.fixHeight
        val borderHeight = (weeks + 1) * wh.borderSize
        heightUnit = if (wh.height != -1) {
            wh.height.toFloat()
        } else {
            var h = (heightSize - paddingTop - paddingBottom - borderHeight) / weeks.toFloat()
            if (h > widthUnit) {
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
        super.onSizeChanged(w, h, oldw, oldh)

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
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val draw = config.drawConfig ?: return
        draw.onDrawStart(canvas, helper.month, this)
        val count = if (config.height.showNear) {
            6 * 7
        } else {
            helper.getWeeks() * 7
        }
        for (i in 0 until count) {
            draw.onDrawDay(
                canvas,
                rectArray.getLeft(i),
                rectArray.getTop(i),
                rectArray.getRight(i),
                rectArray.getBottom(i),
                helper.getIndexDay(i)
            )
        }
        draw.onDrawOver(canvas, helper.month, this)
    }
}