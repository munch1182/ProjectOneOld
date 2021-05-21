package com.munch.pre.lib.calender

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.marginTop

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
    private var weekRect = RectF()
    private var widthUnit = 0f
    private var heightUnit = 0f

    fun update(current: Day, config: CalendarConfig) {
        this.current = current
        this.config = config
        monthViewPager.updateMonth(current.beMonth(), config)
        if (monthViewPager.vp.layoutParams is LayoutParams) {
            val weekHeight = config.height.weekLineHeight.toInt()
            if (marginTop != weekHeight) {
                (monthViewPager.vp.layoutParams as LayoutParams)
                    .setMargins(0, weekHeight, 0, 0)
            }
        }
    }

    fun update(current: Day) {
        if (this.current != current) {
            this.current = current
            monthViewPager.updateMonth(current)
        }
    }

    private var monthViewPager = MonthViewPager(context, current.beMonth(), config)

    init {
        addView(
            monthViewPager.vp,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, config?.height?.weekLineHeight?.toInt() ?: 0, 0, 0)
            })
        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthUnit = (w - paddingLeft - paddingRight - (config?.wh?.borderSize ?: 0) * 8) / 7f
        heightUnit = config?.height?.weekLineHeight ?: 0f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val config = config ?: return
        val draw = config.drawConfig ?: return
        val border = config.wh.borderSize.toFloat()
        for (i in 0..6) {
            val left = paddingStart + border * (i + 1f) + widthUnit * i
            val top = paddingTop + border
            weekRect.set(left, top, widthUnit + left, top + heightUnit)
            var week = (config.firstDayOfWeek + i) % 7
            if (week == 0) {
                week = 7
            }
            draw.onDrawWeekLine(canvas, week, weekRect)
        }
    }

}