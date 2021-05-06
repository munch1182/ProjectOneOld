package com.munch.pre.lib.calender

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Create by munch1182 on 2021/5/6 16:12.
 */
class MonthView(
    context: Context,
    private val month: Month,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : View(context, attrs, defAttr) {

    constructor(context: Context) : this(context, Month.now())

    init {
        analyseMonth()
    }

    private var maxHeight = 0

    private fun analyseMonth() {
        /*month.set(Calendar.DAY_OF_YEAR, 1)
        val startOffset = month.get(Calendar.WEEK_OF_MONTH)
        var days = month.getMaximum(Calendar.DAY_OF_MONTH)
        month.set(Calendar.DAY_OF_YEAR, days)
        val endOffset = month.get(Calendar.WEEK_OF_MONTH)
        days += startOffset + (7 - endOffset)
        //onMeasure去获取
        val height = 1
        maxHeight = days / 7 * height*/
    }
}