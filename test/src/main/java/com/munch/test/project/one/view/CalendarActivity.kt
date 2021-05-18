package com.munch.test.project.one.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
        bind.lifecycleOwner = this
        bind.calendarMonth.apply {
            update(
                Day.now(),
                CalendarConfig(
                    drawConfig = object : DrawConfig {
                        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.BLACK
                            textSize = 30f
                            style = Paint.Style.FILL
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
                            if (p.daySelect is DayClick) {
                                if ((p.daySelect as DayClick).getClickedDay() == p.day) {
                                    paint.color = Color.RED
                                    canvas.drawRoundRect(p.rect.apply {
                                        val w = width() / 8f
                                        val h = height() / 8f
                                        set(left + w, top + h, right - w, bottom - h)
                                    }, 5f, 5f, paint)
                                    paint.color = Color.WHITE
                                } else {
                                    paint.color = Color.BLACK
                                }
                            }
                            canvas.drawTextInCenter(
                                p.day.day.toString(), p.rect.centerX(), p.rect.centerY(), paint
                            )
                        }

                        override fun onDrawWeekLine(
                            canvas: Canvas,
                            week: Int,
                            rectF: RectF
                        ) {
                            super.onDrawWeekLine(canvas, week, rectF)
                            paint.color = Color.BLACK
                            canvas.drawTextInCenter(
                                getStr(week),
                                rectF.centerX(),
                                rectF.centerY(),
                                paint
                            )
                        }

                        private fun getStr(week: Int): String {
                            return when (week) {
                                Calendar.SUNDAY -> "日"
                                Calendar.MONDAY -> "一"
                                Calendar.TUESDAY -> "二"
                                Calendar.WEDNESDAY -> "三"
                                Calendar.THURSDAY -> "四"
                                Calendar.FRIDAY -> "五"
                                Calendar.SATURDAY -> "六"
                                else -> throw UnsupportedOperationException()
                            }
                        }

                    },
                    daySelect = DaySelectHelper.DayClickHelper()
                )
            )
        }
    }
}