package com.munch.lib.weight.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.extend.*
import com.munch.lib.graphics.RectF
import com.munch.lib.helper.array.RectFArrayHelper
import com.munch.lib.log.log
import java.util.*
import kotlin.math.log
import kotlin.math.max

/**
 * Create by munch1182 on 2022/4/25 16:05.
 */
class CalendarMonthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0,
) : View(context, attrs, styleDef) {

    //当前时间
    private val calendar = Calendar.getInstance().apply {
        //firstDayOfWeek = Calendar.MONDAY
    }
    private var choseType: ChoseType = ChoseType.Week

    //当前显示的月份
    private val calendarNow = Calendar.getInstance()

    //选中的时间，只代表点中的时间
    private val calendarSelected = Calendar.getInstance()

    //drawer
    private val month = MonthDrawer(context)
    private val day = DayDrawer(context)
    private val week = WeekDrawer(context)


    //rect, 绘制的区域
    private val rectMonth = RectF()
    private val rectWeek = RectF()
    private val rectDay = RectF()

    private val measureDay = day.onItemMeasure()
    private val measureWeek = week.onItemMeasure()
    private val measureMonth = month.onItemMeasure()

    private val rectBuff = RectF()
    private val calendarBuff = Calendar.getInstance().apply {
        firstDayOfWeek = calendar.firstDayOfWeek
    }

    //存储所有的位置
    private var daysRect = RectFArrayHelper()

    //存储被选中的天
    private var daysChose = arrayListOf<Int>()

    private val itemDesc = OnItemDesc(rectBuff)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = max(
            max(measureDay.w, measureWeek.w) * 7 + paddingLeft + paddingRight,
            MeasureSpec.getSize(widthMeasureSpec)
        )
        //todo 高度设置或者计算
        val height =
            measureMonth.h + measureWeek.h * 3 + measureDay.h * 12 + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val itemWidth = (w - paddingLeft - paddingRight) / 7

        //todo 结构分发
        //此处已经确定好位置的分布
        //第一个月份的位置
        rectMonth.left = paddingLeft.toFloat()
        rectMonth.top = paddingTop.toFloat()
        rectMonth.right = w - paddingRight.toFloat()
        rectMonth.bottom = rectMonth.top + measureMonth.h
        //第一个周的位置
        rectWeek.left = rectMonth.left
        rectWeek.top = paddingTop.toFloat()
        rectWeek.right = rectWeek.left + itemWidth
        rectWeek.bottom = rectWeek.top + measureWeek.h
        //第一个日的位置， 从0开始
        rectDay.left = rectWeek.left
        rectDay.top = paddingTop.toFloat()
        rectDay.right = rectDay.left + itemWidth
        rectDay.bottom = rectDay.top + measureDay.h

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        calendarNow.timeInMillis = calendar.timeInMillis
        var height = paddingTop.toFloat()

        //month
        height += drawMonth(canvas, height)
        //week
        height += drawWeeks(canvas, height)
        //day
        height += drawDays(canvas, height)
    }

    private fun drawDays(canvas: Canvas, height: Float): Float {
        rectBuff.set(rectDay)
        rectBuff.translation(0f, height)

        val width = rectWeek.width()

        calendarBuff.time = calendarNow.time
        calendarBuff.set(Calendar.DAY_OF_MONTH, 1)
        val days = calendarBuff.getActualMaximum(Calendar.DAY_OF_MONTH)

        val lineHeight = rectDay.height()
        var h = lineHeight

        //修改月份前的位置
        val index = calendarBuff.getDayInWeekIndex()
        repeat(index) { rectBuff.translation(width, 0f) }

        daysChose.sort()

        repeat(days) {
            val d = it + 1
            if (rectBuff.right > getWidth()) {
                rectBuff.copyLR(rectDay)
                rectBuff.translation(0f, lineHeight)

                h += lineHeight
            }

            calendarBuff.set(Calendar.DAY_OF_MONTH, d)

            itemDesc.reset()
            itemDesc.isLineStart = (index + it) % 7 == 0
            itemDesc.isLineEnd = (index + it) % 7 == 6 || it == days - 1
            itemDesc.isSelected = daysChose.contains(d)
            //此处只考虑连贯选择
            itemDesc.hasLastSelected = itemDesc.isSelected && daysChose.firstOrNull() != d
            itemDesc.hasNextSelected = itemDesc.isSelected && daysChose.lastOrNull() != d
            day.onItemDraw(canvas, itemDesc, calendarBuff)

            daysRect.add(rectBuff)

            rectBuff.translation(width, 0f)
        }
        return h
    }

    private fun drawWeeks(canvas: Canvas, height: Float): Float {
        rectBuff.set(rectWeek)
        rectBuff.top += height
        rectBuff.bottom += height

        val width = rectWeek.width()

        calendarBuff.time = calendarNow.time
        calendarBuff.set(Calendar.DAY_OF_WEEK, calendarBuff.firstDayOfWeek)
        itemDesc.reset()
        repeat(7) {
            itemDesc.isLineStart = it == 0
            itemDesc.isLineEnd = it == 6
            itemDesc.hasLastSelected = it != 0
            itemDesc.hasNextSelected = it != 6
            week.onItemDraw(canvas, itemDesc, calendarBuff)

            rectBuff.left += width
            rectBuff.right += width
            calendarBuff.add(Calendar.DAY_OF_MONTH, 1)
        }

        return rectWeek.height()
    }

    private fun drawMonth(canvas: Canvas, height: Float): Float {
        rectBuff.set(rectMonth)
        rectBuff.top += height
        rectBuff.bottom += height

        calendarBuff.time = calendarNow.time
        itemDesc.reset()
        month.onItemDraw(canvas, itemDesc, calendarBuff)

        return rectMonth.height()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private val lastPress = PointF()
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val e = event ?: return super.onTouchEvent(event)
        if (lastPress.x != e.x && lastPress.y != e.y) {
            lastPress.set(e.x, e.y)
            choseByLastPress()
        }
        performClick()
        return true
    }

    private fun choseByLastPress() {
        var date = daysRect.indexOfFirst { lastPress in it }
        if (date != -1) {
            date += 1
            when (choseType) {
                ChoseType.Day -> {
                    //因为index是从0开始的
                    daysChose.clear()
                    daysChose.add(date)

                }
                ChoseType.Month -> {
                    daysChose.clear()
                    repeat(daysRect.size) {
                        date = it + 1
                        daysChose.add(date)
                    }
                }
                ChoseType.Week -> {
                    daysChose.clear()
                    calendarBuff.set(Calendar.DAY_OF_MONTH, date)
                    var index = calendarBuff.getDayInWeekIndex()
                    if (index == 0) {
                        index = 6
                    } else {
                        index -= 1
                    }
                    repeat(index) { daysChose.add(date - (it + 1)) }
                    repeat(7 - index) { daysChose.add(date + it) }
                }
            }
            invalidate()
        }
    }

    /**
     * @see choseType
     */
    private fun choseByDate(calendar: Calendar) {
        val monthNumber = calendarBuff.getMonth()
        when (choseType) {
            ChoseType.Day -> {
                if (calendar.getMonth() != monthNumber) {
                    return
                }
                daysChose.clear()
                daysChose.add(calendar.getDay())

            }
            ChoseType.Month -> {
                if (calendar.getMonth() != monthNumber) {
                    return
                }
                daysChose.clear()
                repeat(daysRect.size) {
                    daysChose.add(it + 1)
                }
            }
            ChoseType.Week -> {
                daysChose.clear()
                var index = calendar.getDayInWeekIndex()
                if (index == 0) {
                    index = 6
                } else {
                    index -= 1
                }
                val date = calendar.getDate()
                repeat(index) {
                    if (calendar.getMonth() == monthNumber) {
                        daysChose.add(date - (it + 1))
                    }
                }
                repeat(7 - index) {
                    if (calendar.getMonth() == monthNumber) {
                        daysChose.add(date + it)
                    }
                }
            }
        }
        invalidate()
    }

    sealed class ChoseType {
        object Day : ChoseType()
        object Week : ChoseType()
        object Month : ChoseType()
    }
}