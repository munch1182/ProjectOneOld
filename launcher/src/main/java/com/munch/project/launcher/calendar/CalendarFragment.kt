package com.munch.project.launcher.calendar

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import com.munch.pre.lib.calender.*
import com.munch.pre.lib.extend.getAttrArrayFromTheme
import com.munch.pre.lib.helper.drawTextInCenter
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentCalenderBinding
import java.util.*
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/5/8 11:22.
 */
class CalendarFragment : BaseFragment() {

    private val bind by bind<FragmentCalenderBinding>(R.layout.fragment_calender)
    private val colorPrimary by lazy {
        requireActivity().getAttrArrayFromTheme(R.attr.colorPrimary) {
            this.getColor(0, Color.WHITE)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.lifecycleOwner = this
        val current = Day.now()
        bind.calenderView.apply {
            update(
                current,
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
                            if (p.day == current) {
                                paint.color = colorPrimary
                                canvas.drawRoundRect(p.rect.apply {
                                    val w = width() / 8f
                                    val h = height() / 8f
                                    set(left + w, top + h, right - w, bottom - h)
                                }, 5f, 5f, paint)
                                paint.color = Color.WHITE
                            } else {
                                paint.color = Color.BLACK
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
        bind.calenderLeft.text =
            "${(Day.now() - (Month.now() + 1)).absoluteValue}/${(Day.now() - (Year.now() + 1)).absoluteValue}"
    }
}