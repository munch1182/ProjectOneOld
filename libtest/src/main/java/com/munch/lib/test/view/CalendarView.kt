package com.munch.lib.test.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.helper.SpecialArrayHelper
import com.munch.lib.helper.drawTextInCenter
import com.munch.lib.log
import java.util.*

/**
 * Create by munch1182 on 2021/3/17 15:43.
 */
class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val instance = Calendar.getInstance()
    var colorDay = Color.BLACK
    var colorViewBg = Color.WHITE
    var colorChoseBg = Color.parseColor("#ff888888")
    var colorChoseDay = Color.parseColor("#ffffffff")
    var bgPadding = 8f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorDay
        textSize = 45f
    }
    private val chosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorChoseBg
    }
    private val textCenterArray = SpecialArrayHelper(2)
    private var choseDay = 0
    private var eachWithCenter: Float = 0f
    private var eachHeightCenter: Float = 0f
    private var spaceDay = 0
    private var onItemClick: ((view: CalendarView, date: Date, dayOfMouth: Int) -> Unit)? = null

    init {
        setBackgroundColor(colorViewBg)
    }

    fun setProperty(func: CalendarView.() -> Unit) {
        func.invoke(this)
        invalidate()
    }

    fun getDayHeight(): Float {
        return eachHeightCenter * 2f
    }

    fun getDayWidth(): Float {
        return eachWithCenter * 2f
    }

    fun setOnItemClick(onItemClick: ((view: CalendarView, date: Date, dayOfMouth: Int) -> Unit)? = null): CalendarView {
        this.onItemClick = onItemClick
        return this
    }

    fun choseDay(day: Int) {
        choseDay = day
        invalidate()
    }

    fun choseDay(date: Date) {
        instance.time = date
        choseDay(instance.get(Calendar.DAY_OF_MONTH))
    }

    fun nextMouth() {
        instance.add(Calendar.MONTH, 1)
        requestLayout()
        invalidate()
    }

    fun lastMouth() {
        instance.add(Calendar.MONTH, -1)
        requestLayout()
        invalidate()
    }

    fun set(date: Date = Date(), firstDayOfWeek: Int = Calendar.MONDAY) {
        instance.firstDayOfWeek = firstDayOfWeek
        instance.time = date
        requestLayout()
    }

    private fun getSpaceDayOfWeek(): Int {
        val day = instance.get(Calendar.DAY_OF_WEEK)
        if (instance.firstDayOfWeek == Calendar.MONDAY) {
            if (day == Calendar.SUNDAY) {
                return 6 + day - Calendar.SUNDAY
            }
        }
        return day - instance.firstDayOfWeek
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        eachWithCenter = measureWidth / 7f / 2f
        eachHeightCenter =
            MeasureSpec.getSize(heightMeasureSpec) / instance.getActualMaximum(Calendar.WEEK_OF_MONTH) / 2f
        if (eachHeightCenter > eachWithCenter * 1.5f) {
            eachHeightCenter = eachWithCenter
        }
        textCenterArray.clear()
        instance.set(Calendar.DAY_OF_MONTH, 1)
        spaceDay = getSpaceDayOfWeek()
        val allItem = instance.getActualMaximum(Calendar.DAY_OF_MONTH) + spaceDay
        var day = 1
        for (i in 0 until allItem) {
            if (i < spaceDay) {
                continue
            }
            val week = i % 7
            val weekIndex = i / 7
            textCenterArray.addArray(
                eachWithCenter * 2f * week + eachWithCenter,
                eachHeightCenter * 2f * weekIndex + eachHeightCenter
            )
            day++
        }
        val array = textCenterArray.getArray()
        setMeasuredDimension(measureWidth, (array[array.lastIndex] + eachHeightCenter).toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val size = textCenterArray.getArray().size / 2
        for (i in 1..size) {
            val cx = textCenterArray.getVal(i - 1, 0)
            val cy = textCenterArray.getVal(i - 1, 1)
            if (choseDay == i) {
                canvas.drawRoundRect(
                    cx - eachWithCenter + bgPadding,
                    cy - eachHeightCenter + bgPadding,
                    cx + eachWithCenter - bgPadding,
                    cy + eachHeightCenter - bgPadding,
                    8f, 8f,
                    chosePaint
                )
                paint.color = colorChoseDay
            } else {
                paint.color = colorDay
            }
            canvas.drawTextInCenter(i.toString(), cx, cy, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
            val x = (event.x / (eachHeightCenter * 2f)).toInt()
            val y = (event.y / (eachHeightCenter * 2f)).toInt()
            val index = y * 7 + x - spaceDay + 1
            if (index < textCenterArray.getArray().size / 2 + 1) {
                instance.set(Calendar.DAY_OF_MONTH, index)
                onItemClick?.invoke(this, instance.time, index)
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

}