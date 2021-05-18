package com.munch.project.launcher.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.ConcatAdapter
import com.munch.pre.lib.base.rv.ItemDiffCallBack
import com.munch.pre.lib.calender.*
import com.munch.pre.lib.extend.getAttrArrayFromTheme
import com.munch.pre.lib.helper.drawTextInCenter
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseBindAdapter
import com.munch.project.launcher.base.BaseBindViewHolder
import com.munch.project.launcher.base.BaseDifferBindAdapter
import com.munch.project.launcher.databinding.ItemCalendarBinding
import com.munch.project.launcher.databinding.ItemNoteBinding
import java.util.*

/**
 * Create by munch1182 on 2021/5/18 17:31.
 */
class CalendarAdapter(context: Context) {

    private val noteAdapter = NoteAdapter()
    private val calendarAdapter = CalendarAdapter(context)

    fun getNoteAdapter() = noteAdapter

    private val adapter = ConcatAdapter(calendarAdapter, noteAdapter)

    fun getAdapter() = adapter
    fun getCalendarView() {
        throw UnsupportedOperationException("UNCOMPLETED")
    }

    class NoteAdapter : BaseDifferBindAdapter<Note, ItemNoteBinding>(
        R.layout.item_note,
        ItemDiffCallBack({ it.hashCode() })
    ) {
        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemNoteBinding>,
            bean: Note,
            pos: Int
        ) {
            holder.bind.note = bean
        }
    }

    class CalendarAdapter(private val context: Context) :
        BaseBindAdapter<Int, ItemCalendarBinding>(R.layout.item_calendar) {

        init {
            set(mutableListOf(1))
        }

        private val current = Day.now()
        private val colorPrimary by lazy {
            context.getAttrArrayFromTheme(R.attr.colorPrimary) {
                this.getColor(0, Color.WHITE)
            }
        }

        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemCalendarBinding>,
            bean: Int,
            pos: Int
        ) {
            holder.bind.calenderView.apply {
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
        }
    }
}

