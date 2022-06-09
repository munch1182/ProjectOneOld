package com.munch.lib.weight.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.*
import com.munch.lib.extend.icontext.IContext
import java.util.*

/**
 * Create by munch1182 on 2022/6/2 16:34.
 */
class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0,
) : RecyclerView(context, attrs, styleDef), IContext {

    private val calendarNow = Calendar.getInstance()
    private val calendarStart = Calendar.getInstance().apply {
        set(2020, 1, 1)
    }
    private val count =
        (calendarNow.getYear() - calendarStart.getYear()) * 12 + calendarNow.getMonth() + 3

    private val monthAdapter = MonthAdapter(count)
    private var onDayChose: OnDayChoseListener? = null

    init {
        val lm = LinearLayoutManager(ctx)
        layoutManager = lm
        adapter = monthAdapter
        lm.scrollToPosition(monthAdapter.currIndex)
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = lm.findLastCompletelyVisibleItemPosition()
                if (position > monthAdapter.itemCount - 2) {
                    monthAdapter.raise()
                }
            }
        })
    }

    fun setOnDayChose(onDayChose: OnDayChoseListener) {
        this.onDayChose = onDayChose
    }

    fun get(pos: Int): Calendar? {
        return monthAdapter.get(pos)
    }

    fun select(calendar: Calendar) {
        val month = monthAdapter.get(0) ?: return
        val diff = calendar.getMonthIndex() - month.getMonthIndex()
        monthAdapter.notifyItemChanged(diff, calendar)
        monthAdapter.notifyItemChanged(diff - 1, calendar)
        monthAdapter.notifyItemChanged(diff + 1, calendar)
    }

    override val ctx: Context
        get() = context

    inner class MonthAdapter(private val count: Int = 100) : RecyclerView.Adapter<MonthVH>() {

        val currIndex = count - 4

        private val list = mutableListOf<Calendar>()
        private var lastChose = -1
        private var choseCalendar: Calendar? = null

        init {
            list.clear()
            repeat(count) {
                list.add(0, Calendar.getInstance().apply { addMonth(count - 1 - it - currIndex) })
            }
        }

        fun raise() {
            val start = list.size
            val calendar = list.lastOrNull() ?: return
            repeat(10) {
                list.add(Calendar.getInstance().apply {
                    time = calendar.time
                    addMonth(it + 1)
                })
            }
            notifyItemRangeInserted(start, 10)
        }

        fun get(pos: Int): Calendar? {
            return list.getOrNull(pos)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthVH {
            return MonthVH(CalendarMonthView(parent.context, week = null))
        }

        override fun onBindViewHolder(holder: MonthVH, position: Int) {
            holder.view.setCalender(list[position])
            holder.view.chose(choseCalendar)
            holder.view.onDateChose = object : CalendarMonthView.OnDateChoseListener {
                override fun onDateChose(
                    calendar: Calendar,
                    type: CalendarMonthView.ChoseType
                ) {
                    if (onDayChose != null) {
                        val c = Calendar.getInstance().apply { time = calendar.time }
                        var start = c
                        var end = c
                        when (type) {
                            CalendarMonthView.ChoseType.Month -> {
                                c.setDay(1)
                                start = Calendar.getInstance().apply { time = c.time }
                                c.setDay(c.getMouthDayCount())
                                end = Calendar.getInstance().apply { time = c.time }
                            }
                            CalendarMonthView.ChoseType.Week -> {
                                var index = c.getDayInWeekIndex()
                                //强制星期开/星期一转换
                                if (index == 0) {
                                    index = 7
                                }
                                c.addDay(-index + 1)
                                start = Calendar.getInstance().apply { time = c.time }
                                c.addDay(6)
                                end = Calendar.getInstance().apply { time = c.time }
                            }
                            CalendarMonthView.ChoseType.Day -> {}
                        }
                        val update = onDayChose?.onDayChose(start, end) ?: true
                        if (update) {
                            choseCalendar = calendar
                            updateChose(calendar, holder.absoluteAdapterPosition)
                        }
                    } else {
                        choseCalendar = calendar
                        updateChose(calendar, holder.absoluteAdapterPosition)
                    }

                }
            }
        }

        private fun updateChose(calendar: Calendar, pos: Int) {
            var p = lastChose
            if (p != pos) {
                if (p in 0 until itemCount) {
                    notifyItemChanged(p, false)
                }
                p = lastChose - 1
                if (p in 0 until itemCount) {
                    notifyItemChanged(p, false)
                }
                p = lastChose + 1
                if (p in 0 until itemCount) {
                    notifyItemChanged(p, false)
                }
            }
            lastChose = pos
            p = lastChose
            if (p in 0 until itemCount) {
                notifyItemChanged(p, calendar)
            }
            p = lastChose - 1
            if (p in 0 until itemCount) {
                notifyItemChanged(p, calendar)
            }
            p = lastChose + 1
            if (p in 0 until itemCount) {
                notifyItemChanged(p, calendar)
            }
        }

        override fun getItemCount() = list.size
    }

    class MonthVH(val view: CalendarMonthView) : RecyclerView.ViewHolder(view)

    interface OnDayChoseListener {

        /**
         * 当选中时回调选中的日期
         * 如果是单日选中模式，则[start]和[end]为同一天
         * 如果是其它选择模式，则[start]和[end]为起始只截至时间
         *
         * 当返回true时，则会更新ui选中效果，否则不显示选中
         */
        fun onDayChose(start: Calendar, end: Calendar): Boolean = true
    }
}