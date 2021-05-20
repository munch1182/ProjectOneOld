package com.munch.test.project.one.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import com.munch.pre.lib.calender.*
import com.munch.pre.lib.helper.drawTextInCenter
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityCalendarBinding
import java.util.*

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