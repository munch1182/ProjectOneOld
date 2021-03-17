package com.munch.project.test.view

import android.os.Bundle
import android.view.ViewGroup
import com.munch.lib.helper.formatDate
import com.munch.lib.helper.setMargin
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.view.CalendarView
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/3/17 16:10.
 */
class TestCalendarViewActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = CalendarView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setMargin(16)
            set()
        }
        setContentView(view)
        view.setOnItemClick { _, _, date ->
            view.choseDay(date)
        }
    }
}