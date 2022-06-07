package com.munch.lib.weight.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.addMonth
import com.munch.lib.extend.getMonth
import com.munch.lib.extend.getMonthIndex
import com.munch.lib.extend.getYear
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
        (calendarNow.getYear() - calendarStart.getYear()) * 12 + calendarNow.getMonth() + 2

    private val monthAdapter = MonthAdapter(count)

    init {
        val lm = LinearLayoutManager(ctx)
        layoutManager = lm
        adapter = monthAdapter
        lm.scrollToPosition(monthAdapter.currIndex)
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

    private fun onDateChose(calendar: Calendar, type: CalendarMonthView.ChoseType, pos: Int) {
        select(calendar)
    }

    override val ctx: Context
        get() = context

    inner class MonthAdapter(private val count: Int = 100) : RecyclerView.Adapter<MonthVH>() {

        val currIndex = count - 3

        private val list = mutableListOf<Calendar>()

        init {
            list.clear()
            repeat(count) {
                list.add(0, Calendar.getInstance().apply { addMonth(count - 1 - it - currIndex) })
            }
        }

        fun get(pos: Int): Calendar? {
            return list.getOrNull(pos)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthVH {
            return MonthVH(CalendarMonthView(parent.context, week = null))
        }

        override fun onBindViewHolder(holder: MonthVH, position: Int) {
            holder.view.setCalender(list[position])
            holder.view.onDateChose = object : CalendarMonthView.OnDateChoseListener {
                override fun onDateChose(
                    calendar: Calendar,
                    type: CalendarMonthView.ChoseType
                ) {
                    onDateChose(calendar, type, holder.absoluteAdapterPosition)
                }
            }
        }

        override fun onBindViewHolder(holder: MonthVH, position: Int, payloads: MutableList<Any>) {
            if (payloads.isNotEmpty()) {
                val c = payloads[0] as Calendar? ?: return
                holder.view.chose(c)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }

        override fun getItemCount() = list.size
    }

    class MonthVH(val view: CalendarMonthView) : RecyclerView.ViewHolder(view)
}