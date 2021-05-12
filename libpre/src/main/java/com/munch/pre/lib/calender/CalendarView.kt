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

    private var current = Day.now()
    var config = CalendarConfig()

    private var monthViewPager = MonthViewPager(context, current, config)

    fun updateMonth(month: Month) = monthViewPager.updateMonth(month)
}