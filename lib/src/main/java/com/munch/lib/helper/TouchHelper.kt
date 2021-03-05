package com.munch.lib.helper

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import com.munch.lib.*
import com.munch.lib.base.Orientation
import kotlin.math.absoluteValue

/**
 * 快按屏幕: onTap
 * 双击屏幕(singleTapConfirmed=false): onTap -> onDoubleTap
 * 双击屏幕(singleTapConfirmed=true): onDoubleTap
 * 按下屏幕等待一段时间 onTap –> onLongPress
 * 拖动屏幕：onTap–>onMove(多个)
 * 快速滑动：onTap–>onMove(多个)–> onFling
 *
 * 当一个方向的移动距离大过另一个方向的移动值且该移动距离大于touchSlop时被视为向该方向移动
 *
 * Create by munch1182 on 2021/3/5 15:19.
 */

@UNTEST
@UNCOMPLETE
@Deprecated("相较于android.view.GestureDetector没有明显优势")
class TouchHelper {

    companion object {
        val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()
        val TAP_TIMEOUT = ViewConfiguration.getTapTimeout()
        val DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout()
    }

    private var downPoint = PointF().clear()
    private var currentMovePoint = PointF().clear()

    private val orientation: @Orientation Int = 0
    private val velocityTracker = VelocityTracker.obtain()

    private var onTap: ((x: Float, y: Float) -> Unit)? = null
    private var onLongPress: ((x: Float, y: Float) -> Unit)? = null
    private var onDoubleTap: ((x: Float, y: Float) -> Unit)? = null
    private var onMove: ((
        orientation: @Orientation Int, touchHelper: TouchHelper, velocity: VelocityTracker
    ) -> Unit)? = null
    private var onFling: ((
        orientation: @Orientation Int, touchHelper: TouchHelper, velocity: VelocityTracker
    ) -> Unit)? = null

    private val viewConfiguration = ViewConfiguration.get(BaseApp.getContext())
    private val minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
    private val maximumFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
    private val touchSlop = viewConfiguration.scaledTouchSlop

    private val singleTapConfirmed = true
    private val actionHandler = ActionHandler()

    private class ActionHandler : Handler(Looper.getMainLooper()) {

        companion object {
            private const val TAP = 0
            private const val UP = 1
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }

        fun tap(event: MotionEvent) {
            sendMessageDelayed(Message.obtain(this, TAP), TAP_TIMEOUT.toLong())
        }
    }

    fun manager(orientation: @Orientation Int) {
    }

    fun setOnTapListener(onTap: (x: Float, y: Float) -> Unit): TouchHelper {
        this.onTap = onTap
        return this
    }

    fun setOnLongPressListener(onLongPress: (x: Float, y: Float) -> Unit): TouchHelper {
        this.onLongPress = onLongPress
        return this
    }

    fun setOnDoubleTapListener(onDoubleTap: (x: Float, y: Float) -> Unit): TouchHelper {
        this.onDoubleTap = onDoubleTap
        return this
    }

    fun setOnMoveListener(onMove: (orientation: @Orientation Int, touchHelper: TouchHelper, velocity: VelocityTracker) -> Unit): TouchHelper {
        this.onMove = onMove
        return this
    }

    fun onFling(onFling: (orientation: @Orientation Int, touchHelper: TouchHelper, velocity: VelocityTracker) -> Unit): TouchHelper {
        this.onFling = onFling
        return this
    }

    fun down(event: MotionEvent): TouchHelper {
        this.downPoint.reset(event.x, event.y)
        if (!singleTapConfirmed) {
            onTap?.invoke(event.x, event.y)
        } else {
            actionHandler.tap(event)
        }
        return this
    }


    fun move(event: MotionEvent): TouchHelper {
        if (onMove != null) {
            this.currentMovePoint.reset(event.x, event.y)
            val offsetX = currentMovePoint.x - downPoint.x
            val offsetY = currentMovePoint.y - downPoint.y
            val abValueX = offsetX.absoluteValue
            val abValueY = offsetY.absoluteValue
            //被视为有效的移动
            val orientation = if (abValueX > abValueY && abValueX >= touchSlop) {
                if (offsetX > 0) Orientation.LR else Orientation.RL
            } else if (abValueY >= touchSlop) {
                if (offsetY > 0) Orientation.TB else Orientation.BT
            } else {
                return this
            }
            onMove!!.invoke(orientation, this, velocityTracker)
        }
        return this
    }


    fun up(event: MotionEvent): TouchHelper {
        reset()
        return this
    }

    /**
     * 该次按下是否是双击
     */
    private fun isConsideredDoubleTap(): Boolean {
        return false
    }

    /**
     * 该次按下是否是长按
     */
    private fun isConsideredLongPress(): Boolean {
        return false
    }

    private fun reset() {
        downPoint.reset()
        currentMovePoint.reset()
    }

    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> down(event)
            MotionEvent.ACTION_MOVE -> move(event)
            MotionEvent.ACTION_UP -> up(event)
        }
        return false
    }
}