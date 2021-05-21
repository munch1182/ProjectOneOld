package com.munch.test.project.one.view

import android.os.Bundle
import com.munch.pre.lib.calender.CalendarHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityCalendarBinding

/**
 * Create by munch1182 on 2021/5/6 14:49.
 */
class CalendarActivity : BaseTopActivity() {

    private val bind by bind<ActivityCalendarBinding>(R.layout.activity_calendar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        CalendarHelper.def(bind.calendarMonth)
    }
}