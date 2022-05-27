package com.munch.project.one.weight

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.weight.calendar.CalendarMonthView

/**
 * Create by munch1182 on 2022/4/25 17:54.
 */
class CalendarActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(CalendarMonthView(this))
    }

}