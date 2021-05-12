package com.munch.test.project.one.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import com.munch.pre.lib.calender.*
import com.munch.pre.lib.helper.drawTextInCenter
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityCalendarBinding
import java.util.*

/**
 * Create by munch1182 on 2021/5/6 14:49.
 */
class CalendarActivity : BaseTopActivity() {

    private val bind by bind<ActivityCalendarBinding>(R.layout.activity_calendar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val month = Month.now()
        bind.lifecycleOwner = this
        bind.calendarMonth.apply {
            config = CalendarConfig(
                drawConfig = object : DrawConfig {
                    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.BLACK
                        textSize = 30f
                        style = Paint.Style.STROKE
                    }
                    private val color1 = Color.parseColor("#000000")
                    private val color2 = Color.BLACK

                    override fun onDrawDay(
                        canvas: Canvas, l: Float, t: Float, r: Float, b: Float, day: Day
                    ) {
                        if (day != month) {
                            return
                        }
                        paint.color = color1
                        canvas.drawTextInCenter(
                            day.day.toString(), (r - l) / 2 + l, (b - t) / 2 + t, paint
                        )
                        paint.color = color2
                        canvas.drawLines(floatArrayOf(l, t, r, t, l, t, l, b), paint)
                        if (day.getWeek() == Calendar.SUNDAY) {
                            canvas.drawLines(floatArrayOf(r, t, r, b, l, t, l, t), paint)
                        }
                    }

                    override fun onDrawOver(canvas: Canvas, month: Month, monthView: MonthView) {
                        super.onDrawOver(canvas, month, monthView)
                        paint.color = color2
                        canvas.drawLine(
                            monthView.left.toFloat(), monthView.bottom.toFloat(),
                            monthView.right.toFloat(), monthView.bottom.toFloat(), paint
                        )
                    }

                })
            requestLayout()
            invalidate()
        }
    }
}