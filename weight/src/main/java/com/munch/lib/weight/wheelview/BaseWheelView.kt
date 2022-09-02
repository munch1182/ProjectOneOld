package com.munch.lib.weight.wheelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.extend.drawTextInCenter
import com.munch.lib.extend.lazy
import com.munch.lib.extend.paddingVertical
import com.munch.lib.graphics.RectF
import com.munch.lib.weight.TouchHelperDefault
import kotlin.math.absoluteValue

/**
 * todo 可以通过设置的方式动画更改选中 [moveTo]
 * todo 可以设置方向
 *
 * Create by munch1182 on 2022/5/23 16:41.
 */
abstract class BaseWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    //处理item显示的文字
    protected abstract val item: OnItemListener
    protected open val touchHelper = TouchHelperDefault()
    protected open var itemSelect: OnItemSelectListener? = null

    //要显示的item条数
    open var showItemCount = 5
        set(value) {
            field = when {
                value <= 3 -> 3
                value % 2 == 0 -> value + 1
                else -> value
            }
        }

    open var textSize = 48f

    protected open val halfItemCount: Int
        get() = showItemCount / 2

    //要绘制的半边的item数量, 需要多绘制1个
    protected open val drawItemCount: Int
        get() = halfItemCount + 1
    protected open val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = this@BaseWheelView.textSize
        }
    }

    //正中心的点, 即正中心item文字的中心点
    protected open val center by lazy { PointF() }
    protected open val centerRect by lazy { RectF() }

    fun setOnItemSelectListener(listener: OnItemSelectListener) {
        this.itemSelect = listener
    }

    fun next() = moveTo(item.currIndex + 1)

    fun previous() = moveTo(item.currIndex - 1)

    fun moveTo(index: Int) {
        if (!item.isIndexValid(index)) return

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        center.set(w / 2f, h / 2f)
        val start = paddingLeft.toFloat()
        val end = w.toFloat() - paddingEnd

        val rh = h - paddingVertical()
        val halfItemH = (rh / showItemCount) / 2f
        val top = center.y - halfItemH
        val bottom = center.y + halfItemH

        centerRect.set(start, top, end, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        //canvas.drawColor(Color.WHITE)

        val itemH = centerRect.height()
        if (touchHelper.moveY.absoluteValue > itemH) {
            if (touchHelper.moveY < 0) {
                item.currIndex -= 1
                itemSelect?.onItemSelect(item.currIndex, item.curr)
            } else {
                item.currIndex += 1
                itemSelect?.onItemSelect(item.currIndex, item.curr)
            }
            touchHelper.downPoint.set(touchHelper.movePoint)
        }

        val x = center.x
        val centerY = center.y - touchHelper.moveY
        var y: Float
        var str: String?

        run top@{
            repeat(drawItemCount) {
                y = centerY - (it + 1) * itemH
                str = item.offset(-(it + 1)) ?: return@top
                canvas.drawTextInCenter(str!!, x, y, paint)
            }
        }
        run curr@{
            y = centerY
            str = item.curr ?: return@curr
            canvas.drawTextInCenter(str!!, x, y, paint)
        }
        run bottom@{
            repeat(drawItemCount) {
                y = centerY + (it + 1) * itemH
                str = item.offset((it + 1)) ?: return@bottom
                canvas.drawTextInCenter(str!!, x, y, paint)
            }
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        touchHelper.updateEvent(event)
        if (touchHelper.moveY != 0f) {
            invalidate()
        }
        return true
    }

    interface OnItemListener {
        // 当前选中的索引
        var currIndex: Int

        val curr: String?
            get() = offset(0)

        //当前索引对于的字符
        fun onItem(index: Int): String

        //当前偏移对应的字符
        fun offset(off: Int): String? {
            val i = currIndex + off
            return if (isIndexValid(i)) onItem(i) else null
        }

        //判断索引是否有效
        fun isIndexValid(index: Int): Boolean
    }

    fun interface OnItemSelectListener {
        fun onItemSelect(index: Int, item: String?)
    }
}