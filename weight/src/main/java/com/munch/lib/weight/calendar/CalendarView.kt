package com.munch.lib.weight.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.addMonth
import com.munch.lib.extend.getMonth
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
    private var lastPos = -1

    init {
        val lm = LinearLayoutManager(ctx)

        val count =
            (calendarNow.getYear() - calendarStart.getYear()) * 12 + calendarNow.getMonth() + 2

        val monthAdapter = MonthAdapter(count)

        layoutManager = lm
        adapter = monthAdapter
        lm.scrollToPosition(monthAdapter.currIndex)
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != SCROLL_STATE_IDLE) {
                    val pos = lm.findFirstVisibleItemPosition()
                    if (lastPos == pos) {
                        return
                    }
                    lastPos = pos
                }
            }
        })
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthVH {
            return MonthVH(CalendarMonthView(parent.context))
        }

        override fun onBindViewHolder(holder: MonthVH, position: Int) {
            holder.view.setCalender(list[position])
        }

        override fun getItemCount() = count
    }

    class MonthVH(val view: CalendarMonthView) : RecyclerView.ViewHolder(view)
}