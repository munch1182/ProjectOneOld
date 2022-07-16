package com.munch.lib.weight

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

interface ViewHelper {

    val paint: Paint
    val viewRect: Rect
    val centerPoint: PointF

    fun measureSquare(widthMeasureSpec: Int, heightMeasureSpec: Int): Int {
        val w = View.MeasureSpec.getSize(widthMeasureSpec)
        val h = View.MeasureSpec.getSize(heightMeasureSpec)
        return min(w, h)
    }

    fun layoutView(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        viewRect.set(left, top, right, bottom)
        centerPoint.set(
            (right - top - view.paddingLeft - view.paddingRight) / 2f,
            (bottom - top - view.paddingTop - view.paddingBottom) / 2f
        )
    }
}

object ViewHelperDefault : ViewHelper {
    override val paint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    override val viewRect: Rect by lazy { Rect() }
    override val centerPoint: PointF by lazy { PointF() }
}

interface TouchHelper {

    val downPoint: PointF
    val movePoint: PointF
    val tmpPoint: PointF

    fun updateEvent(event: MotionEvent, limit: ((PointF) -> Boolean)? = null) {
        tmpPoint.set(event.x, event.y)
        if (limit != null && !limit.invoke(tmpPoint)) {
            return
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(tmpPoint)
                movePoint.set(tmpPoint)
            }
            MotionEvent.ACTION_MOVE -> {
                movePoint.set(tmpPoint)
            }
        }
    }
}

object TouchHelperDefault : TouchHelper {
    override val downPoint: PointF = PointF()
    override val movePoint: PointF = PointF()
    override val tmpPoint: PointF = PointF()
}