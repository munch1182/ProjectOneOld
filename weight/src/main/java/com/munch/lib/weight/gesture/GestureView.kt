package com.munch.lib.weight.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.extend.drawTextInYCenter
import com.munch.lib.helper.array.PointFArrayHelper
import kotlin.math.abs

/**
 * Created by munch1182 on 2022/6/11 3:46.
 */
class GestureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val pressed = PointF()
    private val temp = PointF()

    private val move = PointFArrayHelper()
    private val longPressedCheck = LongPressed()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFA500")
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
    }
    private val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        textSize = 20f
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        temp.set(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressed.set(temp)
                move.add(temp)
                longPressedCheck.post(pressed)
            }
            MotionEvent.ACTION_MOVE -> {
                longPressedCheck.removeIfNeed(temp)
                move.add(temp)
            }
            MotionEvent.ACTION_UP -> {
                longPressedCheck.removeIfNeed()
                //move.add(temp)
                if (temp == pressed) {
                    performClick()
                }
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        move.forEachIndexed { index, it ->
            canvas.drawCircle(it.x, it.y, 8f, paint)
            if (index % 5 == 0) {
                canvas.drawTextInYCenter(
                    "(${it.x.toInt()}, ${it.y.toInt()})",
                    it.x + 20,
                    it.y,
                    descPaint
                )
            }
        }
    }


    fun clear() {
        move.clear()
        invalidate()
    }

    internal inner class LongPressed : Runnable {

        private var isRemoved = false
        private var pressed: PointF? = null

        override fun run() {
            clear()
        }

        fun post(pressed: PointF) {
            this.pressed = pressed
            isRemoved = false
            postDelayed(this, 800L)
        }

        fun removeIfNeed(point: PointF? = null) {
            if (!isRemoved) {
                val notMove =
                    point?.let { m ->
                        pressed?.let { p -> abs(m.x - p.x) > 25 && abs(m.y - p.y) > 25 }
                    } ?: true
                if (notMove) {
                    removeCallbacks(this)
                    isRemoved = true
                }
            }
        }
    }
}