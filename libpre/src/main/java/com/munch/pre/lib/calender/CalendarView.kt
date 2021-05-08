package com.munch.pre.lib.calender

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Create by munch1182 on 2021/5/6 15:07.
 */
class CalendarView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    internal var maxDay = Day.now().apply {
        year += 999
        month = 12
        day = 31
    }
    internal var minDay = Day(1, 1, 1)
    private var current = Day.now()

    private var monthViewPager = MonthViewPager(context, current, CalendarConfig())


    fun updateMonth(month: Month) = monthViewPager.updateMonth(month)
}