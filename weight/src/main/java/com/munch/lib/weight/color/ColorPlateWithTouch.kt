package com.munch.lib.weight.color

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.munch.lib.extend.lazy
import com.munch.lib.graphics.Shape
import com.munch.lib.weight.TouchHelper
import com.munch.lib.weight.TouchHelperDefault

open class ColorPlateWithTouch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ColorPlateOnly(context, attrs, defStyleAttr, defStyleRes), TouchHelper by TouchHelperDefault {

    protected open val touchBarHeight = 6f
    protected open val touchBarRadius = 32f
    protected open val circle = Shape.Circle()

    protected open val touchPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = touchBarHeight
            style = Paint.Style.STROKE
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val radius = rectView.width() / 2f
        movePoint.set(radius, radius)
        circle.x = paddingLeft.toFloat()
        circle.y = paddingTop.toFloat()
        circle.radius = radius
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawCircle(movePoint.x, movePoint.y, touchBarRadius, touchPaint)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) performClick()
        updateEvent(event) { it in circle }
        /*val h = calculateHue()
        val s = calculateSaturation()
        val c = Color.HSVToColor(floatArrayOf(h, s, 1f))*/
        invalidate()
        return true
    }

    private fun calculateSaturation(): Float {
        //val line = movePoint.dis(circle.centerPoint)
        return 0f
    }

    private fun calculateHue(): Float {
        return 0f
    }
}
