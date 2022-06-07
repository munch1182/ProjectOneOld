package com.munch.project.one.weight

import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.view.setPadding
import com.munch.lib.extend.dp2Px
import com.munch.lib.extend.newWWLp
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvClassRv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.weight.calendar.CalendarView
import com.munch.lib.weight.recyclerview.CalendarHeaderView
import com.munch.lib.weight.wheelview.WheelView

/**
 * Create by munch1182 on 2022/4/25 17:54.
 */
class WeightActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val vb by fvClassRv(
        listOf(
            CalendarActivity::class,
            WheelActivity::class,
            RecyclerViewHeaderActivity::class
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb.init()
    }

}

class CalendarActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(CalendarView(ctx))
    }
}

class WheelActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(ctx).apply {
            addView(
                WheelView(ctx),
                newWWLp().apply { setPadding(dp2Px(16f).toInt()) })
        })
    }
}

class RecyclerViewHeaderActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(CalendarHeaderView(ctx))
    }
}