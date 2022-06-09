package com.munch.lib.weight.calendar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.munch.lib.weight.recyclerview.RecyclerHeaderView
import java.util.*

/**
 * Create by munch1182 on 2022/6/6 17:03.
 */
class CalendarHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RecyclerHeaderView(context, attrs, defStyleAttr, defStyleRes) {

    private val month by lazy { CalendarMonthView(context, day = null) }
    private val calendar by lazy { CalendarView(context) }

    override val child: View by lazy { month }
    override val recyclerView: CalendarView by lazy { calendar }

    init {
        addView(calendar)
        addView(month)
        month.setBackgroundColor(Color.WHITE)
        calendar.setBackgroundColor(Color.WHITE)
    }

    override fun updateFirstPos(itemPos: Int) {
        super.updateFirstPos(itemPos)
        if (itemPos == -1) return
        month.setCalender(calendar.get(itemPos) ?: return)
    }

    fun select(calendar: Calendar) {
        recyclerView.select(calendar)
    }

    fun setOnDayChose(onDayChose: CalendarView.OnDayChoseListener) {
        calendar.setOnDayChose(onDayChose)
    }


}