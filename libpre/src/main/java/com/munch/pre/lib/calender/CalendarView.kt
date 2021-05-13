package com.munch.pre.lib.calender

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.munch.pre.lib.extend.ViewHelper

/**
 * Create by munch1182 on 2021/5/6 15:07.
 */
class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var current = Day.now()
    private var config: CalendarConfig? = null

    fun update(current: Day, config: CalendarConfig) {
        this.current = current
        this.config = config
        monthViewPager.updateMonth(current.beMonth(), config)
    }

    private var monthViewPager = MonthViewPager(context, current.beMonth(), config)

    init {
        addView(monthViewPager.vp, ViewHelper.newParamsMW())
    }
}