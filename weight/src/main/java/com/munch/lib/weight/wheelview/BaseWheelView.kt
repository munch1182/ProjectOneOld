package com.munch.lib.weight.wheelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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

/**
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
    protected open val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = this@BaseWheelView.textSize
        }
    }

    //正中心的点, 即正中心item文字的中心点
    protected open val center by lazy { PointF() }
    protected open val centerRect by lazy { RectF() }

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

        canvas.drawColor(Color.BLUE)

        val x = center.x
        var y: Float
        var str: String?
        val itemH = centerRect.height()
        kotlin.run top@{
            repeat(halfItemCount) {
                y = center.y - (it + 1) * itemH
                str = item.offset(-(it + 1)) ?: return@top
                canvas.drawTextInCenter(str!!, x, y, paint)
            }
        }
        kotlin.run curr@{
            y = center.y
            str = item.offset(0) ?: return@curr
            canvas.drawTextInCenter(str!!, x, y, paint)
        }
        kotlin.run bottom@{
            repeat(halfItemCount) {
                y = center.y + (it + 1) * itemH
                str = item.offset((it + 1)) ?: return@bottom
                canvas.drawTextInCenter(str!!, x, y, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        /*touchHelper.updateEvent(event) {
            false
        }*/
        return true
    }

    interface OnItemListener {
        // 当前选中的索引
        var currIndex: Int

        //当前索引对于的字符
        fun onItem(index: Int): String

        //当前偏移对应的字符
        fun offset(off: Int): String? {
            val i = currIndex + off
            return if (onIndexValid(i)) onItem(i) else null
        }

        //判断索引是否有效
        fun onIndexValid(index: Int): Boolean
    }
}