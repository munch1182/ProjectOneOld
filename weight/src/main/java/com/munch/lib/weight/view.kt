package com.munch.lib.weight

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.munch.lib.extend.*
import com.munch.lib.graphics.RectF
import kotlin.math.min

interface ViewHelper {

    val paint: Paint
    val rectView: Rect
    val rectTmp: RectF

    fun getSquareRadius(widthMeasureSpec: Int, heightMeasureSpec: Int): Int {
        val w = View.MeasureSpec.getSize(widthMeasureSpec)
        val h = View.MeasureSpec.getSize(heightMeasureSpec)
        return min(w, h)
    }

    fun getDefaultOrSize(
        view: View,
        wDefaultDp: Float,
        hDefaultDp: Float,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): Pair<Int, Int> {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val width = if (widthMode != View.MeasureSpec.EXACTLY) {
            view.context.dp2Px(wDefaultDp).toInt()
        } else {
            View.MeasureSpec.getSize(widthMeasureSpec)
        }
        val height = if (widthMode != View.MeasureSpec.EXACTLY) {
            view.context.dp2Px(hDefaultDp).toInt()
        } else {
            View.MeasureSpec.getSize(heightMeasureSpec)
        }
        return (width + view.paddingHorizontal()) to (height + view.paddingVertical())
    }

    fun updateViewRect(view: View, w: Int, h: Int) {
        rectView.set(
            view.paddingLeft, view.paddingTop, w - view.paddingRight, h - view.paddingBottom
        )
    }
}

object ViewHelperDefault : ViewHelper {
    override val paint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    override val rectView: Rect by lazy { Rect() }
    override val rectTmp: RectF by lazy { RectF() }
}

interface TouchHelper {

    val downPoint: PointF
    val movePoint: PointF
    val tmpPoint: PointF
    val offset: Float

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

    fun updateEvent(event: MotionEvent) {
        tmpPoint.set(event.x, event.y)
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

    fun dispatchClickIfUp(view: View, event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP && isClick) {
            view.performClick()
        }
    }

    val isClick: Boolean
        get() = tmpPoint.isInOffset(downPoint, offset)
}

object TouchHelperDefault : TouchHelper {
    override val downPoint: PointF = PointF()
    override val movePoint: PointF = PointF()
    override val tmpPoint: PointF = PointF()
    override val offset: Float = 25f
}