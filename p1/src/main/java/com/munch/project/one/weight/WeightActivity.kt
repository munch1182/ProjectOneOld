package com.munch.project.one.weight

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.munch.lib.extend.*
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
import com.munch.project.one.databinding.ActivityShapeBinding
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
            ShapeActivity::class,
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
                ColorPlateWithTouch(ctx),
                newWWLp().apply { setPadding(dp2Px(16f).toInt()) })
        })
    }
}

class ShapeActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShapeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewColorHelper.onUpdate {
            binding.shape.onUpdate {
                ViewColorHelper.getColor(this@ShapeActivity)?.let {
                    this.color = it
                    this.strokeColor = it.darker(0.6f)

                    val textColor = if (it.isLight()) Color.BLACK else Color.WHITE
                    binding.text.setTextColor(textColor)
                }
            }
        }

    }
}