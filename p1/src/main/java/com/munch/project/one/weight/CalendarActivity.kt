package com.munch.project.one.weight

import android.graphics.Canvas
import android.graphics.RectF
import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.SupportConfigDialog
import com.munch.lib.log.log
import com.munch.lib.weight.calendar.Calendar
import com.munch.lib.weight.calendar.OnDayDraw

/**
 * Create by munch1182 on 2022/4/25 17:54.
 */
class CalendarActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog()) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = Calendar(this)
        setContentView(view)

        view.onDayDraw = object  : OnDayDraw{

            override fun onDraw(canvas: Canvas, rect: RectF, year: Int, month: Int, day: Int) {
                super.onDraw(canvas, rect, year, month, day)
                log("$year-$month-$day: $rect")
            }
        }
    }

}