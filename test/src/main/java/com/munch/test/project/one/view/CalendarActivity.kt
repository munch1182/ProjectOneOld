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

/**
 * Create by munch1182 on 2021/5/6 14:49.
 */
class CalendarActivity : BaseTopActivity() {

    private val bind by bind<ActivityCalendarBinding>(R.layout.activity_calendar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.calendarMonth.apply {
            update(
                Day.now(),
                CalendarConfig(
                    drawConfig = object : DrawConfig {
                        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.BLACK
                            textSize = 30f
                            style = Paint.Style.STROKE
                        }

                        override fun onDrawStart(
                            canvas: Canvas,
                            month: Month,
                            monthView: MonthView
                        ) {
                            super.onDrawStart(canvas, month, monthView)
                            paint.color = Color.parseColor("#8f8f8f")
                            paint.textSize = 500f
                            canvas.drawTextInCenter(
                                month.month.toString(),
                                monthView.width * 0.5f,
                                monthView.height * 0.5f,
                                paint
                            )
                            paint.textSize = 30f
                            paint.color = Color.BLACK
                        }

                        override fun onDrawDay(canvas: Canvas, p: MonthView.DayParameter) {
                            if (p.view.getMonth() != p.day) {
                                return
                            }
                            canvas.drawTextInCenter(
                                p.day.day.toString(), p.rect.centerX(), p.rect.centerY(), paint
                            )
                        }

                    })
            )
        }
    }
}