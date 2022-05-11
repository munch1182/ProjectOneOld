package com.munch.project.one.weight

import android.graphics.*
import android.os.Bundle
import androidx.core.view.setPadding
import com.munch.lib.extend.dp2Px
import com.munch.lib.extend.drawTextInCenter
import com.munch.lib.extend.drawTextInYCenter
import com.munch.lib.extend.sp2Px
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.weight.calendar.*

/**
 * Create by munch1182 on 2022/4/25 17:54.
 */
class CalendarActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = Calendar(this,
            onDayDraw = object : OnDayDraw {

                private val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = sp2Px(18f)
                }

                private val rect = Rect()
                private val padding = dp2Px(8f).toInt()

                init {
                    paint.getTextBounds("30", 0, 2, rect)
                }

                override fun onMeasure(style: Style): Measure {
                    return Measure(rect.width() + padding, rect.height() + padding * 2)
                }

                override fun onDraw(canvas: Canvas, rect: RectF, year: Int, month: Int, day: Int) {
                    super.onDraw(canvas, rect, year, month, day)
                    canvas.drawTextInCenter(day.toString(), rect.centerX(), rect.centerY(), paint)
                }
            },
            onMonthDraw = object : OnMonthLabelDraw {

                private val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = sp2Px(20f)
                }

                private val rect = Rect()
                private val padding = dp2Px(8f).toInt()

                init {
                    paint.getTextBounds("2020年12月31日", 0, 10, rect)
                }

                override fun onMeasure(style: Style): Measure {
                    return Measure(rect.width() + padding, rect.height() + padding * 2)
                }

                override fun onDraw(canvas: Canvas, rect: RectF, year: Int, month: Int) {
                    super.onDraw(canvas, rect, year, month)
                    canvas.drawTextInYCenter("${year}年${month}月", rect.left, rect.centerY(), paint)
                }
            },
            onWeekDraw = object : OnWeekLabelDraw {
                private val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = sp2Px(14f)
                }

                private val rect = Rect()
                private val padding = dp2Px(8f).toInt()

                init {
                    paint.getTextBounds("星期日", 0, 3, rect)
                }

                override fun onMeasureHeight(): Int {
                    return rect.height() + padding * 2
                }

                override fun onDraw(canvas: Canvas, rect: RectF, dayOfWeek: Int) {
                    super.onDraw(canvas, rect, dayOfWeek)
                    canvas.drawTextInCenter("星期$dayOfWeek", rect.centerX(), rect.centerY(), paint)
                }
            }).apply { setPadding(16) }

        setContentView(view)
    }

}