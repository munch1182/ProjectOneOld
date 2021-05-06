package com.munch.pre.lib.calender

import android.content.Context
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * Create by munch1182 on 2021/5/6 15:07.
 */
class CalendarView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    internal var maxDay = Calendar.getInstance().apply {
        set(2999, 12, 31, 23, 59, 59)
    }
    internal var minDay = Calendar.getInstance().apply {
        set(1, 1, 1, 0, 0, 0)
    }
    private var current = Calendar.getInstance()

    private var monthViewPager = MonthViewPager(context, current)


    fun updateMonth(month: Calendar) = monthViewPager.updateMonth(month)
}