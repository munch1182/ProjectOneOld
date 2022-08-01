package com.munch.project.one.weight

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.munch.lib.extend.dp2Px
import com.munch.lib.extend.newWWLp
import com.munch.lib.extend.toDateStr
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.helper.ViewColorHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvClassRv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.log.log
import com.munch.lib.weight.calendar.CalendarHeaderView
import com.munch.lib.weight.calendar.CalendarView
import com.munch.lib.weight.color.ColorPlateWithTouch
import com.munch.lib.weight.wheelview.WheelView
import com.munch.project.one.databinding.ActivitySimpleViewBinding
import kotlinx.coroutines.delay
import java.util.*

/**
 * Create by munch1182 on 2022/4/25 17:54.
 */
class WeightActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val vb by fvClassRv(
        listOf(
            ColorPlateActivity::class,
            GestureActivity::class,
            CalendarActivity::class,
            WheelActivity::class,
            SimpleViewActivity::class,
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
        setContentView(CalendarHeaderView(ctx).apply {
            setOnDayChose(object : CalendarView.OnDayChoseListener {
                override fun onDayChose(start: Calendar, end: Calendar): Boolean {
                    log(start.toDateStr("yyyy-MM-dd"), end.toDateStr("yyyy-MM-dd"))
                    return true
                }
            })
        })
    }
}

class ColorPlateActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                ColorPlateWithTouch(ctx).apply {
                    setPadding(dp2Px(48f).toInt())
                    setBackgroundColor(Color.BLACK)
                })
        })
    }
}

class SimpleViewActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bind = ActivitySimpleViewBinding.inflate(layoutInflater)
        setContentView(bind.root)
        ViewColorHelper.fitTextColor(bind.text, true)
        bind.viewSwitch.setOnCheck {
            delay(Random().nextInt(1000) + 300L)
            true
        }
    }

}